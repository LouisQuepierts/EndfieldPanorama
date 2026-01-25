package net.quepierts.els.animation;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.quepierts.els.EndfieldLoginScreenMod;

@UtilityClass
@EventBusSubscriber(value = Dist.CLIENT, modid = EndfieldLoginScreenMod.MODID)
public class ClientTickHandler {

    @Getter
    private int tick;

    public float getRenderTime(float partialTick) {
        return (tick + partialTick) * 0.05f; // divide by 20
    }

    @SubscribeEvent
    public static void onClientTick(final ClientTickEvent.Pre event) {
        tick++;
    }

}
