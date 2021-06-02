package com.astroblaze;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.profiling.GLProfiler;

import java.util.ArrayList;

public class AstroblazeGame extends Game {
    public interface ILoadingFinishedListener {
        void finishedLoadingAssets();
    }

    public ModelBatch batch;
    public GameScreen gameScreen;
    public LoadingScreen loadingScreen;
    public InputMultiplexer inputMux;
    public final MusicManager MusicManager = new MusicManager();
    private Scene3D scene;
    private GLProfiler profiler;
    private final ArrayList<ILoadingFinishedListener> loadingFinishedListeners = new ArrayList<>(4);
    private static Preferences prefs;

    public static Preferences getPrefs() {
        return prefs;
    }

    public static Assets assets;

    private static AstroblazeGame instance;

    public static AstroblazeGame getInstance() {
        return instance;
    }

    public Scene3D getScene() {
        return this.scene;
    }

    public AstroblazeGame() {
        // don't put stuff here - most of GL isn't initialized yet
    }

    private void toggleProfiler() {
        if (profiler.isEnabled()) {
            profiler.disable();
            DebugTextDrawer.setExtraReport("");
        } else {
            profiler.enable();
        }
        prefs.putBoolean("profiler", profiler.isEnabled());
        prefs.flush();
    }

    @Override
    public void render() {
        MusicManager.update(Gdx.graphics.getDeltaTime());

        if (Gdx.input.isTouched(3)) {
            toggleProfiler();
        }

        if (profiler.isEnabled()) {
            profiler.reset();
        }

        super.render();

        if (profiler.isEnabled()) {
            String extra = "draw calls: " + profiler.getDrawCalls() +
                    "\nGL calls: " + profiler.getCalls() +
                    "\ntexture bindings: " + profiler.getTextureBindings() +
                    "\nshader switches:  " + profiler.getShaderSwitches() +
                    "\nvertices: " + (int) profiler.getVertexCount().value;
            DebugTextDrawer.setExtraReport(extra);
        }
    }

    public void addOnLoadingFinishedListener(ILoadingFinishedListener listener) {
        loadingFinishedListeners.add(listener);
    }

    public void finishLoading() {
        this.MusicManager.assignOtherAssets();
        this.setScreen(this.gameScreen);
        for (ILoadingFinishedListener listener : loadingFinishedListeners) {
            listener.finishedLoadingAssets();
        }
    }

    @Override
    public void create() {
        instance = this;
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        prefs = Gdx.app.getPreferences("AnnelidWar");
        this.batch = new ModelBatch();
        this.inputMux = new InputMultiplexer();
        this.gameScreen = new GameScreen(this);
        this.loadingScreen = new LoadingScreen(this);
        this.scene = new Scene3D(this);

        Gdx.input.setInputProcessor(inputMux);

        assets = new Assets();
        assets.loadAssets(this.scene.getParticlesSystem());
        assets.finishLoadingAsset(Assets.uiSkin);
        assets.finishLoadingAsset(Assets.uiMusic);
        assets.finishLoadingAsset(Assets.logo);

        this.MusicManager.loadLoadingScreenAssets();

        this.profiler = new GLProfiler(Gdx.graphics);
        if (prefs.getBoolean("profiler", false)) {
            toggleProfiler();
        }

        this.setScreen(loadingScreen);
    }

    public void pauseGame() {
        scene.setTimeScale(0f);
    }

    public void resumeGame() {
        scene.setTimeScale(1f);
    }

    @Override
    public void dispose() {
        assets.dispose();
        assets = null;
        instance = null;
        batch.dispose();
    }
}
