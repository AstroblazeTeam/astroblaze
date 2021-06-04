package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
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
        final float durations = 0.5f;
        this.stage.addAction(Actions.sequence(
                Actions.fadeOut(durations),
                Actions.delay(durations),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        game.getScene().reset();
                    }
                }),
                Actions.fadeIn(1f),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        game.getScene().spawnPlayer();
                    }
                }),
                Actions.delay(durations * 2f),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        game.getScene().getPlayer().setControlled(true);
                    }
                })));
    }

    public void stopGame() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                game.getScene().reset();
            }
        });
    }
}
