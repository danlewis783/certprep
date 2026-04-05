package acme.certprep;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

class SessionManager {
    Path sessionFile;

    SessionManager(ArgParser config) throws IOException {
        Files.createDirectories(Paths.get(config.sessionDir));
        String ds = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int s = 1;
        Path c;
        do {
            c = Paths.get(config.sessionDir, String.format("session-%s-%03d.csv", ds, s++));
        } while (Files.exists(c));
        sessionFile = c;
        Files.writeString(sessionFile, "Chapter,Question,Answer,Completed,Elapsed Time,Correct Yes/No,Reviewed\n");
    }

    static Path resolvePath(String sessionDir, String filename) {
        Path p = Paths.get(filename);
        if (Files.exists(p)) return p;
        return Paths.get(sessionDir, filename);
    }

    static List<SessionRow> loadAllForReview(ArgParser cfg) throws IOException {
        Path p = resolvePath(cfg.sessionDir, cfg.reviewSession);
        List<String> l = Files.readAllLines(p);
        List<SessionRow> r = new ArrayList<>();
        for (int i = 1; i < l.size(); i++) if (!l.get(i).trim().isEmpty()) r.add(new SessionRow(l.get(i), i));
        return r;
    }

    static void upd(ArgParser cfg, SessionRow r, boolean s) throws IOException {
        Path p = resolvePath(cfg.sessionDir, cfg.reviewSession);
        List<String> l = Files.readAllLines(p);
        String[] c = CertPrep.parseCSVLine(l.get(r.lineIndex));
        l.set(r.lineIndex, String.format("%s,%s,\"%s\",%s,%s,%s,%b", c[0], c[1], c[2], c[3], c[4], c[5], s));
        Files.write(p, l);
        r.reviewed = s;
    }

    void logAnswer(QuestionInfo q, String ua, int et, boolean ic) {
        try {
            Files.writeString(sessionFile, String.format("%d,%d,\"%s\",true,%d,%b,false\n", q.ch, q.q, ua, et, ic), StandardOpenOption.APPEND);
        } catch (IOException e) {
        }
    }
}
