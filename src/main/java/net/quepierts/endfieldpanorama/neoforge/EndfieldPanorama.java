package net.quepierts.endfieldpanorama.neoforge;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.loading.NeoForgeLoadingOverlay;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mod(EndfieldPanorama.MODID)
public class EndfieldPanorama {
    public static final String MODID = "endfield_panorama";
    private static final Logger LOGGER = LogUtils.getLogger();

    public EndfieldPanorama(IEventBus modEventBus, ModContainer modContainer) {

    }
}
