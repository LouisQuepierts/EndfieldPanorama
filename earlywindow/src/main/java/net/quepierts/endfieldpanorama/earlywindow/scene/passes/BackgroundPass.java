package net.quepierts.endfieldpanorama.earlywindow.scene.passes;

import net.quepierts.endfieldpanorama.earlywindow.render.PanoramaRenderer;
import net.quepierts.endfieldpanorama.earlywindow.render.procedure.RenderContext;
import net.quepierts.endfieldpanorama.earlywindow.render.procedure.RenderPass;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.ShaderManager;
import net.quepierts.endfieldpanorama.earlywindow.scene.RenderScene;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public final class BackgroundPass implements RenderPass {

    private final PanoramaRenderer  renderer;
    private final Matrix4f          projection;

    public BackgroundPass(@NotNull ShaderManager shaders) {
        renderer    = new PanoramaRenderer(shaders);
        projection  = new Matrix4f();
    }

    @Override
    public void render(
            RenderContext context,
            float delta,
            float time
    ) {
        context.bindFrameBuffer("background");
        this.renderer.render(this.projection, time);
    }

    @Override
    public void resize(int width, int height) {
        this.projection.setPerspective(
                RenderScene.FOV,
                (float) width / height,
                RenderScene.NEAR,
                RenderScene.FAR
        );
    }

    @Override
    public void free() {
        this.renderer.free();
    }
}
