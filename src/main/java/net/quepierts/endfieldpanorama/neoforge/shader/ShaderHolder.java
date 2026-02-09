package net.quepierts.endfieldpanorama.neoforge.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ShaderHolder<T extends ShaderInstance> {
    public static ShaderHolder<ShaderInstance> of(
            ResourceLocation location,
            VertexFormat format
    ) {
        return of(location, format, ShaderInstance::new);
    }

    public static <T extends ShaderInstance> ShaderHolder<T> of(
            ResourceLocation location,
            VertexFormat format,
            ShaderConstructor<T> constructor
    ) {
        return new ShaderHolder<>(location, format, constructor);
    }

    @Getter
    private final @NotNull ResourceLocation location;
    @Getter
    private final @NotNull VertexFormat format;
    @Getter
    private final @NotNull ShaderConstructor<T> constructor;

    private T instance;

    public void register(final RegisterShadersEvent event) throws IOException {
        final ResourceProvider provider = event.getResourceProvider();
        event.registerShader(
                this.constructor.create(provider, location, format),
                this::setInstance
        );
    }

    public @NotNull T getInstance() {
        return Objects.requireNonNull(instance, "Attempted to call get shader [" + location + "] before shaders have finished loading.");
    }

    public @NotNull T use() {
        RenderSystem.setShader(this::getInstance);
        return this.getInstance();
    }

    @SuppressWarnings("unchecked")
    private void setInstance(@NotNull ShaderInstance instance) {
        this.instance = (T) instance;
    }

    public void preload(ResourceProvider resourceProvider) throws IOException {
        this.instance = this.constructor.create(resourceProvider, location, format);
    }

    @FunctionalInterface
    public interface ShaderConstructor<T extends ShaderInstance> {
        T create(
                @NotNull ResourceProvider provider,
                @NotNull ResourceLocation location,
                @NotNull VertexFormat format
        ) throws IOException;
    }
}
