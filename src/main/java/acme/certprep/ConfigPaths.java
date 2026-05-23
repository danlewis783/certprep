package acme.certprep;

import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;
import java.util.Objects;

@NullMarked
final class ConfigPaths {

    private ConfigPaths() {
    }

    static Path normalize(Path path, String name) {
        return Objects.requireNonNull(path, name).toAbsolutePath().normalize();
    }
}
