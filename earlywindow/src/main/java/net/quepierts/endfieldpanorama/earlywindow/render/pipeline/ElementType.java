package net.quepierts.endfieldpanorama.earlywindow.render.pipeline;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.lwjgl.opengl.GL31;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ElementType {
    FLOAT(4, GL31.GL_FLOAT),
    INT(4, GL31.GL_INT);

    final int size;
    final int glType;
}
