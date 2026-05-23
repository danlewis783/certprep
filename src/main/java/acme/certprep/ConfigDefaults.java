package acme.certprep;


import java.nio.file.Path;

public final class ConfigDefaults {

    private ConfigDefaults() {
    }

    public static Path dataDir() {
        return ConfigPaths.normalize(Path.of(System.getProperty("user.home"), ".certprep", "data"), "dataDir");
    }

    public static Path sessionDir() {
        return ConfigPaths.normalize(Path.of(System.getProperty("user.home"), ".certprep", "sessions"), "sessionDir");
    }
}
