package acme.certprep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArgParser {
    private static final Logger logger = LoggerFactory.getLogger(ArgParser.class);

    Integer chapter;
    Integer start;
    Integer end;
    String dataDir = System.getProperty("user.home") + "/.certprep/data";
    String sessionDir = System.getProperty("user.home") + "/.certprep/sessions";
    String reviewFile;
    String gradeFile;
    boolean testMode;
    boolean showHelp;
    boolean interactive;

    ArgParser(String[] args) {
        if (args.length == 0) {
            interactive = true;
            return;
        }
        for (int i = 0; i < args.length; i++) {
            switch (args[i].toLowerCase()) {
                case "help":
                case "--help":
                case "-h":
                    showHelp = true;
                    return;
                case "--test":
                    testMode = true;
                    break;
                case "--chapter":
                    chapter = Integer.parseInt(args[++i]);
                    break;
                case "--start":
                    start = Integer.parseInt(args[++i]);
                    break;
                case "--end":
                    end = Integer.parseInt(args[++i]);
                    break;
                case "--data-dir":
                    dataDir = args[++i];
                    break;
                case "--session-dir":
                    sessionDir = args[++i];
                    break;
                case "--review":
                    reviewFile = args[++i];
                    break;
                case "--grade":
                case "-grade":
                    gradeFile = args[++i];
                    break;
            }
        }
        validate();
    }

    void validate() {
        int modes = 0;
        if (testMode) modes++;
        if (reviewFile != null) modes++;
        if (gradeFile != null) modes++;

        if (!interactive && modes == 0 && !showHelp) {
            throw new IllegalArgumentException("No operational mode specified. Use --test, --review, or --grade.");
        }

        if (modes > 1) {
            throw new IllegalArgumentException("Parameters --test, --review, and --grade are mutually exclusive.");
        }

        if (testMode) {
            if (chapter == null || start == null || end == null) {
                throw new IllegalArgumentException("--test mode requires --chapter, --start, and --end parameters.");
            }
        }
    }

    public String getDataDir() {
        return dataDir;
    }

    public String getReviewFile() {
        return reviewFile;
    }
}
