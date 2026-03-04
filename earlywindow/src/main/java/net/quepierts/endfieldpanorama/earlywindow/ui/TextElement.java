package net.quepierts.endfieldpanorama.earlywindow.ui;

import net.quepierts.endfieldpanorama.earlywindow.render.Font;
import net.quepierts.endfieldpanorama.earlywindow.render.Graphics;
import net.quepierts.endfieldpanorama.earlywindow.render.pipeline.Mesh;
import net.quepierts.endfieldpanorama.earlywindow.render.pipeline.VertexBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TextElement extends UiElement {

    private final Font.Text[]   texts;

    private final VertexBuffer  cache;
    private @Nullable Mesh      baked;

    public TextElement(String... texts) {
        this.texts = new Font.Text[texts.length];
        for (int i = 0; i < texts.length; i++) {
            this.texts[i] = new Font.Text(texts[i] + "\n", 0xFFFFFFFF);
        }

        this.cache = new VertexBuffer();
    }

    public TextElement(Font font, String... texts) {
        this(texts);
        this.prepare(font);
    }

    public TextElement(Font.Text... texts) {
        this.texts = texts;
        this.cache = new VertexBuffer();
    }

    public TextElement(Font font, Font.Text texts) {
        this(texts);
        this.prepare(font);
    }

    public void prepare(@NotNull Font font) {
        if (this.baked  != null) {
            return;
        }

        var baked       = font.bake(0, 0, this.texts);
        this.baked      = baked;
        this.cache      .upload(baked);

        var scale       = new int[2];
        font            .scale(scale, this.texts);

        this.width      = scale[0];
        this.height     = scale[1];

        this.markDirty();
    }

    @Override
    protected void _render(
            @NotNull Graphics graphics,
            int pScreenWidth,
            int pScreenHeight,
            int x0,
            int y0,
            float delta,
            float time
    ) {

        if (this.texts == null) {
            return;
        }

        if (this.baked == null) {
            var font    = graphics.getFont();
            this        .prepare(font);
            return;
        }

        graphics.drawText(this.cache, x0, y0);

    }

    @Override
    public void free() {
        this.cache.free();

        if (this.baked != null) {
            this.baked.free();
        }
    }
}
