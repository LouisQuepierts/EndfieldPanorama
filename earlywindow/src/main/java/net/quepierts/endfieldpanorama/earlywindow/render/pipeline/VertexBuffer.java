package net.quepierts.endfieldpanorama.earlywindow.render.pipeline;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL31;

public final class VertexBuffer {

    private static int BOUND_BUFFER;

    private final int vertex;
    private final int element;
    private final int array;

    private Mesh last;

    public VertexBuffer() {
        this.vertex = GL31.glGenBuffers();
        this.element = GL31.glGenBuffers();
        this.array = GL31.glGenVertexArrays();
    }

    public void upload(final @NotNull Mesh mesh) {

        if (mesh == this.last) {
            return;
        }

        GL31.glBindVertexArray(this.array);
        GL31.glBindBuffer(GL31.GL_ARRAY_BUFFER, this.vertex);
        GL31.glBufferData(GL31.GL_ARRAY_BUFFER, mesh.getVbo(), GL31.GL_STATIC_DRAW);
        GL31.glBindBuffer(GL31.GL_ELEMENT_ARRAY_BUFFER, this.element);
        GL31.glBufferData(GL31.GL_ELEMENT_ARRAY_BUFFER, mesh.getEbo(), GL31.GL_STATIC_DRAW);

        mesh.getFormat().apply();

        GL31.glBindVertexArray(0);
        GL31.glBindBuffer(GL31.GL_ARRAY_BUFFER, 0);
        GL31.glBindBuffer(GL31.GL_ELEMENT_ARRAY_BUFFER, 0);

        this.last = mesh;
    }

    public void draw() {
        this.bind();
        GL31.glDrawElements(GL31.GL_TRIANGLES, this.last.getIndexCount(), GL31.GL_UNSIGNED_INT, 0);
        this.unbind();
    }

    public void bind() {
        if (BOUND_BUFFER != this.array) {
            GL31.glBindVertexArray(this.array);
            GL31.glBindBuffer(GL31.GL_ARRAY_BUFFER, this.vertex);
            GL31.glBindBuffer(GL31.GL_ELEMENT_ARRAY_BUFFER, this.element);
            BOUND_BUFFER = this.array;
        }
    }


    public void unbind() {
        if (BOUND_BUFFER == this.array) {
            GL31.glBindVertexArray(0);
            GL31.glBindBuffer(GL31.GL_ARRAY_BUFFER, 0);
            GL31.glBindBuffer(GL31.GL_ELEMENT_ARRAY_BUFFER, 0);
            BOUND_BUFFER = 0;
        }
    }

    public void free() {
        GL31.glDeleteBuffers(this.vertex);
        GL31.glDeleteBuffers(this.element);
        GL31.glDeleteVertexArrays(this.array);
    }

}
