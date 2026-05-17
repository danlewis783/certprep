package acme.certprep;

import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@NullMarked
public final class ReviewAssetValidator {
    private static final Logger logger = LoggerFactory.getLogger(ReviewAssetValidator.class);

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
