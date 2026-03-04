package net.quepierts.endfieldpanorama.neoforge;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.quepierts.endfieldpanorama.earlywindow.ResourceManager;
import net.quepierts.endfieldpanorama.earlywindow.scene.RenderScene;
import org.jetbrains.annotations.NotNull;

public final class EndfieldPanoramaRenderer {

    private static EndfieldPanoramaRenderer instance;

    private final RenderTarget      mainTarget;

    private final RenderScene       scene;
    private final ResourceManager   manager;

    private float cachedPartialTick;

    private EndfieldPanoramaRenderer(
            @NotNull RenderScene        scene,
            @NotNull ResourceManager    manager
    ) {
        var minecraft           = Minecraft.getInstance();
        this.mainTarget         = minecraft.getMainRenderTarget();
        this.scene              = scene;
        this.manager            = manager;

        scene                   .trigger();
    }

    public void update(float partialTick) {
        this.cachedPartialTick = partialTick * 0.05f;
    }

    public void renderScene() {
        var delta = this.cachedPartialTick;

        this.mainTarget.bindWrite(false);
        this.scene.render(delta, () -> this.mainTarget.bindWrite(false));

        VertexBuffer.unbind();
    }

    public void resize(int width, int height) {
        if (this.scene != null) {
            this.scene.resize(width, height);
        }
    }

    public void destroy() {
        this.manager.free();
    }

    public static void setup(RenderScene scene, ResourceManager manager) {
        if (instance != null) {
            return;
        }

        instance = new EndfieldPanoramaRenderer(scene, manager);
    }

    public static void resize() {
        if (instance == null) {
            return;
        }

        var minecraft   = Minecraft.getInstance();
        var window      = minecraft.getWindow();
        var width       = window.getWidth();
        var height      = window.getHeight();

        instance.resize(width, height);
    }

    @NotNull
    public static EndfieldPanoramaRenderer getInstance() {
        return instance;
    }

    public static boolean setuped() {
        return instance != null;
    }

}
