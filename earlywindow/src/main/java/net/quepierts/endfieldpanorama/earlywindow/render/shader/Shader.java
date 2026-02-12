package net.quepierts.endfieldpanorama.earlywindow.render.shader;

import lombok.Getter;
import net.quepierts.endfieldpanorama.earlywindow.EarlyResourceLoader;
import net.quepierts.endfieldpanorama.earlywindow.Resource;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL31;

@Getter
public final class Shader implements Resource {

    private final int id;
    private final String name;
    private final ShaderType type;

    private boolean free = false;

    public Shader(
            @NotNull String name,
            @NotNull ShaderType type
    ) {

        this.name = name;
        this.type = type;

        this.id   = GL31.glCreateShader(type.glType);

    }

    public void fastload() {
        this.fromSource("shaders/" + name + type.subfix);
    }

    public void upload(@NotNull String source) {
        GL31.glShaderSource(this.id, source);
        GL31.glCompileShader(this.id);
        if (GL31.glGetShaderi(this.id, GL31.GL_COMPILE_STATUS) == GL31.GL_FALSE) {
            throw new RuntimeException(GL31.glGetShaderInfoLog(this.id));
        }
    }

    public void fromSource(@NotNull String url) {
        var source = EarlyResourceLoader.loadText(url);
        this.upload(source);
    }

    @Override
    public void free() {
        if (this.free || this.id == 0) {
            return;
        }

        GL31.glDeleteShader(this.id);
    }
}
