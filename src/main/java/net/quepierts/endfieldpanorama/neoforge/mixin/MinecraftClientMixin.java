package net.quepierts.endfieldpanorama.neoforge.mixin;

import net.minecraft.client.Minecraft;
import net.quepierts.endfieldpanorama.neoforge.render.EndfieldPanoramaRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @Inject(
            method = "resizeDisplay",
            at = @At("RETURN")
    )
    public void endfieldpanorama$resizeDisplay(CallbackInfo ci) {
        if (!EndfieldPanoramaRenderer.setuped()) {
            return;
        }

        var renderer        = EndfieldPanoramaRenderer.getInstance();
        var window          = Minecraft.getInstance().getWindow();
        renderer.resize(window.getWidth(), window.getHeight());
    }

    @Inject(
            method = "destroy",
            at = @At("HEAD")
    )
    public void endfieldpanorama$destroy(CallbackInfo ci) {
        EndfieldPanoramaRenderer.getInstance().destroy();
    }

}
