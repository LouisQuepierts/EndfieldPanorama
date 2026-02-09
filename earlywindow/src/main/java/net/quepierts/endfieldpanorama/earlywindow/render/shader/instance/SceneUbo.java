package net.quepierts.endfieldpanorama.earlywindow.render.shader.instance;

import net.quepierts.endfieldpanorama.earlywindow.render.shader.*;

public final class SceneUbo {

    public final UniformBuffer buffer;

    public final AbstractUniform uProjectionMatrix;
    public final AbstractUniform uViewMatrix;
    public final AbstractUniform uInverseProjectionMatrix;
    public final AbstractUniform uInverseViewMatrix;

    public final AbstractUniform uTime;

    public SceneUbo() {
        this.buffer = new UniformBuffer(
                "Scene",
                UniformDefinition.builder()
                        .add("uProjectionMatrix",           UniformType.MAT4)
                        .add("uInverseProjectionMatrix",    UniformType.MAT4)
                        .add("uViewMatrix",                 UniformType.MAT4)
                        .add("uInverseViewMatrix",          UniformType.MAT4)
                        .add("uTime",                       UniformType.FLOAT)
                        .build(),
                0
        );

        this.uProjectionMatrix          = buffer.getUniform("uProjectionMatrix");
        this.uViewMatrix                = buffer.getUniform("uViewMatrix");
        this.uInverseProjectionMatrix   = buffer.getUniform("uInverseProjectionMatrix");
        this.uInverseViewMatrix         = buffer.getUniform("uInverseViewMatrix");

        this.uTime                      = buffer.getUniform("uTime");
    }

}
