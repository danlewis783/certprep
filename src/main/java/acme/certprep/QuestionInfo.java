package acme.certprep;

import java.util.Arrays;
import java.util.List;

public class QuestionInfo {
    int ch, q;
    String a;
    List<String> p;

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
