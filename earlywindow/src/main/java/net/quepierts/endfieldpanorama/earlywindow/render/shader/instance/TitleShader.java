package net.quepierts.endfieldpanorama.earlywindow.render.shader.instance;

import net.quepierts.endfieldpanorama.earlywindow.render.shader.*;
import org.jetbrains.annotations.NotNull;

public final class TitleShader extends RenderableShader {

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

        this.uMaskSampler               = this.getProgram().getUniform("uMaskSampler");
        this.uBackgroundSampler         = this.getProgram().getUniform("uBackgroundSampler");
    }

}
