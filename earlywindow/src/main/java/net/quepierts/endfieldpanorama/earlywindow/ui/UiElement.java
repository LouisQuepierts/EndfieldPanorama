package net.quepierts.endfieldpanorama.earlywindow.ui;

import lombok.Getter;
import lombok.Setter;
import net.quepierts.endfieldpanorama.earlywindow.Resource;
import net.quepierts.endfieldpanorama.earlywindow.render.Graphics;
import org.jetbrains.annotations.NotNull;

@Setter
@Getter
public abstract class UiElement implements Resource {

    protected Layout horizontal = Layout.MIN;
    protected Layout vertical   = Layout.MIN;

    protected int x;
    protected int y;
    protected int width;
    protected int height;

    protected boolean visible = true;

    private @NotNull Runnable delegateMarkDirty  = () -> {};

    public final void render(
            @NotNull Graphics graphics,
            int pScreenWidth,
            int pScreenHeight,
            float delta,
            float time
    ) {

        if (!this.visible) {
            return;
        }

        var x0 = this.horizontal.map(this.x,    this.width,    pScreenWidth);
        var y0 = this.vertical  .map(this.y,    this.height,   pScreenHeight);

        this._render(
                graphics,
                pScreenWidth,
                pScreenHeight,
                x0,
                y0,
                delta,
                time
        );

    }

    public void markDirty() {
        this.delegateMarkDirty.run();
    }

    public void hide() {
        if (!this.visible) {
            return;
        }

        this.visible    = false;
        this            .markDirty();
    }

    public void show() {
        if (this.visible) {
            return;
        }

        this.visible    = true;
        this            .markDirty();
    }

    public UiElement horizontal(@NotNull Layout layout) {
        if (layout      != this.horizontal)
            this        .markDirty();

        this.horizontal = layout;
        return this;
    }

    public UiElement vertical(@NotNull Layout layout) {
        if (layout      != this.vertical)
            this        .markDirty();
        this.vertical   = layout;
        return this;
    }

    public UiElement position(int x, int y) {
        if (    x       != this.x ||
                y       != this.y)
            this        .markDirty();
        this.x          = x;
        this.y          = y;
        return this;
    }

    public UiElement scale(int width, int height) {
        if (    width   != this.width ||
                height  != this.height)
            this        .markDirty();
        this.width      = width;
        this.height     = height;
        return this;
    }

    protected void update(
            int pScreenWidth,
            int pScreenHeight,
            float delta,
            float time
    ) {}

    protected abstract void _render(
            @NotNull Graphics graphics,
            int pScreenWidth,
            int pScreenHeight,
            int x0,
            int y0,
            float delta,
            float time
    );

    public enum Layout {
        MIN,
        MAX,
        CENTER;

        int map(int p, int scale, int screen) {
            return p + switch (this) {
                case MIN -> 0;
                case MAX -> screen - scale;
                case CENTER -> (screen - scale) / 2;
            };
        }
    }

}
