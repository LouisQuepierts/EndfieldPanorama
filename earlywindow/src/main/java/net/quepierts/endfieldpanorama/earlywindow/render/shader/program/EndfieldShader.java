package net.quepierts.endfieldpanorama.earlywindow.render.shader.program;

import net.quepierts.endfieldpanorama.earlywindow.render.DefaultVertexFormats;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.*;
import org.jetbrains.annotations.NotNull;

public final class EndfieldShader extends ShaderProgram {

    public final AbstractUniform uMaskSampler;
    public final AbstractUniform uBackgroundSampler;

    public EndfieldShader(@NotNull ShaderManager manager) {
        super(
                manager,
                Shaders.Vertex.BLIT,
                Shaders.Fragment.FANCY_BACKGROUND,
                DefaultVertexFormats.BLIT_SCREEN,
                UniformDefinition.builder()
                        .add("uMaskSampler",        UniformType.SAMPLER)
                        .add("uBackgroundSampler",  UniformType.SAMPLER)
                        .build()
        );

        uMaskSampler        = getUniform("uMaskSampler");
        uBackgroundSampler  = getUniform("uBackgroundSampler");

        uMaskSampler.set1i(0);
        uBackgroundSampler.set1i(1);

    }

}
