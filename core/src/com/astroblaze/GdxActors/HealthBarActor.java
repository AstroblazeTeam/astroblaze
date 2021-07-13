package com.astroblaze.GdxActors;

import com.astroblaze.*;
import com.astroblaze.Interfaces.*;
import com.astroblaze.Rendering.*;
import com.astroblaze.Utils.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

/**
 * This actor draws the player's health bar
 */
public class HealthBarActor extends Actor implements IUIChangeListener {
    private final float hpWidth = 4f; // width of hp bar (accounts for dpi)
    private final float hpLerpSpeed = 0.5f; // velocity of hp bar change
    private final float alphaSpeed = 2f; // velocity of opacity change
    private final Scene3D scene;
    private boolean shouldHide = true; // externally controlled hide bool (e.g. player died etc)
    private float targetHp = 0f; // percentage 0f .. 1f
    private float currentHp = 0f; // percentage 0f .. 1f
    private float drawAlpha = 0f; // internal logic decides if we actually draw or not
    private final float defaultFadeTimer = 3f; // time from hp change to fading start
    private float fadeTimer;
    private TextureAtlas.AtlasRegion heart;

    public HealthBarActor(Scene3D scene) {
        this.scene = scene;
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        heart = Assets.asset(Assets.atlas).findRegion("heart_icon");
        AstroblazeGame.getInstance().addUIChangeListener(this);
    }

    @Override
    public boolean remove() {
        AstroblazeGame.getInstance().removeUIChangeListener(this);
        return super.remove();
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        currentHp = MathHelper.moveTowards(currentHp, targetHp,
                hpLerpSpeed * delta);

        if (currentHp != targetHp) {
            fadeTimer = defaultFadeTimer;
        }

        fadeTimer -= delta;
        if (!shouldHide && fadeTimer > 0f) {
            drawAlpha = MathHelper.moveTowards(drawAlpha, 1f, alphaSpeed * delta);
        } else {
            final float lowestAlpha = shouldHide ? 0f : 0.25f;
            drawAlpha = MathHelper.moveTowards(drawAlpha, lowestAlpha, alphaSpeed * delta);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        drawLives(batch);

        if (drawAlpha == 0f)
            return;

        final float h = Gdx.graphics.getHeight();
        final float w = hpWidth * Gdx.graphics.getDensity();

        // make white highlight for just-taken-away health
        if (currentHp > targetHp) {
            drawPartOfHpBar(batch, Color.RED, w, h, currentHp, 1f);
            drawPartOfHpBar(batch, Color.WHITE, w, h, targetHp, currentHp - targetHp);
            drawPartOfHpBar(batch, Color.GREEN, w, h, 0, targetHp);
        } else if (currentHp < targetHp) {
            drawPartOfHpBar(batch, Color.RED, w, h, targetHp, 1f);
            drawPartOfHpBar(batch, Color.WHITE, w, h, currentHp, targetHp - currentHp);
            drawPartOfHpBar(batch, Color.GREEN, w, h, 0, currentHp);
        } else {
            drawPartOfHpBar(batch, Color.RED, w, h, currentHp, 1f);
            drawPartOfHpBar(batch, Color.GREEN, w, h, 0, currentHp);
        }
    }

    private void drawPartOfHpBar(Batch batch, Color c, float w, float h, float from, float to) {
        batch.setColor(c.r, c.g, c.b, drawAlpha);
        batch.draw(Assets.whitePixel, 0, h * from, w, h * to);
    }

    private void drawLives(Batch batch) {
        float scale = 0.5f;
        batch.setColor(Color.WHITE);
        for (int i = 0; i < scene.getLives(); i++) {
            batch.draw(heart, i * heart.getRegionWidth() * scale * 1.25f + hpWidth * 2f, hpWidth * 2f,
                    0, 0, heart.getRegionWidth(), heart.getRegionHeight(),
                    scale, scale, 0f);
        }
    }

    @Override
    public void onHpChanged(PlayerShip playerShip, float newHp, float oldHp) {
        targetHp = newHp / playerShip.getMaxHitpoints();
        if (scene.getLives() < 1) {
            targetHp = 0f;
            fadeTimer = 10f;
        }
    }

    @Override
    public void onHpEnabled(PlayerShip playerShip, boolean enabled) {
        shouldHide = !enabled;
    }

    @Override
    public void onSpecialTextChanged(PlayerShip playerShip, String text1, String text2) {
    }
}
