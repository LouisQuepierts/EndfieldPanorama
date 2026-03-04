package net.quepierts.endfieldpanorama.earlywindow.render.shader.program;

import net.quepierts.endfieldpanorama.earlywindow.render.DefaultVertexFormats;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.*;
import org.jetbrains.annotations.NotNull;

public final class ScreenElementShader extends ShaderProgram {

    public final AbstractUniform uDiffuseSampler;
    public final AbstractUniform uProjectionViewMatrix;
    public final AbstractUniform uColor;
    public final AbstractUniform uRenderType;

    public ScreenElementShader(
            @NotNull ShaderManager  manager,
            @NotNull String         fragment
    ) {

        super(
                manager,
                Shaders.Vertex.SCREEN,
                fragment,
                DefaultVertexFormats.BLIT_SCREEN,
                UniformDefinition.builder()
                        .add("uDiffuseSampler",         UniformType.SAMPLER)
                        .add("uProjectionViewMatrix",   UniformType.MAT4)
                        .add("uColor",                  UniformType.VEC4)
                        .add("uRenderType",             UniformType.INT)
                        .build()
        );

        this.uDiffuseSampler        = this.getUniform("uDiffuseSampler");
        this.uProjectionViewMatrix  = this.getUniform("uProjectionViewMatrix");
        this.uColor                 = this.getUniform("uColor");
        this.uRenderType            = this.getUniform("uRenderType");

    }

}
