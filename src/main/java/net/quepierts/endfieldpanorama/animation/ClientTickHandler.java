package net.quepierts.endfieldpanorama.animation;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.quepierts.endfieldpanorama.EndfieldPanorama;

@UtilityClass
@EventBusSubscriber(value = Dist.CLIENT, modid = EndfieldPanorama.MODID)
public class ClientTickHandler {

    @Getter
    private static int tick;

    private static DeltaTracker timer;

    public static float getRenderTime(float partialTick) {
        return (tick + partialTick) * 0.05f * 0.05f; // divide by 20
    }

    @SubscribeEvent
    public static void onClientTick(final ClientTickEvent.Pre event) {
        tick++;
    }

    public static float getGameTickTime() {
        if (timer == null) {
            timer = Minecraft.getInstance().getTimer();
        }
        return timer.getRealtimeDeltaTicks();
    }
}
