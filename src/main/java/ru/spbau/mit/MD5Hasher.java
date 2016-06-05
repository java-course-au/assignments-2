package ru.spbau.mit;

import java.nio.file.Path;

public interface MD5Hasher {
    String calculate(Path path);
}
