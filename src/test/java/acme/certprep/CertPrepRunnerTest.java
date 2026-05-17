package acme.certprep;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CertPrepRunnerTest {
    @TempDir
    Path tempDir;

    @Test
    void helpPrintsUsageAndReturnsSuccess() {
        RunnerFixture fixture = fixture();

        int exitCode = fixture.runner.run(new String[]{"--help"});

        assertThat(exitCode).isZero();
        assertThat(fixture.out()).contains("Usage: java acme.certprep.CertPrep [options]");
    }

    @Test
    void argumentFailureReturnsError() {
        RunnerFixture fixture = fixture();

        int exitCode = fixture.runner.run(new String[]{"--wat"});

        assertThat(exitCode).isEqualTo(1);
        assertThat(fixture.err()).contains("Argument Error: Unknown argument: --wat");
    }

    @Test
    void gradeConfigPrintsReport() throws IOException {
        Path sessionDir = tempDir.resolve("sessions");
        Files.createDirectories(sessionDir);
        Path sessionFile = sessionDir.resolve("session.csv");
        Files.writeString(sessionFile, CertPrepFiles.SESSION_HEADER + "\n9,38,\"A\",true,60,true,true\n");
        RunnerFixture fixture = fixture();

        int exitCode = fixture.runner.run(new String[]{"--grade", "session.csv", "--session-dir", sessionDir.toString()});

        assertThat(exitCode).isZero();
        assertThat(fixture.out()).contains("SESSION GRADE REPORT");
    }

    @Test
    void reviewConfigLaunchesReviewUiWhenSessionAndAssetsAreValid() throws IOException {
        Path dataDir = tempDir.resolve("data");
        Path sessionDir = tempDir.resolve("sessions");
        Files.createDirectories(dataDir);
        Files.createDirectories(sessionDir);
        Files.writeString(dataDir.resolve(CertPrepFiles.questionImageName(9, 38)), "");
        Files.writeString(dataDir.resolve(CertPrepFiles.answerImageName(9, 38)), "");
        Files.writeString(sessionDir.resolve("session.csv"), CertPrepFiles.SESSION_HEADER + "\n9,38,\"A\",true,60,true,true\n");
        RunnerFixture fixture = fixture();

        int exitCode = fixture.runner.run(new String[]{
                "--review", "session.csv",
                "--data-dir", dataDir.toString(),
                "--session-dir", sessionDir.toString()
        });

        assertThat(exitCode).isZero();
        assertThat(fixture.ui.reviewLaunched).isTrue();
    }

    @Test
    void reviewConfigReturnsSuccessForEmptySession() throws IOException {
        Path sessionDir = tempDir.resolve("sessions");
        Files.createDirectories(sessionDir);
        Files.writeString(sessionDir.resolve("session.csv"), CertPrepFiles.SESSION_HEADER + "\n");
        RunnerFixture fixture = fixture();

        int exitCode = fixture.runner.run(new String[]{"--review", "session.csv", "--session-dir", sessionDir.toString()});

        assertThat(exitCode).isZero();
        assertThat(fixture.out()).contains("The session file appears to be empty.");
        assertThat(fixture.ui.reviewLaunched).isFalse();
    }

    @Test
    void reviewConfigReturnsFailureForMissingAssets() throws IOException {
        Path dataDir = tempDir.resolve("data");
        Path sessionDir = tempDir.resolve("sessions");
        Files.createDirectories(dataDir);
        Files.createDirectories(sessionDir);
        Files.writeString(sessionDir.resolve("session.csv"), CertPrepFiles.SESSION_HEADER + "\n9,38,\"A\",true,60,true,true\n");
        RunnerFixture fixture = fixture();

        int exitCode = fixture.runner.run(new String[]{
                "--review", "session.csv",
                "--data-dir", dataDir.toString(),
                "--session-dir", sessionDir.toString()
        });

        assertThat(exitCode).isEqualTo(1);
        assertThat(fixture.err()).contains("FATAL: Missing Assets");
    }

    @Test
    void examConfigLaunchesExamUi() throws IOException {
        Path dataDir = tempDir.resolve("data");
        Path sessionDir = tempDir.resolve("sessions");
        Files.createDirectories(dataDir);
        Files.writeString(dataDir.resolve(CertPrepFiles.MASTER_ANSWER_KEY_FILENAME),
                "Chapter,Question,Answer,Possible\n9,38,\"A\",\"A,B\"\n");
        RunnerFixture fixture = fixture();

        int exitCode = fixture.runner.run(new String[]{
                "--exam",
                "--chapter", "9",
                "--start", "38",
                "--end", "38",
                "--data-dir", dataDir.toString(),
                "--session-dir", sessionDir.toString()
        });

        assertThat(exitCode).isZero();
        assertThat(fixture.ui.examLaunched).isTrue();
    }

    @Test
    void unexpectedFailureReturnsError() throws IOException {
        Path dataFile = tempDir.resolve("data-file");
        Files.writeString(dataFile, "");
        RunnerFixture fixture = fixture();

        int exitCode = fixture.runner.run(new String[]{
                "--exam",
                "--chapter", "9",
                "--start", "38",
                "--end", "38",
                "--data-dir", dataFile.toString()
        });

        assertThat(exitCode).isEqualTo(1);
        assertThat(fixture.err()).contains("NoSuchFileException");
    }

    private RunnerFixture fixture() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        RecordingUiLauncher ui = new RecordingUiLauncher();
        InteractiveConfigCli interactiveCli = new InteractiveConfigCli(
                tempDir.resolve("data"),
                tempDir.resolve("sessions"),
                prompt -> "9",
                new PrintStream(new ByteArrayOutputStream())
        );
        CertPrepRunner runner = new CertPrepRunner(
                new ArgParser(),
                interactiveCli,
                new GradeReporter(),
                new ReviewAssetValidator(),
                ui,
                new PrintStream(out),
                new PrintStream(err)
        );
        return new RunnerFixture(runner, ui, out, err);
    }

    private static final class RunnerFixture {
        private final CertPrepRunner runner;
        private final RecordingUiLauncher ui;
        private final ByteArrayOutputStream out;
        private final ByteArrayOutputStream err;

        private RunnerFixture(CertPrepRunner runner, RecordingUiLauncher ui, ByteArrayOutputStream out, ByteArrayOutputStream err) {
            this.runner = runner;
            this.ui = ui;
            this.out = out;
            this.err = err;
        }

        private String out() {
            return out.toString();
        }

        private String err() {
            return err.toString();
        }
    }

    private static final class RecordingUiLauncher implements CertPrepRunner.UiLauncher {
        private boolean reviewLaunched;
        private boolean examLaunched;

        @Override
        public void showReview(ReviewConfig config, Session session, List<SessionRow> rows) {
            reviewLaunched = true;
        }

        @Override
        public void showExam(ExamConfig config, List<QuestionInfo> questions, Session session) {
            examLaunched = true;
        }
    }
}
