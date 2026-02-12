package net.quepierts.endfieldpanorama.earlywindow;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
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

    public static byte @NotNull [] loadByteArray(final String path) {
        try (var is     = loadResource(path);
             var out    = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }

            return out.toByteArray();

        } catch (IOException e) {
            return new byte[0];
        }
    }


}
