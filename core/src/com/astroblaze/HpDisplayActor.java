package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class HpDisplayActor extends Actor implements AstroblazeGame.IHpChangeListener {
    private final float hpWidth = 8f;
    private boolean shouldHide = true; // externally controlled (e.g. player died etc)
    private float targetHpPercentage = 0f;
    private float currentHpPercentage = 0f;
    private float hpLerpSpeed = 1f;
    private float drawAlpha = 0f; // internal logic decides if we actually draw or not
    private float alphaSpeed = 2f;
    private final float defaultFadeTimer = 3f;
    private float fadeTimer;
    private ShapeRenderer shapeRenderer;
    private TextureAtlas.AtlasRegion heartTex;

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        AstroblazeGame.getInstance().addHpChangeListener(this);
        if (shapeRenderer == null)
            shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        currentHpPercentage = MathHelper.moveTowards(currentHpPercentage, targetHpPercentage,
                hpLerpSpeed * delta);

        if (currentHpPercentage != targetHpPercentage) {
            fadeTimer = defaultFadeTimer;
        }

        fadeTimer -= delta;
        if (!shouldHide && fadeTimer > 0f) {
            drawAlpha = MathHelper.moveTowards(drawAlpha, 1f, alphaSpeed * delta);
        } else {
            final float lowestAlpha = shouldHide ? 0f : 0.1f;
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

        Color c = Color.GREEN;
        c.a = drawAlpha;
        batch.setColor(c);
        batch.draw(Assets.whitePixel, 0, 0, hpWidth, h * currentHpPercentage);
        c = Color.RED;
        c.a = drawAlpha;
        batch.setColor(c);
        batch.draw(Assets.whitePixel, 0, h * currentHpPercentage, hpWidth, h);
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
        targetHpPercentage = newHp / ship.getMaxHp();
        if (AstroblazeGame.getInstance().getScene().getLives() < 1) {
            targetHpPercentage = 0f;
            fadeTimer = 10f;
        }
    }

    @Override
    public void onHpEnabled(Ship ship, boolean enabled) {
        shouldHide = !enabled;
    }
}
