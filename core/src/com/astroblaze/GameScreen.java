package com.astroblaze;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameScreen extends ScreenAdapter {
    private final AstroblazeGame game;
    private final Stage stage;
    private ParallaxBackground parallax;
    private FadePainter fadePainter;
    private HpDisplayActor hpDisplayActor;
    private LevelController levelController;
    private ShipPreviewActor shipPreview;

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

    public LevelController getLevelController() {
        return this.levelController;
    }

    public ShipPreviewActor getShipPreview() {
        return shipPreview;
    }

    public boolean isGameRunning() {
        return this.levelController != null;
    }

    @Override
    public void resize(int width, int height) {
        this.game.getScene().resize(width, height);
    }

    @Override
    public void show() {
        fadePainter = new FadePainter(this.game.getScene().getCamera());
        parallax = new ParallaxBackground(8f);

        hpDisplayActor = new HpDisplayActor();
        hpDisplayActor.setVisible(false);

        shipPreview = new ShipPreviewActor(game.getScene());
        shipPreview.setVisible(false);

        this.stage.addActor(parallax);
        this.stage.addActor(new DebugTextDrawer());
        this.stage.addActor(hpDisplayActor);
        this.stage.addActor(shipPreview);
        this.stage.addAction(Actions.sequence(Actions.fadeOut(0f), Actions.fadeIn(1f)));
    }

    public void startGame(int level) {
        final float duration = 0.5f;

        levelController = new LevelController(game.getScene());
        levelController.setLevel(level);
        this.stage.addActor(levelController);
        game.clearText();
        this.stage.addAction(Actions.sequence(
                Actions.fadeOut(duration),
                Actions.delay(duration),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        game.getScene().reset();
                    }
                }),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        hpDisplayActor.setVisible(true);
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
        if (!isGameRunning())
            return;

        final float duration = 0.5f;
        game.clearText();
        if (levelController != null) {
            levelController.remove();
            levelController = null;
        }
        this.stage.act(30f);
        this.stage.addAction(Actions.sequence(
                Actions.fadeOut(duration),
                Actions.delay(duration),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        game.getScene().reset();
                    }
                }),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        hpDisplayActor.setVisible(false);
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
