package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.utils.viewport.StretchViewport;

public class LoadingScreen extends ScreenAdapter {
    private final AstroblazeGame game;

    private Stage stage;
    private ProgressBar pgLoading;
    private float loadingTime = 0f;
    private boolean loaded = false;

    public LoadingScreen(AstroblazeGame game) {
        this.game = game;
    }

    @Override
    public void render(float delta) {
        loadingTime += delta;

        final float minLoadingTime = 2f;
        float p = AstroblazeGame.assets.getProgress();
        pgLoading.setValue(p < 1f ? MathUtils.lerp(pgLoading.getValue(), p, 0.1f * delta) : 1f);
        if (AstroblazeGame.assets.update() && !loaded) {
            loaded = true;
            Gdx.app.log("LoadingScreen", "Assets loaded in " + loadingTime + " secs.");
            pgLoading.addAction(Actions.sequence(
                    Actions.fadeOut(0.5f),
                    Actions.delay(MathUtils.clamp(minLoadingTime - loadingTime, 0f, minLoadingTime)),
                    new RunnableAction() {
                        @Override
                        public void run() {
                            stage.addAction(Actions.sequence(
                                    Actions.delay(MathUtils.clamp(minLoadingTime - loadingTime, 0f, minLoadingTime)),
                                    Actions.fadeOut(0.5f),
                                    new RunnableAction() {
                                        @Override
                                        public void run() {
                                            game.finishLoading();
                                        }
                                    }));
                        }
                    }));
            return;
        }
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, false);
    }

    @Override
    public void show() {
        StretchViewport viewport = new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage = new Stage(viewport);
        stage.addAction(Actions.sequence(Actions.fadeOut(0f), Actions.fadeIn(0.5f)));
        Image image = new Image(Assets.asset(Assets.logo));
        image.setPosition(
                (viewport.getScreenWidth() / 2f) - (image.getWidth() / 2),
                (viewport.getScreenHeight() / 2f) - (image.getHeight() / 2));
        stage.addActor(image);

        pgLoading = new ProgressBar(0, 1f, 0.1f, false, Assets.asset(Assets.uiSkin));
        float h = 16 * Gdx.graphics.getDensity();
        pgLoading.setPosition(viewport.getScreenWidth() * 0.25f, h);
        pgLoading.setSize(viewport.getScreenWidth() * 0.5f, pgLoading.getHeight());
        stage.addActor(pgLoading);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}