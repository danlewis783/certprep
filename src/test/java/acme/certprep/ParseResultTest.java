package acme.certprep;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class ParseResultTest {
    @Test
    void successContainsConfig() {
        ExamConfig config = new ExamConfig(1, 1, 2, Path.of("data"), Path.of("sessions"));
        ParseResult result = ParseResult.success(config);

        assertThat(result.getStatus()).isEqualTo(ParseResult.Status.SUCCESS);
        assertThat(result.getConfig()).isSameAs(config);
    }

    @Test
    void failureContainsMessage() {
        ParseResult result = ParseResult.failure("bad args");

        assertThat(result.getStatus()).isEqualTo(ParseResult.Status.FAILURE);
        assertThat(result.getMessage()).isEqualTo("bad args");
    }

    @Test
    void nonMatchingAccessorsThrow() {
        assertThatIllegalStateException()
                .isThrownBy(() -> ParseResult.help().getConfig())
                .withMessage("Parse result does not contain a config");
        assertThatIllegalStateException()
                .isThrownBy(() -> ParseResult.interactive().getMessage())
                .withMessage("Parse result does not contain a message");
    }

    @Test
    void factoriesRejectNullRequiredValues() {
        assertThatNullPointerException()
                .isThrownBy(() -> ParseResult.success(null))
                .withMessage("config");
        assertThatNullPointerException()
                .isThrownBy(() -> ParseResult.failure(null))
                .withMessage("message");
    }
}
