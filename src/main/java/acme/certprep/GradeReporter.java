package acme.certprep;

import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@NullMarked
public final class GradeReporter {
    private static final Logger logger = LoggerFactory.getLogger(GradeReporter.class);

    private static final String RESET = "\033[0m";
    private static final String GREEN = "\033[0;32m";
    private static final String RED = "\033[0;31m";
    private static final String CYAN = "\033[0;36m";

    public String report(GradeConfig config) {
        Session session = new SessionRepository(config.getSessionDir()).existing(config.getSessionFile());
        if (!Files.exists(session.getFile())) {
            return RED + "Error: Session file not found: " + session.getFile() + RESET + System.lineSeparator();
        }

        try {
            List<SessionRow> rows = session.loadRows();
            int total = rows.size();
            int correct = countCorrect(rows);

            if (total == 0) {
                return RED + "No questions found in session: " + session.getDisplayName() + RESET + System.lineSeparator();
            }

            double percent = (correct * 100.0) / total;
            String color = percent >= 68.0 ? GREEN : RED;
            String outcome = percent >= 68.0 ? GREEN + " (PASS)" + RESET : RED + " (FAIL)" + RESET;

            return System.lineSeparator() +
                    CYAN + "========================================" + RESET + System.lineSeparator() +
                    " SESSION GRADE REPORT: " + session.getDisplayName() + System.lineSeparator() +
                    CYAN + "========================================" + RESET + System.lineSeparator() +
                    " Total Questions: " + total + System.lineSeparator() +
                    " Number Correct:  " + correct + System.lineSeparator() +
                    " Final Score:     " + color + String.format("%.2f%%", percent) + RESET + outcome + System.lineSeparator() +
                    CYAN + "========================================" + RESET + System.lineSeparator() +
                    System.lineSeparator();
        } catch (IOException e) {
            logger.warn("Unable to read session file {}", session.getFile(), e);
            return RED + "Error reading session file: " + e.getMessage() + RESET + System.lineSeparator();
        }
    }

    private int countCorrect(List<SessionRow> rows) {
        int correct = 0;
        for (SessionRow row : rows) {
            if (row.isCorrect()) {
                correct++;
            }
        }
        return correct;
    }
}
