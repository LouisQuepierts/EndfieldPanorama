package net.quepierts.endfieldpanorama.neoforge;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.Mth;
import net.quepierts.endfieldpanorama.earlywindow.EndfieldEarlyWindow;
import net.quepierts.endfieldpanorama.earlywindow.service.LoadingProgressService;
import net.quepierts.endfieldpanorama.earlywindow.ResourceManager;
import net.quepierts.endfieldpanorama.earlywindow.render.pipeline.VertexBuffer;
import net.quepierts.endfieldpanorama.earlywindow.scene.RenderScene;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public final class Overlay extends LoadingOverlay implements LoadingProgressService {

    private static final String[]   MESSAGES = {
            "// Loading Resources...",
            "// Compiling Shaders..."
    };

    private final Minecraft         minecraft;
    private final ReloadInstance    reload;
    private final Consumer<Optional<Throwable>> onFinish;
    private final ResourceManager   manager;
    private RenderScene             scene;
    private EndfieldEarlyWindow     window;

    private long                    fadeOutStart    = -1L;
    private boolean                 triggered       = false;

    private float                   progress        = 0.0f;
    private String                  message         = "// Compiling Shaders...";

    public Overlay(
            Minecraft minecraft,
            ReloadInstance reload,
            Consumer<Optional<Throwable>> onFinish,
            boolean fadeIn,
            EndfieldEarlyWindow window
    ) {
        super(minecraft, reload, onFinish, fadeIn);
        this.minecraft      = minecraft;
        this.onFinish       = onFinish;
        this.reload         = reload;

        this.window         = window;
        this.manager        = new ResourceManager();

        LoadingProgressService.setProvider(this);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
//        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (this.scene == null) {
            var scene       = window.getScene().duplicate(manager);
            window          .close();
            this.window     = null;
            this.scene      = scene;
        }

        GlStateManager._clear(GlConst.GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX);

        VertexBuffer.unbind0();
        com.mojang.blaze3d.vertex.VertexBuffer.unbind();

        long millis         = Util.getMillis();
        float fadeOutTimer  = this.fadeOutStart > -1L ? (float) (millis - this.fadeOutStart) / 1000.0F : -1.0F;

        float progress      = this.reload.getActualProgress();
        this.progress       = Mth.clamp(this.progress * 0.943f + progress * 0.057f, 0.0f, 1.0f);

        if (fadeOutTimer > 2.0f) {
            this.triggered  = true;
            EndfieldPanoramaRenderer.setup(this.scene, this.manager);

            this.minecraft.setOverlay(null);
            return;
        }

        RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
        this.scene          .render(partialTick * 0.05f, () -> target.bindWrite(false));

        if (this.fadeOutStart == -1L && this.reload.isDone()) {
            this.fadeOutStart = Util.getMillis();
            try {
                this.reload.checkExceptions();
                this.onFinish.accept(Optional.empty());
            } catch (Throwable throwable) {
                this.onFinish.accept(Optional.of(throwable));
            }

            if (this.minecraft.screen != null) {
                this.minecraft.screen.init(this.minecraft, this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight());
            }
        }
    }

    @Override
    public void provide(LoadingProgressService.Context context) {
        context         .fit(1);

        var inout       = context.getProgress();
        var first       = inout.getFirst();

        if (first.getDelegate() != this) {
            first       .setDelegate(this);
            first       .setEnable(true);

            for (int i = 1; i < inout.size(); i++) {
                var bar = inout.get(i);
                bar     .setEnable(false);
                bar     .setDelegate(null);
            }
        }

        var idx         = Math.min((int) (this.progress * 2), 1);
        var message     = MESSAGES[idx];

        first           .setProgress(this.progress);
        first           .setMessage(message);
    }

    @Override
    public boolean hasProgress() {
        return !this.triggered;
    }
}
