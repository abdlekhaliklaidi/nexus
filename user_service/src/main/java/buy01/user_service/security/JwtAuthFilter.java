package buy01.user_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class JwtAuthFilter extends OncePerRequestFilter {

    // Must match the key used in JwtUtil (at least 32 characters for HS256)
    private static final String SECRET_KEY =
        "mysecretkeymysecretkeymysecretkey123456789012345678901234567890"; 

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();

            try {
                // New JJWT 0.12.x syntax
                Claims claims = Jwts.parser()
                        .verifyWith(getSigningKey()) 
                        .build()
                        .parseSignedClaims(token)
                        .getPayload(); // .getBody() is now .getPayload()

                String userId = claims.getSubject();
                String role = claims.get("role", String.class);

                if (role == null || (!role.equals("CLIENT") && !role.equals("SELLER"))) {
                    throw new JwtException("Invalid role in token");
                }

                // Wrap the role as a GrantedAuthority (Spring Security standard)
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));

                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (ExpiredJwtException e) {
                request.setAttribute("expired", e.getMessage());
                // Optional: handle response directly if you don't have a Global Exception Handler
            } catch (MalformedJwtException | SignatureException e) {
                request.setAttribute("invalid", "Invalid Token");
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}