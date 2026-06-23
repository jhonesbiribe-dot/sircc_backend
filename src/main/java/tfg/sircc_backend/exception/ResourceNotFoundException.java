package tfg.sircc_backend.exception;

// exception/ResourceNotFoundException.java


public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}