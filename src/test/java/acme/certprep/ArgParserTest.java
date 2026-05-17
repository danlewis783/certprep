package acme.certprep;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ArgParserTest {
    private final ArgParser parser = new ArgParser();

    @Test
    void noArgsRequestsInteractiveMode() {
        ParseResult result = parser.parse(new String[0]);

        assertThat(result.getStatus()).isEqualTo(ParseResult.Status.INTERACTIVE);
    }

    @Test
    void helpArgsRequestHelp() {
        assertThat(parser.parse(new String[]{"help"}).getStatus()).isEqualTo(ParseResult.Status.HELP);
        assertThat(parser.parse(new String[]{"--help"}).getStatus()).isEqualTo(ParseResult.Status.HELP);
        assertThat(parser.parse(new String[]{"-h"}).getStatus()).isEqualTo(ParseResult.Status.HELP);
    }

    @Test
    void parsesTestConfig() {
        ParseResult result = parser.parse(new String[]{
                "--test",
                "--chapter", "9",
                "--start", "38",
                "--end", "40",
                "--data-dir", "my-data",
                "--session-dir", "my-sessions"
        });

        assertThat(result.getStatus()).isEqualTo(ParseResult.Status.SUCCESS);
        assertThat(result.getConfig()).isInstanceOfSatisfying(TestConfig.class, config -> {
            assertThat(config.getChapter()).isEqualTo(9);
            assertThat(config.getStart()).isEqualTo(38);
            assertThat(config.getEnd()).isEqualTo(40);
            assertThat(config.getDataDir()).isEqualTo(normalize(Path.of("my-data")));
            assertThat(config.getSessionDir()).isEqualTo(normalize(Path.of("my-sessions")));
        });
    }

    @Test
    void parsesReviewConfigAfterSessionDirRegardlessOfOrder() {
        ParseResult result = parser.parse(new String[]{"--review", "session.csv", "--session-dir", "sessions"});

        assertThat(result.getStatus()).isEqualTo(ParseResult.Status.SUCCESS);
        assertThat(result.getConfig()).isInstanceOfSatisfying(ReviewConfig.class, config ->
                assertThat(config.getSessionFile()).isEqualTo(normalize(Path.of("sessions").resolve("session.csv"))));
    }

    @Test
    void parsesGradeConfig() {
        ParseResult result = parser.parse(new String[]{"--grade", "session.csv", "--session-dir", "sessions"});

        assertThat(result.getStatus()).isEqualTo(ParseResult.Status.SUCCESS);
        assertThat(result.getConfig()).isInstanceOfSatisfying(GradeConfig.class, config ->
                assertThat(config.getSessionFile()).isEqualTo(normalize(Path.of("sessions").resolve("session.csv"))));
    }

    @Test
    void reportsUserFacingFailures() {
        assertThat(parser.parse(new String[]{"--wat"}).getMessage())
                .isEqualTo("Unknown argument: --wat");
        assertThat(parser.parse(new String[]{"--test", "--chapter", "1", "--start", "1"}).getMessage())
                .isEqualTo("--test mode requires --chapter, --start, and --end parameters.");
        assertThat(parser.parse(new String[]{"--test", "--review", "s.csv", "--chapter", "1", "--start", "1", "--end", "2"}).getMessage())
                .isEqualTo("Parameters --test, --review, and --grade are mutually exclusive.");
        assertThat(parser.parse(new String[]{"--chapter", "1"}).getMessage())
                .isEqualTo("No operational mode specified. Use --test, --review, or --grade.");
        assertThat(parser.parse(new String[]{"--chapter"}).getMessage())
                .isEqualTo("--chapter requires a value.");
        assertThat(parser.parse(new String[]{"--chapter", "x"}).getMessage())
                .isEqualTo("--chapter must be a valid integer.");
        assertThat(parser.parse(new String[]{"--chapter", "0"}).getMessage())
                .isEqualTo("--chapter must be positive.");
        assertThat(parser.parse(new String[]{"--grade", "dir/session.csv"}).getMessage())
                .isEqualTo("Please provide only a session filename, not a path: dir/session.csv");
    }

    private static Path normalize(Path path) {
        return path.toAbsolutePath().normalize();
    }
}
