package acme.certprep;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigDefaultsTest {
    @Test
    void defaultDirectoriesAreUnderUserCertprepDirectory() {
        Path certprepDir = Path.of(System.getProperty("user.home"), ".certprep").toAbsolutePath().normalize();

        assertThat(ConfigDefaults.dataDir()).isEqualTo(certprepDir.resolve("data"));
        assertThat(ConfigDefaults.sessionDir()).isEqualTo(certprepDir.resolve("sessions"));
    }
}
