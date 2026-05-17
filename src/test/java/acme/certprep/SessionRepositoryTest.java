package acme.certprep;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SessionRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void createsNewSessionWithHeaderAndSequentialName() throws IOException {
        SessionRepository repository = new SessionRepository(tempDir);
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Files.writeString(tempDir.resolve("session-" + today + "-001.csv"), "existing");

        Session session = repository.createNew();

        assertThat(session.getDisplayName()).isEqualTo("session-" + today + "-002.csv");
        assertThat(Files.readString(session.getFile()))
                .isEqualTo("Chapter,Question,Answer,Completed,Elapsed Time,Correct Yes/No,Reviewed\n");
    }

    @Test
    void listsCsvSessionsNewestNameFirstAsImmutableList() throws IOException {
        Files.writeString(tempDir.resolve("session-20250101-001.csv"), header());
        Files.writeString(tempDir.resolve("session-20250102-001.csv"), header());
        Files.writeString(tempDir.resolve("notes.txt"), "ignored");
        SessionRepository repository = new SessionRepository(tempDir);

        List<Session> sessions = repository.listSessions();

        assertThat(sessions).extracting(Session::getDisplayName)
                .containsExactly("session-20250102-001.csv", "session-20250101-001.csv");
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(sessions::clear);
    }

    @Test
    void existingWrapsProvidedPath() {
        Path file = tempDir.resolve("session.csv");
        Session session = new SessionRepository(tempDir).existing(file);

        assertThat(session.getFile()).isEqualTo(file);
    }

    private static String header() {
        return "Chapter,Question,Answer,Completed,Elapsed Time,Correct Yes/No,Reviewed\n";
    }
}
