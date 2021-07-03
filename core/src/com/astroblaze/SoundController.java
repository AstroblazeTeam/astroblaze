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

    private float sfxVolume;
    private float UIVolume;

    SoundController(AstroblazeGame game) {
        game.addOnLoadingFinishedListener(this);
        state = AstroblazeGame.getPlayerState();
        sfxVolume = state.getSoundVolume();
        UIVolume = state.getUiVolume();
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

    public void playBonusSound() {
        playSound(soundBonus);
    }

    public void playUIPurchaseSound() {
        playUISound(soundPurchase);
    }

    public void playUICancelSound() {
        playUISound(soundBoop);
    }

    public void playUIGenericSound() {
        playUISound(soundBleep);
    }

    public void playUIConfirm() {
        playUISound(soundConfirm);
    }

    public void playUISwapSound() {
        playUISound(soundSwap);
    }

    private void playSound(Sound sfx) {
        sfx.play(sfxVolume);
    }

    private void playUISound(Sound uiSfx) {
        uiSfx.play(UIVolume);
    }

    public void playSoundAsset(AssetDescriptor<Sound> sfx) {
        Gdx.app.log("SoundController", "Playing " + sfx.toString() + " at " + (int) (sfxVolume * 100f) + " volume.");
        Assets.asset(sfx).play(sfxVolume);
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(float newSfxVolume) {
        sfxVolume = newSfxVolume;
        state.setSoundVolume(newSfxVolume);
        Gdx.app.log("SoundManager", "Set SFX volume to " + sfxVolume);
    }

    public float getUIVolume() {
        return UIVolume;
    }

    public void setUIVolume(float newUIVolume) {
        UIVolume = newUIVolume;
        state.setUiVolume(newUIVolume);
        Gdx.app.log("SoundManager", "Set UI volume to " + UIVolume);
    }
}
