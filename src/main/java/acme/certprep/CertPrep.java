package acme.certprep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CertPrep {
    private static final Logger logger = LoggerFactory.getLogger(CertPrep.class);

    private CertPrep() {
    }

    public static void main(String[] args) {
        int exitCode = new CertPrepRunner().run(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }
}
