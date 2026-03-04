package net.quepierts.endfieldpanorama.earlywindow.render;

import net.quepierts.endfieldpanorama.earlywindow.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Manager<T extends Resource> {

    protected final Map<String, T> resources = new HashMap<>();

    public @Nullable T get(String name) {
        return resources.get(name);
    }

    public @NotNull T create(String name, Supplier<T> supplier) {
        var resource    = this.resources.get(name);
        if (resource != null) {
            return resource;
        }

        var instance     = supplier.get();
        this.resources  .put(name, instance);

        return instance;
    }

    public void free() {
        for (T value : resources.values()) {
            value.free();
        }
    }

}
