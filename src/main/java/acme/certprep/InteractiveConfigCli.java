package acme.certprep;

import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Console;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

@NullMarked
public final class InteractiveConfigCli {
    private static final Logger logger = LoggerFactory.getLogger(InteractiveConfigCli.class);

    private static final String RESET = "\033[0m";
    private static final String GREEN = "\033[0;32m";
    private static final String CYAN = "\033[0;36m";

    private final Path dataDir;
    private final Path sessionDir;
    private final LineReader lineReader;
    private final PrintStream out;

    public InteractiveConfigCli() {
        this(ConfigDefaults.dataDir(), ConfigDefaults.sessionDir(), defaultLineReader(), System.out);
    }

    InteractiveConfigCli(Path dataDir, Path sessionDir, LineReader lineReader, PrintStream out) {
        this.dataDir = Objects.requireNonNull(dataDir, "dataDir");
        this.sessionDir = Objects.requireNonNull(sessionDir, "sessionDir");
        this.lineReader = Objects.requireNonNull(lineReader, "lineReader");
        this.out = Objects.requireNonNull(out, "out");
    }

    public Config prompt() throws InteractiveConfigException {
        SessionRepository sessionRepository = new SessionRepository(sessionDir);

        out.println(CYAN + "Welcome to CertPrep Interactive CLI" + RESET);
        out.println("Available modes:");
        out.println("  1. Exam");
        out.println("  2. Review");
        out.println("  3. Grade");
        String choice = readLine("Select mode (1/2/3): ").trim();

        if ("1".equals(choice)) {
            return promptForExam();
        } else if ("2".equals(choice) || "3".equals(choice)) {
            return promptForExistingSession(sessionRepository, "2".equals(choice));
        }
        throw new InteractiveConfigException("Invalid choice.");
    }

    private Config promptForExam() throws InteractiveConfigException {
        if (!Files.exists(dataDir)) {
            throw new InteractiveConfigException("Data directory does not exist: " + dataDir);
        }

        Map<Integer, List<Integer>> chapterQuestions = loadChapterQuestions();
        List<Integer> chapters = new ArrayList<>(chapterQuestions.keySet());
        if (chapters.isEmpty()) {
            throw new InteractiveConfigException("No chapter files found in " + dataDir);
        }

        Collections.sort(chapters);
        out.println("\nChapters found:");
        for (int i = 0; i < chapters.size(); i++) {
            int chapter = chapters.get(i);
            List<Integer> questions = chapterQuestions.get(chapter);
            Collections.sort(questions);
            out.printf("  %d. Chapter %d (Q%d-%d)\n", i + 1, chapter, questions.get(0), questions.get(questions.size() - 1));
        }

        try {
            int index = Integer.parseInt(readLine("Select chapter #: ").trim()) - 1;
            if (index < 0 || index >= chapters.size()) {
                throw new InteractiveConfigException("Invalid choice.");
            }
            int chapter = chapters.get(index);
            int start = Integer.parseInt(readLine("Enter Start Question #: ").trim());
            int end = Integer.parseInt(readLine("Enter End Question #: ").trim());
            return new ExamConfig(chapter, start, end, dataDir, sessionDir);
        } catch (NumberFormatException e) {
            throw new InteractiveConfigException("Input must be a valid number.", e);
        }
    }

