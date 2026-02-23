package net.quepierts.endfieldpanorama.earlywindow.render.shader;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import net.quepierts.endfieldpanorama.earlywindow.Resource;
import net.quepierts.endfieldpanorama.earlywindow.render.pipeline.VertexFormat;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL31;

import java.util.Map;

public class ShaderProgram implements UniformContained, Resource {

    private static ShaderProgram last;

    @Getter
    private final Shader        vertex;
    @Getter
    private final Shader        fragment;
    @Getter
    private final VertexFormat  format;

    @Getter
    private final int           program;

    private final Map<String, AbstractUniform> uniforms;

    private boolean free = false;

    public ShaderProgram(
            @NotNull ShaderManager      manager,
            @NotNull String             vertex,
            @NotNull String             fragment,
            @NotNull VertexFormat       format,
            @NotNull UniformDefinition  definitions
    ) {

        var vsh = manager.getShader(vertex, ShaderType.VERTEX);
        var fsh = manager.getShader(fragment, ShaderType.FRAGMENT);

        if (vsh == null) {
            throw new RuntimeException("Shader " + vertex + " not found");
        }

        if (fsh == null) {
            throw new RuntimeException("Shader " + fragment + " not found");
        }

        this.vertex     = vsh;
        this.fragment   = fsh;

        this.format     = format;

        var program     = GL31.glCreateProgram();

        GL31.glAttachShader(program, vsh.getId());
        GL31.glAttachShader(program, fsh.getId());

        this.format.bindAttributePosition(this);

        GL31.glLinkProgram(program);

        if (GL31.glGetProgrami(program, GL31.GL_LINK_STATUS) == GL31.GL_FALSE) {
            throw new RuntimeException(GL31.glGetProgramInfoLog(program));
        }

        GL31.glDetachShader(program, vsh.getId());
        GL31.glDetachShader(program, fsh.getId());

        this.program = program;

        var builder  = ImmutableMap.<String, AbstractUniform>builder();

        definitions.accept((uName, uType, uOffset) -> {
            var location    = GL31.glGetUniformLocation(program, uName);
            var uniform     = location == -1 ?
                                AbstractUniform.DUMMY : new ProgramUniform(uName, uType, location);

            builder.put(uName, uniform);
        });

        this.uniforms = builder.build();
    }

    @Override
    public void bind() {
        if (last == this) {
            return;
        }
        GL31.glUseProgram(program);
    }

    @Override
    public void unbind() {
        GL31.glUseProgram(0);
        last = null;
    }

    public void upload() {
        for (var uniform : uniforms.values()) {
            uniform.upload();
        }
    }

    @Override
    public void free() {
        if (this.free) {
            return;
        }
        this.free = true;
        GL31.glDeleteProgram(program);
    }

    public void bind(@NotNull UniformBuffer buffer) {
        var location = GL31.glGetUniformBlockIndex(program, buffer.getName());
        GL31.glUniformBlockBinding(program, location, buffer.getBindingPoint());
    }

    @Override
    public @NotNull AbstractUniform getUniform(@NotNull String name) {
        return this.uniforms.getOrDefault(name, AbstractUniform.DUMMY);
    }

    @Getter
    private static final class ProgramUniform extends AbstractUniform {

        private final String name;
        private final int location;

        public ProgramUniform(@NotNull String name, @NotNull UniformType type, int location) {
            super(type);
            this.name = name;
            this.location = location;
        }

        @Override
        public void _upload() {
            switch (this.getType()) {
                case FLOAT:
                    GL31.glUniform1f(location, floats[0]);
                    break;
                case VEC2:
                    GL31.glUniform2f(location, floats[0], floats[1]);
                    break;
                case VEC3:
                    GL31.glUniform3f(location, floats[0], floats[1], floats[2]);
                    break;
                case VEC4:
                    GL31.glUniform4f(location, floats[0], floats[1], floats[2], floats[3]);
                    break;
                case MAT4:
                    GL31.glUniformMatrix4fv(location, false, floats);
                    break;
                case SAMPLER:
                    GL31.glUniform1i(location, ints[0]);
                    break;
            }
        }
    }
}
