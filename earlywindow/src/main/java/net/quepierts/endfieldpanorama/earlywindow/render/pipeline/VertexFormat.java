package net.quepierts.endfieldpanorama.earlywindow.render.pipeline;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.lwjgl.opengl.GL31;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class VertexFormat {

    private final int[] size;
    private final int[] offset;
    private final int[] type;

    @Getter
    private final int vertexSize;

    private final int stride;

    public void apply() {
        for (int i = 0; i < size.length; i++) {
            GL31.glVertexAttribPointer(i, size[i], type[i], false, stride, offset[i]);
            GL31.glEnableVertexAttribArray(i);
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

        public Builder addElement(int length, ElementType type) {
            elements.add(new VertexElement(length, type));
            return this;
        }

        public VertexFormat build() {
            int[] size = new int[elements.size()];
            int[] type = new int[elements.size()];
            int[] offset = new int[elements.size()];
            int vertexSize = 0;
            int vertexStride = 0;

            for (int i = 0; i < elements.size(); i++) {
                VertexElement element = elements.get(i);
                size[i] = element.length;
                type[i] = element.type.glType;
                offset[i] = vertexStride;
                vertexSize += element.length;
                vertexStride += element.length * element.type.size;
            }

            return new VertexFormat(size, offset, type, vertexSize, vertexStride);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class VertexElement {
        final int length;
        final ElementType type;
    }

    @FunctionalInterface
    public interface FloatWriter {
        void write(float v);
    }

}
