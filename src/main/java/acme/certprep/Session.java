package acme.certprep;

import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@NullMarked
public final class Session {
    private static final Logger logger = LoggerFactory.getLogger(Session.class);

    private final Path file;

    Session(Path file) {
        this.file = Objects.requireNonNull(file, "file");
    }

    public Path getFile() {
        return file;
    }

    public String getDisplayName() {
        return file.getFileName().toString();
    }

    public List<SessionRow> loadRows() throws IOException {
        List<String> lines = Files.readAllLines(file);
        List<SessionRow> rows = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            if (!lines.get(i).trim().isEmpty()) {
                rows.add(new SessionRow(lines.get(i), i));
            }
        }
        return Collections.unmodifiableList(rows);
    }

    public void updateReviewed(SessionRow row, boolean reviewed) throws IOException {
        List<String> lines = Files.readAllLines(file);
        String[] cols = CsvLineParser.parseLine(lines.get(row.lineIndex));
        lines.set(row.lineIndex, String.format("%s,%s,\"%s\",%s,%s,%s,%b", cols[0], cols[1], cols[2], cols[3], cols[4], cols[5], reviewed));
        Files.write(file, lines);
        row.reviewed = reviewed;
    }

    public boolean isFullyReviewed() {
        try {
            List<SessionRow> rows = loadRows();
            if (rows.isEmpty()) {
                return false;
            }
            for (SessionRow row : rows) {
                if (!row.isReviewed()) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            logger.warn("Unable to determine reviewed status for session {}", file, e);
            return false;
        }
    }

    public String getSummary() {
        try {
            List<String> lines = Files.readAllLines(file);
            if (lines.size() <= 1) {
                return "";
            }

            int chapter = -1;
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            for (int i = 1; i < lines.size(); i++) {
                if (lines.get(i).trim().isEmpty()) {
                    continue;
                }
                String[] cols = CsvLineParser.parseLine(lines.get(i));
                if (cols.length >= 2) {
                    try {
                        int currentChapter = Integer.parseInt(cols[0]);
                        int question = Integer.parseInt(cols[1]);
                        if (chapter == -1) {
                            chapter = currentChapter;
                        }
                        min = Math.min(min, question);
                        max = Math.max(max, question);
                    } catch (NumberFormatException e) {
                        logger.warn("Skipping malformed session row while summarizing {}: {}", file, lines.get(i), e);
                    }
                }
            }
            if (chapter == -1) {
                return "";
            }
            return String.format("(Ch%d: Q%d-%d)", chapter, min, max);
        } catch (IOException e) {
            logger.warn("Unable to summarize session {}", file, e);
            return "";
        }
    }

    public void logAnswer(QuestionInfo question, String userAnswer, int elapsedSeconds, boolean correct) {
        try {
            Files.writeString(
                    file,
                    String.format("%d,%d,\"%s\",true,%d,%b,false\n", question.ch, question.q, userAnswer, elapsedSeconds, correct),
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            logger.warn("Unable to append answer to session file {}", file, e);
        }
    }
}
