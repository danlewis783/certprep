package acme.certprep;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class QuestionBankTest {
    @TempDir
    Path tempDir;

    @Test
    void loadsMatchingQuestionsAsImmutableList() throws IOException {
        Path dataDir = tempDir.resolve("data");
        Files.createDirectories(dataDir);
        Files.writeString(dataDir.resolve("master-answer-key.csv"),
                "Chapter,Question,Answer,Possible\n" +
                        "9,37,\"A\",\"A,B\"\n" +
                        "9,38,\"A,B\",\"A,B,C\"\n" +
                        "9,39,\"C\",\"A,B,C\"\n" +
                        "10,1,\"D\",\"A,B,C,D\"\n");
        TestConfig config = new TestConfig(9, 38, 39, dataDir, tempDir.resolve("sessions"));

        List<QuestionInfo> questions = QuestionBank.load(config);

        assertThat(questions).hasSize(2);
        assertThat(questions)
                .extracting(QuestionInfo::getQuestion)
                .containsExactly(38, 39);
        assertThat(questions.get(0).getAnswer()).isEqualTo("A,B");
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> questions.add(new QuestionInfo(9, 40, "A", "A,B")));
    }
}
