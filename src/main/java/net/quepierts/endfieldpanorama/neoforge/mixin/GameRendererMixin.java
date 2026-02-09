package net.quepierts.endfieldpanorama.neoforge.mixin;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.quepierts.endfieldpanorama.neoforge.reference.Animations;
import net.quepierts.endfieldpanorama.neoforge.reference.Shaders;
import net.quepierts.endfieldpanorama.neoforge.render.EndfieldPanoramaRenderer;
import net.quepierts.endfieldpanorama.neoforge.resource.JarResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(
            method = "preloadUiShader",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;preloadShader(Lnet/minecraft/server/packs/resources/ResourceProvider;Ljava/lang/String;Lcom/mojang/blaze3d/vertex/VertexFormat;)Lnet/minecraft/client/renderer/ShaderInstance;",
                    ordinal = 0
            )
    )
    public void endfieldpanorama$preloadShader(
            ResourceProvider resourceProvider,
            CallbackInfo ci
    ) {
        Shaders.preload(JarResourceProvider.INSTANCE);
        Animations.preload(JarResourceProvider.INSTANCE);
        EndfieldPanoramaRenderer.setup();
    }

}
