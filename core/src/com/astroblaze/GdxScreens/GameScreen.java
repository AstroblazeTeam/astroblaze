package com.astroblaze.GdxScreens;

import com.astroblaze.*;
import com.astroblaze.GdxActors.*;
import com.astroblaze.Rendering.*;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameScreen extends ScreenAdapter {
    private final AstroblazeGame game;
    private final Stage stage;

    private FadeOverlayActor fadeOverlayActor; // fade transition translucent overlay
    private HealthBarActor healthBarActor; // health bar display
    private BossHealthBarActor bossHealthBarActor; // health bar display
    private LevelControllerActor levelControllerActor; // level controller (enemy spawner)
    private ShipPreviewActor shipPreviewActor; // ship preview (for level select)
    private Scene3D scene;

    public GameScreen(AstroblazeGame game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 0f, true);

        this.stage.act(delta);
        this.scene.act(delta);
        this.stage.draw();
        this.scene.render(game.getBatch());
        if (this.stage.getRoot().getColor().a != 1f) {
            fadeOverlayActor.draw(stage.getBatch(), stage.getRoot().getColor().a);
        }
    }

    public Stage getStage() {
        return stage;
    }

    public LevelControllerActor getLevelController() {
        return levelControllerActor;
    }

    public ShipPreviewActor getShipPreview() {
        return shipPreviewActor;
    }

    public BossHealthBarActor getBossTracker() {
        return this.bossHealthBarActor;
    }

    public boolean isGameRunning() {
        return levelControllerActor != null;
    }

    @Override
    public void resize(int width, int height) {
        scene.resize(width, height);
    }

    @Override
    public void show() {
        this.scene = game.getScene();

        fadeOverlayActor = new FadeOverlayActor(scene.getCamera());

        healthBarActor = new HealthBarActor(scene);
        healthBarActor.setVisible(false);

        bossHealthBarActor = new BossHealthBarActor();
        bossHealthBarActor.setVisible(false);

        shipPreviewActor = new ShipPreviewActor(scene);
        shipPreviewActor.setVisible(false);

        stage.addActor(healthBarActor);
        stage.addActor(bossHealthBarActor);
        stage.addActor(shipPreviewActor);
        stage.addActor(new DebugTextActor());
        stage.addAction(Actions.sequence(Actions.fadeOut(0f), Actions.fadeIn(1f)));
    }

    public void startGame(int level, int shipSelect) {
        final float duration = 0.5f;
        final PlayerShipVariant shipVariant = ShipPreviewActor.getVariant(shipSelect);

        AstroblazeGame.getMusicController().randomizeGameTrack();
        levelControllerActor = new LevelControllerActor(scene);
        levelControllerActor.setLevel(level);
        game.clearText();
        stage.addActor(levelControllerActor);
        stage.addAction(Actions.sequence(
                Actions.fadeOut(duration),
                Actions.delay(duration),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        scene.reset();
                    }
                }),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        healthBarActor.setVisible(true);
                    }
                }),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        bossHealthBarActor.setVisible(true);
                    }
                }),
                Actions.fadeIn(1f),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        scene.respawnPlayer(shipVariant);
                    }
                })));
    }

    public void stopGame() {
        if (!isGameRunning())
            return;

        final float duration = 0.5f;
        game.clearText();
        if (levelControllerActor != null) {
            levelControllerActor.remove();
            levelControllerActor = null;
        }
        bossHealthBarActor.setTrackedEnemy(null);
        stage.act(30f);
        stage.addAction(Actions.sequence(
                Actions.fadeOut(duration),
                Actions.delay(duration),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        scene.reset();
                    }
                }),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        healthBarActor.setVisible(false);
                    }
                }),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        bossHealthBarActor.setVisible(false);
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
