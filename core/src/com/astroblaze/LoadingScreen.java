package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.utils.viewport.StretchViewport;

public class LoadingScreen extends ScreenAdapter {
    private final AstroblazeGame game;

    private Stage stage;
    private ProgressBar pgLoading;
    private float loadingTime = 0;

    public LoadingScreen(AstroblazeGame game) {
        this.game = game;
    }

    @Override
    public void render(float delta) {
        float minLoadingTime = 1f;
        if ((loadingTime > minLoadingTime) && (AstroblazeGame.assets.update(10))) {
            game.setScreen(game.gameScreen);
            return;
        }
        loadingTime += delta;
        pgLoading.setValue(AstroblazeGame.assets.getProgress());
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
        Image image = new Image(Assets.asset(Assets.loadingImage));
        image.setPosition(
                (viewport.getScreenWidth() / 2f) - (image.getWidth() / 2),
                (viewport.getScreenHeight() / 2f) - (image.getHeight() / 2));
        stage.addActor(image);

        pgLoading = new ProgressBar(0, 1f, 0.1f, false, Assets.asset(Assets.uiSkin));
        pgLoading.setPosition(0, 0);
        pgLoading.setSize(viewport.getScreenWidth(), 64);
        stage.addActor(pgLoading);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}