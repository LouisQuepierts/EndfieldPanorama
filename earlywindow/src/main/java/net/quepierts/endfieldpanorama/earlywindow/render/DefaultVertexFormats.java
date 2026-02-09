package net.quepierts.endfieldpanorama.earlywindow.render;

import lombok.experimental.UtilityClass;
import net.quepierts.endfieldpanorama.earlywindow.render.pipeline.ElementType;
import net.quepierts.endfieldpanorama.earlywindow.render.pipeline.VertexFormat;

@UtilityClass
public class DefaultVertexFormats {

    public static final VertexFormat BLIT_SCREEN;
    public static final VertexFormat POSITION_TEXTURE;


    static {
        BLIT_SCREEN = new VertexFormat.Builder()
                .addElement(3, ElementType.FLOAT)
                .build();

        POSITION_TEXTURE = new VertexFormat.Builder()
                .addElement(3, ElementType.FLOAT)
                .addElement(2, ElementType.FLOAT)
                .build();
    }

}
