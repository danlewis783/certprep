package certprep;

class SessionRow {
    int chapter, question, time, lineIndex;
    String userAnswer;
    boolean correct, reviewed;

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
}
