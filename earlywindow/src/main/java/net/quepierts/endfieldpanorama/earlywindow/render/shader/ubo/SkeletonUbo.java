package net.quepierts.endfieldpanorama.earlywindow.render.shader.ubo;

import lombok.Getter;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.UniformBuffer;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.nio.FloatBuffer;

public final class SkeletonUbo extends UniformBuffer {

    public static final int BINDING_POINT   = 1;
    public static final int MAT4_SIZE       = 16 * 4;

    private final FloatBuffer   view;
    private final float[]       cache;

    @Getter
    private final int           length;

    public SkeletonUbo(int length) {

        super(
                "AnimationTransform",
                length * MAT4_SIZE,
                SkeletonUbo.BINDING_POINT
        );

        this.length = length;

        this.view = this.buffer.asFloatBuffer();
        this.cache = new float[16];

    }

    public void put(int index, @NotNull Matrix4f matrix) {
        view.position(index * 16);
        view.put(matrix.get(cache));

        this.dirty = true;
    }

    public void put(float[] value) {

        if (this.length * 16 >= value.length) {
            throw new IllegalArgumentException("Value length is not equal to length * 16");
        }

        view.rewind();
        view.put(value);
        this.dirty = true;
    }

}
