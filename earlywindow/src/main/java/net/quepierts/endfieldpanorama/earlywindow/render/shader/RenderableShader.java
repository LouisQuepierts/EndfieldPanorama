package net.quepierts.endfieldpanorama.earlywindow.render.shader;

import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class RenderableShader {

    @Getter(AccessLevel.PROTECTED)
    protected final ShaderProgram program;

    public RenderableShader(
            @NotNull ShaderManager      manager,
            @NotNull String             vertex,
            @NotNull String             fragment,
            @NotNull UniformDefinition  definitions
    ) {
        this.program = new ShaderProgram(manager, vertex, fragment, definitions);
    }

    public void use() {
        program.use();
    }

    public void unuse() {
        program.unuse();
    }

    public void upload() {
        program.upload();
    }

    public void bindUbo(@NotNull UniformBuffer buffer) {
        program.bind(buffer);
    }

}
