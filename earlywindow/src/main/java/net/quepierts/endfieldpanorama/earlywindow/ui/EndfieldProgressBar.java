package net.quepierts.endfieldpanorama.earlywindow.ui;

import net.quepierts.endfieldpanorama.earlywindow.render.DefaultVertexFormats;
import net.quepierts.endfieldpanorama.earlywindow.render.Font;
import net.quepierts.endfieldpanorama.earlywindow.render.Graphics;
import net.quepierts.endfieldpanorama.earlywindow.render.pipeline.Mesh;
import net.quepierts.endfieldpanorama.earlywindow.render.pipeline.VertexBuffer;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL31;

public final class EndfieldProgressBar extends UiElement {

    private final VertexBuffer  text;

    private String              message;
    private boolean             rebuild;

    private float               progress;

    public EndfieldProgressBar() {
        this.text           = new VertexBuffer();

        this.horizontal     = Layout.MIN;
        this.vertical       = Layout.MAX;

        this.width          = 22 * 48 + 28;
        this.height         = 120;
    }

    public void update(String message, float progress) {
        var dirty   = false;

        if (!message.equals(this.message)) {
            this.message    = message;
            this.rebuild    = true;
            this.progress   = progress;

            dirty           = true;
        }

        if (progress > this.progress) {
            this.progress   = progress;
            dirty           = true;
        }

        if (dirty) {
            this.markDirty();
        }
    }

    @Override
    protected void update(int pScreenWidth, int pScreenHeight, float delta, float time) {
        if (this.progress   < 0.943f) {
            var progress    = this.progress;
            var speed       = progress < 0.5f ? Math.max(0.05f, progress) : 1.0f - progress;
            this.progress   = Math.min(this.progress + delta * speed * 0.5f, 0.943f);
            this            .markDirty();
        }
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

        final var font              = graphics.getFont();

        final var meshes            = graphics.meshes;
        final var textures          = graphics.textures;

        final var mx                = pScreenWidth / 2;
        final var my                = y0 + this.height / 2 - 4;

        var sprite                  = textures.create("ui/sprite_progress_bar.png", GL31.GL_NEAREST, GL31.GL_CLAMP);

        var left                    = meshes.create("endfield_progress_bar::left",      () -> EndfieldProgressBar.edge(0));
        var right                   = meshes.create("endfield_progress_bar::right",     () -> EndfieldProgressBar.edge(28));
        var segment                 = meshes.create("endfield_progress_bar::segment",   EndfieldProgressBar::segment);

        var element                 = graphics.getElement();

        element.uRenderType         .set1i(Graphics.TYPE_TEXTURE);
        element.uColor              .set4f(0.2f, 0.2f, 0.2f, 0.5f);
        element.uDiffuseSampler     .set1i(0);
        sprite                      .bind(0);

        final var sw                = 22;
        final var segments          = 48;
        final var bar               = sw * segments;
        final var half              = bar / 2;

        var lx                      = mx - half;
        var rx                      = mx + half;

        GL31                        .glEnable(GL31.GL_BLEND);
        graphics                    .element(left, lx - 4, my, 1, 1);

        for (int i = 0; i < segments; i++) {
            var sx                  = lx + i * sw;
            graphics                .element(segment, sx, my, 1, 1);
        }

        graphics                    .element(right, rx + 10, my, 1, 1);

        if (!Float.isNaN(this.progress) && this.progress > 0.0f) {
            var progress            = this.progress * bar;
            graphics                .quad(
                                        lx + 4, my + 5,
                                        progress + 4, 4,
                                        0.943f, 0.943f, 0.943f, 1.0f,
                                        Graphics.TYPE_REGULAR
                                    );
        }

        if (this.rebuild) {
            this.rebuild(font);
        }

        if (this.message != null) {
            graphics.drawText(text, mx, my - font.getLineSpacing());
        }

    }

    private void rebuild(@NotNull Font font) {

        this.rebuild                = false;

        if (this.message == null) {
            return;
        }

        var textWidth               = font.width(this.message);
        var last                    = this.text.getLast();
        var mesh                    = font.bake(-textWidth / 2, 0, new Font.Text(this.message, 0xFFFFFFFF));

        this.text                   .upload(mesh);
        if (last != null) {
            last.free();
        }

    }

    private static VertexBuffer edge(int shift) {
        final var scale = 1.0f / 32.0f;

        var u0      = scale * shift;
        var u1      = scale * (shift + 4);

        var mesh    = Mesh.builder(DefaultVertexFormats.SCREEN_ELEMENT, 32)
                .floats(0, 0,   u0, 0).intValue(0xFFFFFFFF).countVertex()
                .floats(0, 16,  u0, 1).intValue(0xFFFFFFFF).countVertex()
                .floats(4, 16,  u1, 1).intValue(0xFFFFFFFF).countVertex()
                .floats(4, 0,   u1, 0).intValue(0xFFFFFFFF).countVertex()
                .fastQuad(0)
                .build();

        var vbo     = new VertexBuffer();
        vbo         .upload(mesh);
        return vbo;
    }

    private static VertexBuffer segment() {
        final var scale = 1.0f / 32.0f;

        var u0      = scale * 5;
        var u1      = scale * 27;

        var mesh    = Mesh.builder(DefaultVertexFormats.SCREEN_ELEMENT, 32)
                .floats(5, 0,   u0, 0).intValue(0xFFFFFFFF).countVertex()
                .floats(5, 16,  u0, 1).intValue(0xFFFFFFFF).countVertex()
                .floats(27, 16, u1, 1).intValue(0xFFFFFFFF).countVertex()
                .floats(27, 0,  u1, 0).intValue(0xFFFFFFFF).countVertex()
                .fastQuad(0)
                .build();

        var vbo     = new VertexBuffer();
        vbo         .upload(mesh);
        return vbo;
    }

    @Override
    public void free() {
        this.text       .free();
    }
}
