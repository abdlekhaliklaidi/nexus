package buy01.user_service.exceptions;
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException() {
        super("Resource not found");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}