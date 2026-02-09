package net.quepierts.endfieldpanorama.earlywindow.render.shader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public final class ShaderManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("EARLYDISPLAY");

    private final HashMap<String, Shader>[] shaders;

    public ShaderManager() {
        this.shaders = new HashMap[ShaderType.values().length];

        for (int i = 0; i < this.shaders.length; i++) {
            this.shaders[i] = new HashMap<>();
        }
    }

    public @Nullable Shader getShader(
            @NotNull String     name,
            @NotNull ShaderType type
    ) {
        var shaders = this.shaders[type.ordinal()];
        var exists  = shaders.containsKey(name);

        if (!exists) {
            var shader = new Shader(name, type);
            try {
                shader.fastload();
                shaders.put(name, shader);
            } catch (Exception e) {
                LOGGER.error("Failed to load shader: {}", name, e);
                return null;
            }
            return shader;
        }

        return shaders.get(name);
    }

    public void free() {
        for (var shaders : this.shaders) {
            for (var shader : shaders.values()) {
                shader.free();
            }
        }
    }

}
