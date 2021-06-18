package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.MathUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.graalvm.compiler.loop.MathUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayerState {
    private final transient ArrayList<IPlayerStateChangedListener> playerStateChangeListeners = new ArrayList<>(4);
    private final transient static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private PlayerStateData data = new PlayerStateData();

    private class PlayerStateData {
        public String name = "Anonymous"; // not localized on purpose
        public float money;
        public float score;
        public ArrayList<UnlockedShip> unlockedShips = new ArrayList<>(4);
        public HashMap<Integer, ArrayList<ShopItem>> unlockedUpgrades = new HashMap<>();

    }

    public static class UnlockedShip {
        public int id;
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
        if (Math.abs(mod) > 1000) {
            saveState();
        }
        reportStateChanged();
        Gdx.app.log("AstroblazeGame", "Player score modded by " + mod + " to " + data.score);
    }

    public void modPlayerMoney(float mod) {
        data.money += mod;
        if (Math.abs(mod) > 1000) {
            saveState();
        }
        reportStateChanged();
        Gdx.app.log("AstroblazeGame", "Player money modded by " + mod + " to " + data.money);
    }

    public ArrayList<ShopItem> getUpgrades(int variantId) {
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
        ArrayList<ShopItem> upgrades = new ArrayList<>();

        upgrades.add(new ShopItem("Shield", 1f, 0, 5, 0.1f, 3000f, ShopItemType.ShieldUpgrade));
        upgrades.add(new ShopItem("Damage", 1f, 0, 5, 0.1f, 30000f, ShopItemType.DamageUpgrade));
        upgrades.add(new ShopItem("Speed", 1f, 0, 5, 0.1f, 3000000f, ShopItemType.SpeedUpgrade));

        data.unlockedUpgrades.put(unlocked.id, upgrades);
        saveState();
        reportStateChanged();
        return true;
    }

    public boolean canBuyUpgrade(PlayerShipVariant variant, ShopItem item) {
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

    public boolean buyUpgrade(PlayerShipVariant variant, ShopItem item) {
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
        if (stateString == null || stateString.trim().equals(""))
            stateString = "{}";
        data = gson.fromJson(stateString, PlayerStateData.class);
        if (data.unlockedUpgrades == null)
            data.unlockedUpgrades = new HashMap<>();
        if (data.unlockedShips == null || data.unlockedShips.size() == 0) {
            data.unlockedShips = new ArrayList<>();
            unlockShipVariant(PlayerShipVariant.Scout);
        }
        Gdx.app.log("PlayerState", "Restored state: " + stateString);
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
