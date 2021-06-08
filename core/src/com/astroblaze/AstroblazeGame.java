package com.astroblaze;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;

public class AstroblazeGame extends Game {
    public GameScreen gameScreen;
    public LoadingScreen loadingScreen;
    public final InputMultiplexer inputMux = new InputMultiplexer();
    private final ArrayList<ILoadingFinishedListener> loadingFinishedListeners = new ArrayList<>(4);
    private final ArrayList<IHpChangeListener> hpChangeListeners = new ArrayList<>(4);
    private final MusicController musicController = new MusicController();
    private Scene3D scene;
    private GLProfiler profiler;
    private ModelBatch batch;
    private Preferences prefs;
    private IGUIRenderer guiRenderer;

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

    public void setGuiRenderer(IGUIRenderer guiRenderer) {
        this.guiRenderer = guiRenderer;
    }

    public AstroblazeGame() {
        instance = this;
        // don't put stuff here - most of GL isn't initialized yet
    }

    @Override
    public void create() {
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

    public void finishLoading() {
        Assets.getInstance().finalizeLoading();
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
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                scene.getPlayer().fireMissiles();
            }
        });
    }

    public void handleBtnExtra1Click() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                scene.getPlayer().missileSalvo += 1;
            }
        });
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
        scene.getPlayer().stopMoving(true);
        scene.setTimeScale(0f);
    }

    public void resumeGame() {
        scene.setTimeScale(1f);
    }

    public void addOnLoadingFinishedListener(ILoadingFinishedListener listener) {
        if (!this.loadingFinishedListeners.contains(listener))
            this.loadingFinishedListeners.add(listener);
    }

    public void addHpChangeListener(IHpChangeListener listener) {
        if (!this.hpChangeListeners.contains(listener))
            this.hpChangeListeners.add(listener);
    }

    public void reportHpChanged(Ship ship, float newHp, float oldHp) {
        for (IHpChangeListener listener : hpChangeListeners) {
            listener.onHpChanged(ship, newHp, oldHp);
        }
    }

    public void reportHpEnabled(Ship ship, boolean enabled) {
        Gdx.app.log("AstroblazeGame", "reportHpEnabled(" + enabled + ")");
        for (IHpChangeListener listener : hpChangeListeners) {
            listener.onHpEnabled(ship, enabled);
        }
    }

    public boolean isDebugging() {
        return this.profiler.isEnabled();
    }

    public void clearText() {
        if (guiRenderer == null) {
            Gdx.app.log("AstroblazeGame", "renderTextAt with null reference, too early?");
            return;
        }
        for (int i = 0; i < 2; i++) {
            guiRenderer.renderText(i, "", 24f, 0f, 0f);
        }
    }

    public void renderText(int idx, String txt) {
        if (guiRenderer == null) {
            Gdx.app.log("AstroblazeGame", "renderTextAt with null reference, too early?");
            return;
        }
        guiRenderer.renderText(idx, txt, 24f,
                Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.5f);
    }

    public void renderText(int idx, String txt, float x, float y) {
        if (guiRenderer == null) {
            Gdx.app.log("AstroblazeGame", "renderTextAt with null reference, too early?");
            return;
        }
        guiRenderer.renderText(idx, txt, 24f, x, Gdx.graphics.getHeight() - y);
    }

    public void renderTextAt(int idx, String txt, Renderable renderable) {
        if (guiRenderer == null) {
            Gdx.app.log("AstroblazeGame", "renderTextAt with null reference, too early?");
            return;
        }
        renderTextAt(idx, txt, renderable, 0f, 0f);
    }

    public void renderTextAt(int idx, String txt, Renderable renderable, float offsetX, float offsetY) {
        if (guiRenderer == null || scene.getCamera() == null || renderable == null) {
            Gdx.app.log("AstroblazeGame", "renderTextAt with null reference, too early?");
            return;
        }
        Vector3 screenPos = scene.getCamera().project(renderable.position.cpy());
        guiRenderer.renderText(idx, txt, 24f,
                screenPos.x + offsetX, Gdx.graphics.getHeight() - screenPos.y + offsetY);
    }

    @Override
    public void dispose() {
        getPrefs().flush();
        assets.dispose();
        instance = null;
        batch.dispose();
    }
}
