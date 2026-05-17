package acme.certprep;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfigTest {
    private static final Path DATA_DIR = Path.of("data");
    private static final Path SESSION_DIR = Path.of("sessions");
    private static final Path SESSION_FILE = SESSION_DIR.resolve("session.csv");

    @Test
    void examConfigRetainsValidValues() {
        ExamConfig config = new ExamConfig(2, 3, 4, DATA_DIR, SESSION_DIR);

        assertThat(config.getChapter()).isEqualTo(2);
        assertThat(config.getStart()).isEqualTo(3);
        assertThat(config.getEnd()).isEqualTo(4);
        assertThat(config.getDataDir()).isEqualTo(normalize(DATA_DIR));
        assertThat(config.getSessionDir()).isEqualTo(normalize(SESSION_DIR));
    }

    @Test
    void examConfigRejectsInvalidRanges() {
        assertThatThrownBy(() -> new ExamConfig(0, 1, 1, DATA_DIR, SESSION_DIR))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("chapter must be positive");
        assertThatThrownBy(() -> new ExamConfig(1, 0, 1, DATA_DIR, SESSION_DIR))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("start must be positive");
        assertThatThrownBy(() -> new ExamConfig(1, 1, 0, DATA_DIR, SESSION_DIR))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("end must be positive");
        assertThatThrownBy(() -> new ExamConfig(1, 2, 1, DATA_DIR, SESSION_DIR))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("start must be less than or equal to end");
    }

    @Test
    void reviewAndGradeConfigsRetainSessionFile() {
        ReviewConfig review = new ReviewConfig(DATA_DIR, SESSION_DIR, SESSION_FILE);
        GradeConfig grade = new GradeConfig(DATA_DIR, SESSION_DIR, SESSION_FILE);

        assertThat(review.getDataDir()).isEqualTo(normalize(DATA_DIR));
        assertThat(review.getSessionDir()).isEqualTo(normalize(SESSION_DIR));
        assertThat(review.getSessionFile()).isEqualTo(normalize(SESSION_FILE));
        assertThat(grade.getDataDir()).isEqualTo(normalize(DATA_DIR));
        assertThat(grade.getSessionDir()).isEqualTo(normalize(SESSION_DIR));
        assertThat(grade.getSessionFile()).isEqualTo(normalize(SESSION_FILE));
    }

    @Test
    void configsRejectNullPaths() {
        assertThatNullPointerException()
                .isThrownBy(() -> new ExamConfig(1, 1, 1, null, SESSION_DIR))
                .withMessage("dataDir");
        assertThatNullPointerException()
                .isThrownBy(() -> new ReviewConfig(DATA_DIR, SESSION_DIR, null))
                .withMessage("sessionFile");
        assertThatNullPointerException()
                .isThrownBy(() -> new GradeConfig(DATA_DIR, null, SESSION_FILE))
                .withMessage("sessionDir");
    }

    @Test
    void configsNormalizePaths() {
        ExamConfig config = new ExamConfig(1, 1, 1, Path.of("data", ".", "chapter", ".."), Path.of("sessions", "."));

        assertThat(config.getDataDir()).isAbsolute();
        assertThat(config.getDataDir()).isEqualTo(normalize(Path.of("data")));
        assertThat(config.getSessionDir()).isEqualTo(normalize(Path.of("sessions")));
    }

    private static Path normalize(Path path) {
        return path.toAbsolutePath().normalize();
    }
}
