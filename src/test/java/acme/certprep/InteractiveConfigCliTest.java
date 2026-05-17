package acme.certprep;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Queue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class InteractiveConfigCliTest {
    @TempDir
    Path tempDir;

    @Test
    void promptsForExamConfig() throws Exception {
        Path dataDir = tempDir.resolve("data");
        Files.createDirectories(dataDir);
        Files.writeString(dataDir.resolve("ch-q01.png"), "");
        Files.writeString(dataDir.resolve("chAA-q01.png"), "");
        Files.writeString(dataDir.resolve(CertPrepFiles.questionImageName(9, 38)), "");
        Files.writeString(dataDir.resolve(CertPrepFiles.questionImageName(9, 40)), "");
        InteractiveConfigCli cli = cli(dataDir, tempDir.resolve("sessions"), "1", "1", "38", "40");

        Config config = cli.prompt();

        assertThat(config).isInstanceOfSatisfying(ExamConfig.class, examConfig -> {
            assertThat(examConfig.getChapter()).isEqualTo(9);
            assertThat(examConfig.getStart()).isEqualTo(38);
            assertThat(examConfig.getEnd()).isEqualTo(40);
        });
    }

    @Test
    void promptsForReviewConfig() throws Exception {
        Path sessionDir = writeSessionDirectory();
        InteractiveConfigCli cli = cli(tempDir.resolve("data"), sessionDir, "2", "1");

        Config config = cli.prompt();

        assertThat(config).isInstanceOf(ReviewConfig.class);
    }

    @Test
    void promptsForGradeConfig() throws Exception {
        Path sessionDir = writeSessionDirectory();
        InteractiveConfigCli cli = cli(tempDir.resolve("data"), sessionDir, "3", "1");

        Config config = cli.prompt();

        assertThat(config).isInstanceOf(GradeConfig.class);
    }

    @Test
    void rejectsInvalidModeChoice() {
        InteractiveConfigCli cli = cli(tempDir.resolve("data"), tempDir.resolve("sessions"), "9");

        assertThatExceptionOfType(InteractiveConfigException.class)
                .isThrownBy(cli::prompt)
                .withMessage("Invalid choice.");
    }

    @Test
    void rejectsMissingDataDirectory() {
        InteractiveConfigCli cli = cli(tempDir.resolve("missing-data"), tempDir.resolve("sessions"), "1");

        assertThatExceptionOfType(InteractiveConfigException.class)
                .isThrownBy(cli::prompt)
                .withMessageContaining("Data directory does not exist:");
    }

    @Test
    void rejectsEmptyChapterList() throws IOException {
        Path dataDir = tempDir.resolve("data");
        Files.createDirectories(dataDir);
        InteractiveConfigCli cli = cli(dataDir, tempDir.resolve("sessions"), "1");

        assertThatExceptionOfType(InteractiveConfigException.class)
                .isThrownBy(cli::prompt)
                .withMessageContaining("No chapter files found in");
    }

    @Test
    void rejectsInvalidChapterSelection() throws IOException {
        Path dataDir = tempDir.resolve("data");
        Files.createDirectories(dataDir);
        Files.writeString(dataDir.resolve(CertPrepFiles.questionImageName(9, 38)), "");
        InteractiveConfigCli cli = cli(dataDir, tempDir.resolve("sessions"), "1", "2");

        assertThatExceptionOfType(InteractiveConfigException.class)
                .isThrownBy(cli::prompt)
                .withMessage("Invalid choice.");
    }

    @Test
    void rejectsNonNumericExamInput() throws IOException {
        Path dataDir = tempDir.resolve("data");
        Files.createDirectories(dataDir);
        Files.writeString(dataDir.resolve(CertPrepFiles.questionImageName(9, 38)), "");
        InteractiveConfigCli cli = cli(dataDir, tempDir.resolve("sessions"), "1", "x");

        assertThatExceptionOfType(InteractiveConfigException.class)
                .isThrownBy(cli::prompt)
                .withMessage("Input must be a valid number.");
    }

    @Test
    void reportsChapterListingFailure() throws IOException {
        Path dataFile = tempDir.resolve("data-file");
        Files.writeString(dataFile, "");
        InteractiveConfigCli cli = cli(dataFile, tempDir.resolve("sessions"), "1");

        assertThatExceptionOfType(InteractiveConfigException.class)
                .isThrownBy(cli::prompt)
                .withMessageContaining("Error listing chapters:");
    }

    @Test
    void rejectsMissingSessionDirectory() {
        InteractiveConfigCli cli = cli(tempDir.resolve("data"), tempDir.resolve("missing-sessions"), "2");

        assertThatExceptionOfType(InteractiveConfigException.class)
                .isThrownBy(cli::prompt)
                .withMessageContaining("Session directory does not exist:");
    }

    @Test
    void rejectsEmptySessionList() throws IOException {
        Path sessionDir = tempDir.resolve("sessions");
        Files.createDirectories(sessionDir);
        InteractiveConfigCli cli = cli(tempDir.resolve("data"), sessionDir, "2");

        assertThatExceptionOfType(InteractiveConfigException.class)
                .isThrownBy(cli::prompt)
                .withMessageContaining("No session files found in");
    }

    @Test
    void rejectsInvalidSessionSelection() throws IOException {
        Path sessionDir = writeSessionDirectory();
        InteractiveConfigCli cli = cli(tempDir.resolve("data"), sessionDir, "2", "2");

        assertThatExceptionOfType(InteractiveConfigException.class)
                .isThrownBy(cli::prompt)
                .withMessage("Invalid choice.");
    }

    @Test
    void rejectsNonNumericSessionSelection() throws IOException {
        Path sessionDir = writeSessionDirectory();
        InteractiveConfigCli cli = cli(tempDir.resolve("data"), sessionDir, "2", "x");

        assertThatExceptionOfType(InteractiveConfigException.class)
                .isThrownBy(cli::prompt)
                .withMessage("Input must be a valid number.");
    }

    @Test
    void reportsSessionListingFailure() throws IOException {
        Path sessionFile = tempDir.resolve("sessions-file");
        Files.writeString(sessionFile, "");
        InteractiveConfigCli cli = cli(tempDir.resolve("data"), sessionFile, "2");

        assertThatExceptionOfType(InteractiveConfigException.class)
                .isThrownBy(cli::prompt)
                .withMessageContaining("Error listing sessions:");
    }

    private InteractiveConfigCli cli(Path dataDir, Path sessionDir, String... responses) {
        Queue<String> queue = new ArrayDeque<>(java.util.List.of(responses));
        return new InteractiveConfigCli(dataDir, sessionDir, prompt -> queue.remove(), new PrintStream(new ByteArrayOutputStream()));
    }

    private Path writeSessionDirectory() throws IOException {
        Path sessionDir = tempDir.resolve("sessions");
        Files.createDirectories(sessionDir);
        Files.writeString(sessionDir.resolve("session-20260517-001.csv"), CertPrepFiles.SESSION_HEADER + "\n");
        return sessionDir;
    }
}
