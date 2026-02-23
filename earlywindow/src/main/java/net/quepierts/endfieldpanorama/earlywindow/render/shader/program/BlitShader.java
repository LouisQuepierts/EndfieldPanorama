package net.quepierts.endfieldpanorama.earlywindow.render.shader.program;

import net.quepierts.endfieldpanorama.earlywindow.render.DefaultVertexFormats;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.*;
import org.jetbrains.annotations.NotNull;

public class BlitShader extends ShaderProgram {

    private static final UniformDefinition DEFAULT = UniformDefinition.builder()
            .add("uDiffuseSampler", UniformType.SAMPLER)
            .build();

    public final AbstractUniform uDiffuseSampler;

    public BlitShader(
            @NotNull ShaderManager manager,
            @NotNull String fragment,
            @NotNull UniformDefinition definitions
    ) {
        super(
                manager,
                Shaders.Vertex.BLIT,
                fragment,
                DefaultVertexFormats.BLIT_SCREEN,
                definitions
        );

        uDiffuseSampler = getUniform("uDiffuseSampler");
        uDiffuseSampler.set1i(0);
    }

    public BlitShader(
            @NotNull ShaderManager manager,
            @NotNull String fragment
    ) {
        this(manager, fragment, DEFAULT);
    }

}
