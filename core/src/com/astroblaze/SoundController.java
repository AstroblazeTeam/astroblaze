package com.astroblaze;

import com.astroblaze.Interfaces.ILoadingFinishedListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;

public class SoundController implements ILoadingFinishedListener {
    private final PlayerState state;

    private Sound soundExplosion;
    private Sound soundPurchase;
    private Sound soundBonus;
    private Sound soundBleep;
    private Sound soundBoop;
    private Sound soundConfirm;
    private Sound soundSwap;

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
        soundBonus = Assets.asset(Assets.soundBonus);
        soundBleep = Assets.asset(Assets.soundBleep);
        soundBoop = Assets.asset(Assets.soundBoop);
        soundConfirm = Assets.asset(Assets.soundConfirm);
        soundSwap = Assets.asset(Assets.soundSwap);
    }

    public void playExplosionSound() {
        playSound(soundExplosion);
    }

    public void playPurchaseSound() {
        playSound(soundPurchase);
    }

    public void playBonusSound() {
        playSound(soundBonus);
    }

    public void playCancelSound() {
        playSound(soundBoop);
    }

    public void playUIGenericSound() {
        playSound(soundBleep);
    }

    public void playConfirmSound() {
        playSound(soundConfirm);
    }

    public void playSwapSound() {
        playSound(soundSwap);
    }

    private void playSound(Sound sfx) {
        sfx.play(volume);
    }

    public void playSoundAsset(AssetDescriptor<Sound> sfx) {
        Gdx.app.log("SoundController", "Playing " + sfx.toString() + " at " + (int) (volume * 100f) + " volume.");
        Assets.asset(sfx).play(volume);
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
