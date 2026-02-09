package net.quepierts.endfieldpanorama.neoforge.reference;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.logging.LogUtils;
import lombok.experimental.UtilityClass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.quepierts.endfieldpanorama.neoforge.EndfieldPanorama;
import net.quepierts.endfieldpanorama.neoforge.shader.ShaderHolder;
import net.quepierts.endfieldpanorama.neoforge.shader.TitleCombineShaderInstance;
import org.slf4j.Logger;

import java.io.IOException;

@UtilityClass
@EventBusSubscriber(value = Dist.CLIENT, modid = EndfieldPanorama.MODID)
public class Shaders {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final ShaderHolder<TitleCombineShaderInstance> FANCY_BACKGROUND;

    @SubscribeEvent
    public static void onRegisterShader(final RegisterShadersEvent event) throws IOException {
        FANCY_BACKGROUND.register(event);
    }

    public static void preload(ResourceProvider resourceProvider) {
        try {
            FANCY_BACKGROUND.preload(resourceProvider);
        } catch (IOException e) {
            LOGGER.error("Failed to preload fancy background shader", e);
        }
    }

    private static ResourceLocation _location(String name) {
        return ResourceLocation.fromNamespaceAndPath(EndfieldPanorama.MODID, name);
    }

    static {

        FANCY_BACKGROUND = ShaderHolder.of(
                _location("fancy_background"),
                DefaultVertexFormat.BLIT_SCREEN,
                TitleCombineShaderInstance::new
        );
    }
}
