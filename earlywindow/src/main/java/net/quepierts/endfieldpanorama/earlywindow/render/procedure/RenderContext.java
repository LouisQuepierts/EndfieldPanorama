package net.quepierts.endfieldpanorama.earlywindow.render.procedure;

import org.jetbrains.annotations.Nullable;

public interface RenderContext {

    void bindFrameBuffer(String name);

    void clearFrameBuffer(String name);

    void bindTexture(String name, int glTextureSlot);

    void unbindTexture(String name, int glTextureSlot);

    void signal(String signal, Object value, boolean enable);

    boolean hasSignal(String name);

    @Nullable Object getSignal(String name);

}
