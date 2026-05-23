package acme.certprep;


public final class CertPrepFiles {

    public static final String MASTER_ANSWER_KEY_FILENAME = "master-answer-key.csv";
    public static final String QUESTION_IMAGE_GLOB = "ch*-q*.png";
    public static final String SESSION_FILE_GLOB = "*.csv";
    public static final String SESSION_HEADER = "Chapter,Question,Answer,Completed,Elapsed Time,Correct Yes/No,Reviewed";

    private CertPrepFiles() {
    }

    public static String sessionFileName(String date, int sequence) {
        return String.format("session-%s-%03d.csv", date, sequence);
    }

    public static String questionImageName(int chapter, int question) {
        return String.format("ch%02d-q%02d.png", chapter, question);
    }

    public static String answerImageName(int chapter, int question) {
        return String.format("ch%02d-q%02d-answer.png", chapter, question);
    }

    public static String alternateAnswerImageName(int chapter, int question) {
        return String.format("ch%02d-q%02d-ans.png", chapter, question);
    }
}
