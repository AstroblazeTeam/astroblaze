package com.astroblaze;

import com.astroblaze.Interfaces.ILoadingFinishedListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class SoundController implements ILoadingFinishedListener {
    private final PlayerState state;

    private Sound soundExplosion;
    private Sound soundPurchase;

    private float volume;

    SoundController(AstroblazeGame game) {
        game.addOnLoadingFinishedListener(this);
        state = AstroblazeGame.getPlayerState();
        volume = state.getSoundVolume();
    }

    @Override
    public void finishedLoadingAssets() {
        soundExplosion = Assets.asset(Assets.soundExplosion);
        soundPurchase = Assets.asset(Assets.soundPurchase);
    }

    public void playExplosionSound() {
        playSound(soundExplosion);
    }

    public void playPurchaseSound() {
        playSound(soundPurchase);
    }

    private void playSound(Sound sfx) {
        sfx.play(volume);
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float newVolume) {
        volume = newVolume;
        state.setSoundVolume(newVolume);
        Gdx.app.log("SoundManager", "Set sound volume to " + this.volume);
    }
}
