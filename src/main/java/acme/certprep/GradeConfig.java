package acme.certprep;


import java.nio.file.Path;

public final class GradeConfig implements Config {

    private final Path dataDir;
    private final Path sessionDir;
    private final Path sessionFile;

    public GradeConfig(Path dataDir, Path sessionDir, Path sessionFile) {
        this.dataDir = ConfigPaths.normalize(dataDir, "dataDir");
        this.sessionDir = ConfigPaths.normalize(sessionDir, "sessionDir");
        this.sessionFile = ConfigPaths.normalize(sessionFile, "sessionFile");
    }

    public Path getDataDir() {
        return dataDir;
    }

    public Path getSessionDir() {
        return sessionDir;
    }

    public Path getSessionFile() {
        return sessionFile;
    }
}
