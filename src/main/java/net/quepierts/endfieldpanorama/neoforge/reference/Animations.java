package net.quepierts.endfieldpanorama.neoforge.reference;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.quepierts.endfieldpanorama.neoforge.EndfieldPanorama;
import net.quepierts.endfieldpanorama.neoforge.animation.AnimationHolder;
import org.jetbrains.annotations.NotNull;

public final class Animations {

    public static final AnimationHolder DEFAULT;

    public static void preload(@NotNull ResourceProvider provider) {
        DEFAULT.load(provider);
    }

    private static ResourceLocation _location(String path) {
        return ResourceLocation.fromNamespaceAndPath(EndfieldPanorama.MODID, path);
    }

    static {
        DEFAULT = AnimationHolder.of(_location("slim"));
    }

}
