package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

public class ParallaxBackground extends Actor {
    private final Array<Texture> textures;
    private float speed;
    private float scroll;

    public ParallaxBackground(Array<Texture> textures) {
        this.textures = textures;
        for (int i = 0; i < textures.size; i++) {
            this.textures.get(i).setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        }
    }

    public void setSpeed(float newSpeed) {
        this.speed = newSpeed;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        scroll += speed * delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        int LAYER_SPEED_DIFFERENCE = 2;
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        for (int i = 0; i < textures.size; i++) {
            Texture tex = textures.get(i);
            TextureRegion texRegion = new TextureRegion(tex, (int) width, (int) height);
            texRegion.setRegion((int) (scroll + i * LAYER_SPEED_DIFFERENCE * scroll), 0, (int) width, (int) height);
            batch.draw(texRegion, 0, 0, 0, 0, width, height,
                    1f, 1f, 0f);
        }
    }
}
