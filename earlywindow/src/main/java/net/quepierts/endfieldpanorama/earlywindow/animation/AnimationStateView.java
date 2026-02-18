package net.quepierts.endfieldpanorama.earlywindow.animation;

public interface AnimationStateView extends WritableTarget {

    int getCursor(int cid);

    void setCursor(int cid, int cursor);

}
