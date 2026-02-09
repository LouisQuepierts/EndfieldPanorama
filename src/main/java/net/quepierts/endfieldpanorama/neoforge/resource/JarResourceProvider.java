package net.quepierts.endfieldpanorama.neoforge.resource;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.neoforged.neoforge.resource.EmptyPackResources;
import net.quepierts.endfieldpanorama.neoforge.EndfieldPanorama;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Optional;

public final class JarResourceProvider implements ResourceProvider {

    public static final ResourceProvider INSTANCE = new JarResourceProvider();

    private JarResourceProvider() {}

    private final PackResources dummyPackResources = new EmptyPackResources(
            new PackLocationInfo(EndfieldPanorama.MODID, Component.empty(), PackSource.BUILT_IN, Optional.empty()),
            new PackMetadataSection(Component.empty(), 943)
    );

    @Override
    public @NotNull Optional<Resource> getResource(@NotNull ResourceLocation location) {
        var path        = path(location);

        if (JarResourceLoader.hasResource(path)) {
            IoSupplier<InputStream> supplier = () -> JarResourceLoader.loadResourceOrThrow(path);

            var resource = new Resource(dummyPackResources, supplier);
            return Optional.of(resource);
        }


        return Optional.empty();
    }

    private static String path(@NotNull ResourceLocation location) {
        return "assets/" + location.getNamespace() + "/" + location.getPath();
    }
}
