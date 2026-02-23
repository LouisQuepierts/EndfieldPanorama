package net.quepierts.endfieldpanorama.earlywindow.render.pipeline;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.quepierts.endfieldpanorama.earlywindow.render.shader.ShaderProgram;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL31;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class VertexFormat {

    private final String[]  name;
    private final int[]     size;
    private final int[]     offset;
    private final int[]     type;

    @Getter
    private final int vertexSize;

    private final int stride;

    public void apply() {
        for (int i = 0; i < size.length; i++) {
            int glType = type[i];
            if (glType == GL31.GL_FLOAT) {
                GL31.glVertexAttribPointer(i, size[i], glType, false, stride, offset[i]);
            } else {
                GL31.glVertexAttribIPointer(i, size[i], glType, stride, offset[i]);
            }
            GL31.glEnableVertexAttribArray(i);
        }
    }

    public void bindAttributePosition(@NotNull ShaderProgram program) {
        var pid     = program.getProgram();
        for (int i = 0; i < this.type.length; i++) {
            GL31.glBindAttribLocation(pid, i, name[i]);
        }
    }

    public void write(float[] src, Mesh.Builder builder) {
        this.write(src, 0, builder);
    }

    public void write(float[] src, int offset, Mesh.Builder builder) {
        int ptr = offset;
        for (int i = 0; i < this.type.length; i++) {
            var size    = this.size[i];

            for (int j = 0; j < size; j++) {
                builder.floatValue(src[ptr]);
                ptr++;
            }
        }
    }

    public static class Builder {
        private final List<VertexElement> elements = new ArrayList<>();

        public Builder addElement(
                String      name,
                ElementType type,
                int         length
        ) {
            elements.add(new VertexElement(name, length, type));
            return this;
        }

        public VertexFormat build() {
            String[]    name    = new String[elements.size()];
            int[]       size    = new int[elements.size()];
            int[]       type    = new int[elements.size()];
            int[]       offset  = new int[elements.size()];
            int vertexSize      = 0;
            int vertexStride    = 0;

            for (int i = 0; i < elements.size(); i++) {
                VertexElement element = elements.get(i);
                name[i] = element.name;
                size[i] = element.length;
                type[i] = element.type.glType;
                offset[i] = vertexStride;
                vertexSize += element.length;
                vertexStride += element.length * element.type.size;
            }

            return new VertexFormat(name, size, offset, type, vertexSize, vertexStride);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class VertexElement {
        final String        name;
        final int           length;
        final ElementType   type;
    }

    @FunctionalInterface
    public interface FloatWriter {
        void write(float v);
    }

}
