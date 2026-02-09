package net.quepierts.endfieldpanorama.neoforge.render;

import com.mojang.blaze3d.vertex.*;
import lombok.experimental.UtilityClass;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import net.quepierts.endfieldpanorama.neoforge.EndfieldPanorama;

@UtilityClass
@EventBusSubscriber(value = Dist.CLIENT, modid = EndfieldPanorama.MODID)
public class RenderHelper {

    private static VertexBuffer quad;
    private static boolean initialized = false;

    public static void blit() {
        if (!initialized) {
            quad = new VertexBuffer(VertexBuffer.Usage.STATIC);

            BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            bufferbuilder.addVertex(0.0F, 0.0F, 0.0F);
            bufferbuilder.addVertex(1.0F, 0.0F, 0.0F);
            bufferbuilder.addVertex(1.0F, 1.0F, 0.0F);
            bufferbuilder.addVertex(0.0F, 1.0F, 0.0F);

            quad.bind();
            quad.upload(bufferbuilder.buildOrThrow());
            initialized = true;
            VertexBuffer.unbind();
        }

        quad.bind();
        quad.draw();;
        VertexBuffer.unbind();
    }

    @SubscribeEvent
    public static void onShutdown(final GameShuttingDownEvent event) {
        quad.close();
    }
}
