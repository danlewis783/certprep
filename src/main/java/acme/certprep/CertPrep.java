package acme.certprep;

import acme.certprep.ui.CertPrepUi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Console;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class CertPrep {
    private static final Logger logger = LoggerFactory.getLogger(CertPrep.class);

    // ANSI Colors for Console Grading
    private static final String RESET = "\033[0m";
    private static final String GREEN = "\033[0;32m";
    private static final String RED   = "\033[0;31m";
    private static final String CYAN  = "\033[0;36m";

    public static void main(String[] args) {
        ParseResult parseResult = new ArgParser().parse(args);
        switch (parseResult.getStatus()) {
            case HELP:
                printHelp();
                System.exit(0);
                return;
            case FAILURE:
                System.err.println("Argument Error: " + parseResult.getMessage());
                System.err.println("Run 'java acme.certprep.CertPrep help' for usage details.");
                System.exit(1);
                return;
            case INTERACTIVE:
                runConfig(runInteractiveMode());
                return;
            case SUCCESS:
                runConfig(parseResult.getConfig());
                return;
            default:
                System.err.println("Unknown parser result: " + parseResult.getStatus());
                System.exit(1);
        }
    }

    private static void runConfig(Config config) {
        try {
            if (config instanceof GradeConfig) {
                runGradingReport((GradeConfig) config);
            } else if (config instanceof ReviewConfig) {
                ReviewConfig reviewConfig = (ReviewConfig) config;
                Session session = new SessionRepository(reviewConfig.getSessionDir()).existing(reviewConfig.getSessionFile());
                final List<SessionRow> allRows = session.loadRows();
                if (allRows.isEmpty()) {
                    System.out.println("The session file appears to be empty.");
                    System.exit(0);
                }
                validateReviewAssets(reviewConfig, allRows);
                CertPrepUi.showReview(reviewConfig, session, allRows);
            } else if (config instanceof ExamConfig) {
                ExamConfig examConfig = (ExamConfig) config;
                final List<QuestionInfo> questions = QuestionBank.load(examConfig);
                final Session session = new SessionRepository(examConfig.getSessionDir()).createNew();
                CertPrepUi.showExam(examConfig, questions, session);
            } else {
                throw new IllegalArgumentException("Unsupported config type: " + config.getClass().getName());
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static Config runInteractiveMode() {
        Console console = System.console();
        Scanner scanner = (console == null) ? new Scanner(System.in) : null;
        Path dataDir = ConfigDefaults.dataDir();
        Path sessionDir = ConfigDefaults.sessionDir();
        SessionRepository sessionRepository = new SessionRepository(sessionDir);

        System.out.println(CYAN + "Welcome to CertPrep Interactive CLI" + RESET);
        System.out.println("Available modes:");
        System.out.println("  1. Exam");
        System.out.println("  2. Review");
        System.out.println("  3. Grade");
        String choice = readLine(console, scanner, "Select mode (1/2/3): ").trim();

        if ("1".equals(choice)) {
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
                    } catch (NumberFormatException e) {
                        logger.warn("Skipping question image with unrecognized filename: {}", name, e);
                    }
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
                int chapter = chapters.get(index);
                int start = Integer.parseInt(readLine(console, scanner, "Enter Start Question #: ").trim());
                int end = Integer.parseInt(readLine(console, scanner, "Enter End Question #: ").trim());
                return new ExamConfig(chapter, start, end, dataDir, sessionDir);
            } catch (NumberFormatException e) {
                System.err.println("Error: Input must be a valid number.");
                System.exit(1);
            }
        } else if ("2".equals(choice) || "3".equals(choice)) {
            boolean isReview = "2".equals(choice);
            if (!Files.exists(sessionDir)) {
                System.err.println("Error: Session directory does not exist: " + sessionDir);
                System.exit(1);
            }

            List<Session> availableSessions;
            try {
                availableSessions = sessionRepository.listSessions();
            } catch (IOException e) {
                System.err.println("Error listing sessions: " + e.getMessage());
                System.exit(1);
                throw new IllegalStateException("Unable to list sessions", e);
            }

            if (availableSessions.isEmpty()) {
                System.err.println("No session files found in " + sessionDir);
                System.exit(1);
            }

            System.out.println("\nSessions found:");
            for (int i = 0; i < availableSessions.size(); i++) {
                Session session = availableSessions.get(i);
                System.out.printf(
                        "  %d. %s %s %s\n",
                        i + 1,
                        session.getDisplayName(),
                        session.getSummary(),
                        session.isFullyReviewed() ? (GREEN + "[REVIEWED]" + RESET) : ""
                );
            }

            String sessionChoice = readLine(console, scanner, "Select session #: ").trim();
            try {
                int index = Integer.parseInt(sessionChoice) - 1;
                if (index < 0 || index >= availableSessions.size()) {
                    System.err.println("Error: Invalid choice.");
                    System.exit(1);
                }
                Session session = availableSessions.get(index);
                if (isReview) {
                    return new ReviewConfig(dataDir, sessionDir, session.getFile());
                } else {
                    return new GradeConfig(dataDir, sessionDir, session.getFile());
                }
            } catch (NumberFormatException e) {
                System.err.println("Error: Input must be a valid number.");
                System.exit(1);
            }
        } else {
            System.err.println("Error: Invalid choice.");
            System.exit(1);
        }

        throw new IllegalStateException("Interactive mode exited without a config");
    }

    private static String readLine(Console console, Scanner scanner, String prompt) {
        if (console != null) {
            return console.readLine(prompt);
        } else {
            System.out.print(prompt);
            return scanner.nextLine();
        }
    }

    private static void runGradingReport(GradeConfig config) {
        try {
            Session session = new SessionRepository(config.getSessionDir()).existing(config.getSessionFile());
            if (!Files.exists(session.getFile())) {
                System.err.println(RED + "Error: Session file not found: " + session.getFile() + RESET);
                return;
            }

            List<SessionRow> rows = session.loadRows();
            int total = rows.size();
            int correct = 0;
            for (SessionRow row : rows) {
                if (row.isCorrect()) {
                    correct++;
                }
            }

            if (total == 0) {
                System.out.println(RED + "No questions found in session: " + session.getDisplayName() + RESET);
                return;
            }

            double percent = (correct * 100.0) / total;
            String color = (percent >= 68.0) ? GREEN : RED;

            System.out.println("\n" + CYAN + "========================================" + RESET);
            System.out.println(" SESSION GRADE REPORT: " + session.getDisplayName());
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

    private static void validateReviewAssets(ReviewConfig config, List<SessionRow> rows) {
        List<String> missing = new ArrayList<>();
        for (SessionRow r : rows) {
            String q = String.format("ch%02d-q%02d.png", r.getChapter(), r.getQuestion());
            String a1 = String.format("ch%02d-q%02d-answer.png", r.getChapter(), r.getQuestion());
            String a2 = String.format("ch%02d-q%02d-ans.png", r.getChapter(), r.getQuestion());
            if (!Files.exists(config.getDataDir().resolve(q))) {
                missing.add(q);
            }
            if (!Files.exists(config.getDataDir().resolve(a1)) && !Files.exists(config.getDataDir().resolve(a2))) {
                missing.add(a1 + "/" + a2);
            }
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
        System.out.println("  --exam                Start a new practice exam (requires --chapter, --start, --end)");
        System.out.println("  --chapter <#>         Chapter for exam");
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
            if (c == '\"') {
                q = !q;
            } else if (c == ',' && !q) { res.add(sb.toString().trim()); sb.setLength(0); }
            else {
                sb.append(c);
            }
        }
        res.add(sb.toString().trim());
        return res.toArray(new String[0]);
    }

}
