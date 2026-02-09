package net.quepierts.endfieldpanorama.neoforge.shader;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class EnhancedShaderInstance extends ShaderInstance {

    private final AbstractUniform uInvResolution;

    public EnhancedShaderInstance(
            @NotNull ResourceProvider provider,
            @NotNull ResourceLocation location,
            @NotNull VertexFormat format
    ) throws IOException {
        super(provider, location, format);

        uInvResolution = this.safeGetUniform("uInvResolution");
    }

    public void setResolution(float width, float height) {
        uInvResolution.set(1.0f / width, 1.0f / height);
    }
}
