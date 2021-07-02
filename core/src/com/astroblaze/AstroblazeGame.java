package com.astroblaze;

import com.astroblaze.GdxActors.*;
import com.astroblaze.GdxScreens.*;
import com.astroblaze.Interfaces.*;
import com.astroblaze.Rendering.*;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;

public class AstroblazeGame extends Game implements ILoadingFinishedListener {
    public GameScreen gameScreen;
    public LoadingScreen loadingScreen;

    private static final PlayerState playerState = new PlayerState();
    private static AstroblazeGame instance;

    private final ArrayList<ILoadingFinishedListener> loadingFinishedListeners = new ArrayList<>(4);
    private final ArrayList<IUIChangeListener> uiChangeListeners = new ArrayList<>(4);
    private final InputMultiplexer inputMux = new InputMultiplexer();

    private SoundController soundController;
    private MusicController musicController;
    private Scene3D scene;
    private final LevelStatTracker statTracker = new LevelStatTracker();
    private GLProfiler profiler;
    private ModelBatch batch;
    private IGUIRenderer guiRenderer;
    private Assets assets;

    public static PlayerState getPlayerState() {
        return playerState;
    }

    public static AstroblazeGame getInstance() {
        return instance;
    }

    public ModelBatch getBatch() {
        return this.batch;
    }

    public Scene3D getScene() {
        return this.scene;
    }

    public static LevelStatTracker getLevelStatTracker() {
        return instance.statTracker;
    }

    public static LevelControllerActor getLevelController() {
        return instance.gameScreen.getLevelController();
    }

    public static SoundController getSoundController() {
        return instance.soundController;
    }

    public static MusicController getMusicController() {
        return instance.musicController;
    }

    public IGUIRenderer getGuiRenderer() {
        return this.guiRenderer;
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
        Gdx.app.setLogLevel(Application.LOG_INFO);
        playerState.restoreState();
        assets = new Assets(this); // IMPORTANT: make sure this is constructed first!
        addOnLoadingFinishedListener(this);

        gameScreen = new GameScreen(this);
        loadingScreen = new LoadingScreen(this);
        scene = new Scene3D(this);
        batch = new ModelBatch();

        Gdx.input.setInputProcessor(inputMux);

        assets.loadLoadingScreenAssets();
        assets.finishLoadingAsset(Assets.uiSkin);
        assets.finishLoadingAsset(Assets.uiMusic);
        assets.finishLoadingAsset(Assets.logo);

        soundController = new SoundController(this);
        musicController = new MusicController(this);
        musicController.loadLoadingScreenAssets();

        profiler = new GLProfiler(Gdx.graphics);
        if (playerState.isProfilerEnabled()) {
            toggleProfiler();
        }

        setScreen(loadingScreen);
        assets.loadAssets(this.scene.getParticlesSystem());
    }

    public void finishLoading() {
        for (ILoadingFinishedListener listener : loadingFinishedListeners) {
            listener.finishedLoadingAssets();
        }
    }

    @Override
    public void finishedLoadingAssets() {
        this.setScreen(this.gameScreen);
    }

    private void toggleProfiler() {
        if (profiler.isEnabled()) {
            profiler.disable();
            DebugTextActor.setExtraReport("");
        } else {
            profiler.enable();
        }
        playerState.setProfilerEnabled(profiler.isEnabled());
    }

    @Override
    public void render() {
        musicController.update(Gdx.graphics.getDeltaTime());

        if (Gdx.input.isTouched(3)) { // profiler/cheat mode with 4 finger tap
            toggleProfiler();
            playerState.modPlayerMoney(5000);
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
            DebugTextActor.setExtraReport(extra);
        }
    }

    public void pauseGame() {
        if (scene.getPlayer() != null) {
            scene.getPlayer().stopMoving(true);
        }
        playerState.saveState();
        scene.setTimeScale(0f);
    }

    public void resumeGame() {
        scene.setTimeScale(1f);
    }

    public void addOnLoadingFinishedListener(ILoadingFinishedListener listener) {
        if (!this.loadingFinishedListeners.contains(listener))
            this.loadingFinishedListeners.add(listener);
    }

    public void addUIChangeListener(IUIChangeListener listener) {
        if (!this.uiChangeListeners.contains(listener))
            this.uiChangeListeners.add(listener);
    }

    public void removeUIChangeListener(IUIChangeListener listener) {
        this.uiChangeListeners.remove(listener);
    }

    public void reportStateChanged(PlayerShip playerShip, float newHp, float oldHp) {
        for (IUIChangeListener listener : uiChangeListeners) {
            listener.onHpChanged(playerShip, newHp, oldHp);
        }
    }

    public void reportExtrasChanged(PlayerShip playerShip, String text1, String text2) {
        for (IUIChangeListener listener : uiChangeListeners) {
            listener.onSpecialTextChanged(playerShip, text1, text2);
        }
    }

    public void reportHpEnabled(PlayerShip playerShip, boolean enabled) {
        Gdx.app.log("AstroblazeGame", "reportHpEnabled(" + enabled + ")");
        for (IUIChangeListener listener : uiChangeListeners) {
            listener.onHpEnabled(playerShip, enabled);
        }
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
        Vector3 screenPos = scene.getCamera().project(renderable.getPosition().cpy());
        guiRenderer.renderText(idx, txt, 24f,
                screenPos.x + offsetX, Gdx.graphics.getHeight() - screenPos.y + offsetY);
    }

    @Override
    public void dispose() {
        assets.dispose();
        instance = null;
        batch.dispose();
    }
}
