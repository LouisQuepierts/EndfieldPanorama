package net.quepierts.endfieldpanorama.boostrap;

import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.neoforgespi.earlywindow.GraphicsBootstrapper;
import net.quepierts.endfieldpanorama.earlywindow.EndfieldEarlyWindow;

import java.util.Objects;


public final class EndfieldEarlyWindowBootstrapper implements GraphicsBootstrapper {
    @Override
    public String name() {
        return "endfield_early_window_boostrapper";
    }

    @Override
    public void bootstrap(String[] arguments) {

        if (!FMLConfig.getBoolConfigValue(FMLConfig.ConfigValue.EARLY_WINDOW_CONTROL)) {
            return;
        }

        String currentProvider = FMLConfig.getConfigValue(FMLConfig.ConfigValue.EARLY_WINDOW_PROVIDER);
        if (EndfieldEarlyWindow.PROVIDER_NAME.equals(currentProvider)) {
            return;
        }
        if (!Objects.equals(currentProvider, "fmlearlywindow")) {
            return;
        }

        FMLConfig.updateConfig(FMLConfig.ConfigValue.EARLY_WINDOW_PROVIDER, EndfieldEarlyWindow.PROVIDER_NAME);

    }
}
