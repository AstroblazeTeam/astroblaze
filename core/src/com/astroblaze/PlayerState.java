package com.astroblaze;

import com.astroblaze.Interfaces.*;
import com.astroblaze.Utils.Debouncer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.MathUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class PlayerState {
    private final transient ArrayList<IPlayerStateChangedListener> playerStateChangeListeners = new ArrayList<>(4);
    private final transient static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private PlayerStateData data = new PlayerStateData();
    private final Debouncer debouncer = new Debouncer();
    private final Runnable stateChangedUpdaterRunnable = new Runnable() {
        @Override
        public void run() {
            for (IPlayerStateChangedListener listener : playerStateChangeListeners) {
                listener.onStateChanged(PlayerState.this);
            }
        }
    };

    private static class PlayerStateData {
        public String id = UUID.randomUUID().toString();
        public String name = "Anonymous"; // not localized on purpose
        public float money;
        public float score;
        public int seed; // seed for things such as level select, level reward randomizer etc
        public int level; // max unlocked level
        public int selectedShip; // last selected ship
        public float lastScoreSubmitted;
        public boolean profilerEnabled;
        public boolean screenFlip = false;
        public boolean screenShake = true;
        public boolean vibrate = true;
        public float soundVolume = 0.5f;
        public float uiVolume = 0.5f;
        public float musicVolume = 0.5f;
        public Date lastScoreSubmit = new Date(0);
        public HashMap<Integer, ArrayList<UpgradeEntry>> ownedShips = new HashMap<>();
    }

    public String getId() {
        return data.id;
    }

    public int getSeed() {
        return data.seed;
    }

    public String getName() {
        return data.name;
    }

    public void setName(String name) {
        data.name = name;
        saveState();
    }

    public boolean shouldSubmitScore() {
        if (data.lastScoreSubmitted != data.score) {
            return true;
        }
        final long diff = new Date().getTime() - data.lastScoreSubmit.getTime();
        return data.score > 0 && TimeUnit.MILLISECONDS.toMinutes(diff) > 5;
    }

    public void submittedScore() {
        data.lastScoreSubmit = new Date();
        saveState();
    }

    public boolean isProfilerEnabled() {
        return data.profilerEnabled;
    }

    public void setProfilerEnabled(boolean enabled) {
        data.profilerEnabled = enabled;
        saveState();
    }

    public float getSoundVolume() {
        return data.soundVolume;
    }

    public void setSoundVolume(float newVolume) {
        data.soundVolume = newVolume;
        saveState();
    }

    public float getMusicVolume() {
        return data.musicVolume;
    }

    public void setMusicVolume(float newVolume) {
        data.musicVolume = newVolume;
        saveState();
    }

    public float getUiVolume() {
        return data.uiVolume;
    }

    public void setUiVolume(float newVolume) {
        data.uiVolume = newVolume;
        saveState();
    }

    public boolean getScreenFlip() {
        return data.screenFlip;
    }

    public void setScreenFlip(boolean screenFlip) {
        data.screenFlip = screenFlip;
        saveState();
        AstroblazeGame.getInstance().setFlipHorizontal(screenFlip);
    }

    public boolean getScreenShake() {
        return data.screenShake;
    }

    public void setScreenShake(boolean screenShake) {
        data.screenShake = screenShake;
        saveState();
        if (data.screenShake)
            AstroblazeGame.getInstance().getScene().getCamera().shake();
    }

    public boolean getVibrate() {
        return data.vibrate;
    }

    public void setVibrate(boolean vibrate) {
        data.vibrate = vibrate;
        saveState();
        AstroblazeGame.getInstance().getScene().getCamera().vibrate();
    }

    public int getMaxLevel() {
        return data.level;
    }

    public void setMaxLevel(int level) {
        data.level = level;
        saveState();
    }

    public int getLastSelectedShip() {
        return data.selectedShip;
    }

    public void setLastSelectedShip(int newValue) {
        data.selectedShip = newValue;
        saveState();
    }

    public float getPlayerMoney() {
        return data.money;
    }

    public float getPlayerScore() {
        return data.score;
    }

    public void modPlayerScore(float mod) {
        data.score += mod;
        modPlayerMoney(mod);
        if (Math.abs(mod) > 1000f) {
            saveState();
        } else { // small sum, just update UI
            reportStateChanged();
        }
        Gdx.app.log("AstroblazeGame", "Player score modded by " + mod + " to " + data.score);
    }

    public void modPlayerMoney(float mod) {
        if (MathUtils.isEqual(mod, 0, 0.1f)) {
            return;
        }
        data.money += mod;
        if (Math.abs(mod) > 1000f) {
            saveState();
        } else { // small sum, just update UI
            reportStateChanged();
        }
        Gdx.app.log("AstroblazeGame", "Player money modded by " + mod + " to " + data.money);
    }

    public ArrayList<UpgradeEntry> getUpgrades(int variantId) {
        return data.ownedShips.get(variantId);
    }

    public ArrayList<PlayerShipVariant> getUnlockedShips() {
        ArrayList<PlayerShipVariant> result = new ArrayList<>();
        PlayerShipVariant[] variants = PlayerShipVariant.values();
        for (int shipId : data.ownedShips.keySet()) {
            result.add(variants[shipId]);
        }
        return result;
    }

    public boolean isShipOwned(PlayerShipVariant variant) {
        for (int shipId : data.ownedShips.keySet()) {
            if (variant.id == shipId) {
                return true;
            }
        }
        return false;
    }

    public boolean canBuyShip(PlayerShipVariant variant) {
        return data.money >= variant.price;
    }

    public void buyShip(PlayerShipVariant variant) {
        if (!canBuyShip(variant)) {
            Gdx.app.log("PlayerState", "Player can't buy ship but buyShipVariant was called");
            return;
        }
        if (isShipOwned(variant)) {
            Gdx.app.log("PlayerState", "Player already owns variant " + variant.id);
            return;
        }
        modPlayerMoney(-variant.price);
        ArrayList<UpgradeEntry> upgrades = new ArrayList<>();
        fillUpgrades(upgrades);

        data.ownedShips.put(variant.id, upgrades);
        saveState();

        if (AstroblazeGame.getSoundController() != null) {
            // avoid NPE when initializing new player state
            AstroblazeGame.getSoundController().playUIPurchaseSound();
        }
    }

    private void addUpgradeUnlessExists(ArrayList<UpgradeEntry> upgrades, UpgradeEntry upgrade) {
        for (UpgradeEntry u : upgrades) {
            if (u.type == upgrade.type)
                return;
        }
        upgrades.add(upgrade);
    }

    private void fillUpgrades(ArrayList<UpgradeEntry> upgrades) {
        addUpgradeUnlessExists(upgrades,
                new UpgradeEntry(UpgradeEntryType.ShieldUpgrade, 0, 5, 0.1f, 3000f, 0.01f, 10000f));
        addUpgradeUnlessExists(upgrades,
                new UpgradeEntry(UpgradeEntryType.DamageUpgrade, 0, 5, 0.1f, 5000f, 0.01f, 10000f));
        addUpgradeUnlessExists(upgrades,
                new UpgradeEntry(UpgradeEntryType.SpeedUpgrade, 0, 5, 0.1f, 10000f, 0.01f, 10000f));
        addUpgradeUnlessExists(upgrades,
                new UpgradeEntry(UpgradeEntryType.TurretSpeed, 0, 5, 0.1f, 10000f, 0.0f, 10000f));
        addUpgradeUnlessExists(upgrades,
                new UpgradeEntry(UpgradeEntryType.LaserCapacity, 0, 8, 0.25f, 10000f, 0f, 0f));
        addUpgradeUnlessExists(upgrades,
                new UpgradeEntry(UpgradeEntryType.MaxMissiles, 0, 8, 0.25f, 10000f, 0f, 0f));
    }

    public boolean canBuyUpgrade(PlayerShipVariant variant, UpgradeEntry item) {
        return isShipOwned(variant)
                && data.money >= item.getUpgradePrice()
                && (item.currentTier < item.maxTier || item.multiplierExtra > 0f);
    }

    private boolean isShipUnlocked(PlayerShipVariant variant) {
        for (int shipId : data.ownedShips.keySet()) {
            if (shipId == variant.id) {
                return true;
            }
        }
        return false;
    }

    public boolean buyUpgrade(PlayerShipVariant variant, UpgradeEntry item) {
        if (!canBuyUpgrade(variant, item)) {
            return false;
        }
        if (!isShipUnlocked(variant)) {
            Gdx.app.error("PlayerState", "Ship " + variant + " is not unlocked yet!");
            return false;
        }
        modPlayerMoney(-item.getUpgradePrice());
        item.currentTier++;
        AstroblazeGame.getSoundController().playUIPurchaseSound();
        saveState();
        return true;
    }

    public void saveState() {
        String stateString = gson.toJson(data);
        Preferences prefs = Gdx.app.getPreferences("Astroblaze");
        prefs.putString("playerState", stateString);
        prefs.flush();
        Gdx.app.log("PlayerState", "Saved state");
        reportStateChanged();
    }

    public void restoreState() {
        String stateString = Gdx.app.getPreferences("Astroblaze").getString("playerState");
        if (stateString == null || stateString.trim().equals("")) {
            // init fresh state
            data = new PlayerStateData();
            data.seed = MathUtils.random(1 << 24);
            Gdx.app.log("PlayerState", "Initialized fresh state.");
            buyShip(PlayerShipVariant.Shuttle);
        } else {
            data = gson.fromJson(stateString, PlayerStateData.class);
            AstroblazeGame.getInstance().setFlipHorizontal(data.screenFlip);
            for (ArrayList<UpgradeEntry> upgrades : data.ownedShips.values()) {
                fillUpgrades(upgrades); // add new upgrades for old saves
            }
            Gdx.app.log("PlayerState", "Restored existing state.");
        }
    }

    private void reportStateChanged() {
        debouncer.debounce(PlayerState.class, stateChangedUpdaterRunnable, 100, TimeUnit.MILLISECONDS);
    }

    public void addPlayerStateChangeListener(IPlayerStateChangedListener listener) {
        if (!playerStateChangeListeners.contains(listener))
            playerStateChangeListeners.add(listener);
        reportStateChanged();
    }

    public void removePlayerStateChangeListener(IPlayerStateChangedListener listener) {
        playerStateChangeListeners.remove(listener);
    }
}
