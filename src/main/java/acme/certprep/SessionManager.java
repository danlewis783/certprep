package acme.certprep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

    Path sessionFile;

    SessionManager(TestConfig config) throws IOException {
        Files.createDirectories(config.getSessionDir());
        String ds = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int s = 1;
        Path c;
        do {
            c = config.getSessionDir().resolve(String.format("session-%s-%03d.csv", ds, s++));
        } while (Files.exists(c));
        sessionFile = c;
        Files.writeString(sessionFile, "Chapter,Question,Answer,Completed,Elapsed Time,Correct Yes/No,Reviewed\n");
    }

    static List<SessionRow> loadAllForReview(ReviewConfig config) throws IOException {
        List<String> l = Files.readAllLines(config.getSessionFile());
        List<SessionRow> r = new ArrayList<>();
        for (int i = 1; i < l.size(); i++) if (!l.get(i).trim().isEmpty()) r.add(new SessionRow(l.get(i), i));
        return r;
    }

    public static void upd(ReviewConfig config, SessionRow r, boolean s) throws IOException {
        Path p = config.getSessionFile();
        List<String> l = Files.readAllLines(p);
        String[] c = CertPrep.parseCSVLine(l.get(r.lineIndex));
        l.set(r.lineIndex, String.format("%s,%s,\"%s\",%s,%s,%s,%b", c[0], c[1], c[2], c[3], c[4], c[5], s));
        Files.write(p, l);
        r.reviewed = s;
    }

    static boolean isFullyReviewed(Path sessionDir, String filename) {
        try {
            Path p = sessionDir.resolve(filename);
            List<String> lines = Files.readAllLines(p);
            if (lines.size() <= 1) return false;
            for (int i = 1; i < lines.size(); i++) {
                if (lines.get(i).trim().isEmpty()) continue;
                String[] cols = CertPrep.parseCSVLine(lines.get(i));
                if (cols.length < 7 || !Boolean.parseBoolean(cols[6])) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            logger.warn("Unable to determine reviewed status for session {}", filename, e);
            return false;
        }
    }

    static String getSessionSummary(Path sessionDir, String filename) {
        try {
            Path p = sessionDir.resolve(filename);
            List<String> lines = Files.readAllLines(p);
            if (lines.size() <= 1) return "";
            int ch = -1;
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            for (int i = 1; i < lines.size(); i++) {
                if (lines.get(i).trim().isEmpty()) continue;
                String[] cols = CertPrep.parseCSVLine(lines.get(i));
                if (cols.length >= 2) {
                    try {
                        int currentCh = Integer.parseInt(cols[0]);
                        int q = Integer.parseInt(cols[1]);
                        if (ch == -1) ch = currentCh;
                        min = Math.min(min, q);
                        max = Math.max(max, q);
                    } catch (NumberFormatException e) {
                        logger.warn("Skipping malformed session row while summarizing {}: {}", filename, lines.get(i), e);
                    }
                }
            }
            if (ch == -1) return "";
            return String.format("(Ch%d: Q%d-%d)", ch, min, max);
        } catch (IOException e) {
            logger.warn("Unable to summarize session {}", filename, e);
            return "";
        }
    }

    public void logAnswer(QuestionInfo q, String ua, int et, boolean ic) {
        try {
            Files.writeString(sessionFile, String.format("%d,%d,\"%s\",true,%d,%b,false\n", q.ch, q.q, ua, et, ic), StandardOpenOption.APPEND);
        } catch (IOException e) {
            logger.warn("Unable to append answer to session file {}", sessionFile, e);
        }
    }
}
