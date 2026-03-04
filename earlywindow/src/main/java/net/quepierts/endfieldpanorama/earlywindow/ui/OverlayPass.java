package net.quepierts.endfieldpanorama.earlywindow.ui;

import net.quepierts.endfieldpanorama.earlywindow.render.procedure.RenderContext;
import net.quepierts.endfieldpanorama.earlywindow.render.procedure.RenderPass;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL31;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class OverlayPass implements RenderPass {

    private final List<UiElement>   elements;

    private boolean                 dirty = true;

    public OverlayPass() {
        this.elements   = new ArrayList<>();
    }

    public OverlayPass add(@NotNull Supplier<UiElement> supplier) {
        this.dirty      = true;
        var element     = supplier.get();
        element         .setDelegateMarkDirty(this::markDirty);
        this.elements   .add(element);
        return this;
    }

    @Override
    public void render(RenderContext context, float delta, float time) {
        
        var graphics    = context.getGraphics();

        var width       = graphics.getUiWidth();
        var height      = graphics.getUiHeight();

        for (var element : this.elements) {
            element     .update(width, height, delta, time);
        }

        GL31            .glEnable(GL31.GL_BLEND);
        GL31            .glBlendFunc(GL31.GL_ONE, GL31.GL_ONE_MINUS_SRC_ALPHA);
        GL31            .glDisable(GL31.GL_DEPTH_TEST);
        GL31            .glDisable(GL31.GL_CULL_FACE);

        if (this.dirty) {

//            graphics   .uiViewPort();
            context         .clearFrameBuffer("overlay");
            context         .bindFrameBuffer("overlay");

//            this.overlay    .clear();
//            this.overlay    .bind();
            for (var element : this.elements) {
                element     .render(graphics, width, height, delta, time);
            }

            this.dirty      = false;

        }


        context         .bindFrameBuffer("main");
        context         .bindTexture("buffer.overlay", 0);
//        this.overlay    .bind(0);

        var shader      = graphics.getBlit();
//        graphics   .uiBlitViewPort();
        graphics   .blit(shader);
//        graphics   .defViewPort();

        context         .unbindTexture("buffer.overlay", 0);
//        this.overlay    .unbind(0);
        GL31            .glDisable(GL31.GL_BLEND);

    }

    @Override
    public void free() {
        for (var element : this.elements) {
            element.free();
        }
    }

    @Override
    public void resize(int width, int height) {
        this        .markDirty();
    }

    public void markDirty() {
        this.dirty  = true;
    }
}
