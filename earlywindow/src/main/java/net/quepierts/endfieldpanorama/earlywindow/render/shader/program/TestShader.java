package net.quepierts.endfieldpanorama.earlywindow.render.shader.program;

import net.quepierts.endfieldpanorama.earlywindow.render.DefaultVertexFormats;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.*;
import org.jetbrains.annotations.NotNull;

public final class TestShader extends ShaderProgram {

    public final AbstractUniform uTexture;

    public TestShader(@NotNull ShaderManager manager) {
        super(
                manager,
                Shaders.Vertex.BLIT,
                "test",
                DefaultVertexFormats.BLIT_SCREEN,
                UniformDefinition.builder()
                        .add("uTexture", UniformType.SAMPLER)
                        .build()
        );

        this.uTexture = this.getUniform("uTexture");

    }
}
