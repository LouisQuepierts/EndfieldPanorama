package net.quepierts.endfieldpanorama.earlywindow;

public interface Resource {
    void free();

    default void bind() {}

    default void unbind() {}
}
