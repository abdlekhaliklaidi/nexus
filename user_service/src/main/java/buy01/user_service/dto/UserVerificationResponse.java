package buy01.user_service.dto;

public class UserVerificationResponse {

    private final boolean exists;
    private final String role;

    public UserVerificationResponse(boolean exists, String role) {
        this.exists = exists;
        this.role = role;
    }

    public boolean exists() {
        return exists;
    }

    public String role() {
        return role;
    }
}