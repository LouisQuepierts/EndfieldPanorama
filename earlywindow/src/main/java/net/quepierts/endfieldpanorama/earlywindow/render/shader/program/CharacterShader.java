package net.quepierts.endfieldpanorama.earlywindow.render.shader.program;

import net.quepierts.endfieldpanorama.earlywindow.render.shader.*;
import org.jetbrains.annotations.NotNull;

public final class CharacterShader extends ShaderProgram {

    public final AbstractUniform uModelMatrix;
    public final AbstractUniform uTexture;

    public CharacterShader(@NotNull ShaderManager manager) {

        super(
                manager,
                Shaders.Vertex.CHARACTER,
                Shaders.Fragment.CHARACTER,
                UniformDefinition.builder()
                        .add("uModelMatrix",    UniformType.MAT4)
                        .add("uTexture",        UniformType.SAMPLER)
                        .build()
        );

        this.uModelMatrix   = this.getUniform("uModelMatrix");
        this.uTexture       = this.getUniform("uTexture");

    }

}
