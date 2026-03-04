package net.quepierts.endfieldpanorama.earlywindow.render.procedure;

import net.quepierts.endfieldpanorama.earlywindow.render.Graphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface RenderContext {

    void bindFrameBuffer(String name);

    void clearFrameBuffer(String name);

    void blitFrameBuffer(String name, int width, int height);

    void bindTexture(String name, int glTextureSlot);

    void unbindTexture(String name, int glTextureSlot);

    void signal(String signal, Object value, boolean enable);

    boolean hasSignal(String name);

    @Nullable Object getSignal(String name);

    @NotNull Graphics getGraphics();

}
