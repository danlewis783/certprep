package acme.certprep;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class GradeReporterTest {
    @TempDir
    Path tempDir;

    private final GradeReporter reporter = new GradeReporter();

    @Test
    void reportsPassingScore() throws IOException {
        Path sessionFile = writeSession(
                "9,38,\"A\",true,60,true,true\n" +
                        "9,39,\"B\",true,70,true,false\n");

        String report = reporter.report(config(sessionFile));

        assertThat(report).contains("SESSION GRADE REPORT: session.csv");
        assertThat(report).contains("Total Questions: 2");
        assertThat(report).contains("Number Correct:  2");
        assertThat(report).contains("100.00%");
        assertThat(report).contains("(PASS)");
    }

    @Test
    void reportsFailingScore() throws IOException {
        Path sessionFile = writeSession(
                "9,38,\"A\",true,60,true,true\n" +
                        "9,39,\"B\",true,70,false,false\n");

        String report = reporter.report(config(sessionFile));

        assertThat(report).contains("50.00%");
        assertThat(report).contains("(FAIL)");
    }

    @Test
    void reportsMissingSessionFile() {
        Path sessionFile = tempDir.resolve("missing.csv");

        assertThat(reporter.report(config(sessionFile)))
                .contains("Error: Session file not found: " + sessionFile.toAbsolutePath().normalize());
    }

    @Test
    void reportsEmptySession() throws IOException {
        Path sessionFile = writeSession("");

        assertThat(reporter.report(config(sessionFile)))
                .contains("No questions found in session: session.csv");
    }

    @Test
    void reportsReadFailure() {
        assertThat(reporter.report(config(tempDir)))
                .contains("Error reading session file:");
    }

    private GradeConfig config(Path sessionFile) {
        return new GradeConfig(tempDir.resolve("data"), tempDir, sessionFile);
    }

    private Path writeSession(String rows) throws IOException {
        Path sessionFile = tempDir.resolve("session.csv");
        Files.writeString(sessionFile, CertPrepFiles.SESSION_HEADER + "\n" + rows);
        return sessionFile;
    }
}
