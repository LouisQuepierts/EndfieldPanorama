package net.quepierts.endfieldpanorama.neoforge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.quepierts.endfieldpanorama.earlywindow.EndfieldEarlyWindow;

import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public final class Overlay extends LoadingOverlay {

    private final EndfieldEarlyWindow window;

    public Overlay(
            Minecraft minecraft,
            ReloadInstance reload,
            Consumer<Optional<Throwable>> onFinish,
            boolean fadeIn,
            EndfieldEarlyWindow window
    ) {
        super(minecraft, reload, onFinish, fadeIn);
        this.window = window;
    }

}
