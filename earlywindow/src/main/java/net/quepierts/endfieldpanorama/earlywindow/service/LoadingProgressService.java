package net.quepierts.endfieldpanorama.earlywindow.service;

import lombok.Getter;
import lombok.Setter;
import net.neoforged.fml.loading.progress.StartupNotificationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface LoadingProgressService {
    @NotNull Context CONTEXT        = new Context();

    static Collection<ProgressBar> getCurrentProgress() {
        return CONTEXT.getCurrentProgress();
    }

    static void setProvider(@Nullable LoadingProgressService service) {
        CONTEXT.service = service == null ?
                new Fml() :
                service;
    }

    static int size() {
        return CONTEXT.size();
    }


    void provide(Context inout);

    boolean hasProgress();

    @Setter
    @Getter
    class ProgressBar {
        private String          message;
        private float           progress;
        private boolean         enable;
        private Object          delegate;
    }

    class Fml implements LoadingProgressService {

        @Override
        public void provide(Context context) {
            var progress        = StartupNotificationManager.getCurrentProgress();

            var i               = 0;
            context.fit(progress.size());

            var bars            = context.getProgress();
            for (; i < progress.size(); i++) {
                var pm          = progress.get(i);
                var bar         = bars.get(i);

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

            for (; i < LoadingProgressService.size(); i++) {
                var bar         = bars.get(i);
                bar             .setEnable(false);
                bar             .setDelegate(null);
            }
        }

        @Override
        public boolean hasProgress() {
            return true;
        }

    }

    class Context {

        private final List<ProgressBar>     empty       = List.of();
        @Getter
        private final List<ProgressBar>     progress    = new ArrayList<>();

        private LoadingProgressService      service;

        public Collection<ProgressBar> getCurrentProgress() {

            var service      = CONTEXT.service;

            if (service == null) {
                return empty;
            }

            var hasProgress     = service.hasProgress();
            if (hasProgress) {
                service        .provide(this);
                return progress;
            } else {
                progress.clear();
                return empty;
            }
        }

        protected void fit(int size) {
            while (progress.size() < size) {
                progress.add(new ProgressBar());
            }
        }

        public int size() {
            return this.progress.size();
        }
    }

}
