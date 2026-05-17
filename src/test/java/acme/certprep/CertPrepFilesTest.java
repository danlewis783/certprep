package acme.certprep;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CertPrepFilesTest {
    @Test
    void formatsSessionFileNames() {
        assertThat(CertPrepFiles.sessionFileName("20260517", 7)).isEqualTo("session-20260517-007.csv");
    }

    @Test
    void formatsQuestionAndAnswerImageNames() {
        assertThat(CertPrepFiles.questionImageName(3, 4)).isEqualTo("ch03-q04.png");
        assertThat(CertPrepFiles.answerImageName(3, 4)).isEqualTo("ch03-q04-answer.png");
        assertThat(CertPrepFiles.alternateAnswerImageName(3, 4)).isEqualTo("ch03-q04-ans.png");
    }
}
