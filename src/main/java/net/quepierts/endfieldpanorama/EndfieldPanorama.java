package net.quepierts.endfieldpanorama;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(EndfieldPanorama.MODID)
public class EndfieldPanorama {
    public static final String MODID = "endfield_panorama";
    private static final Logger LOGGER = LogUtils.getLogger();

    public EndfieldPanorama(IEventBus modEventBus, ModContainer modContainer) {

    }
}