    private Map<Integer, List<Integer>> loadChapterQuestions() throws InteractiveConfigException {
        Map<Integer, List<Integer>> chapterQuestions = new HashMap<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dataDir, CertPrepFiles.QUESTION_IMAGE_GLOB)) {
            for (Path entry : stream) {
                String name = entry.getFileName().toString();
                tryAddChapterQuestion(chapterQuestions, name);
            }
        } catch (IOException e) {
            throw new InteractiveConfigException("Error listing chapters: " + e.getMessage(), e);
        }
        return chapterQuestions;
    }

    private void tryAddChapterQuestion(Map<Integer, List<Integer>> chapterQuestions, String name) {
        Optional<ChapterAndQuestion> maybeChapterAndQuestion = parseChapterAndQuestionNumber(name);
        maybeChapterAndQuestion.ifPresent(chapterAndQuestion ->
                chapterQuestions.computeIfAbsent(
                        chapterAndQuestion.chapter, key -> new ArrayList<>()).add(chapterAndQuestion.question));
    }

    static class ChapterAndQuestion {
        private final int chapter;
        private final int question;

        ChapterAndQuestion(int chapter, int question) {
            this.chapter = chapter;
            this.question = question;
        }

        int getChapter() {
            return chapter;
        }

        int getQuestion() {
            return question;
        }
    }

    static Optional<ChapterAndQuestion> parseChapterAndQuestionNumber(String name) {
        if (! name.startsWith("ch")) {
            logger.warn("Unrecognized filename: '{}' must start with 'ch'.", name);
            return Optional.empty();
        }
        int chIndex = name.indexOf("ch");
        int dashIndex = name.indexOf('-');
        int qIndex = name.indexOf("-q");
        int dotIndex = name.lastIndexOf('.');
        int nameLen = name.length();
        int extLen = nameLen - (dotIndex + 1);
        if (chIndex == 0 && dashIndex > 2 && qIndex != -1 && dotIndex != -1 && (extLen == 3 || extLen == 4)) {
            int chapter;
            try {
                chapter = Integer.parseInt(name.substring(2, dashIndex));
            } catch (NumberFormatException e) {
                logger.warn("Unrecognized chapter in filename: '{}'", name, e);
                return Optional.empty();
            }
            if (chapter < 1) {
                logger.warn("Invalid chapter number {} in filename: '{}'", chapter, name);
                return Optional.empty();
            }

            int question;
            try {
                question = Integer.parseInt(name.substring(qIndex + 2, dotIndex));
            } catch (NumberFormatException e) {
                logger.warn("Unrecognized question in filename: '{}'", name, e);
                return Optional.empty();
            }

            if (question < 1) {
                logger.warn("Invalid question number {} in filename: '{}'", question, name);
                return Optional.empty();
            }

            return Optional.of(new ChapterAndQuestion(chapter, question));
        }
        return Optional.empty();
    }

    private Config promptForExistingSession(SessionRepository sessionRepository, boolean review) throws InteractiveConfigException {
        if (!Files.exists(sessionDir)) {
            throw new InteractiveConfigException("Session directory does not exist: " + sessionDir);
        }

        List<Session> availableSessions;
        try {
            availableSessions = sessionRepository.listSessions();
        } catch (IOException e) {
            throw new InteractiveConfigException("Error listing sessions: " + e.getMessage(), e);
        }

        if (availableSessions.isEmpty()) {
            throw new InteractiveConfigException("No session files found in " + sessionDir);
        }

        out.println("\nSessions found:");
        for (int i = 0; i < availableSessions.size(); i++) {
            Session session = availableSessions.get(i);
            out.printf(
                    "  %d. %s %s %s\n",
                    i + 1,
                    session.getDisplayName(),
                    session.getSummary(),
                    session.isFullyReviewed() ? (GREEN + "[REVIEWED]" + RESET) : ""
            );
        }

        try {
            int index = Integer.parseInt(readLine("Select session #: ").trim()) - 1;
            if (index < 0 || index >= availableSessions.size()) {
                throw new InteractiveConfigException("Invalid choice.");
            }
            Session session = availableSessions.get(index);
            if (review) {
                return new ReviewConfig(dataDir, sessionDir, session.getFile());
            }
            return new GradeConfig(dataDir, sessionDir, session.getFile());
        } catch (NumberFormatException e) {
            throw new InteractiveConfigException("Input must be a valid number.", e);
        }
    }

    private String readLine(String prompt) {
        return lineReader.readLine(prompt);
    }

    private static LineReader defaultLineReader() {
        Console console = System.console();
        if (console != null) {
            return console::readLine;
        }
        Scanner scanner = new Scanner(System.in);
        return prompt -> {
            System.out.print(prompt);
            return scanner.nextLine();
        };
    }

    interface LineReader {
        String readLine(String prompt);
    }
}
