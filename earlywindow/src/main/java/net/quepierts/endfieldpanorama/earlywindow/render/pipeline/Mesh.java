package net.quepierts.endfieldpanorama.earlywindow.render.pipeline;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

@RequiredArgsConstructor
public final class Mesh {

    private static final int VERTEX_SIZE = 4;
    private static final int INDEX_SIZE = 4;

    private static final MemoryUtil.MemoryAllocator ALLOCATOR = MemoryUtil.getAllocator(false);


    @Getter
    private final VertexFormat format;

    private final long vboAdr;
    private final int vboSize;
    private final long eboAdr;
    private final int eboSize;

    @Getter
    private final int indexCount;

    public static Builder builder(VertexFormat format, int capacity) {
        return new Builder(format, capacity);
    }

    public void free() {
        ALLOCATOR.free(vboAdr);
        ALLOCATOR.free(eboAdr);
    }

    public ByteBuffer getVbo() {
        return MemoryUtil.memByteBuffer(vboAdr, (int) vboSize);
    }

    public ByteBuffer getEbo() {
        return MemoryUtil.memByteBuffer(eboAdr, (int) eboSize);
    }

    public static final class Builder {
        private final VertexFormat format;

        private final DynamicBuffer vbo;
        private final DynamicBuffer ebo;

        private int vertexCount;
        private int indexCount;

        /**
         * @param format vertex format
         * @param capacity index count
         */
        public Builder(
                final VertexFormat format,
                int capacity
        ) {

            this.format = format;

            var verticesCap = (long) capacity * format.getVertexSize() * VERTEX_SIZE;
            var indicesCap = (long) capacity * INDEX_SIZE;

            this.vbo = new DynamicBuffer(verticesCap);
            this.ebo = new DynamicBuffer(indicesCap);

        }

        public Builder quad(
                float[] v00,
                float[] v01,
                float[] v10,
                float[] v11
        ) {
            var count = vertexCount;

            // put vbo
            this.format.write(v00, this::vertex);
            this.format.write(v01, this::vertex);
            this.format.write(v10, this::vertex);
            this.format.write(v11, this::vertex);

            // put ebo
            this.index(count);
            this.index(count + 1);
            this.index(count + 2);
            this.index(count + 2);
            this.index(count + 3);
            this.index(count);

            vertexCount += 4;
            indexCount += 6;
            return this;
        }

        public Builder cube(
                float[] v000,
                float[] v001,
                float[] v010,
                float[] v011,
                float[] v100,
                float[] v101,
                float[] v110,
                float[] v111
        ) {
            quad(v000, v001, v010, v011);
            quad(v100, v101, v110, v111);
            quad(v000, v100, v110, v111);
            quad(v000, v010, v110, v111);
            quad(v001, v101, v111, v011);
            quad(v010, v011, v111, v110);

            return this;
        }

        private void vertex(float v) {
            var ptr = this.vbo.pointer;
            MemoryUtil.memPutFloat(ptr, v);
            this.vbo.pointer += Float.BYTES;
        }

        private void index(int v) {
            var ptr = this.ebo.pointer;
            MemoryUtil.memPutInt(ptr, v);
            this.ebo.pointer += Integer.BYTES;
        }

        public Mesh build() {
            var fboAddress  = this.vbo.address;
            var fboCapacity = this.vbo.pointer - fboAddress;
            var eboAddress  = this.ebo.address;
            var eboCapacity = this.ebo.pointer - eboAddress;

            return new Mesh(
                    format,
                    fboAddress,
                    (int) fboCapacity,
                    eboAddress,
                    (int) eboCapacity,
                    indexCount
            );
        }
    }

    private static final class DynamicBuffer {

        private long address;
        private long pointer;
        private long capacity;

        public DynamicBuffer(long capacity) {
            this.address = ALLOCATOR.malloc(capacity);
            this.pointer = this.address;
            this.capacity = capacity;
        }

        public void resize(long capacity) {
            if (capacity > this.capacity) {
                var address     = ALLOCATOR.malloc(capacity);
                var offset      = this.pointer - this.address;

                MemoryUtil.memCopy(this.address, address, this.capacity);
                ALLOCATOR.free(this.address);

                this.address    = address;
                this.pointer    = address + offset;
                this.capacity   = capacity;
            }
        }

        public void free() {
            ALLOCATOR.free(this.address);
        }

    }
}
