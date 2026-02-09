package net.quepierts.endfieldpanorama.earlywindow.render.shader;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL31;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Map;

public final class UniformBuffer implements UniformContained {

    private static final int MAX_BINDING_POINT = Math.min(8, GL31.glGetInteger(GL31.GL_MAX_UNIFORM_BUFFER_BINDINGS));
    private static final UniformBuffer[] BUFFERS = new UniformBuffer[MAX_BINDING_POINT];

    @Getter
    private final String name;

    @Getter
    private final int id;

    @Getter
    private final int bindingPoint;

    private final int size;

    private final ByteBuffer buffer;

    @Getter
    private boolean dirty;

    private final Map<String, AbstractUniform> uniforms;

    public UniformBuffer(
            @NotNull    String             name,
            @NotNull    UniformDefinition  definitions,
                        int                bindingPoint
    ) {
        if (definitions.isEmpty()) {
            throw new IllegalArgumentException("No uniforms defined for UniformBuffer " + name);
        }

        this.name           = name;
        this.bindingPoint   = bindingPoint;

        this.id         = GL31.glGenBuffers();
        this.size       = definitions.getByteSize();
        this.buffer     = MemoryUtil.memAlloc(size);

        GL31.glBindBuffer(GL31.GL_UNIFORM_BUFFER, this.id);
        GL31.glBufferData(GL31.GL_UNIFORM_BUFFER, size, GL31.GL_DYNAMIC_DRAW);
        GL31.glBindBuffer(GL31.GL_UNIFORM_BUFFER, 0);

        var builder     = ImmutableMap.<String, AbstractUniform>builder();

        definitions.accept((uName, uType, uOffset) -> {
            var uniform = new ViewUniform(this, uType, uOffset);
            builder.put(uName, uniform);
        });

        this.uniforms = builder.build();
    }

    public void upload() {
        for (var uniform : this.uniforms.values()) {
            uniform.upload();
        }

        if (!this.dirty) {
            return;
        }

        this.dirty  = false;
        this.buffer.rewind();

        GL31.glBindBuffer(GL31.GL_UNIFORM_BUFFER, this.id);
        GL31.glBufferSubData(GL31.GL_UNIFORM_BUFFER, 0, this.buffer);
        GL31.glBindBuffer(GL31.GL_UNIFORM_BUFFER, 0);
    }

    public void free() {
        GL31.glDeleteBuffers(this.id);
        MemoryUtil.memFree(this.buffer);
    }

    public void bind() {
        if (BUFFERS[this.bindingPoint] != this) {
            BUFFERS[this.bindingPoint] = this;
            GL31.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, this.bindingPoint, this.id);
        }
    }

    public void unbind() {
        if (BUFFERS[this.bindingPoint] == this) {
            BUFFERS[this.bindingPoint] = null;
            GL31.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, this.bindingPoint, 0);
        }
    }

    @Override
    public @NotNull AbstractUniform getUniform(@NotNull String name) {
        return this.uniforms.getOrDefault(name, AbstractUniform.DUMMY);
    }

    private static final class ViewUniform extends AbstractUniform {

        private final int offset;
        private final UniformBuffer parent;

        private final FloatBuffer floatView;
        private final IntBuffer intView;

        ViewUniform(UniformBuffer parent, UniformType type, int offset) {
            super(type);
            this.offset = offset / type.size;
            this.parent = parent;

            var ref = parent.buffer;
            this.floatView = ref.asFloatBuffer();
            this.intView = ref.asIntBuffer();
        }

        @Override
        protected void _upload() {

            this.parent.dirty = true;

            var type = this.getType();
            switch (type.datatype) {
                case GL31.GL_FLOAT:
                    this.floatView.position(offset);
                    this.floatView.put(floats);
                    break;
                case GL31.GL_INT:
                    this.intView.position(offset);
                    this.intView.put(ints);
                    break;
            }

        }
    }

}
