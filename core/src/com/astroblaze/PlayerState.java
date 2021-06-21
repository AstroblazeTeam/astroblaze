package com.astroblaze;

import com.astroblaze.Interfaces.*;
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

    private static class PlayerStateData {
        public String id = UUID.randomUUID().toString();
        public String name = "Anonymous"; // not localized on purpose
        public float money;
        public float score;
        public int level; // max unlocked level
        public float lastScoreSubmitted;
        public boolean profilerEnabled;
        public boolean screenShake = true;
        public float soundVolume = 1f;
        public float musicVolume = 1f;
        public Date lastScoreSubmit = new Date(0);
        public ArrayList<UnlockedShip> unlockedShips = new ArrayList<>(4);
        public HashMap<Integer, ArrayList<UpgradeEntry>> unlockedUpgrades = new HashMap<>();
    }

    public static class UnlockedShip {
        public int id;
    }

    public String getId() {
        return data.id;
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

    public float getMusicVolume() {
        return data.musicVolume;
    }

    public void setSoundVolume(float newVolume) {
        data.soundVolume = newVolume;
        saveState();
    }

    public void setMusicVolume(float newVolume) {
        data.musicVolume = newVolume;
        saveState();
    }

    public boolean getScreenShake() {
        return data.screenShake;
    }

    public void setScreenShake(boolean screenShake) {
        data.screenShake = screenShake;
        saveState();
    }

    public int getMaxLevel() {
        return data.level;
    }

    public void setMaxLevel(int level) {
        data.level = level;
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
        }
        reportStateChanged();
        Gdx.app.log("AstroblazeGame", "Player score modded by " + mod + " to " + data.score);
    }

    public void modPlayerMoney(float mod) {
        if (MathUtils.isEqual(mod, 0, 0.1f)) {
            return;
        }
        data.money += mod;
        if (Math.abs(mod) > 1000f) {
            saveState();
        }
        reportStateChanged();
        Gdx.app.log("AstroblazeGame", "Player money modded by " + mod + " to " + data.money);
    }

    public ArrayList<UpgradeEntry> getUpgrades(int variantId) {
        return data.unlockedUpgrades.get(variantId);
    }

    public ArrayList<PlayerShipVariant> getUnlockedShips() {
        ArrayList<PlayerShipVariant> result = new ArrayList<>();
        PlayerShipVariant[] variants = PlayerShipVariant.values();
        for (int i = 0; i < data.unlockedShips.size(); i++) {
            result.add(variants[data.unlockedShips.get(i).id]);
        }
        return result;
    }

    public boolean isShipVariantUnlocked(PlayerShipVariant variant) {
        for (UnlockedShip s : data.unlockedShips) {
            if (s.id == variant.id) {
                return true;
            }
        }
        return false;
    }

    public boolean canUnlockShip(PlayerShipVariant variant) {
        return data.money >= variant.price;
    }

    public boolean unlockShipVariant(PlayerShipVariant variant) {
        if (!canUnlockShip(variant) || isShipVariantUnlocked(variant))
            return false;
        UnlockedShip unlocked = new UnlockedShip();
        unlocked.id = variant.id;
        modPlayerMoney(-variant.price);
        data.unlockedShips.add(unlocked);
        ArrayList<UpgradeEntry> upgrades = new ArrayList<>();

        upgrades.add(new UpgradeEntry("Shield", 1f, 0, 5, 0.1f, 3000f, UpgradeEntryType.ShieldUpgrade));
        upgrades.add(new UpgradeEntry("Damage", 1f, 0, 5, 0.1f, 5000f, UpgradeEntryType.DamageUpgrade));
        upgrades.add(new UpgradeEntry("Speed", 1f, 0, 5, 0.1f, 10000f, UpgradeEntryType.SpeedUpgrade));

        data.unlockedUpgrades.put(unlocked.id, upgrades);
        saveState();
        reportStateChanged();
        AstroblazeGame.getSoundController().playPurchaseSound();
        return true;
    }

    public boolean canBuyUpgrade(PlayerShipVariant variant, UpgradeEntry item) {
        return isShipVariantUnlocked(variant)
                && item.currentTier < item.maxTier
                && data.money >= item.price;
    }

    private UnlockedShip getUnlockedShip(PlayerShipVariant variant) {
        for (UnlockedShip s : data.unlockedShips) {
            if (s.id == variant.id) {
                return s;
            }
        }
        return null;
    }

    public boolean buyUpgrade(PlayerShipVariant variant, UpgradeEntry item) {
        if (!canBuyUpgrade(variant, item)) {
            return false;
        }
        UnlockedShip unlocked = getUnlockedShip(variant);
        if (unlocked == null) {
            Gdx.app.error("PlayerState", "Ship " + variant + " is not unlocked yet!");
            return false;
        }
        item.currentTier = MathUtils.clamp(item.currentTier + 1, 0, item.maxTier);
        modPlayerMoney(-item.price);
        AstroblazeGame.getSoundController().playPurchaseSound();
        saveState();
        return true;
    }

    public void saveState() {
        String stateString = gson.toJson(data);
        Preferences prefs = Gdx.app.getPreferences("Astroblaze");
        prefs.putString("playerState", stateString);
        prefs.flush();
        Gdx.app.log("PlayerState", "Saved state");
    }

    public void restoreState() {
        String stateString = Gdx.app.getPreferences("Astroblaze").getString("playerState");
        if (stateString == null || stateString.trim().equals("")) {
            // init fresh state
            data = new PlayerStateData();
            Gdx.app.log("PlayerState", "Initialized fresh state.");
            unlockShipVariant(PlayerShipVariant.Scout);
        } else {
            data = gson.fromJson(stateString, PlayerStateData.class);
            Gdx.app.log("PlayerState", "Restored state: " + stateString);
        }
    }

    private void reportStateChanged() {
        for (IPlayerStateChangedListener listener : playerStateChangeListeners) {
            listener.onStateChanged(this);
        }
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
