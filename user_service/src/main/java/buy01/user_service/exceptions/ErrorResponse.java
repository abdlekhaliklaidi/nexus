package buy01.user_service.exceptions;

public class ErrorResponse {

    private final String errorMessage;

    public ErrorResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String errorMessage() {
        return errorMessage;
    }
}
