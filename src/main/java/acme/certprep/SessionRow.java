package acme.certprep;

public class SessionRow {
    final int chapter;
    final int question;
    final int time;
    final int lineIndex;
    final String userAnswer;
    final boolean correct;
    boolean reviewed;

    SessionRow(String line, int idx) {
        String[] c = CertPrep.parseCSVLine(line);
        chapter = Integer.parseInt(c[0]);
        question = Integer.parseInt(c[1]);
        userAnswer = c[2];
        time = Integer.parseInt(c[4]);
        correct = Boolean.parseBoolean(c[5]);
        reviewed = c.length > 6 && Boolean.parseBoolean(c[6]);
        lineIndex = idx;
    }

    public int getChapter() {
        return chapter;
    }

    public int getQuestion() {
        return question;
    }

    public int getTime() {
        return time;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public boolean isCorrect() {
        return correct;
    }

    public boolean isReviewed() {
        return reviewed;
    }
}
