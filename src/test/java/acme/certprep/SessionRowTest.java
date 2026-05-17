package acme.certprep;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SessionRowTest {
    @Test
    void parsesSessionCsvRow() {
        SessionRow row = new SessionRow("9,38,\"A,B\",true,72,false,true", 3);

        assertThat(row.getChapter()).isEqualTo(9);
        assertThat(row.getQuestion()).isEqualTo(38);
        assertThat(row.getUserAnswer()).isEqualTo("A,B");
        assertThat(row.getTime()).isEqualTo(72);
        assertThat(row.isCorrect()).isFalse();
        assertThat(row.isReviewed()).isTrue();
        assertThat(row.lineIndex).isEqualTo(3);
    }

    @Test
    void missingReviewedColumnDefaultsToFalse() {
        SessionRow row = new SessionRow("9,38,\"A\",true,72,true", 1);

        assertThat(row.isReviewed()).isFalse();
    }
}
