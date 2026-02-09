package net.quepierts.endfieldpanorama.neoforge.shader;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class ShaderList {
    private final String modid;
    private final List<ShaderHolder<?>> holders;

    public ShaderList(String modid) {
        this.modid = modid;
        this.holders = new ArrayList<>();
    }

    public void onRegisterShader(final RegisterShadersEvent event) throws IOException {
        for (ShaderHolder<?> holder : holders) {
            holder.register(event);
        }
    }

    public ShaderHolder<ShaderInstance> register(
            @NotNull String name,
            @NotNull VertexFormat format
    ) {
        ShaderHolder<ShaderInstance> instance = ShaderHolder.of(ResourceLocation.fromNamespaceAndPath(modid, name), format);
        this.holders.add(instance);
        return instance;
    }

    public <T extends ShaderInstance> ShaderHolder<T> register(
            @NotNull String name,
            @NotNull VertexFormat format,
            @NotNull ShaderHolder.ShaderConstructor<T> constructor
    ) {
        ShaderHolder<T> instance = ShaderHolder.of(ResourceLocation.fromNamespaceAndPath(modid, name), format, constructor);
        this.holders.add(instance);
        return instance;
    }
}
