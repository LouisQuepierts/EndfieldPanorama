package net.quepierts.endfieldpanorama.earlywindow.render.shader.ubo;

import lombok.Getter;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.UniformBuffer;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.nio.FloatBuffer;

public final class SkeletonUbo extends UniformBuffer {

    public static final int BINDING_POINT   = 1;
    public static final int MAT4_SIZE       = 16 * 4;
    public static final int MAX_BONE        = 64;

    private final FloatBuffer   view;

    @Getter
    private final int           length;

    public SkeletonUbo(int length) {

        super(
                "AnimationSkeleton",
                MAX_BONE * MAT4_SIZE,
                SkeletonUbo.BINDING_POINT
        );

        this.length = length;

        this.view = this.buffer.asFloatBuffer();

        this.identity();
    }

    public void put(int index, @NotNull Matrix4f matrix) {
        view.position(index * 16);
        matrix.get(view);

        this.dirty = true;
    }

    public void put(Matrix4f[] matrices) {
        var ptr = 0;

        for (var matrix : matrices) {
            this.view.position(ptr);
            matrix.get(this.view);
            ptr += 16;
        }

        this.dirty = true;
    }

    public void identity() {
        var mat = new Matrix4f();
        var ptr = 0;

        for (int i = 0; i < this.length; i++) {
            this.view.position(ptr);
            mat.get(this.view);
            ptr += 16;
        }

        this.dirty = true;
    }

    public void put(float[] value) {

        if (this.length * 16 > value.length) {
            throw new IllegalArgumentException("Value length is not equal to length * 16");
        }

        view.rewind();
        view.put(value);
        this.dirty = true;
    }

}
