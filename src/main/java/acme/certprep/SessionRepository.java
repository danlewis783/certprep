package acme.certprep;

import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@NullMarked
public final class SessionRepository {
    private static final Logger logger = LoggerFactory.getLogger(SessionRepository.class);

    private final Path sessionDir;

    public SessionRepository(Path sessionDir) {
        this.sessionDir = Objects.requireNonNull(sessionDir, "sessionDir");
    }

    public Session createNew() throws IOException {
        Files.createDirectories(sessionDir);
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int sequence = 1;
        Path file;
        do {
            file = sessionDir.resolve(String.format("session-%s-%03d.csv", date, sequence++));
        } while (Files.exists(file));

        Files.writeString(file, "Chapter,Question,Answer,Completed,Elapsed Time,Correct Yes/No,Reviewed\n");
        return new Session(file);
    }

    public Session existing(Path sessionFile) {
        return new Session(Objects.requireNonNull(sessionFile, "sessionFile"));
    }

    public List<Session> listSessions() throws IOException {
        List<Session> sessions = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sessionDir, "*.csv")) {
            for (Path entry : stream) {
                sessions.add(new Session(entry));
            }
        }
        sessions.sort((left, right) -> right.getDisplayName().compareTo(left.getDisplayName()));
        return Collections.unmodifiableList(sessions);
    }
}
