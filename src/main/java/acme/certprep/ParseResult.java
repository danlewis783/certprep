package acme.certprep;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class ParseResult {

    public enum Status {
        SUCCESS,
        FAILURE,
        HELP,
        INTERACTIVE
    }

    private final Status status;
    private final @Nullable Config config;
    private final @Nullable String message;

    private ParseResult(Status status, @Nullable Config config, @Nullable String message) {
        this.status = Objects.requireNonNull(status, "status");
        this.config = config;
        this.message = message;
    }

    public static ParseResult success(Config config) {
        return new ParseResult(Status.SUCCESS, Objects.requireNonNull(config, "config"), null);
    }

    public static ParseResult failure(String message) {
        return new ParseResult(Status.FAILURE, null, Objects.requireNonNull(message, "message"));
    }

    public static ParseResult help() {
        return new ParseResult(Status.HELP, null, null);
    }

    public static ParseResult interactive() {
        return new ParseResult(Status.INTERACTIVE, null, null);
    }

    public Status getStatus() {
        return status;
    }

    public Config getConfig() {
        if (config == null) {
            throw new IllegalStateException("Parse result does not contain a config");
        }
        return config;
    }

    public String getMessage() {
        if (message == null) {
            throw new IllegalStateException("Parse result does not contain a message");
        }
        return message;
    }
}
