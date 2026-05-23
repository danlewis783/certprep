package acme.certprep;

public final class CertPrep {

    private CertPrep() {
    }

    public static void main(String[] args) {
        int exitCode = new CertPrepRunner().run(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }
}
