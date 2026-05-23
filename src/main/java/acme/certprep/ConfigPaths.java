package acme.certprep;


import java.nio.file.Path;
import java.util.Objects;

final class ConfigPaths {

    private ConfigPaths() {
    }

    static Path normalize(Path path, String name) {
        return Objects.requireNonNull(path, name).toAbsolutePath().normalize();
    }
}
