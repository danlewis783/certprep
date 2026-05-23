package acme.certprep;

public final class InteractiveConfigException extends Exception {

    InteractiveConfigException(String message) {
        super(message);
    }

    InteractiveConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
