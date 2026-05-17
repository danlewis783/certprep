package acme.certprep;

import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

@NullMarked
public final class ConfigDefaults {
    private static final Logger logger = LoggerFactory.getLogger(ConfigDefaults.class);

    private ConfigDefaults() {
    }

    public static Path dataDir() {
        return ConfigPaths.normalize(Path.of(System.getProperty("user.home"), ".certprep", "data"), "dataDir");
    }

    public static Path sessionDir() {
        return ConfigPaths.normalize(Path.of(System.getProperty("user.home"), ".certprep", "sessions"), "sessionDir");
    }
}
