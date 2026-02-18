package net.quepierts.endfieldpanorama.neoforge.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import net.quepierts.endfieldpanorama.neoforge.render.EndfieldPanoramaRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    /*@Inject(
            method = "<init>(ZLnet/minecraft/client/gui/components/LogoRenderer;)V",
            at = @At("RETURN")
    )
    public void endfieldpanorama$setup(CallbackInfo ci) {
        EndfieldPanoramaRenderer.setup();
    }*/

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var renderer = EndfieldPanoramaRenderer.getInstance();
        renderer.update(partialTick);
        renderer.renderScene();
    }


}
