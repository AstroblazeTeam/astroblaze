package com.astroblaze.Utils;

import com.astroblaze.AstroblazeGame;
import com.astroblaze.Rendering.Scene3D;
import com.badlogic.gdx.scenes.scene2d.Action;

/**
 * This provides an Action similar to DelayAction but respects the world's time scale
 * so that Actors don't move while game is paused
 */
public class SceneDelayAction extends Action {
    private final Scene3D scene;
    private float duration;

    public SceneDelayAction(float duration) {
        this.scene = AstroblazeGame.getInstance().getScene();
        this.duration = duration;
    }

    public boolean act(float delta) {
        delta *= scene.getTimeScale();
        if (delta <= 0f) return false;
        duration -= delta;
        return duration <= 0f;
    }
}
