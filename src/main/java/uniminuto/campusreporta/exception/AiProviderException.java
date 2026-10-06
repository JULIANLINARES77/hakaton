package uniminuto.campusreporta.exception;

public class AiProviderException extends RuntimeException {
    private final int status;

    public AiProviderException(String message, int status) {
        super(message);
        this.status = status;
    }

    public AiProviderException(String message, int status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
