package acme.certprep.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TestData {
    private static final Logger logger = LoggerFactory.getLogger(TestData.class);

    final int chapter;
    final int question;
    final String answer;

    TestData(int c, int q, String a) {
        this.chapter = c;
        this.question = q;
        this.answer = a;
    }
}
