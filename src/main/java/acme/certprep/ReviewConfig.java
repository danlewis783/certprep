package acme.certprep;

import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

@NullMarked
public final class ReviewConfig implements Config {
    private static final Logger logger = LoggerFactory.getLogger(ReviewConfig.class);

    private final Path dataDir;
    private final Path sessionDir;
    private final Path sessionFile;

    public ReviewConfig(Path dataDir, Path sessionDir, Path sessionFile) {
        this.dataDir = ConfigPaths.normalize(dataDir, "dataDir");
        this.sessionDir = ConfigPaths.normalize(sessionDir, "sessionDir");
        this.sessionFile = ConfigPaths.normalize(sessionFile, "sessionFile");
    }

    @Override
    public Path getDataDir() {
        return dataDir;
    }

    @Override
    public Path getSessionDir() {
        return sessionDir;
    }

    public Path getSessionFile() {
        return sessionFile;
    }
}
