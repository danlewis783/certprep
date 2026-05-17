package acme.certprep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class QuestionBank {
    private static final Logger logger = LoggerFactory.getLogger(QuestionBank.class);

    final List<QuestionInfo> questions = new ArrayList<>();

    QuestionBank(TestConfig config) throws IOException {
        Path k = config.getDataDir().resolve("master-answer-key.csv");
        List<String> lines = Files.readAllLines(k);
        for (int i = 1; i < lines.size(); i++) {
            String[] c = CertPrep.parseCSVLine(lines.get(i));
            int ch = Integer.parseInt(c[0]);
            int q = Integer.parseInt(c[1]);
            if (ch == config.getChapter() && q >= config.getStart() && q <= config.getEnd()) {
                questions.add(new QuestionInfo(ch, q, c[2], c[3]));
            }
        }
    }

    public int size() {
        return questions.size();
    }

    public QuestionInfo get(int index) {
        return questions.get(index);
    }
}
