package net.quepierts.endfieldpanorama.earlywindow.render;

import org.lwjgl.opengl.GL31;

public abstract class BaseTexture {

    protected abstract void bind();

    protected abstract void unbind();

    public void bind(int slot) {
        GL31.glActiveTexture(GL31.GL_TEXTURE0 + slot);
        bind();
        GL31.glActiveTexture(GL31.GL_TEXTURE0);
    }

    public void unbind(int slot) {
        GL31.glActiveTexture(GL31.GL_TEXTURE0 + slot);
        unbind();
        GL31.glActiveTexture(GL31.GL_TEXTURE0);
    }

}
