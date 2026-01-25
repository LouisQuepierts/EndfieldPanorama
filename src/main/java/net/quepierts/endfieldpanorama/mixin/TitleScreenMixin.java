package net.quepierts.endfieldpanorama.mixin;

import net.minecraft.client.gui.screens.TitleScreen;
import net.quepierts.endfieldpanorama.render.EndfieldPanoramaRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Inject(
            method = "<init>(ZLnet/minecraft/client/gui/components/LogoRenderer;)V",
            at = @At("RETURN")
    )
    public void els$setup(CallbackInfo ci) {
        EndfieldPanoramaRenderer.setup();
    }

    /*@Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/TitleScreen;renderPanorama(Lnet/minecraft/client/gui/GuiGraphics;F)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    public void els$cancel(CallbackInfo ci) {
        ci.cancel();
    }*/

}
