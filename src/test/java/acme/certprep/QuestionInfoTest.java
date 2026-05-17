package acme.certprep;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class QuestionInfoTest {
    @Test
    void parsesPossibleAnswers() {
        QuestionInfo question = new QuestionInfo(10, 2, "A,C", "A,B,C,D");

        assertThat(question.getChapter()).isEqualTo(10);
        assertThat(question.getQuestion()).isEqualTo(2);
        assertThat(question.getAnswer()).isEqualTo("A,C");
        assertThat(question.getPossibleAnswers()).containsExactly("A", "B", "C", "D");
    }

    @Test
    void possibleAnswersCannotBeStructurallyModified() {
        QuestionInfo question = new QuestionInfo(1, 1, "A", "A,B");

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> question.getPossibleAnswers().add("C"));
    }
}
