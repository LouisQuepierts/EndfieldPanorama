package net.quepierts.endfieldpanorama.earlywindow.render.shader.ubo;

import net.quepierts.endfieldpanorama.earlywindow.render.shader.*;

public final class SceneUbo extends UniformBuffer {

    public static final int BINDING_POINT = 0;

    public final AbstractUniform uProjectionMatrix;
    public final AbstractUniform uViewMatrix;
    public final AbstractUniform uInverseProjectionMatrix;
    public final AbstractUniform uInverseViewMatrix;

    public final AbstractUniform uTime;

    public SceneUbo() {
        super(
                "Scene",
                UniformDefinition.builder()
                        .add("uProjectionMatrix",           UniformType.MAT4)
                        .add("uInverseProjectionMatrix",    UniformType.MAT4)
                        .add("uViewMatrix",                 UniformType.MAT4)
                        .add("uInverseViewMatrix",          UniformType.MAT4)
                        .add("uTime",                       UniformType.FLOAT)
                        .build(),
                SceneUbo.BINDING_POINT
        );

        this.uProjectionMatrix          = this.getUniform("uProjectionMatrix");
        this.uViewMatrix                = this.getUniform("uViewMatrix");
        this.uInverseProjectionMatrix   = this.getUniform("uInverseProjectionMatrix");
        this.uInverseViewMatrix         = this.getUniform("uInverseViewMatrix");

        this.uTime                      = this.getUniform("uTime");
    }

}
