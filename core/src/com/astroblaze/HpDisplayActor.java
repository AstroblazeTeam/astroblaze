package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class HpDisplayActor extends Actor implements IPlayerStateChangeListener {
    private final float hpWidth = 4f;
    private boolean shouldHide = true; // externally controlled (e.g. player died etc)
    private float targetHp = 0f; // percentage
    private float currentHp = 0f; // percentage
    private float hpLerpSpeed = 0.5f; // velocity of hp bar change
    private float drawAlpha = 0f; // internal logic decides if we actually draw or not
    private float alphaSpeed = 2f;
    private final float defaultFadeTimer = 3f;
    private float fadeTimer;
    private ShapeRenderer shapeRenderer;
    private TextureAtlas.AtlasRegion heartTex;

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        AstroblazeGame.getInstance().addPlayerStateChangeListener(this);

        if (shapeRenderer == null)
            shapeRenderer = new ShapeRenderer();
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
        final float w = 4f * Gdx.graphics.getDensity();

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
        TextureAtlas.AtlasRegion heart = Assets.heartTexture;
        float scale = 0.5f;
        for (int i = 0; i < AstroblazeGame.getInstance().getScene().getLives(); i++) {
            batch.draw(heart, i * heart.getRegionWidth() * scale * 1.25f + hpWidth * 2f, hpWidth * 2f,
                    0, 0, heart.getRegionWidth(), heart.getRegionHeight(),
                    scale, scale, 0f);
        }
    }

    @Override
    public void onHpChanged(Ship ship, float newHp, float oldHp) {
        targetHp = newHp / ship.getMaxHp();
        if (AstroblazeGame.getInstance().getScene().getLives() < 1) {
            targetHp = 0f;
            fadeTimer = 10f;
        }
    }

    @Override
    public void onHpEnabled(Ship ship, boolean enabled) {
        shouldHide = !enabled;
    }

    @Override
    public void onSpecialTextChanged(Ship ship, String text1, String text2) {
        //
    }
}
