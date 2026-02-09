package net.quepierts.endfieldpanorama.earlywindow.render.shader;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.lwjgl.opengl.GL31;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ShaderType {
    VERTEX(GL31.GL_VERTEX_SHADER, ".vsh"),
    FRAGMENT(GL31.GL_FRAGMENT_SHADER, ".fsh");

    public final int glType;
    public final String subfix;
}
