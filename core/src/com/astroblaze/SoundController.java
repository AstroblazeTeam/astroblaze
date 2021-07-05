package com.astroblaze;

import com.astroblaze.Interfaces.ILoadingFinishedListener;
import com.astroblaze.Utils.MathHelper;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;

public class SoundController implements ILoadingFinishedListener {
    private final PlayerState state;

    private Sound soundExplosion;
    private Sound soundPurchase;
    private Sound soundBonus;
    private Sound soundPositive;
    private Sound soundNegative;
    private Sound soundConfirm;
    private Sound soundSwap;

    private float sfxVolume;
    private float UIVolume;

    // laser hum
    private Sound laserSound;
    private boolean laserActive;
    private long laserSoundId;
    private float laserVolume;

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
        soundPositive = Assets.asset(Assets.soundPositive);
        soundNegative = Assets.asset(Assets.soundNegative);
        soundConfirm = Assets.asset(Assets.soundConfirm);
        soundSwap = Assets.asset(Assets.soundSwap);

        laserSound = Assets.asset(Assets.soundLaser);
    }

    public void playExplosionSound() {
        playSfxSound(soundExplosion);
    }

    public void playBonusSound() {
        playSfxSound(soundBonus);
    }

    public void playUIPurchaseSound() {
        playUISound(soundPurchase);
    }

    public void playUIPositive() {
        playUISound(soundPositive);
    }

    public void playUINegative() {
        playUISound(soundNegative);
    }

    public void playUIConfirm() {
        playUISound(soundConfirm);
    }

    public void playUISwapSound() {
        playUISound(soundSwap);
    }

    private void playSfxSound(Sound sfx) {
        sfx.play(sfxVolume);
    }

    private void playUISound(Sound uiSfx) {
        uiSfx.play(0.25f * UIVolume);
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

    public boolean getLaserActive() {
        return laserActive;
    }

    public void setLaserActive(boolean active) {
        laserActive = active;
    }

    public void update(float delta) {
        if (laserSound == null) // assets not loaded yet
            return;
        laserVolume = MathHelper.moveTowards(laserVolume, laserActive ? sfxVolume : 0f, 5f * delta);

        if (laserSoundId == -1) {
            laserSoundId = laserSound.loop(laserVolume);
        } else if (laserVolume == 0f) {
            laserSound.stop(laserSoundId);
            laserSoundId = -1;
        } else {
            laserSound.setVolume(laserSoundId, laserVolume);
        }
    }
}
