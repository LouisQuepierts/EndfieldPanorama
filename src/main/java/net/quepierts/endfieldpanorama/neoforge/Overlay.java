package net.quepierts.endfieldpanorama.neoforge;

import lombok.Getter;
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
    }


}
