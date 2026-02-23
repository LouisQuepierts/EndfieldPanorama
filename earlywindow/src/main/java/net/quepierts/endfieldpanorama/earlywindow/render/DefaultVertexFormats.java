package net.quepierts.endfieldpanorama.earlywindow.render;

import lombok.experimental.UtilityClass;
import net.quepierts.endfieldpanorama.earlywindow.render.pipeline.ElementType;
import net.quepierts.endfieldpanorama.earlywindow.render.pipeline.VertexFormat;

@UtilityClass
public class DefaultVertexFormats {

    public static final VertexFormat BLIT_SCREEN;
    public static final VertexFormat PANORAMA;
    public static final VertexFormat POSITION_TEXTURE;

    public static final VertexFormat CHARACTER;


    static {
        BLIT_SCREEN = new VertexFormat.Builder()
                .addElement("Position", ElementType.FLOAT, 3)   // Position
                .build();

        PANORAMA = BLIT_SCREEN;

        POSITION_TEXTURE = new VertexFormat.Builder()
                .addElement("Position", ElementType.FLOAT, 3)   // Position
                .addElement("UV", ElementType.FLOAT, 2)         // UV
                .build();

        CHARACTER = new VertexFormat.Builder()
                .addElement("Position", ElementType.FLOAT, 3)   // Position
                .addElement("UV", ElementType.FLOAT, 2)         // UV
                .addElement("Group", ElementType.INT, 1)     // Group
                .build();
    }

}
