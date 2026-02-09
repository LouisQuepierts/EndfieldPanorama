package net.quepierts.endfieldpanorama.neoforge.shader;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class TitleCombineShaderInstance extends ShaderInstance {

    public final AbstractUniform uTime;

    public final AbstractUniform uInverseViewMatrix;
    public final AbstractUniform uInverseProjectionMatrix;

    public TitleCombineShaderInstance(
            @NotNull ResourceProvider provider,
            @NotNull ResourceLocation location,
            @NotNull VertexFormat format
    ) throws IOException {
        super(provider, location, format);

        uTime = this.safeGetUniform("uTime");
        uInverseViewMatrix = this.safeGetUniform("uInverseViewMatrix");
        uInverseProjectionMatrix = this.safeGetUniform("uInverseProjectionMatrix");
    }
}
