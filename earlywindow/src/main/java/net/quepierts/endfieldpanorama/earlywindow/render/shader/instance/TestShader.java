package net.quepierts.endfieldpanorama.earlywindow.render.shader.instance;

import net.quepierts.endfieldpanorama.earlywindow.render.shader.RenderableShader;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.ShaderManager;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.Shaders;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.UniformDefinition;
import org.jetbrains.annotations.NotNull;

public final class TestShader extends RenderableShader {
    public TestShader(@NotNull ShaderManager manager) {
        super(
                manager,
                Shaders.Vertex.BLIT,
                "test",
                UniformDefinition.builder().build()
        );
    }
}
