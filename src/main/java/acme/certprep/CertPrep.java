package acme.certprep;

import acme.certprep.ui.CertPrepUi;
import java.io.Console;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class CertPrep {

    // ANSI Colors for Console Grading
    private static final String RESET = "\033[0m";
    private static final String GREEN = "\033[0;32m";
    private static final String RED   = "\033[0;31m";
    private static final String CYAN  = "\033[0;36m";

    public static void main(String[] args) {
        ArgParser configRaw = null;
        try {
            configRaw = new ArgParser(args);
            if (configRaw.showHelp) {
                printHelp();
                System.exit(0);
            }
        } catch (Exception e) {
            System.err.println("Argument Error: " + e.getMessage());
            System.err.println("Run 'java acme.certprep.CertPrep help' for usage details.");
            System.exit(1);
        }

        if (configRaw.gradeFile != null) {
            runGradingReport(configRaw);
            System.exit(0);
        }

        if (configRaw.interactive) {
            runInteractiveMode(configRaw);
        }

        try {
            final ArgParser config = configRaw;
            if (config.reviewFile != null) {
                final List<SessionRow> allRows = SessionManager.loadAllForReview(config);
                if (allRows.isEmpty()) {
                    System.out.println("The session file appears to be empty.");
                    System.exit(0);
                }
                validateReviewAssets(config, allRows);
                CertPrepUi.showReview(config, allRows);
            } else if (config.testMode) {
                final QuestionBank bank = new QuestionBank(config);
                final SessionManager session = new SessionManager(config);
                CertPrepUi.showTest(config, bank, session);
            } else if (!config.interactive) {
                System.err.println("No operational mode specified. Use --test, --review, or --grade.");
                printHelp();
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static void runInteractiveMode(ArgParser config) {
        Console console = System.console();
        Scanner scanner = (console == null) ? new Scanner(System.in) : null;

        System.out.println(CYAN + "Welcome to CertPrep Interactive CLI" + RESET);
        System.out.println("Available modes:");
        System.out.println("  1. Test");
        System.out.println("  2. Review");
        System.out.println("  3. Grade");
        String choice = readLine(console, scanner, "Select mode (1/2/3): ").trim();

        if ("1".equals(choice)) {
            config.testMode = true;
            Path dataDir = Paths.get(config.dataDir);
            if (!Files.exists(dataDir)) {
                System.err.println("Error: Data directory does not exist: " + dataDir);
                System.exit(1);
            }

            java.util.Map<Integer, int[]> chapterRanges = new java.util.HashMap<>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dataDir, "ch*-q*.png")) {
                for (Path entry : stream) {
                    String name = entry.getFileName().toString();
                    try {
                        int dashIndex = name.indexOf('-');
                        int qIndex = name.indexOf("-q");
                        int dotIndex = name.lastIndexOf('.');
                        if (dashIndex > 2 && qIndex != -1 && dotIndex != -1) {
                            int ch = Integer.parseInt(name.substring(2, dashIndex));
                            int q = Integer.parseInt(name.substring(qIndex + 2, dotIndex));
                            int[] range = chapterRanges.computeIfAbsent(ch, k -> new int[]{Integer.MAX_VALUE, Integer.MIN_VALUE});
                            range[0] = Math.min(range[0], q);
                            range[1] = Math.max(range[1], q);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            } catch (IOException e) {
                System.err.println("Error listing chapters: " + e.getMessage());
                System.exit(1);
            }

            List<Integer> chapters = new ArrayList<>(chapterRanges.keySet());
            if (chapters.isEmpty()) {
                System.err.println("No chapter files found in " + dataDir);
                System.exit(1);
            }

            Collections.sort(chapters);
            System.out.println("\nChapters found:");
            for (int i = 0; i < chapters.size(); i++) {
                int ch = chapters.get(i);
                int[] range = chapterRanges.get(ch);
                System.out.printf("  %d. Chapter %d (Q%d-%d)\n", i + 1, ch, range[0], range[1]);
            }

            String chapterChoice = readLine(console, scanner, "Select chapter #: ").trim();
            try {
                int index = Integer.parseInt(chapterChoice) - 1;
                if (index < 0 || index >= chapters.size()) {
                    System.err.println("Error: Invalid choice.");
                    System.exit(1);
                }
                config.chapter = chapters.get(index);
                config.start = Integer.parseInt(readLine(console, scanner, "Enter Start Question #: ").trim());
                config.end = Integer.parseInt(readLine(console, scanner, "Enter End Question #: ").trim());
            } catch (NumberFormatException e) {
                System.err.println("Error: Input must be a valid number.");
                System.exit(1);
            }
        } else if ("2".equals(choice) || "3".equals(choice)) {
            boolean isReview = "2".equals(choice);
            Path sessionDir = Paths.get(config.sessionDir);
            if (!Files.exists(sessionDir)) {
                System.err.println("Error: Session directory does not exist: " + sessionDir);
                System.exit(1);
            }

            List<String> sessions = new ArrayList<>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(sessionDir, "*.csv")) {
                for (Path entry : stream) {
                    sessions.add(entry.getFileName().toString());
                }
            } catch (IOException e) {
                System.err.println("Error listing sessions: " + e.getMessage());
                System.exit(1);
            }

            if (sessions.isEmpty()) {
                System.err.println("No session files found in " + sessionDir);
                System.exit(1);
            }

            sessions.sort(Collections.reverseOrder());
            System.out.println("\nSessions found:");
            for (int i = 0; i < sessions.size(); i++) {
                String name = sessions.get(i);
                boolean reviewed = SessionManager.isFullyReviewed(config.sessionDir, name);
                String summary = SessionManager.getSessionSummary(config.sessionDir, name);
                System.out.printf("  %d. %s %s %s\n", i + 1, name, summary, reviewed ? (GREEN + "[REVIEWED]" + RESET) : "");
            }

            String sessionChoice = readLine(console, scanner, "Select session #: ").trim();
            try {
                int index = Integer.parseInt(sessionChoice) - 1;
                if (index < 0 || index >= sessions.size()) {
                    System.err.println("Error: Invalid choice.");
                    System.exit(1);
                }
                if (isReview) {
                    config.reviewFile = sessions.get(index);
                } else {
                    config.gradeFile = sessions.get(index);
                    runGradingReport(config);
                    System.exit(0);
                }
            } catch (NumberFormatException e) {
                System.err.println("Error: Input must be a valid number.");
                System.exit(1);
            }
        } else {
            System.err.println("Error: Invalid choice.");
            System.exit(1);
        }

        try {
            config.validate();
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static String readLine(Console console, Scanner scanner, String prompt) {
        if (console != null) {
            return console.readLine(prompt);
        } else {
            System.out.print(prompt);
            return scanner.nextLine();
        }
    }

    private static void runGradingReport(ArgParser config) {
        try {
            Path path = SessionManager.resolvePath(config.sessionDir, config.gradeFile);
            if (!Files.exists(path)) {
                System.err.println(RED + "Error: Session file not found: " + path + RESET);
                return;
            }

            List<String> lines = Files.readAllLines(path);
            int total = 0;
            int correct = 0;

            for (int i = 1; i < lines.size(); i++) {
                if (lines.get(i).trim().isEmpty()) continue;
                String[] cols = parseCSVLine(lines.get(i));
                if (cols.length > 5) {
                    total++;
                    if (Boolean.parseBoolean(cols[5])) {
                        correct++;
                    }
                }
            }

            if (total == 0) {
                System.out.println(RED + "No questions found in session: " + config.gradeFile + RESET);
                return;
            }

            double percent = (correct * 100.0) / total;
            String color = (percent >= 68.0) ? GREEN : RED;

            System.out.println("\n" + CYAN + "========================================" + RESET);
            System.out.println(" SESSION GRADE REPORT: " + config.gradeFile);
            System.out.println(CYAN + "========================================" + RESET);
            System.out.println(" Total Questions: " + total);
            System.out.println(" Number Correct:  " + correct);
            System.out.print(" Final Score:     " + color + String.format("%.2f%%", percent) + RESET);
            System.out.println(percent >= 68.0 ? GREEN + " (PASS)" + RESET : RED + " (FAIL)" + RESET);
            System.out.println(CYAN + "========================================\n" + RESET);

        } catch (IOException e) {
            System.err.println(RED + "Error reading session file: " + e.getMessage() + RESET);
        }
    }

    private static void validateReviewAssets(ArgParser config, List<SessionRow> rows) {
        List<String> missing = new ArrayList<>();
        for (SessionRow r : rows) {
            String q = String.format("ch%02d-q%02d.png", r.chapter, r.question);
            String a1 = String.format("ch%02d-q%02d-answer.png", r.chapter, r.question);
            String a2 = String.format("ch%02d-q%02d-ans.png", r.chapter, r.question);
            if (!Files.exists(Paths.get(config.dataDir, q))) missing.add(q);
            if (!Files.exists(Paths.get(config.dataDir, a1)) && !Files.exists(Paths.get(config.dataDir, a2))) missing.add(a1 + "/" + a2);
        }
        if (!missing.isEmpty()) {
            System.err.println(RED + "FATAL: Missing Assets" + RESET);
            missing.forEach(m -> System.err.println(" - " + m));
            System.exit(1);
        }
    }

    static void printHelp() {
        System.out.println("Usage: java acme.certprep.CertPrep [options]");
        System.out.println("Options:");
        System.out.println("  --test                Start a new practice test (requires --chapter, --start, --end)");
        System.out.println("  --chapter <#>         Chapter to test");
        System.out.println("  --start <#>           First question number");
        System.out.println("  --end <#>             Last question number");
        System.out.println("  --review <f>          Navigate/toggle review for a session CSV");
        System.out.println("  --grade <f>           Output score report for a session CSV");
        System.out.println("  --data-dir <path>     Path to images (default: '~/.certprep/data')");
        System.out.println("  --session-dir <path>  Path to sessions (default: '~/.certprep/sessions')");
    }

    public static String[] parseCSVLine(String line) {
        List<String> res = new ArrayList<>();
        boolean q = false; StringBuilder sb = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == '\"') q = !q;
            else if (c == ',' && !q) { res.add(sb.toString().trim()); sb.setLength(0); }
            else sb.append(c);
        }
        res.add(sb.toString().trim());
        return res.toArray(new String[0]);
    }

}
