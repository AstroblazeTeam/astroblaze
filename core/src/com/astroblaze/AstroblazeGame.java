package com.astroblaze;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import java.util.ArrayList;
import java.util.Random;

public class AstroblazeGame extends Game {
    public interface ILoadingFinishedListener {
        void finished();
    }

    public final static int PIXELS_PER_METER = 50;

    public Skin skin;
    public ModelBatch batch;
    public GameScreen gameScreen;
    public LoadingScreen loadingScreen;
    public InputMultiplexer inputMux;
    private final ArrayList<ILoadingFinishedListener> loadingFinishedListeners = new ArrayList<>(4);
    private static Preferences prefs;

    public static Preferences getPrefs() {
        return prefs;
    }

    public static Random rng;
    public static Assets assets;

    private static AstroblazeGame instance;

    public static AstroblazeGame getInstance() {
        return instance;
    }

    public void addListener(ILoadingFinishedListener listener) {
        loadingFinishedListeners.add(listener);
    }

    public void finishLoading() {
        this.setScreen(this.gameScreen);
        for (ILoadingFinishedListener listener : loadingFinishedListeners) {
            listener.finished();
        }
    }

    @Override
    public void create() {
        instance = this;
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        prefs = Gdx.app.getPreferences("AnnelidWar");
        this.skin = new Skin(Gdx.files.internal("ui/clean-crispy-ui.json"));
        this.batch = new ModelBatch();
        this.inputMux = new InputMultiplexer();
        this.gameScreen = new GameScreen(this);
        loadingScreen = new LoadingScreen(this);

        Gdx.input.setInputProcessor(inputMux);

        rng = new Random();
        assets = new Assets();
        assets.loadAssets();
        assets.finishLoadingAsset(Assets.uiSkin);
        this.setScreen(loadingScreen);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        assets.dispose();
        assets = null;
        instance = null;
        batch.dispose();
    }
}
