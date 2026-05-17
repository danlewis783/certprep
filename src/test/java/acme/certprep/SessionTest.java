package acme.certprep;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SessionTest {
    @TempDir
    Path tempDir;

    @Test
    void loadsRowsAndSummaryFromSessionFile() throws IOException {
        Path sessionFile = writeSession(
                "9,38,\"A\",true,60,true,true\n" +
                        "9,39,\"B\",true,70,false,false\n");
        Session session = new Session(sessionFile);

        List<SessionRow> rows = session.loadRows();

        assertThat(rows).hasSize(2);
        assertThat(rows).extracting(SessionRow::getQuestion).containsExactly(38, 39);
        assertThat(session.getDisplayName()).isEqualTo("session.csv");
        assertThat(session.getSummary()).isEqualTo("(Ch9: Q38-39)");
        assertThat(session.isFullyReviewed()).isFalse();
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(rows::clear);
    }

    @Test
    void updatesReviewedFlagInFileAndRow() throws IOException {
        Path sessionFile = writeSession("9,38,\"A\",true,60,true,false\n");
        Session session = new Session(sessionFile);
        SessionRow row = session.loadRows().get(0);

        session.updateReviewed(row, true);

        assertThat(row.isReviewed()).isTrue();
        assertThat(Files.readString(sessionFile)).contains("9,38,\"A\",true,60,true,true");
        assertThat(session.isFullyReviewed()).isTrue();
    }

    @Test
    void appendsLoggedAnswer() throws IOException {
        Path sessionFile = writeSession("");
        Session session = new Session(sessionFile);
        QuestionInfo question = new QuestionInfo(10, 2, "C", "A,B,C");

        session.logAnswer(question, "C", 42, true);

        assertThat(Files.readString(sessionFile))
                .contains("10,2,\"C\",true,42,true,false");
    }

    @Test
    void emptySessionIsNotFullyReviewedAndHasNoSummary() throws IOException {
        Path sessionFile = writeSession("");
        Session session = new Session(sessionFile);

        assertThat(session.loadRows()).isEmpty();
        assertThat(session.isFullyReviewed()).isFalse();
        assertThat(session.getSummary()).isEmpty();
    }

    private Path writeSession(String rows) throws IOException {
        Path sessionFile = tempDir.resolve("session.csv");
        Files.writeString(sessionFile, "Chapter,Question,Answer,Completed,Elapsed Time,Correct Yes/No,Reviewed\n" + rows);
        return sessionFile;
    }
}
