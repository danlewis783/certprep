package acme.certprep;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class QuestionBank {
    List<QuestionInfo> questions = new ArrayList<>();

    QuestionBank(ArgParser config) throws IOException {
        Path k = Paths.get(config.dataDir, "master-answer-key.csv");
        List<String> lines = Files.readAllLines(k);
        for (int i = 1; i < lines.size(); i++) {
            String[] c = CertPrep.parseCSVLine(lines.get(i));
            int ch = Integer.parseInt(c[0]), q = Integer.parseInt(c[1]);
            if ((config.chapter == null || ch == config.chapter) && (config.start == null || q >= config.start) && (config.end == null || q <= config.end))
                questions.add(new QuestionInfo(ch, q, c[2], c[3]));
        }
    }
}
