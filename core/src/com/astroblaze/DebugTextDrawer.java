package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class DebugTextDrawer extends Actor {
    private final static BitmapFont font = new BitmapFont();
    private float timeAccumulator = 0f;
    private static String report = "";
    private static String extraReport = "";
    private static float width;
    private static float height;

    public DebugTextDrawer() {
        font.getData().scale(1.5f);
    }

    public static void setExtraReport(String extra) {
        GlyphLayout layout = new GlyphLayout(); //dont do this every frame! Store it as member
        layout.setText(font, report + " " + extraReport);
        width = layout.width + 10f;// contains the width of the current set text
        height = layout.height; // contains the height of the current set text
        extraReport = extra;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        timeAccumulator += delta;
        float refreshTime = 0.25f;
        if (timeAccumulator >= refreshTime) {
            timeAccumulator = 0f;
            report = Gdx.graphics.getFramesPerSecond() + " fps";
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        font.draw(batch, report + " " + extraReport, Gdx.graphics.getWidth() - width, Gdx.graphics.getHeight() - height);
    }

    @Override
    public boolean remove() {
        font.dispose();
        return super.remove();
    }
}
