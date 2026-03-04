package net.quepierts.endfieldpanorama.earlywindow;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import net.neoforged.fml.loading.progress.StartupNotificationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@UtilityClass
public class LoadingProgressServiceProvider {

    private static final List<ProgressBar>  EMPTY       = List.of();
    private static final List<ProgressBar>  PROGRESS    = new ArrayList<>();

    private static ProgressService          provider    = new Fml();

    public static Collection<ProgressBar> getCurrentProgress() {
        if (provider == null) {
            return EMPTY;
        }

        var hasProgress     = provider.hasProgress();
        if (hasProgress) {
            provider        .provide(PROGRESS);
            return PROGRESS;
        } else {
            provider        = null;
            PROGRESS.clear();
            return EMPTY;
        }
    }

    public static void setProvider(@Nullable LoadingProgressServiceProvider.ProgressService provider) {
        LoadingProgressServiceProvider.provider = provider == null ?
                                            new Fml() :
                                            provider;
    }

    public static void fit(int size) {
        var current     = PROGRESS.size();
        if (current     >= size) {
            return;
        }

        for (; current < size; current ++) {
            PROGRESS.add(new ProgressBar());
        }
    }

    public static int size() {
        return PROGRESS.size();
    }

    @Setter
    @Getter
    public static class ProgressBar {
        private String          message;
        private float           progress;
        private boolean         enable;
        private Object          delegate;
    }

    private static class Fml implements ProgressService {

        @Override
        public void provide(@NotNull List<ProgressBar> inout) {
            var progress        = StartupNotificationManager.getCurrentProgress();

            var i               = 0;
            fit(progress.size());

            for (; i < progress.size(); i++) {
                var pm          = progress.get(i);
                var bar         = inout.get(i);

                if (bar.getDelegate() != pm) {
                    var text    = pm.label().getText();

                    bar         .setMessage("// " + text + "...");
                    bar         .setDelegate(pm);
                    bar         .setEnable(true);
                }

                var prog        = pm.progress();

                if (Float.isNaN(prog)) {
                    prog        = 0.0f;
                }
                bar             .setProgress(prog);
            }

            for (; i < LoadingProgressServiceProvider.size(); i++) {
                var bar         = inout.get(i);
                bar             .setEnable(false);
                bar             .setDelegate(null);
            }
        }

        @Override
        public boolean hasProgress() {
            return true;
        }

    }

    public interface ProgressService {
        void provide(@NotNull List<ProgressBar> inout);

        boolean hasProgress();
    }

}
