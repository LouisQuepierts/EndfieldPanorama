package net.quepierts.endfieldpanorama.earlywindow.render;

import net.quepierts.endfieldpanorama.earlywindow.Resource;
import net.quepierts.endfieldpanorama.earlywindow.render.pipeline.Mesh;
import net.quepierts.endfieldpanorama.earlywindow.render.pipeline.VertexBuffer;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.ShaderProgram;
import org.jetbrains.annotations.NotNull;

public final class Graphics implements Resource {

    private final VertexBuffer quadVbo;

    public Graphics() {
        this.quadVbo = new VertexBuffer();

        var mesh = Mesh.builder(DefaultVertexFormats.BLIT_SCREEN, 6)
                .quad(
                        new float[] {0.0f, 0.0f, 0.0f},
                        new float[] {1.0f, 0.0f, 0.0f},
                        new float[] {1.0f, 1.0f, 0.0f},
                        new float[] {0.0f, 1.0f, 0.0f}
                )
                .build();
        this.quadVbo.upload(mesh);
    }

    public void blit(@NotNull ShaderProgram program) {
        program.bind();
        program.upload();
        this.quadVbo.draw();
        program.unbind();
    }

    @Override
    public void free() {
        this.quadVbo.getLast().free();
        this.quadVbo.free();
    }

}
