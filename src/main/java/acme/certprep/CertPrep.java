package acme.certprep;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
            System.err.println("Run 'java JavaPractice.java help' for usage details.");
            System.exit(1);
        }

        // REQUIREMENT: If grade option is present, skip UI and print report
        if (configRaw.gradeFile != null) {
            runGradingReport(configRaw);
            System.exit(0);
        }

        applyDarkTheme();

        try {
            final ArgParser config = configRaw;
            if (config.reviewSession != null) {
                final List<SessionRow> allRows = SessionManager.loadAllForReview(config);
                if (allRows.isEmpty()) {
                    System.out.println("The session file appears to be empty.");
                    System.exit(0);
                }
                validateReviewAssets(config, allRows);
                SwingUtilities.invokeLater(() -> new ReviewUI(config, allRows));
            } else {
                final QuestionBank bank = new QuestionBank(config);
                final SessionManager session = new SessionManager(config);
                SwingUtilities.invokeLater(() -> new TestUI(config, bank, session));
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
//            System.err.println("Initialization Error: " + e.getMessage());
            System.exit(1);
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

    private static void validateReviewAssets(ArgParser config, List<SessionRow> rows) throws FileNotFoundException {
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

    private static void applyDarkTheme() {
        UIManager.put("Panel.background", Color.BLACK);
        UIManager.put("Panel.foreground", Color.WHITE);
        UIManager.put("Label.background", Color.BLACK);
        UIManager.put("Label.foreground", Color.WHITE);
        UIManager.put("CheckBox.background", Color.BLACK);
        UIManager.put("CheckBox.foreground", Color.WHITE);
        UIManager.put("Button.background", Color.DARK_GRAY);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("ToggleButton.background", Color.DARK_GRAY);
        UIManager.put("ToggleButton.foreground", Color.WHITE);
        UIManager.put("ScrollPane.background", Color.BLACK);
        UIManager.put("Viewport.background", Color.BLACK);
    }

    static void printHelp() {
        System.out.println("Usage: java JavaPractice.java [options]");
        System.out.println("Options:");
        System.out.println("  --chapter <#>         Chapter to test");
        System.out.println("  --review-session <f>  Navigate/toggle review for a session CSV");
        System.out.println("  --grade <f>           Output score report for a session CSV");
        System.out.println("  --data <path>         Path to images (default: 'data')");
        System.out.println("  --session <path>      Path to sessions (default: 'sessions')");
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