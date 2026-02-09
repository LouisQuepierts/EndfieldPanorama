package net.quepierts.endfieldpanorama.neoforge.mixin;

import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    /*@Inject(
            method = "<init>(ZLnet/minecraft/client/gui/components/LogoRenderer;)V",
            at = @At("RETURN")
    )
    public void endfieldpanorama$setup(CallbackInfo ci) {
        EndfieldPanoramaRenderer.setup();
    }*/

}
