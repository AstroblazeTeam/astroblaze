package com.astroblaze;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;

public class AstroblazeGame extends Game {
    public GameScreen gameScreen;
    public LoadingScreen loadingScreen;
    public final InputMultiplexer inputMux = new InputMultiplexer();
    private final ArrayList<ILoadingFinishedListener> loadingFinishedListeners = new ArrayList<>(4);
    private final ArrayList<IPlayerStateChangeListener> playerStateChangeListeners = new ArrayList<>(4);
    private final ArrayList<IScoreChangeListener> scoreChangeListeners = new ArrayList<>(4);
    private MusicController musicController;
    private Scene3D scene;
    private GLProfiler profiler;
    private ModelBatch batch;
    private Preferences prefs;
    private IGUIRenderer guiRenderer;
    private ShipPreviewActor shipPreview;
    private float playerScore;
    private float playerMoney;

    public static Preferences getPrefs() {
        return getInstance().prefs;
    }

    private Assets assets;

    private static AstroblazeGame instance;

    public static AstroblazeGame getInstance() {
        return instance;
    }

    public float getPlayerMoney() {
        return playerMoney;
    }

    public float getPlayerScore() {
        return playerScore;
    }

    public void modPlayerScore(float mod) {
        this.playerScore += mod;
        modPlayerMoney(mod);
        reportScoreChanged();
        Gdx.app.log("AstroblazeGame", "Player score modded by " + mod + " to " + this.playerScore);
    }

    public void modPlayerMoney(float mod) {
        this.playerMoney += mod;
        if (Math.abs(mod) > 1000) {
            prefs.putFloat("score", playerScore);
            prefs.putFloat("money", playerMoney);
            prefs.flush();
        }
        reportScoreChanged();
        Gdx.app.log("AstroblazeGame", "Player money modded by " + mod + " to " + this.playerMoney);
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

    public IGUIRenderer getGuiRenderer() {
        return this.guiRenderer;
    }

    private int getUnlockedShips() {
        return prefs.getInteger("unlockedShips", 1);
    }

    public boolean isShipUnlocked(int shipId) {
        return (getUnlockedShips() & (1 << shipId)) == 1 << shipId;
    }

    public boolean canUnlock(PlayerShipVariant variant) {
        return variant != null && playerMoney >= variant.price;
    }

    public void unlockShip(PlayerShipVariant variant) {
        if (!canUnlock(variant)) {
            return;
        }
        modPlayerMoney(-variant.price);
        prefs.putInteger("unlockedShips", getUnlockedShips() | (1 << variant.id));
        prefs.flush();
        reportScoreChanged(); // refresh ui
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
        assets = new Assets(this); // IMPORTANT: make sure this is constructed first!

        gameScreen = new GameScreen(this);
        loadingScreen = new LoadingScreen(this);
        scene = new Scene3D(this);
        batch = new ModelBatch();

        Gdx.input.setInputProcessor(inputMux);

        assets.loadAssets(this.scene.getParticlesSystem());
        assets.finishLoadingAsset(Assets.uiSkin);
        assets.finishLoadingAsset(Assets.uiMusic);
        assets.finishLoadingAsset(Assets.logo);

        musicController = new MusicController(this);
        musicController.loadLoadingScreenAssets();

        profiler = new GLProfiler(Gdx.graphics);
        if (prefs.getBoolean("profiler", false)) {
            toggleProfiler();
        }

        setScreen(loadingScreen);
        playerScore = prefs.getFloat("score", 0f);
        playerMoney = prefs.getFloat("money", 0f);
        reportScoreChanged();
    }

    public void finishLoading() {
        for (ILoadingFinishedListener listener : loadingFinishedListeners) {
            listener.finishedLoadingAssets();
        }
        this.setScreen(this.gameScreen);
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

    public void setShipPreview(ShipPreviewActor shipPreview) {
        this.shipPreview = shipPreview;
    }

    public ShipPreviewActor getShipPreview() {
        return this.shipPreview;
    }

    public int getMaxLevel() {
        return prefs.getInteger("level", 0);
    }

    public void setMaxLevel(int level) {
        prefs.putInteger("level", level);
        prefs.flush();
    }

    public void handleBtnExtra2Click() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    public void handleBtnExtra1Click() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                scene.getPlayer().fireMissiles();
            }
        });
    }

    @Override
    public void render() {
        musicController.update(Gdx.graphics.getDeltaTime());

        if (Gdx.input.isTouched(3)) {
            toggleProfiler();
            modPlayerMoney(5000);
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
        if (scene.getPlayer() != null) {
            scene.getPlayer().stopMoving(true);
        }
        scene.setTimeScale(0f);

        prefs.putFloat("score", playerScore);
        prefs.putFloat("money", playerMoney);
        prefs.flush();
    }

    public void resumeGame() {
        playerScore = prefs.getFloat("score", 0f);
        playerMoney = prefs.getFloat("money", 0f);
        reportScoreChanged();
        scene.setTimeScale(1f);
    }

    public void addOnLoadingFinishedListener(ILoadingFinishedListener listener) {
        if (!this.loadingFinishedListeners.contains(listener))
            this.loadingFinishedListeners.add(listener);
    }

    public void addPlayerStateChangeListener(IPlayerStateChangeListener listener) {
        if (!this.playerStateChangeListeners.contains(listener))
            this.playerStateChangeListeners.add(listener);
    }

    public void removePlayerStateChangeListener(IPlayerStateChangeListener listener) {
        this.playerStateChangeListeners.remove(listener);
    }

    public void addScoreChangeListener(IScoreChangeListener listener) {
        if (!this.scoreChangeListeners.contains(listener))
            this.scoreChangeListeners.add(listener);
        reportScoreChanged();
    }

    public void removeScoreChangeListener(IScoreChangeListener listener) {
        this.scoreChangeListeners.remove(listener);
    }

    public void reportStateChanged() { // for ui refresh
        Ship player = scene.getPlayer();
        reportStateChanged(player, player.getHp(), player.getHp());
    }

    public void reportStateChanged(Ship ship, float newHp, float oldHp) {
        for (IPlayerStateChangeListener listener : playerStateChangeListeners) {
            listener.onHpChanged(ship, newHp, oldHp);
        }
    }

    public void reportExtrasChanged(Ship ship, String text1, String text2) {
        for (IPlayerStateChangeListener listener : playerStateChangeListeners) {
            listener.onSpecialTextChanged(ship, text1, text2);
        }
    }

    public void reportHpEnabled(Ship ship, boolean enabled) {
        Gdx.app.log("AstroblazeGame", "reportHpEnabled(" + enabled + ")");
        for (IPlayerStateChangeListener listener : playerStateChangeListeners) {
            listener.onHpEnabled(ship, enabled);
        }
    }

    public void reportScoreChanged() {
        for (IScoreChangeListener listener : scoreChangeListeners) {
            listener.scoreChanged(getPlayerMoney(), getPlayerScore());
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
