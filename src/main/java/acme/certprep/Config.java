package acme.certprep;

import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;

@NullMarked
public interface Config {
    Path getDataDir();

    Path getSessionDir();
}
