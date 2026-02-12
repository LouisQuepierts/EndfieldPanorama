package net.quepierts.endfieldpanorama.earlywindow.render;

import lombok.experimental.UtilityClass;
import net.quepierts.endfieldpanorama.earlywindow.render.pipeline.ElementType;
import net.quepierts.endfieldpanorama.earlywindow.render.pipeline.VertexFormat;

@UtilityClass
public class DefaultVertexFormats {

    public static final VertexFormat BLIT_SCREEN;
    public static final VertexFormat POSITION_TEXTURE;

    public static final VertexFormat CHARACTER;


    static {
        BLIT_SCREEN = new VertexFormat.Builder()
                .addElement(3, ElementType.FLOAT)   // Position
                .build();

        POSITION_TEXTURE = new VertexFormat.Builder()
                .addElement(3, ElementType.FLOAT)   // Position
                .addElement(2, ElementType.FLOAT)   // UV
                .build();

        CHARACTER = new VertexFormat.Builder()
                .addElement(3, ElementType.FLOAT)   // Position
                .addElement(2, ElementType.FLOAT)   // UV
                .addElement(1, ElementType.INT)     // Group
                .build();
    }

}
