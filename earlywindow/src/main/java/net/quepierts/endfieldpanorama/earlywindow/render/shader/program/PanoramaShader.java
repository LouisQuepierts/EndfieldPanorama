package net.quepierts.endfieldpanorama.earlywindow.render.shader.program;

import net.quepierts.endfieldpanorama.earlywindow.render.DefaultVertexFormats;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.*;
import org.jetbrains.annotations.NotNull;

public final class PanoramaShader extends ShaderProgram {

    public final AbstractUniform uTexture;
    public final AbstractUniform uProjectionViewMatrix;

    public PanoramaShader(@NotNull ShaderManager manager) {

        super(
                manager,
                Shaders.Vertex.PANORAMA,
                Shaders.Fragment.PANORAMA,
                DefaultVertexFormats.PANORAMA,
                UniformDefinition.builder()
                        .add("uTexture",                UniformType.SAMPLER)
                        .add("uProjectionViewMatrix",   UniformType.MAT4)
                        .build()
        );

        this.uTexture               = this.getUniform("uTexture");
        this.uProjectionViewMatrix  = this.getUniform("uProjectionViewMatrix");

        this.uTexture.set1i(0);

    }

}
