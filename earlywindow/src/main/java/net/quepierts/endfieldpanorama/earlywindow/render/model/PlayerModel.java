package net.quepierts.endfieldpanorama.earlywindow.render.model;

import net.quepierts.endfieldpanorama.earlywindow.render.DefaultVertexFormats;
import net.quepierts.endfieldpanorama.earlywindow.render.pipeline.Mesh;

public final class PlayerModel extends AbstractModel {

    public PlayerModel(boolean slim) {
        super(PlayerModel.build(slim));
    }

    private static Mesh build(boolean slim) {
        var builder = Mesh.builder(DefaultVertexFormats.CHARACTER, 1024);

        // head & hat
        AbstractModel.bone(builder, -4, 0, -4, 8, 8, 8, 0.0f, 0, 0, 64, 0);
        AbstractModel.bone(builder, -4, 0, -4, 8, 8, 8, 0.5f, 32, 0, 64, 0);

        // body & shirt
        AbstractModel.bone(builder, -4, -12, -2, 8, 12, 4, 0.0f, 16, 16, 64, 1);
        AbstractModel.bone(builder, -4, -12, -2, 8, 12, 4, 0.25f, 16, 32, 64, 1);

        if (slim) {
            // right arm & sleeve
            AbstractModel.bone(builder, 4, -12, -2, 3, 12, 4, 0.0f, 40, 16, 64, 2);
            AbstractModel.bone(builder, 4, -12, -2, 3, 12, 4, 0.25f, 40, 32, 64, 2);

            // left arm & sleeve
            AbstractModel.bone(builder, -7, -12, -2, 3, 12, 4, 0.0f, 32, 48, 64, 3);
            AbstractModel.bone(builder, -7, -12, -2, 3, 12, 4, 0.25f, 48, 48, 64, 3);

        } else {
            // right arm & sleeve
            AbstractModel.bone(builder, 4, -12, -2, 4, 12, 4, 0.0f, 40, 16, 64, 2);
            AbstractModel.bone(builder, 4, -12, -2, 4, 12, 4, 0.25f, 40, 32, 64, 2);

            // left arm & sleeve
            AbstractModel.bone(builder, -8, -12, -2, 4, 12, 4, 0.0f, 32, 48, 64, 3);
            AbstractModel.bone(builder, -8, -12, -2, 4, 12, 4, 0.25f, 48, 48, 64, 3);

        }

        // right leg & pants
        AbstractModel.bone(builder, 0, -24, -2, 4, 12, 4, 0.0f, 0, 16, 64, 4);
        AbstractModel.bone(builder, 0, -24, -2, 4, 12, 4, 0.25f, 0, 32, 64, 4);

        // left leg & pants
        AbstractModel.bone(builder, -4, -24, -2, 4, 12, 4, 0.0f, 16, 48, 64, 5);
        AbstractModel.bone(builder, -4, -24, -2, 4, 12, 4, 0.25f, 0, 48, 64, 5);

        return builder.build();
    }
}
