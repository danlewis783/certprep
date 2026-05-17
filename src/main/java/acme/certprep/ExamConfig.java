package acme.certprep;

import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

@NullMarked
public final class ExamConfig implements Config {
    private static final Logger logger = LoggerFactory.getLogger(ExamConfig.class);

    private final int chapter;
    private final int start;
    private final int end;
    private final Path dataDir;
    private final Path sessionDir;

    public ExamConfig(int chapter, int start, int end, Path dataDir, Path sessionDir) {
        if (chapter <= 0) {
            throw new IllegalArgumentException("chapter must be positive");
        }
        if (start <= 0) {
            throw new IllegalArgumentException("start must be positive");
        }
        if (end <= 0) {
            throw new IllegalArgumentException("end must be positive");
        }
        if (start > end) {
            throw new IllegalArgumentException("start must be less than or equal to end");
        }

        this.chapter = chapter;
        this.start = start;
        this.end = end;
        this.dataDir = ConfigPaths.normalize(dataDir, "dataDir");
        this.sessionDir = ConfigPaths.normalize(sessionDir, "sessionDir");
    }

    public int getChapter() {
        return chapter;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public Path getDataDir() {
        return dataDir;
    }

    @Override
    public Path getSessionDir() {
        return sessionDir;
    }
}
