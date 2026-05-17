package acme.certprep;

import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Objects;

@NullMarked
final class ConfigPaths {
    private static final Logger logger = LoggerFactory.getLogger(ConfigPaths.class);

    private ConfigPaths() {
    }

    static Path normalize(Path path, String name) {
        return Objects.requireNonNull(path, name).toAbsolutePath().normalize();
    }
}
