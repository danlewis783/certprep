package acme.certprep;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ReviewAssetValidatorTest {
    @TempDir
    Path tempDir;

    @Test
    void reportsMissingQuestionAndAnswerAssets() throws IOException {
        Path dataDir = tempDir.resolve("data");
        Files.createDirectories(dataDir);
        Files.writeString(dataDir.resolve(CertPrepFiles.questionImageName(9, 38)), "");
        Files.writeString(dataDir.resolve(CertPrepFiles.answerImageName(9, 38)), "");
        Files.writeString(dataDir.resolve(CertPrepFiles.questionImageName(9, 39)), "");
        Files.writeString(dataDir.resolve(CertPrepFiles.alternateAnswerImageName(9, 39)), "");
        ReviewConfig config = new ReviewConfig(dataDir, tempDir.resolve("sessions"), tempDir.resolve("session.csv"));
        List<SessionRow> rows = List.of(
                new SessionRow("9,38,\"A\",true,60,true,true", 1),
                new SessionRow("9,39,\"B\",true,70,false,false", 2),
                new SessionRow("9,40,\"C\",true,80,false,false", 3)
        );

        List<String> missing = new ReviewAssetValidator().missingAssets(config, rows);

        assertThat(missing).containsExactly(
                CertPrepFiles.questionImageName(9, 40),
                CertPrepFiles.answerImageName(9, 40) + "/" + CertPrepFiles.alternateAnswerImageName(9, 40)
        );
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> missing.add("extra"));
    }
}
