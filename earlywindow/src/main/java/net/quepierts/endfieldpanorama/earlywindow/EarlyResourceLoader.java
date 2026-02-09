package net.quepierts.endfieldpanorama.earlywindow;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@UtilityClass
@SuppressWarnings("unused")
public class EarlyResourceLoader {

    public static boolean hasResource(final String path) {
        var is = loadResource(path);
        if (is != null) {
            try {
                is.close();
                return true;
            } catch (IOException ignored) {
            }
        }
        return false;
    }

    public static @Nullable InputStream loadResource(final String path) {
        var p = path.startsWith("/") ? path.substring(1) : path;

        return EarlyResourceLoader.class
                .getClassLoader()
                .getResourceAsStream(p);
    }

    public static @NotNull InputStream loadResourceOrThrow(final String path) {
        var is = loadResource(path);

        if (is == null) {
            throw new RuntimeException("Failed to load resource: " + path);
        }

        return is;
    }

    public static @NotNull String loadText(final String path) {
        try (var is     = loadResource(path);
             var reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
        ) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            return "";
        }
    }


}
