package net.quepierts.endfieldpanorama.earlywindow.render.shader;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface UniformContained {

    @NotNull AbstractUniform getUniform(@NotNull String name);

    default @NotNull AbstractUniform getUniform(@NotNull String name, @NotNull UniformType type) {
        var uniform = this.getUniform(name);

        if (uniform.getType() != type) {
            // throw with wrong type message
            throw new RuntimeException("Wrong uniform type");
        }

        return uniform;
    }

}
