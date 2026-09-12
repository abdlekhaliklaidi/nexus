# Buy02 — Nexus Repository Manager Integration

## Overview

This project implements a centralized artifact management system using **Nexus Repository Manager** for the `buy02` e-commerce microservices platform. Nexus acts as the single source of truth for:

- **Maven artifacts** (JARs) produced by the Java Spring Boot microservices
- **Docker images** produced by `docker compose build` for each service and the Angular frontend

Nexus is fully integrated into a **Jenkins CI/CD pipeline** that automatically builds, tests, versions, and publishes every artifact on each push to `main`.

### Microservices covered

| Service | Type | Artifact published to Nexus |
|---|---|---|
| `gateway_service` | Spring Boot (Spring Cloud Gateway) | JAR + Docker image |
| `user_service` | Spring Boot | JAR + Docker image |
| `product_service` | Spring Boot | JAR + Docker image |
| `media_service` | Spring Boot | JAR + Docker image |
| `cart_service` | Spring Boot | JAR + Docker image |
| `order_service` | Spring Boot | JAR + Docker image |
| `client` (frontend) | Angular | Docker image only (Nginx) |


### Architecture

Project Structure
02E-COM/

│
├── gateway-service/
│
├── user-service/
│
├── product-service/
│
├── media-service/
|
|__ order-service/
|
|__ cared-service
│
├── client/
|
|__ docker-compose.nexus.yml
│
├── docker-compose.yml
|
|__ Dockerfile.jenkins
|
|__ Jenkinsfile
|
|__ sonar-project.properties
│
└── README.md

---

## 1. Prerequisites

- Docker & Docker Compose
- Node.js 20+ (for the Angular client, only needed outside the CI container)
- Jenkins with the following plugins: Pipeline, SonarQube Scanner, JUnit, JaCoCo, Email Extension
- A dedicated Linux user for Nexus (never run Nexus as `root`)

---

## 2. Nexus Setup

### 2.1 Installation

Nexus is deployed as a Docker container (`sonatype/nexus3`) on the CI Docker network so Jenkins, SonarQube, and Nexus can all talk to each other by service name.

```bash
docker network create ci-plateform_ci_net   # shared CI network, if not already created

docker run -d --name nexus \
  --network ci-plateform_ci_net \
  -p 8081:8081 -p 8082:8082 \
  -v nexus-data:/nexus-data \
  sonatype/nexus3:3.96.0
```

> 📸 **Screenshot to add:** `docker ps` output showing the `nexus` container running, and the Nexus web UI landing page at `http://<host>:8081`.

