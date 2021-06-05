package com.astroblaze;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.profiling.GLProfiler;

import java.util.ArrayList;
import java.util.Locale;

public class AstroblazeGame extends Game {
    public interface ILoadingFinishedListener {
        void finishedLoadingAssets();
    }

    public GameScreen gameScreen;
    public LoadingScreen loadingScreen;
    public final InputMultiplexer inputMux = new InputMultiplexer();
    private final ArrayList<ILoadingFinishedListener> loadingFinishedListeners = new ArrayList<>(4);
    private final MusicController musicController = new MusicController();
    private Scene3D scene;
    private GLProfiler profiler;
    private ModelBatch batch;
    private Preferences prefs;

    public static Preferences getPrefs() {
        return getInstance().prefs;
    }

    private final Assets assets = new Assets();

    private static AstroblazeGame instance;

    public static AstroblazeGame getInstance() {
        return instance;
    }

    public ModelBatch getBatch() {
        return this.batch;
    }

    public Scene3D getScene() {
        return this.scene;
    }

    public MusicController getMusicController() {
        return this.musicController;
    }

    public AstroblazeGame() {
        // don't put stuff here - most of GL isn't initialized yet
    }

    @Override
    public void create() {
        instance = this;
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        prefs = Gdx.app.getPreferences("AnnelidWar");
        this.gameScreen = new GameScreen(this);
        this.loadingScreen = new LoadingScreen(this);
        this.scene = new Scene3D(this);
        this.batch = new ModelBatch();

        Gdx.input.setInputProcessor(inputMux);

        assets.loadAssets(this.scene.getParticlesSystem());
        assets.finishLoadingAsset(Assets.uiSkin);
        assets.finishLoadingAsset(Assets.uiMusic);
        assets.finishLoadingAsset(Assets.logo);

        this.musicController.loadLoadingScreenAssets();

        this.profiler = new GLProfiler(Gdx.graphics);
        if (prefs.getBoolean("profiler", false)) {
            toggleProfiler();
        }

        this.setScreen(loadingScreen);
    }

    public void addOnLoadingFinishedListener(ILoadingFinishedListener listener) {
        loadingFinishedListeners.add(listener);
    }

    public void finishLoading() {
        Assets.bullets.clear();
        for (int i = 1; i < 11; i++) {
            Assets.bullets.add(Assets.asset(Assets.atlas).findRegion(String.format(Locale.US, "%02d", i)));
        }
        this.musicController.assignOtherAssets();
        this.setScreen(this.gameScreen);
        for (ILoadingFinishedListener listener : loadingFinishedListeners) {
            listener.finishedLoadingAssets();
        }
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

    public void handleBtnExtra2Click() {
        scene.getPlayer().reset();
    }

    public void handleBtnExtra1Click() {
        scene.getPlayer().modGunDamage(10f);
    }

    @Override
    public void render() {
        musicController.update(Gdx.graphics.getDeltaTime());

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

    public void pauseGame() {
        scene.setTimeScale(0f);
    }

    public void resumeGame() {
        scene.setTimeScale(1f);
    }

    @Override
    public void dispose() {
        assets.dispose();
        instance = null;
        batch.dispose();
    }
}
