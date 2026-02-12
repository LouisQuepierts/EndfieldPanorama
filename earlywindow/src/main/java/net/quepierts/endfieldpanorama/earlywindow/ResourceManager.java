package net.quepierts.endfieldpanorama.earlywindow;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class ResourceManager {

    private final List<Resource> resources;

    public ResourceManager() {
        this.resources = new ArrayList<>();
    }

    public void register(@NotNull Resource resource) {
        if (!resources.contains(resource)) {
            resources.add(resource);
        }
    }

    public void free() {
        for (Resource element : resources) {
            element.unbind();
            element.free();
        }
    }

}
