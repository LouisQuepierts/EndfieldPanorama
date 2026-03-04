package net.quepierts.endfieldpanorama.earlywindow.render;

import lombok.Getter;
import net.quepierts.endfieldpanorama.earlywindow.Resource;
import net.quepierts.endfieldpanorama.earlywindow.render.pipeline.Mesh;
import net.quepierts.endfieldpanorama.earlywindow.render.pipeline.VertexBuffer;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.ShaderManager;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.ShaderProgram;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.Shaders;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.program.BlitShader;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.program.ScreenElementShader;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.awt.*;

public final class Graphics implements Resource {

    public static final int             TYPE_REGULAR    = 0;
    public static final int             TYPE_TEXTURE    = 1;
    public static final int             TYPE_CUTOUT     = 2;

    public final TextureManager         textures        = new TextureManager();
    public final Manager<VertexBuffer>  meshes          = new Manager<>();

    private final VertexBuffer          quadVbo;
    private final Matrix4f              uiProjection;

    @Getter
    private final BlitShader            blit;
    @Getter
    private final ScreenElementShader   element;
    @Getter
    private final ScreenElementShader   text;

    @Getter
    private final Font                  font;

    @Getter
    private int                         uiWidth;

    @Getter
    private int                         uiHeight;

    @Getter
    private int                         width;

    @Getter
    private int                         height;

    public Graphics(
            Font font,
            ShaderManager shaders,
            int uiWidth, int uiHeight
    ) {
        this.uiWidth        = uiWidth;
        this.uiHeight       = uiHeight;
        this.quadVbo        = new VertexBuffer();
        this.uiProjection   = new Matrix4f();

        this.blit       = new BlitShader(shaders, Shaders.Fragment.BLIT);
        this.element    = new ScreenElementShader(shaders, Shaders.Fragment.SCREEN);
        this.text       = new ScreenElementShader(shaders, Shaders.Fragment.TEXT);

        this.font       = font;

        var mesh = Mesh.builder(DefaultVertexFormats.BLIT_SCREEN, 6)
                .quad(
                        new float[] {0.0f, 0.0f, 0.0f},
                        new float[] {1.0f, 0.0f, 0.0f},
                        new float[] {1.0f, 1.0f, 0.0f},
                        new float[] {0.0f, 1.0f, 0.0f}
                )
                .build();
        this.quadVbo.upload(mesh);

        this.meshes.create("quad", () -> this.quadVbo);
    }

    public void drawText(@NotNull VertexBuffer text, int x0, int y0) {
        var model   = new Matrix4f()
                    .translate(x0, y0, 0.0f);

        var mvp     = new Matrix4f(this.uiProjection)
                    .mul(model);

        this.text.uProjectionViewMatrix .setMatrix4f(mvp);
        this.text.uDiffuseSampler       .set1i(0);

        this.font                       .bind();
        this.text                       .bind();
        this.text                       .upload();

        text                            .draw();

        this.text                       .unbind();
        this.font                       .unbind();
    }

    public void quad(
            float x, float y, float w, float h,
            float r, float g, float b, float a,
            int renderType
    ) {
        var model   = new Matrix4f()
                    .translate(x, y, 0.0f)
                    .scale(w, h, 1.0f);

        var mvp     = new Matrix4f(this.uiProjection)
                    .mul(model);

        this.element.uProjectionViewMatrix  .setMatrix4f(mvp);
        this.element.uColor                 .set4f(r, g, b, a);
        this.element.uRenderType            .set1i(renderType);

        this.blit(this.element);
    }

    public void element(
            @NotNull VertexBuffer mesh,
            int x, int y, int w, int h
    ) {
        var model   = new Matrix4f()
                .translate(x, y, 0.0f)
                .scale(w, h, 1.0f);

        var mvp     = new Matrix4f(this.uiProjection)
                .mul(model);

        this.element.uProjectionViewMatrix  .setMatrix4f(mvp);
        this.element                        .bind();
        this.element                        .upload();

        mesh                                .draw();

        this.element                        .unbind();
    }

    public void blit(@NotNull ShaderProgram program) {
        program         .bind();
        program         .upload();
        this.quadVbo    .draw();
        program         .unbind();
    }

    public Matrix4f mvp(Matrix4f model) {
        return new Matrix4f(this.uiProjection).mul(model);
    }

    @Override
    public void free() {
        this.font       .free();

        this.blit       .free();
        this.text       .free();
        this.element    .free();

        this.meshes     .free();
        this.textures   .free();
    }

    public void resize(int width, int height) {
        this.width      = width;
        this.height     = height;

        var aspect      = (float) width / height;
        var uiWidth     = 1080f * aspect;
        var uiHeight    = 1080f;

        this.uiWidth    = (int) uiWidth;
        this.uiHeight   = (int) uiHeight;

        this.uiProjection.identity()
                .ortho(
                        0, uiWidth,
                        uiHeight, 0,
                        -1, 1
                );
    }
}
