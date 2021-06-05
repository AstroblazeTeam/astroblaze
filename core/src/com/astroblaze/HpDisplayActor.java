package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class HpDisplayActor extends Actor implements AstroblazeGame.IHpChangeListener {
    private float hpPercentage = 0f;

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        AstroblazeGame.getInstance().addHpChangeListener(this);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        final float hpWidth = 32f;
        final float h = Gdx.graphics.getHeight();
        batch.setColor(Color.GREEN);
        batch.draw(Assets.whitePixel, 0, 0, hpWidth, h * hpPercentage);
        batch.setColor(Color.RED);
        batch.draw(Assets.whitePixel, 0, h * hpPercentage, hpWidth, h);
    }

    @Override
    public void onHpChanged(Ship ship, float newHp, float oldHp) {
        hpPercentage = newHp / ship.getMaxHp();
    }

    @Override
    public void onHpEnabled(Ship ship, boolean enabled) {
        this.setVisible(enabled);
    }
}
