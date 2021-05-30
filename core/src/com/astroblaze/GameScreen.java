package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameScreen extends ScreenAdapter {
    private final AstroblazeGame game;
    private final Stage stage;
    private float timeScale = 1f;

    public GameScreen(AstroblazeGame game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        this.stage.act(delta);
        game.getScene().act(timeScale * delta);
        this.stage.draw();
        game.getScene().render();
    }

    @Override
    public void resize(int width, int height) {
        game.getScene().resize(width, height);
    }

    @Override
    public void show() {
        if (timeScale > 0f) {
            this.stage.addAction(Actions.sequence(Actions.fadeOut(0f), Actions.fadeIn(1f)));
        }
        this.stage.addActor(new ParallaxBackground(8f));
        this.stage.addActor(new DebugTextDrawer());
    }

    public void startGame() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                game.getScene().addActors();
            }
        });
    }

    public void stopGame() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                game.getScene().clearActors();
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
