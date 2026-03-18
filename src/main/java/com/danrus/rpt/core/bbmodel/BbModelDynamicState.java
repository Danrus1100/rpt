package com.danrus.rpt.core.bbmodel;

import com.danrus.bb4j.model.BbModelDocument;

import java.util.Objects;

public class BbModelDynamicState {
    private String animation;
    private double animationTime;

    public double getAnimationTime() {
        return animationTime;
    }

    public void setAnimationTime(double animationTime) {
        this.animationTime = animationTime;
    }

    public String getAnimation() {
        return animation;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BbModelDynamicState that)) return false;
        return animationTime == that.animationTime && Objects.equals(animation, that.animation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(animation, animationTime);
    }
}
