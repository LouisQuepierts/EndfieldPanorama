package net.quepierts.endfieldpanorama.earlywindow.render.shader;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Arrays;

@Getter
public abstract class AbstractUniform {

    public static final AbstractUniform DUMMY;

    private final UniformType type;

    protected final float[] floats;
    protected final int[] ints;

    private boolean dirty;

    protected AbstractUniform(UniformType type) {
        this.type = type;

        this.floats = new float[type.length];
        this.ints = new int[type.length];
    }

    public void set1f(float value) {
        if (floats[0] != value) {
            dirty = true;
            floats[0] = value;
        }
    }

    public void set2f(float value1, float value2) {
        if (floats[0] != value1
                || floats[1] != value2) {
            dirty = true;
            floats[0] = value1;
            floats[1] = value2;
        }
    }

    public void set3f(float value1, float value2, float value3) {
        if (floats[0] != value1
                || floats[1] != value2
                || floats[2] != value3
        ) {
            dirty = true;
            floats[0] = value1;
            floats[1] = value2;
            floats[2] = value3;
        }
    }

    public void set4f(float value1, float value2, float value3, float value4) {
        if (floats[0] != value1
                || floats[1] != value2
                || floats[2] != value3
                || floats[3] != value4
        ) {
            dirty = true;
            floats[0] = value1;
            floats[1] = value2;
            floats[2] = value3;
            floats[3] = value4;
        }
    }

    public void setMatrix4f(float[] matrix) {
        if (!Arrays.equals(floats, matrix)) {
            dirty = true;
            System.arraycopy(matrix, 0, floats, 0, matrix.length);
        }
    }

    public void set1i(int value) {
        if (ints[0] != value) {
            dirty = true;
            ints[0] = value;
        }
    }

    public void set2fv(@NotNull Vector2f vector) {
        set2f(vector.x(), vector.y());
    }

    public void set3fv(@NotNull Vector3f vector) {
        set3f(vector.x(), vector.y(), vector.z());
    }

    public void set4fv(@NotNull Vector4f vector) {
        set4f(vector.x(), vector.y(), vector.z(), vector.w());
    }

    public void setMatrix4f(@NotNull Matrix4f matrix) {
        setMatrix4f(matrix.get(new float[16]));
    }

    protected abstract void _upload();

    public final void upload() {
        if (!dirty) {
            return;
        }

        this._upload();

        dirty = false;
    }

    static {
        DUMMY = new AbstractUniform(UniformType.DUMMY) {

            @Override
            public void set1f(float value) {

            }

            @Override
            public void set2f(float value1, float value2) {

            }

            @Override
            public void set3f(float value1, float value2, float value3) {

            }

            @Override
            public void set4f(float value1, float value2, float value3, float value4) {

            }

            @Override
            public void setMatrix4f(float[] matrix) {

            }

            @Override
            public void set1i(int value) {

            }

            @Override
            protected void _upload() {

            }
        };
    }
}
