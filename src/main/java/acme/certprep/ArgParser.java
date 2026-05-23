package acme.certprep;

import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class ArgParser {

    public ParseResult parse(String[] args) {
        if (args.length == 0) {
            return ParseResult.interactive();
        }

        Path dataDir = ConfigDefaults.dataDir();
        Path sessionDir = ConfigDefaults.sessionDir();
        Integer chapter = null;
        Integer start = null;
        Integer end = null;
        String reviewFile = null;
        String gradeFile = null;
        boolean examMode = false;

        try {
            for (int i = 0; i < args.length; i++) {
                switch (args[i].toLowerCase()) {
                    case "help":
                    case "--help":
                    case "-h":
                        return ParseResult.help();
                    case "--exam":
                        examMode = true;
                        break;
                    case "--chapter":
                        chapter = parsePositiveInt(readValue(args, ++i, "--chapter"), "--chapter");
                        break;
                    case "--start":
                        start = parsePositiveInt(readValue(args, ++i, "--start"), "--start");
                        break;
                    case "--end":
                        end = parsePositiveInt(readValue(args, ++i, "--end"), "--end");
                        break;
                    case "--data-dir":
                        dataDir = Paths.get(readValue(args, ++i, "--data-dir"));
                        break;
                    case "--session-dir":
                        sessionDir = Paths.get(readValue(args, ++i, "--session-dir"));
                        break;
                    case "--review":
                        reviewFile = readValue(args, ++i, "--review");
                        break;
                    case "--grade":
                        String option = args[i];
                        gradeFile = readValue(args, ++i, option);
                        break;
                    default:
                        return ParseResult.failure("Unknown argument: " + args[i]);
                }
            }

            return buildConfig(examMode, chapter, start, end, dataDir, sessionDir, reviewFile, gradeFile);
        } catch (IllegalArgumentException e) {
            return ParseResult.failure(e.getMessage());
        }
    }

    private static ParseResult buildConfig(
            boolean examMode,
            @Nullable Integer chapter,
            @Nullable Integer start,
            @Nullable Integer end,
            Path dataDir,
            Path sessionDir,
            @Nullable String reviewFile,
            @Nullable String gradeFile
    ) {
        int modes = 0;
        if (examMode) {
            modes++;
        }
        if (reviewFile != null) {
            modes++;
        }
        if (gradeFile != null) {
            modes++;
        }

        if (modes == 0) {
            return ParseResult.failure("No operational mode specified. Use --exam, --review, or --grade.");
        }
        if (modes > 1) {
            return ParseResult.failure("Parameters --exam, --review, and --grade are mutually exclusive.");
        }
        if (examMode) {
            if (chapter == null || start == null || end == null) {
                return ParseResult.failure("--exam mode requires --chapter, --start, and --end parameters.");
            }
            return ParseResult.success(new ExamConfig(chapter, start, end, dataDir, sessionDir));
        }
        if (reviewFile != null) {
            return ParseResult.success(new ReviewConfig(dataDir, sessionDir, resolveSessionFile(sessionDir, reviewFile)));
        }
        return ParseResult.success(new GradeConfig(dataDir, sessionDir, resolveSessionFile(sessionDir, gradeFile)));
    }

    private static String readValue(String[] args, int index, String option) {
        if (index >= args.length) {
            throw new IllegalArgumentException(option + " requires a value.");
        }
        return args[index];
    }

    private static int parsePositiveInt(String value, String option) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed <= 0) {
                throw new IllegalArgumentException(option + " must be positive.");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(option + " must be a valid integer.", e);
        }
    }

    private static Path resolveSessionFile(Path sessionDir, String filename) {
        Path file = Paths.get(filename);
        if (file.getNameCount() != 1) {
            throw new IllegalArgumentException("Please provide only a session filename, not a path: " + filename);
        }
        return sessionDir.resolve(file);
    }
}
