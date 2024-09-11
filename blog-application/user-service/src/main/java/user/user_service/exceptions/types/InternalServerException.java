package user.user_service.exceptions.types;

public class InternalServerException extends RuntimeException{
    public InternalServerException(String message) {
        super(message);
    }
}