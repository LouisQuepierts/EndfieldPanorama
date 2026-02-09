package net.quepierts.endfieldpanorama.earlywindow.render.shader;

import lombok.RequiredArgsConstructor;
import org.lwjgl.opengl.GL31;

@RequiredArgsConstructor
public enum UniformType {
    FLOAT(1, 4, 4, GL31.GL_FLOAT),
    VEC2(2, 8, 8, GL31.GL_FLOAT),
    VEC3(3, 12, 16, GL31.GL_FLOAT),
    VEC4(4, 16, 16, GL31.GL_FLOAT),
    MAT4(16, 64, 64, GL31.GL_FLOAT),
    SAMPLER(1, 4, 4, GL31.GL_INT),
    DUMMY(0, 0, 0, -1);

    public final int length;
    public final int size;
    public final int std140;
    public final int datatype;
}
