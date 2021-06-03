package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.FloatCounter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameScreen extends ScreenAdapter {
    private final AstroblazeGame game;
    private final Stage stage;

    public GameScreen(AstroblazeGame game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 0f, true);

        this.stage.act(delta);
        this.game.getScene().act(delta);
        this.stage.draw();
        this.game.getScene().render(game.getBatch());
    }

    @Override
    public void resize(int width, int height) {
        this.game.getScene().resize(width, height);
    }

    @Override
    public void show() {
        this.stage.addAction(Actions.sequence(Actions.fadeOut(0f), Actions.fadeIn(1f)));
        this.stage.addActor(new ParallaxBackground(8f));
        this.stage.addActor(new DebugTextDrawer());
        this.stage.addActor(new EnemySpawner(game.getScene(), 2f));
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
}
