package net.quepierts.endfieldpanorama.earlywindow.render;

import lombok.experimental.UtilityClass;
import net.quepierts.endfieldpanorama.earlywindow.render.pipeline.*;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.RenderableShader;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.Shaders;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class RenderHelper {

    private static final Mesh           BLIT_MESH;
    private static final VertexBuffer   BLIT_VBO;

    public static void init() {
        BLIT_VBO.upload(BLIT_MESH);
    }

    public static void blit(@NotNull RenderableShader shader) {
        shader.use();
        BLIT_VBO.draw();
        shader.unuse();
    }

    public static void free() {
        BLIT_VBO.free();
    }


    static {

        BLIT_MESH       = Mesh.builder(DefaultVertexFormats.BLIT_SCREEN, 6)
                            .quad(
                                    new float[] {0.0f, 0.0f, 0.0f},
                                    new float[] {1.0f, 0.0f, 0.0f},
                                    new float[] {1.0f, 1.0f, 0.0f},
                                    new float[] {0.0f, 1.0f, 0.0f}
                            )
                            .build();
        BLIT_VBO        = new VertexBuffer();
    }


}
