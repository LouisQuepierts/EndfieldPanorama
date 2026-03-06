package net.quepierts.endfieldpanorama.earlywindow.ui;

import net.quepierts.endfieldpanorama.earlywindow.service.LoadingProgressService;
import net.quepierts.endfieldpanorama.earlywindow.render.Graphics;
import org.jetbrains.annotations.NotNull;

public final class EndfieldLoadingControl extends UiElement {

    private final EndfieldProgressBar[] bars;

    public EndfieldLoadingControl() {
        this.bars           = new EndfieldProgressBar[2];

        for (int i = 0; i < this.bars.length; i++) {
            var bar         = new EndfieldProgressBar();
            this.bars[i]    = bar;

            bar             .position(0, 8 + 32 * i);
            bar             .hide();
            bar             .setDelegateMarkDirty(this::markDirty);
        }

        this.horizontal     = Layout.MIN;
        this.vertical       = Layout.MAX;
    }

    @Override
    protected void update(int pScreenWidth, int pScreenHeight, float delta, float time) {

        var dirty       = false;

        if (this.width  != pScreenWidth) {
            this.width  = pScreenWidth;
            dirty       = true;
        }

        var height      = (int) (pScreenHeight * 0.08f);
        if (this.height != height) {
            this.height = height;
            dirty       = true;
        }

        var progress    = LoadingProgressService.getCurrentProgress();
        if (!progress.isEmpty()) {
            this.show();

            var iterator    = progress.iterator();
            var amount      = Math.min(progress.size(), this.bars.length);
            var i           = 0;

            for (; i < amount; i++) {
                var bar     = this.bars[i];
                var pm      = iterator.next();

                if (pm.isEnable()) {
                    var p   = pm.getProgress();
                    var msg = pm.getMessage();

                    bar     .show();
                    bar     .update(msg, p);
                    bar     .update(pScreenWidth, pScreenHeight, delta, time);
                } else if (i != 0) {
                    bar     .hide();
                }
            }

            for (; i < this.bars.length; i++) {
                this.bars[i].hide();
            }
        } else {
            this            .hide();
        }

        if (dirty) {
            this.markDirty();
        }
    }

    @Override
    protected void _render(@NotNull Graphics graphics, int pScreenWidth, int pScreenHeight, int x0, int y0, float delta, float time) {

        graphics.quad(
                x0, y0, this.width, this.height,
                0.0f, 0.0f, 0.0f, 0.943f,
                Graphics.TYPE_REGULAR
        );

        for (var bar : this.bars) {
            bar.render(graphics, pScreenWidth, pScreenHeight, delta, time);
        }

    }

    @Override
    public void free() {
        for (var bar : this.bars) {
            bar.free();
        }
    }
}
