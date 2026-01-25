package net.quepierts.endfieldpanorama.reference;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import lombok.experimental.UtilityClass;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.quepierts.endfieldpanorama.EndfieldPanorama;
import net.quepierts.endfieldpanorama.shader.ShaderHolder;
import net.quepierts.endfieldpanorama.shader.ShaderList;
import net.quepierts.endfieldpanorama.shader.TitleCombineShaderInstance;

import java.io.IOException;

@UtilityClass
@EventBusSubscriber(value = Dist.CLIENT, modid = EndfieldPanorama.MODID)
public class Shaders {

    public static final ShaderHolder<TitleCombineShaderInstance> TITLE_COMBINE;

    private static final ShaderList INSTANCES;

    @SubscribeEvent
    public static void onRegisterShader(final RegisterShadersEvent event) throws IOException {
        INSTANCES.onRegisterShader(event);
    }

    static {
        INSTANCES = new ShaderList(EndfieldPanorama.MODID);

        TITLE_COMBINE = INSTANCES.register(
                "title_combine",
                DefaultVertexFormat.BLIT_SCREEN,
                TitleCombineShaderInstance::new
        );
    }
}
