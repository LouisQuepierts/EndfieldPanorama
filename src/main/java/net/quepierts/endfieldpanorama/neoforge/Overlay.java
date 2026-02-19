package net.quepierts.endfieldpanorama.neoforge;

import lombok.Getter;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.quepierts.endfieldpanorama.earlywindow.EndfieldEarlyWindow;
import net.quepierts.endfieldpanorama.earlywindow.ResourceManager;
import net.quepierts.endfieldpanorama.earlywindow.scene.RenderScene;
import net.quepierts.endfieldpanorama.neoforge.render.EndfieldPanoramaRenderer;

import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public final class Overlay extends LoadingOverlay {

    @Getter
    private final EndfieldEarlyWindow window;
    private final ResourceManager manager;
    private RenderScene scene;

    private long fadeOutStart = -1L;
    private boolean triggered = false;

    public Overlay(
            Minecraft minecraft,
            ReloadInstance reload,
            Consumer<Optional<Throwable>> onFinish,
            boolean fadeIn,
            EndfieldEarlyWindow window
    ) {
        super(minecraft, reload, onFinish, fadeIn);
        this.window = window;
        this.manager = new ResourceManager();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (this.scene == null) {
            var scene = window.getScene().duplicate(manager);
            window.close();
            this.scene = scene;
            EndfieldPanoramaRenderer.getInstance().setup(scene, manager);
        }

        this.scene.render(partialTick * 0.05f);

        long millis = Util.getMillis();
        if (this.fadeOutStart == -1L && !this.triggered) {
            this.fadeOutStart = millis;
        }
        float fadeOutTimer = this.fadeOutStart > -1L ? (float) (millis - this.fadeOutStart) / 1000.0F : -1.0F;

        if (!this.triggered && fadeOutTimer > 2.0f) {
            this.triggered = true;
            EndfieldPanoramaRenderer.getInstance().trigger();
        }
    }


}
