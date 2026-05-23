package acme.certprep;


import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ReviewAssetValidator {

    public List<String> missingAssets(ReviewConfig config, List<SessionRow> rows) {
        List<String> missing = new ArrayList<>();
        for (SessionRow row : rows) {
            String question = CertPrepFiles.questionImageName(row.getChapter(), row.getQuestion());
            String answer = CertPrepFiles.answerImageName(row.getChapter(), row.getQuestion());
            String alternateAnswer = CertPrepFiles.alternateAnswerImageName(row.getChapter(), row.getQuestion());
            if (!Files.exists(config.getDataDir().resolve(question))) {
                missing.add(question);
            }
            if (!Files.exists(config.getDataDir().resolve(answer)) && !Files.exists(config.getDataDir().resolve(alternateAnswer))) {
                missing.add(answer + "/" + alternateAnswer);
            }
        }
        return Collections.unmodifiableList(missing);
    }
}
