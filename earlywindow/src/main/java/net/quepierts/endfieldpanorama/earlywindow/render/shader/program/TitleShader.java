package net.quepierts.endfieldpanorama.earlywindow.render.shader.program;

import net.quepierts.endfieldpanorama.earlywindow.render.shader.*;
import org.jetbrains.annotations.NotNull;

public final class TitleShader extends ShaderProgram {

    public final AbstractUniform uMaskSampler;
    public final AbstractUniform uBackgroundSampler;

    public TitleShader(@NotNull ShaderManager manager) {

        super(
                manager,
                Shaders.Vertex.BLIT,
                Shaders.Fragment.FANCY_BACKGROUND,

                UniformDefinition.builder()
                        .add("uMaskSampler",        UniformType.SAMPLER)
                        .add("uBackgroundSampler",  UniformType.SAMPLER)
                        .build()
        );

        this.uMaskSampler               = this.getUniform("uMaskSampler");
        this.uBackgroundSampler         = this.getUniform("uBackgroundSampler");
    }

}
