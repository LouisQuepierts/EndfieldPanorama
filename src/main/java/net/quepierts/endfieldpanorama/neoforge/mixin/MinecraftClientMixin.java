package net.quepierts.endfieldpanorama.neoforge.mixin;

import net.minecraft.client.Minecraft;
import net.quepierts.endfieldpanorama.neoforge.EndfieldPanoramaRenderer;
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
        EndfieldPanoramaRenderer.resize();
    }

    @Inject(
            method = "close",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;close()V"
            )
    )
    public void endfieldpanorama$destroy(CallbackInfo ci) {
        EndfieldPanoramaRenderer.getInstance().destroy();
    }

}
