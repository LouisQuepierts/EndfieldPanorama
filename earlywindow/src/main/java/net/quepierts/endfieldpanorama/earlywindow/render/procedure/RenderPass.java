package net.quepierts.endfieldpanorama.earlywindow.render.procedure;

import net.quepierts.endfieldpanorama.earlywindow.Resource;

public interface RenderPass extends Resource {
    void render(RenderContext context, float delta, float time);

    default void resize(int width, int height) {}

    default void duplicate(RenderPass other) {}
}
