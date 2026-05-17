package acme.certprep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class QuestionInfo {
    private static final Logger logger = LoggerFactory.getLogger(QuestionInfo.class);

    final int ch;
    final int q;
    final String a;
    final List<String> p;

    QuestionInfo(int ch, int q, String a, String p) {
        this.ch = ch;
        this.q = q;
        this.a = a;
        this.p = Arrays.asList(p.split(","));
    }

    public int getChapter() {
        return ch;
    }

    public int getQuestion() {
        return q;
    }

    public String getAnswer() {
        return a;
    }

    public List<String> getPossibleAnswers() {
        return p;
    }
}
