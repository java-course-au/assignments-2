package ru.spbau.mit;

import java.nio.file.Path;

/**
 * Created by ldvsoft on 01.03.16.
 */
public interface FileHasher {
    String getDigest(Path path);
}
