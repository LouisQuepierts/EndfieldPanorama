package net.quepierts.endfieldpanorama.earlywindow.scene.passes;

import net.quepierts.endfieldpanorama.earlywindow.render.procedure.RenderContext;
import net.quepierts.endfieldpanorama.earlywindow.render.procedure.RenderPass;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.ShaderManager;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.Shaders;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.program.BlitShader;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.program.SilhouetteShader;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.ubo.SceneUbo;
import org.jetbrains.annotations.NotNull;

public final class CombinePass implements RenderPass {

    public static final String      SIGNAL_CHLADNI  = "chladni";

    private final BlitShader        pattern;
    private final BlitShader        background;
    private final BlitShader        scanLines;

    private final SilhouetteShader  silhouette;

    public CombinePass(
            @NotNull ShaderManager manager,
            @NotNull SceneUbo sceneUbo
    ) {

        this.pattern            = new BlitShader(manager, Shaders.Fragment.PATTERN);
        this.background         = new BlitShader(manager, Shaders.Fragment.BACKGROUND);
        this.scanLines          = new BlitShader(manager, Shaders.Fragment.SCAN_LINE);
        this.silhouette         = new SilhouetteShader(manager);

        this.pattern        .bind(sceneUbo);
        this.background     .bind(sceneUbo);
        this.scanLines      .bind(sceneUbo);
        this.silhouette     .bind(sceneUbo);

    }

    @Override
    public void render(RenderContext context, float delta, float time) {
        
        var graphics    = context.getGraphics();

        context         .clearFrameBuffer("scene");
        context         .bindFrameBuffer("scene");

        if (context     .hasSignal(SIGNAL_CHLADNI)) {
            graphics    .blit(this.pattern);
        }

        graphics        .blit(this.background);

        context         .bindTexture("buffer.mask", 0);
        context         .bindTexture("buffer.background", 1);
        graphics        .blit(this.silhouette);
        context         .unbindTexture("buffer.mask", 0);
        context         .unbindTexture("buffer.background", 1);

        context         .clearFrameBuffer("mask");

        context         .bindFrameBuffer("main");
        context         .bindTexture("buffer.scene", 0);
        graphics        .blit(this.scanLines);
        context         .unbindTexture("buffer.scene", 0);

    }

    @Override
    public void free() {
        this.pattern            .free();
        this.background         .free();
        this.scanLines          .free();
        this.silhouette         .free();
    }
}
