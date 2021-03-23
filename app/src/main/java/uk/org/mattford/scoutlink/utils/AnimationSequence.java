package uk.org.mattford.scoutlink.utils;

import android.view.View;
import android.view.animation.Animation;

import java.util.List;

public class AnimationSequence {
    private final List<Animation> animations;
    private int currentAnimation;
    private boolean repeatEnabled = false;
    private int repeatDelay = 1000;
    private int iterationCount = 0;

    public AnimationSequence(List<Animation> animations) {
        this.animations = animations;
    }

    public void setRepeatEnabled(boolean value) {
        repeatEnabled = value;
    }

    public void setRepeatDelay(int delay) {
        repeatDelay = delay;
    }

    public void runOnView(View view) {
        currentAnimation = 0;
        runNextAnimation(view);
    }

    private void runNextAnimation(View view) {
        if (currentAnimation >= 0 && currentAnimation < animations.size()) {
            Animation animation = animations.get(currentAnimation);
            if (currentAnimation == 0 && iterationCount > 0) {
                animation.setStartOffset(repeatDelay);
            }
            incrementCurrentAnimation();
            animation.setAnimationListener(new Listener(view));
            view.startAnimation(animation);
        }
    }

    private void incrementCurrentAnimation() {
        currentAnimation++;
        if (repeatEnabled && currentAnimation >= animations.size()) {
            currentAnimation = 0;
            iterationCount++;
        }
    }

    private class Listener implements Animation.AnimationListener {
        private final View view;
        public Listener(View view) {
            this.view = view;
        }

        @Override
        public void onAnimationStart(Animation animation) {}

        @Override
        public void onAnimationEnd(Animation animation) {
            runNextAnimation(view);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}
    }

}
