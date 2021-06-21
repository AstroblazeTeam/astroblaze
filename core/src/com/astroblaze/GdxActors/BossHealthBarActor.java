package com.astroblaze.GdxActors;

import com.astroblaze.Assets;
import com.astroblaze.Rendering.Enemy;
import com.astroblaze.Rendering.Scene3D;
import com.astroblaze.Utils.MathHelper;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class BossHealthBarActor extends Actor {
    private final float hpWidth = 4f; // width of hp bar (accounts for dpi)
    private final float hpLerpSpeed = 0.5f; // velocity of hp bar change
    private final float alphaSpeed = 2f; // velocity of opacity change
    private float targetHp = 0f; // percentage 0f .. 1f
    private float currentHp = 0f; // percentage 0f .. 1f
    private float drawAlpha = 0f; // internal logic decides if we actually draw or not
    private Enemy trackedEnemy;

    public BossHealthBarActor() {
    }

    public void setTrackedEnemy(Enemy e) {
        this.trackedEnemy = e;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (trackedEnemy == null) {
            targetHp = 0f;
        } else {
            targetHp = trackedEnemy.getHitpoints() / trackedEnemy.getMaxHitpoints();
            if (targetHp == 0f) {
                trackedEnemy = null;
            }
        }

        currentHp = MathHelper.moveTowards(currentHp, targetHp, hpLerpSpeed * delta);
        drawAlpha = MathHelper.moveTowards(drawAlpha, trackedEnemy != null ? 1f : 0f, alphaSpeed * delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        if (drawAlpha == 0f)
            return;

        final float h = Gdx.graphics.getHeight();
        final float w = hpWidth * Gdx.graphics.getDensity();

        Color c = new Color(Color.RED);
        batch.setColor(c);
        batch.draw(Assets.whitePixel,
                Gdx.graphics.getWidth() - w, 0,
                Gdx.graphics.getWidth(), h * currentHp);
    }
}
