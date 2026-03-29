package certprep;

class ArgParser {
    Integer chapter = null, start = null, end = null;
    String dataDir = "data", sessionDir = "sessions", reviewSession = null, gradeFile = null;
    boolean showHelp = false;

    ArgParser(String[] args) {
        if (args.length == 0) {
            showHelp = true;
            return;
        }
        for (int i = 0; i < args.length; i++) {
            switch (args[i].toLowerCase()) {
                case "help":
                case "--help":
                case "-h":
                    showHelp = true;
                    return;
                case "--chapter":
                    chapter = Integer.parseInt(args[++i]);
                    break;
                case "--start":
                    start = Integer.parseInt(args[++i]);
                    break;
                case "--end":
                    end = Integer.parseInt(args[++i]);
                    break;
                case "--data":
                    dataDir = args[++i];
                    break;
                case "--session":
                    sessionDir = args[++i];
                    break;
                case "--review-session":
                    reviewSession = args[++i];
                    break;
                case "--grade":
                case "-grade":
                    gradeFile = args[++i];
                    break;
            }
        }
    }
}
