package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameScreen extends ScreenAdapter {
    private final AstroblazeGame game;
    private final Scene3D scene;
    private final Stage stage;
    private float timeScale = 1f;

    public GameScreen(AstroblazeGame game) {
        this.game = game;
        this.scene = new Scene3D(game);
        this.stage = new Stage(new ScreenViewport());

        Array<Texture> textures = new Array<Texture>();
        for (int i = 0; i <= 3; i++) {
            textures.add(new Texture(Gdx.files.internal("parallax/Starscape0" + i + ".png")));
            textures.get(textures.size - 1).setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.MirroredRepeat);
        }

        ParallaxBackground parallaxBackground = new ParallaxBackground(textures);
        parallaxBackground.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        parallaxBackground.setSpeed(8f);

        this.stage.addActor(parallaxBackground);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        this.stage.act(timeScale * delta);
        this.stage.draw();
        this.scene.act(timeScale * delta);
        this.scene.render();
    }

    @Override
    public void resize(int width, int height) {
        this.scene.resize(width, height);
    }

    public void startGame() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                scene.addActors();
            }
        });
    }

    public void stopGame() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                scene.clearActors();
            }
        });
    }

    public void pauseGame() {
        timeScale = 0f;
    }

    public void unpauseGame() {
        timeScale = 1f;
    }
}
