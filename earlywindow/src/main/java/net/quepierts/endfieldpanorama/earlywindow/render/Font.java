package net.quepierts.endfieldpanorama.earlywindow.render;

import lombok.Getter;
import net.quepierts.endfieldpanorama.earlywindow.Resource;
import net.quepierts.endfieldpanorama.earlywindow.render.pipeline.Mesh;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.opengl.GL11C.GL_LINEAR;
import static org.lwjgl.opengl.GL11C.GL_RED;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11C.glTexImage2D;
import static org.lwjgl.opengl.GL11C.glTexParameteri;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.stb.STBTruetype.stbtt_GetPackedQuad;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class Font implements Resource {
    private static final int    GLYPH_COUNT = 127 - 32;

    private final ImageTexture  atlas;

    @Getter
    private final int           lineSpacing;
    private final int           descent;

    private final Glyph[]       glyphs;

    public static Font minecraft() {
        try {
            final var stbClazz  = Class.forName("net.neoforged.fml.earlydisplay.STBHelper");
            final var read      = stbClazz.getMethod("readFromClasspath", String.class, int.class);

            read.trySetAccessible();
            final var buffer    = read.invoke(null, "Monocraft.ttf", 200000);

            return new Font((ByteBuffer) buffer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Font(@NotNull ByteBuffer buffer) {
        var info = STBTTFontinfo.create();
        if (!stbtt_InitFont(info, buffer)) {
            throw new IllegalStateException("Bad font");
        }

        var ascent = new float[1];
        var descent = new float[1];
        var lineGap = new float[1];
        int fontSize = 24;
        stbtt_GetScaledFontVMetrics(buffer, 0, fontSize, ascent, descent, lineGap);
        this.lineSpacing = (int) (ascent[0] - descent[0] + lineGap[0]);
        this.descent = (int) Math.floor(descent[0]);

        var texture = new ImageTexture();
        texture.bind(0);

        try (var packedchars = STBTTPackedchar.malloc(GLYPH_COUNT)) {
            int texwidth = 256;
            int texheight = 128;
            try (STBTTPackRange.Buffer packRanges = STBTTPackRange.malloc(1)) {
                var bitmap = BufferUtils.createByteBuffer(texwidth * texheight);
                try (STBTTPackRange packRange = STBTTPackRange.malloc()) {
                    packRanges.put(packRange.set(fontSize, 32, null, GLYPH_COUNT, packedchars, (byte) 1, (byte) 1));
                    packRanges.flip();
                }

                try (STBTTPackContext pc = STBTTPackContext.malloc()) {
                    stbtt_PackBegin(pc, bitmap, texwidth, texheight, 0, 1, NULL);
                    stbtt_PackSetOversampling(pc, 1, 1);
                    stbtt_PackSetSkipMissingCodepoints(pc, true);
                    stbtt_PackFontRanges(pc, buffer, 0, packRanges);
                    stbtt_PackEnd(pc);
                    glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, texwidth, texheight, 0, GL_RED, GL_UNSIGNED_BYTE, bitmap);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
                }
            }

            texture.unbind(0);
            try (var q = STBTTAlignedQuad.malloc()) {
                float[] x = new float[1];
                float[] y = new float[1];
                glyphs = new Glyph[GLYPH_COUNT];

                for (int i = 0; i < GLYPH_COUNT; i++) {
                    x[0] = 0f;
                    y[0] = fontSize;
                    stbtt_GetPackedQuad(packedchars, texwidth, texheight, i, x, y, q, true);
                    glyphs[i] = new Glyph((char) (i + 32), (int) (x[0] - 0f), new int[] { (int) q.x0(), (int) q.y0(), (int) q.x1(), (int) q.y1() }, new float[] { q.s0(), q.t0(), q.s1(), q.t1() });
                }
            }
        }

        this.atlas = texture;
    }

    public int width(String text) {
        var bytes = text.getBytes(StandardCharsets.US_ASCII);
        int len = 0;
        for (int i = 0; i < bytes.length; i++) {
            final byte c = bytes[i];
            len += switch (c) {
                case '\n', '\t' -> 0;
                case ' ' -> glyphs[0].charwidth();
                default -> {
                    if (c - 32 < GLYPH_COUNT && c > 32) {
                        yield this.glyphs[c - 32].charwidth();
                    } else {
                        yield 0;
                    }
                }
            };
        }
        return len;
    }

    public void scale(int[] scale, Text... texts) {
        var width   = 0;
        var height  = this.lineSpacing;

        for (var text : texts) {
            var length  = 0;
            for (byte c : text.bytes) {
                switch (c) {
                    case '\n': {
                        if (length  > width) {
                            width   = length;
                            length  = 0;
                        }

                        height  += this.lineSpacing;
                        break;
                    }
                    case '\t': {
                        length  += glyphs[0].charwidth() * 4;
                        break;
                    }
                    case ' ': {
                        length  += glyphs[0].charwidth();
                        break;
                    }
                    default: {
                        if (c - 32 < GLYPH_COUNT && c > 32) {
                            length  += glyphs[c - 32].charwidth();
                        }
                    }
                }
            }
        }

        scale[0]    = width;
        scale[1]    = height;
    }

    public void compile(
            Mesh.Builder builder,
            int x, int y,
            Text... texts
    ) {
        var pos         = new MutablePos(x, y);

        for (int i = 0; i < texts.length; i++) {
            var text    = texts[i];
            text.compile(builder, pos, this);
        }
    }

    public Mesh bake(
            int x, int y,
            Text... texts
    ) {
        var builder     = Mesh.builder(DefaultVertexFormats.SCREEN_ELEMENT, 32768);

        this            .compile(builder, x, y, texts);
        return builder  .build();
    }

    @Override
    public void free() {
        this.atlas.free();
    }

    @Override
    public void bind() {
        this.atlas.bind(0);
    }

    @Override
    public void unbind() {
        this.atlas.unbind(0);
    }

    private record Glyph(
            char c,
            int charwidth,
            int[] pos,
            float[] uv
    ) {
        public void compile(
                Mesh.Builder builder,
                int px, int py,
                int color
        ) {
            final var x0    = px + pos[0];
            final var y0    = py + pos[1];
            final var x1    = px + pos[2];
            final var y1    = py + pos[3];
            
            int idx = builder.getVertexCount();
            
            builder.floats(x0, y0, uv[0], uv[1]);
            builder.intValue(color);
            builder.countVertex();

            builder.floats(x0, y1, uv[0], uv[3]);
            builder.intValue(color);
            builder.countVertex();

            builder.floats(x1, y1, uv[2], uv[3]);
            builder.intValue(color);
            builder.countVertex();

            builder.floats(x1, y0, uv[2], uv[1]);
            builder.intValue(color);
            builder.countVertex();

            builder.index(idx);
            builder.index(idx + 1);
            builder.index(idx + 2);
            builder.index(idx + 2);
            builder.index(idx + 3);
            builder.index(idx);
        }
    }

    private static class MutablePos {

        public final    int x0;
        public final    int y0;

        public          int px;
        public          int py;

        public MutablePos(int x, int y) {
            this.x0 = x;
            this.y0 = y;
            this.px = x;
            this.py = y;
        }
    }

    public static class Text {
        public final String line;
        public final int color;

        private final byte[] bytes;

        public Text(String line, int color) {
            this.line   = line;
            this.color  = color;

            this.bytes  = line.getBytes(StandardCharsets.US_ASCII);
        }

        public void compile(
                Mesh.Builder    builder,
                MutablePos      pos,
                Font            font
        ) {
            var glyphs  = font.glyphs;

            for (int i = 0; i < this.bytes.length; i++) {
                var c   = this.bytes[i];

                switch (c) {
                    case '\n': {
                        pos.px  =   pos.x0;
                        pos.py  +=  font.lineSpacing;
                        break;
                    }
                    case '\t': {
                        pos.px  +=  glyphs[0].charwidth() * 4;
                        break;
                    }
                    case ' ': {
                        pos.px  +=  glyphs[0].charwidth();
                        break;
                    }
                    default: {
                        if (c > 32 && c - 32 < Font.GLYPH_COUNT) {
                            var glyph   =  glyphs[c - 32];
                            glyph       .compile(builder, pos.px, pos.py, this.color);

                            pos.px      += glyph.charwidth();
                        }
                    }
                }
            }
        }

        public int length() {
            return this.bytes.length;
        }
    }

    @FunctionalInterface
    public interface Consumer2f {
        void accept(float w, float h);
    }

}
