package net.quepierts.endfieldpanorama.earlywindow.render.shader;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Shaders {

    @UtilityClass
    public static class Vertex {

        public static final String BLIT             = "blit_screen";
        public static final String CHARACTER        = "character";

        public static final String PANORAMA         = "panorama";
    }

    @UtilityClass
    public static class Fragment {

        public static final String CHARACTER        = "character";
        public static final String FANCY_BACKGROUND = "fancy_background";

        public static final String PATTERN          = "noise_pattern";
        public static final String BACKGROUND       = "endfield_background";
        public static final String SCAN_LINE = "scan_line";

        public static final String PANORAMA         = "panorama";
        public static final String SILHOUETTE       = "glitch_silhouette";

    }

}
