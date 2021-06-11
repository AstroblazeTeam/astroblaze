package com.astroblaze;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g3d.Model;
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
            fadePainter.draw(stage.getBatch(), stage.getRoot().getColor().a);
        }
    }

    public Stage getStage() {
        return stage;
    }

    public LevelController getLevelController() {
        return levelController;
    }

    public ShipPreviewActor getShipPreview() {
        return shipPreview;
    }

    public boolean isGameRunning() {
        return levelController != null;
    }

    @Override
    public void resize(int width, int height) {
        game.getScene().resize(width, height);
    }

    @Override
    public void show() {
        fadePainter = new FadePainter(this.game.getScene().getCamera());
        parallax = new ParallaxBackground(8f);

        hpDisplayActor = new HpDisplayActor();
        hpDisplayActor.setVisible(false);

        shipPreview = new ShipPreviewActor(game.getScene());
        shipPreview.setVisible(false);

        AstroblazeGame.getInstance().setShipPreview(shipPreview);

        stage.addActor(parallax);
        stage.addActor(new DebugTextDrawer());
        stage.addActor(hpDisplayActor);
        stage.addActor(shipPreview);
        stage.addAction(Actions.sequence(Actions.fadeOut(0f), Actions.fadeIn(1f)));
    }

    public void startGame(int level, int shipSelect) {
        final float duration = 0.5f;
        final PlayerShipVariant shipVariant = getShipPreview().getVariant(shipSelect);

        levelController = new LevelController(game.getScene());
        levelController.setLevel(level);
        game.clearText();
        stage.addActor(levelController);
        stage.addAction(Actions.sequence(
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
                        game.getScene().respawnPlayer(shipVariant);
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
        stage.act(30f);
        stage.addAction(Actions.sequence(
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