Inside the container, Nexus already runs under a dedicated, non-root `nexus` user by default (this is the image's built-in behavior) — confirmed with:

Retrieve the initial admin password and log in:

```bash
docker exec nexus cat /nexus-data/admin.password
```

> 📸 **Screenshot to add:** first-login screen where the admin password is changed and anonymous access is disabled.

### 2.2 Repository configuration

From **Administration → Repository → Repositories**, the following repositories are created:

| Name | Type | Format | Purpose |
|---|---|---|---|
| `maven-releases` | hosted | maven2 | Store release JARs built by the pipeline |
| `maven-snapshots` | hosted | maven2 | Store `-SNAPSHOT` JARs during development |
| `maven-central` | proxy | maven2 | Proxy to `https://repo1.maven.org/maven2/` for external dependencies |
| `maven-public` | group | maven2 | Aggregates the three repos above — this is the single URL Maven talks to |
| `docker-hosted` | hosted | docker | Store the microservices' Docker images |

> 📸 **Screenshot to add:** the Repositories list page showing all 5 repos with their type/format columns, plus the detailed config screen of the `maven-public` group showing its member repos in order (`maven-releases` → `maven-snapshots` → `maven-central`).

For the Docker repository, enable the **HTTP connector** on a dedicated port (`8082` in this project) under Repository settings, since Docker requires either HTTPS or an explicit insecure-registry allowance.

> ⚠️ **Note on HTTP/HTTPS:** Our Docker daemon is configured for HTTPS by default, while Nexus's Docker repository is exposed over plain HTTP. Rather than marking the Docker daemon as insecure, the pipeline spins up a lightweight `socat` TCP proxy (`alpine/socat`) that forwards a local port to Nexus's HTTP Docker port, so `docker login` / `docker push` work without weakening the daemon's global TLS policy. See [Section 6](#6-docker-integration) for details.

---

## 3. Maven Integration

### 3.1 `settings.xml`

Each microservice's build uses a Maven `settings.xml` (generated dynamically by Jenkins from a credential, never committed to the repo) that:

1. Points **all** dependency resolution through the `maven-public` group repo (acts as a proxy/mirror for every external dependency).
2. Provides authenticated server entries so Maven can both **read** from `maven-public` and **deploy** to `maven-releases` / `maven-snapshots`.

```xml
<settings>
  <servers>
    <server>
      <id>nexus-releases</id>
      <username>${NEXUS_USERNAME}</username>
      <password>${NEXUS_PASSWORD}</password>
    </server>
    <server>
      <id>nexus-snapshots</id>
      <username>${NEXUS_USERNAME}</username>
      <password>${NEXUS_PASSWORD}</password>
    </server>
    <!-- Required: the mirror id below must also have a matching <server> entry,
         otherwise Maven sends unauthenticated requests and Nexus returns 401 -->
    <server>
      <id>nexus-public</id>
      <username>${NEXUS_USERNAME}</username>
      <password>${NEXUS_PASSWORD}</password>
    </server>
  </servers>

  <mirrors>
    <mirror>
      <id>nexus-public</id>
      <url>http://nexus:8081/repository/maven-public/</url>
      <mirrorOf>*</mirrorOf>
    </mirror>
  </mirrors>
</settings>
```

### 3.2 `pom.xml` — `distributionManagement`

Each service's `pom.xml` declares where `mvn deploy` should publish artifacts, switching automatically between the releases and snapshots repository based on the version suffix:

```xml
<distributionManagement>
  <repository>
    <id>nexus-releases</id>
    <url>http://nexus:8081/repository/maven-releases/</url>
  </repository>
  <snapshotRepository>
    <id>nexus-snapshots</id>
    <url>http://nexus:8081/repository/maven-snapshots/</url>
  </snapshotRepository>
</distributionManagement>
```

### 3.3 Publishing an artifact

```bash
cd gateway_service
./mvnw deploy -DskipTests -s settings-nexus.xml
```

> 📸 **Screenshot to add:** Jenkins console log showing `Uploading to nexus-releases: ...` / `BUILD SUCCESS`, and the corresponding artifact visible under **Browse → maven-releases** in the Nexus UI.

### 3.4 Verifying dependency resolution through Nexus

```bash
./mvnw dependency:resolve -s settings-nexus.xml
```

All lines should show `Downloading from nexus-public: http://nexus:8081/repository/maven-public/...` confirming no dependency is fetched directly from Maven Central — everything is cached and served by Nexus.

---

## 4. Versioning

- Each service's version lives in its `pom.xml` (`<version>`).
- The pipeline stamps the produced Docker images with the shared `DOCKER_TAG` environment variable (currently `0.0.1`), keeping Maven artifact versions and Docker image tags aligned.
- Snapshot builds (`-SNAPSHOT` suffix) are automatically routed to `maven-snapshots`; tagged releases go to `maven-releases`, so multiple versions of the same artifact can coexist and be rolled back to individually.


> 📸 **Screenshot to add:** the Nexus **Browse** view of `maven-releases/buy01/gateway_service/` showing multiple version folders (e.g. `0.0.1`, `0.0.2`), demonstrating rollback capability by re-pulling an older version.

---

## 5. Docker Integration

### 5.1 Docker repository

A `docker-hosted` repository is created in Nexus with its HTTP connector on port `8082`.

### 5.2 The HTTP/HTTPS bridge (`socat`)

Because the host Docker daemon expects HTTPS registries and Nexus's Docker repo is plain HTTP, the pipeline starts a temporary `socat` proxy container that forwards `127.0.0.1:5000` → `nexus:8082`:

```bash
docker run -d --name nexus-proxy --network ci-plateform_ci_net \
  -p 5000:8082 \
  alpine/socat \
  TCP-LISTEN:8082,fork TCP:10.1.13.9:8082
```

Docker is configured to treat `127.0.0.1:5000` as an **insecure registry** (localhost is trusted by default by the Docker daemon), which avoids touching the daemon's global TLS configuration for the real Nexus host.

### 5.3 Build, tag, push

```bash
docker compose -p 02e_com build

docker login 127.0.0.1:5000 -u <NEXUS_USERNAME> -p <NEXUS_PASSWORD>

docker tag 01e_com-gateway 127.0.0.1:5000/gateway_service:0.0.1
docker push 127.0.0.1:5000/gateway_service:0.0.1
```

> 📸 **Screenshot to add:** terminal output of a successful `docker push`, and the **Browse → docker-hosted** view in Nexus listing the pushed image with its tag and digest.

### 5.4 Pulling back from Nexus (verification)

```bash
docker pull 127.0.0.1:5000/gateway_service:0.0.1
docker run --rm 127.0.0.1:5000/gateway_service:0.0.1 --version
```

> 📸 **Screenshot to add:** `docker pull` succeeding against the Nexus registry, proving retrieval works end-to-end.

---

## 6. CI/CD Pipeline (Jenkins)

The pipeline (`Jenkinsfile`) is triggered automatically via `githubPush()` on every commit, plus a weekly SonarQube health scan (`cron('H H * * 0')`).

### Pipeline stages

1. **Checkout & Setup Env** — pulls the repo and injects the `.env` file from Jenkins credentials.
2. **Parallel Automated Testing** — Angular unit tests (`npm run test:ci`) and Java unit tests (`./mvnw test jacoco:report`) run in parallel.
3. **SonarQube Scanner Analysis** — static analysis + coverage across all six services and the Angular client.
4. **Quality Gate Check** — pipeline aborts if SonarQube's quality gate fails.
5. **Build Artifacts & Container Images** — `mvn package -DskipTests` for every service (parallelized) followed by `docker compose build`.
6. **Publish Docker Images to Nexus** — runs only on `main`, not on scheduled/timer builds; uses the `socat` bridge described above.
7. **Nexus Repository Readiness** — polls `GET /service/rest/v1/status` until Nexus answers `2xx` before publishing Maven artifacts.
8. **Publish Maven Artifacts to Nexus** — `mvn deploy` for every service using the dynamically generated `settings-nexus.xml`.
9. **Deploy Application** — `docker compose up -d` on the target deployment directory, pulling the freshly published images.

> 📸 **Screenshot to add:** the Jenkins **Stage View** (Blue Ocean or classic) showing all stages green end-to-end, and the SonarQube dashboard showing the project's quality gate as **Passed**.

### Running the pipeline manually

The same steps can be reproduced locally without Jenkins:

```bash

docker compose -p up --build
docker compose -p docker compose -f docker-compose.nexus.yml up -d
```

---