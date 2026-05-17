package acme.certprep.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SampleQuestion {
    private static final Logger logger = LoggerFactory.getLogger(SampleQuestion.class);

    final int chapter;
    final int question;
    final String answer;

    SampleQuestion(int c, int q, String a) {
        this.chapter = c;
        this.question = q;
        this.answer = a;
    }
}
