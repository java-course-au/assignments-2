package ru.spbau.mit;

import java.nio.file.Path;

public interface MD5Hasher {
    public String calculate(Path path);
}
