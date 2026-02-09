package net.quepierts.endfieldpanorama.neoforge.render;

import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.resources.ResourceLocation;

@UtilityClass
public final class LoadingOverlayRenderer {

    private static final CubeMap CUBE_MAP = new CubeMap(ResourceLocation.withDefaultNamespace("textures/gui/title/background/panorama"));
    private static final PanoramaRenderer PANORAMA = new PanoramaRenderer(CUBE_MAP);

    public static void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        var renderer = EndfieldPanoramaRenderer.getInstance();

        renderer.preparePanorama();
        PANORAMA.render(guiGraphics, mouseX, mouseY, 1.0F, partialTick);
        renderer.renderScene();

    }

}
