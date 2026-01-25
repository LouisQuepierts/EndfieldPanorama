package net.quepierts.endfieldpanorama.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.quepierts.endfieldpanorama.render.EndfieldPanoramaRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PanoramaRenderer.class)
public class PanoramaRendererMixin {

    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    public void els$preparePanorama(GuiGraphics guiGraphics, int width, int height, float fade, float partialTick, CallbackInfo ci) {
        var renderer = EndfieldPanoramaRenderer.getInstance();
        renderer.preparePanorama();
    }

    @Inject(
            method = "render",
            at = @At("RETURN")
    )
    public void els$renderScene(GuiGraphics guiGraphics, int width, int height, float fade, float partialTick, CallbackInfo ci) {
        var renderer = EndfieldPanoramaRenderer.getInstance();
        renderer.renderScene(guiGraphics, partialTick);
    }

}
