package com.astroblaze.GdxScreens;

import com.astroblaze.*;
import com.astroblaze.Utils.MathHelper;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.StretchViewport;

public class LoadingScreen extends ScreenAdapter {
    private final AstroblazeGame game;
    private final float lerpSpeed = 5f;

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
        final float p = Assets.getInstance().getProgress();
        pgLoading.setValue(MathUtils.lerp(pgLoading.getValue(), p, lerpSpeed * delta));
        if (!loaded && Assets.getInstance().update(8) && pgLoading.getValue() >= 0.95f) {
            loaded = true;
            Gdx.app.log("LoadingScreen", "Assets loaded in " + loadingTime + " secs.");
            pgLoading.addAction(Actions.sequence(
                    Actions.fadeOut(1f),
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
        Table layout = new Table(Assets.asset(Assets.uiSkin));
        layout.setFillParent(true);
        Image image = new Image(Assets.asset(Assets.logo));
        layout.add(image).center().padBottom(32f * Gdx.graphics.getDensity());
        layout.row();
        image.setScaling(Scaling.fit);

        pgLoading = new ProgressBar(0, 1f, 0.001f, false, Assets.asset(Assets.uiSkin));
        float h = 16 * Gdx.graphics.getDensity();
        pgLoading.setPosition(viewport.getScreenWidth() * 0.25f, h);
        pgLoading.setSize(viewport.getScreenWidth() * 0.5f, pgLoading.getHeight());
        layout.add(pgLoading).fillX().padBottom(32f * Gdx.graphics.getDensity());
        stage.addActor(layout);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}