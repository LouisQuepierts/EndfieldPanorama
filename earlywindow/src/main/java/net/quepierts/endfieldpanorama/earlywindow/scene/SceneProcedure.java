package net.quepierts.endfieldpanorama.earlywindow.scene;

import lombok.Getter;
import lombok.Setter;
import net.quepierts.endfieldpanorama.earlywindow.Resource;
import net.quepierts.endfieldpanorama.earlywindow.render.Graphics;
import net.quepierts.endfieldpanorama.earlywindow.render.pipeline.FrameBuffer;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.ShaderManager;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.Shaders;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.program.BlitShader;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.program.SilhouetteShader;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.ubo.SceneUbo;
import org.jetbrains.annotations.NotNull;

public final class SceneProcedure implements Resource {

    @Getter
    private final FrameBuffer       backgroundTarget;
    @Getter
    private final FrameBuffer       maskTarget;

    private final FrameBuffer       resultTarget;

    private final BlitShader        pattern;
    private final BlitShader        background;
    private final BlitShader        scanLines;

    private final SilhouetteShader  silhouette;

    private final Graphics          graphics;

    @Setter
    private boolean                 renderPattern;

    public SceneProcedure(
            @NotNull ShaderManager manager,
            @NotNull Graphics graphics,
            @NotNull SceneUbo sceneUbo
    ) {

        this.backgroundTarget   = new FrameBuffer(false);
        this.maskTarget         = new FrameBuffer(true);
        this.resultTarget       = new FrameBuffer(false);

        this.pattern            = new BlitShader(manager, Shaders.Fragment.PATTERN);
        this.background         = new BlitShader(manager, Shaders.Fragment.BACKGROUND);
        this.scanLines          = new BlitShader(manager, Shaders.Fragment.SCAN_LINE);
        this.silhouette         = new SilhouetteShader(manager);

        this.graphics           = graphics;

        this.pattern        .bind(sceneUbo);
        this.background     .bind(sceneUbo);
        this.scanLines      .bind(sceneUbo);
        this.silhouette     .bind(sceneUbo);

        this.resultTarget   .clearColor(0.943f, 0.943f, 0.943f, 1.0f);

    }

    public void render(@NotNull Runnable bindMainTarget) {

        this.resultTarget.clear();
        this.resultTarget.bind();

        if (this.renderPattern) {
            this.graphics.blit(this.pattern);
        }

        this.graphics.blit(this.background);

        this.maskTarget.bind(0);
        this.backgroundTarget.bind(1);
        this.graphics.blit(this.silhouette);
        this.maskTarget.unbind(0);
        this.backgroundTarget.unbind(1);

        this.maskTarget.clear();

        bindMainTarget.run();
        this.resultTarget.bind(0);
        this.graphics.blit(this.scanLines);
        this.resultTarget.unbind(0);
    }

    public void resize(int width, int height) {
        this.backgroundTarget   .resize(width, height);
        this.maskTarget         .resize(width, height);
        this.resultTarget       .resize(width, height);
    }

    @Override
    public void free() {
        this.backgroundTarget   .free();
        this.maskTarget         .free();
        this.resultTarget       .free();

        this.pattern            .free();
        this.background         .free();
        this.scanLines          .free();
        this.silhouette         .free();
    }

    public void bindMaskTarget() {
        this.maskTarget.bind();
    }

    public void bindBackgroundTarget() {
        this.backgroundTarget.bind();
    }
}
