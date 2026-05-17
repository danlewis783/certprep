package acme.certprep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class InteractiveConfigException extends Exception {
    private static final Logger logger = LoggerFactory.getLogger(InteractiveConfigException.class);

    InteractiveConfigException(String message) {
        super(message);
    }

    InteractiveConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
