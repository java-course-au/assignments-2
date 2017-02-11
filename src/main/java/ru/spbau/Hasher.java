package ru.spbau;

import java.io.File;

/**
 * Created by rebryk on 06/03/16.
 */

public interface Hasher {
    String getHash(File file) throws Exception;
}
