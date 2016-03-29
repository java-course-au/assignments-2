package ru.spbau.mit.simpleftp.etc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

public final class ConfigParser {
    private ConfigParser() {
    }

    public static Properties parseConfig(Path path) throws IOException {
        InputStream propStream = new FileInputStream(path.toString());
        Properties properties = new Properties();
        properties.load(propStream);
        return properties;
    }
}
