package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameScreen extends ScreenAdapter {
    private final AstroblazeGame game;
    private final Stage stage;
    private ParallaxBackground parallax;
    private FadePainter fadePainter;
    private Label gameOver;
    private HpDisplayActor hpDisplayActor;

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
        if (this.stage.getRoot().getColor().a != 1f) {
            fadePainter.draw(stage.getBatch(), this.stage.getRoot().getColor().a);
        }
    }

    public Stage getStage() {
        return this.stage;
    }

    @Override
    public void resize(int width, int height) {
        this.game.getScene().resize(width, height);
    }

    @Override
    public void show() {
        fadePainter = new FadePainter(this.game.getScene().getCamera());
        parallax = new ParallaxBackground(8f);

        gameOver = new Label("Game Over", Assets.asset(Assets.uiSkin));
        gameOver.setFontScale(8f);
        gameOver.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        gameOver.setAlignment(Align.center);
        gameOver.setVisible(false);

        hpDisplayActor = new HpDisplayActor();
        hpDisplayActor.setVisible(false);

        this.stage.addActor(parallax);
        this.stage.addActor(new DebugTextDrawer());
        this.stage.addActor(new EnemySpawner(game.getScene(), 2f));
        this.stage.addAction(Actions.sequence(Actions.fadeOut(0f), Actions.fadeIn(1f)));
        this.stage.addActor(gameOver);
        this.stage.addActor(hpDisplayActor);
    }

    public void setGameOverVisible(boolean visible) {
        gameOver.setVisible(visible);
    }

    public void startGame() {
        final float duration = 0.5f;
        this.setGameOverVisible(false);
        this.stage.act(10f);
        this.stage.addAction(Actions.sequence(
                Actions.fadeOut(duration),
                Actions.delay(duration),
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
                        game.getScene().respawnPlayer();
                    }
                })));
    }

    public void stopGame() {
        final float duration = 0.5f;
        this.setGameOverVisible(false);
        this.stage.act(10f);
        this.stage.addAction(Actions.sequence(
                Actions.fadeOut(duration),
                Actions.delay(duration),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        game.getScene().reset();
                    }
                }),
                Actions.fadeIn(1f),
                Actions.delay(duration),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        game.resumeGame();
                    }
                })));
    }
}
