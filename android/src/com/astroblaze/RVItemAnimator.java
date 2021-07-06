package com.astroblaze;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import kotlin.Triple;

public class RVItemAnimator extends DefaultItemAnimator {
    private final AccelerateInterpolator accelInterpolator = new AccelerateInterpolator(2f);
    private final DecelerateInterpolator decelInterpolator = new DecelerateInterpolator(2f);
    private final ArrayMap<RecyclerView.ViewHolder, Triple<Animator, ValueAnimator, ValueAnimator>> animations = new ArrayMap<>();

    @Override
    public boolean canReuseUpdatedViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder) {
        return true;
    }

    @Override
    public boolean animateChange(@NonNull final RecyclerView.ViewHolder oldHolder, @NonNull final RecyclerView.ViewHolder newHolder, @NonNull ItemHolderInfo preInfo, @NonNull ItemHolderInfo postInfo) {
        if (oldHolder != newHolder) {
            return super.animateChange(oldHolder, newHolder, preInfo, postInfo);
        }

        final float translateWidth = 0.5f * newHolder.itemView.getWidth();
        final Triple<Animator, ValueAnimator, ValueAnimator> animInfo = animations.get(newHolder);
        long prevAnimPlayTime = 0;
        boolean half = false;
        if (animInfo != null) {
            half = animInfo.getSecond() != null && animInfo.getSecond().isRunning();
            prevAnimPlayTime = half
                    ? animInfo.getSecond().getCurrentPlayTime()
                    : animInfo.getThird().getCurrentPlayTime();
            animInfo.getFirst().cancel();
        }
        final View itemView = newHolder.itemView;

        ValueAnimator firstAnimator = null;
        if (animInfo == null || half) {
            firstAnimator = ValueAnimator.ofFloat(0f, 1f);
            firstAnimator.setInterpolator(accelInterpolator);
            firstAnimator.setDuration(400);
            firstAnimator.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                itemView.setAlpha(1f - 4f * value);
                itemView.setTranslationX(translateWidth * value);
            });
            if (animInfo != null) {
                firstAnimator.setCurrentPlayTime(prevAnimPlayTime);
            }
        }

        ValueAnimator secondAnimator = ValueAnimator.ofFloat(-1f, 0f);
        secondAnimator.setInterpolator(decelInterpolator);
        secondAnimator.setDuration(1200);
        secondAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            itemView.setAlpha(1f - 0.25f * value);
            itemView.setTranslationX(translateWidth * value);
        });
        if (animInfo != null && !half) {
            secondAnimator.setCurrentPlayTime(prevAnimPlayTime);
        }

        final AnimatorSet overallAnimation = new AnimatorSet();
        if (firstAnimator != null) {
            overallAnimation.playSequentially(firstAnimator, secondAnimator);
        } else {
            overallAnimation.play(secondAnimator);
        }

        overallAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(@NonNull final Animator animation) {
                itemView.setTranslationZ(1);
            }

            @Override
            public void onAnimationEnd(@NonNull final Animator animation) {
                itemView.setTranslationZ(0);
                dispatchAnimationFinished(newHolder);
                animations.remove(newHolder);
            }
        });
        overallAnimation.start();

        animations.put(newHolder, new Triple<>(overallAnimation, firstAnimator, secondAnimator));

        return true;
    }

    @Override
    public void endAnimation(@NonNull final RecyclerView.ViewHolder item) {
        super.endAnimation(item);

        Triple<Animator, ValueAnimator, ValueAnimator> animInfo = animations.getOrDefault(item, null);
        if (animInfo != null)
            animInfo.getFirst().cancel();
    }

    @Override
    public boolean isRunning() {
        return super.isRunning() || !animations.isEmpty();
    }

    @Override
    public void endAnimations() {
        super.endAnimations();

        for (Triple<Animator, ValueAnimator, ValueAnimator> i : animations.values()) {
            i.getFirst().cancel();
        }
    }
}
