package com.astroblaze;

import com.astroblaze.Interfaces.ILoadingFinishedListener;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;

import java.util.Locale;

public class Assets extends AssetManager implements ILoadingFinishedListener {
    private static Assets instance;
    private static final int spaceShipModelsAvailable = 14;

    // these 3 assets loaded first to show loading screen
    public final static AssetDescriptor<Skin> uiSkin = new AssetDescriptor<>("ui/clean-crispy-ui.json", Skin.class);
    public final static AssetDescriptor<Texture> logo = new AssetDescriptor<>("logo.png", Texture.class);
    public final static AssetDescriptor<Music> musicUI = new AssetDescriptor<>("music/ui.ogg", Music.class);
    public final static AssetDescriptor<Music> musicLevel1 = new AssetDescriptor<>("music/level1.ogg", Music.class);
    public final static AssetDescriptor<Music> musicLevel2 = new AssetDescriptor<>("music/level2.ogg", Music.class);
    public final static AssetDescriptor<Music> musicLevel3 = new AssetDescriptor<>("music/level3.ogg", Music.class);
    public final static AssetDescriptor<Music> musicEnding = new AssetDescriptor<>("music/ending.ogg", Music.class);
    public final static AssetDescriptor<Model> missile = new AssetDescriptor<>("projectiles/missile.obj", Model.class);
    public final static AssetDescriptor<Texture> laser = new AssetDescriptor<>("laser/laser.png", Texture.class);
    public final static AssetDescriptor<TextureAtlas> atlas = new AssetDescriptor<>("projectiles/projectiles.atlas", TextureAtlas.class);
    public final static AssetDescriptor<ShaderProgram> fadeShader = new AssetDescriptor<>("shaders/fade.vert", ShaderProgram.class);
    public final static AssetDescriptor<ShaderProgram> laserShader = new AssetDescriptor<>("shaders/laser.vert", ShaderProgram.class);
    public final static AssetDescriptor<Sound> soundExplosion = new AssetDescriptor<>("sfx/explosion1.ogg", Sound.class);
    public final static AssetDescriptor<Sound> soundPurchase = new AssetDescriptor<>("sfx/cha_ching.ogg", Sound.class);
    public final static AssetDescriptor<Sound> soundWarning = new AssetDescriptor<>("sfx/warning.ogg", Sound.class);
    public final static AssetDescriptor<Sound> soundPositive = new AssetDescriptor<>("sfx/positive.ogg", Sound.class);
    public final static AssetDescriptor<Sound> soundNegative = new AssetDescriptor<>("sfx/negative.ogg", Sound.class);
    public final static AssetDescriptor<Sound> soundConfirm = new AssetDescriptor<>("sfx/confirm.ogg", Sound.class);
    public final static AssetDescriptor<Sound> soundSwap = new AssetDescriptor<>("sfx/swap.ogg", Sound.class);
    public final static AssetDescriptor<Sound> soundBonus = new AssetDescriptor<>("sfx/bonus.ogg", Sound.class);
    public final static AssetDescriptor<Sound> soundLaser = new AssetDescriptor<>("laser/laser.ogg", Sound.class);

    // these are loaded late, don't rely on them available at start!
    public final static Array<TextureAtlas.AtlasRegion> bullets = new Array<>(16);
    public final static Array<AssetDescriptor<Model>> spaceShipAssets = new Array<>(16);
    public final static Array<Model> spaceShipModels = new Array<>(16);
    public static AssetDescriptor<ParticleEffect> flame;
    public static TextureAtlas atlas1;
    public static TextureAtlas.AtlasRegion whitePixel;
    public static TextureAtlas.AtlasRegion heartTexture;

    public Assets(AstroblazeGame game) {
        instance = this;
        game.addOnLoadingFinishedListener(this);
    }

    public static Assets getInstance() {
        return instance;
    }

    public void loadLoadingScreenAssets() {
        load(uiSkin);
        load(musicUI);
        load(logo);
    }

    public void loadAssets(ParticleSystem particles) {
        spaceShipAssets.clear();
        for (int i = 0; i < spaceShipModelsAvailable; i++) {
            AssetDescriptor<Model> spaceShipAsset = new AssetDescriptor<>("spaceships/spaceship" + i + ".obj", Model.class);
            load(spaceShipAsset);
            spaceShipAssets.add(spaceShipAsset);
        }
        load(missile);
        load(musicLevel1);
        load(musicLevel2);
        load(musicLevel3);
        load(musicEnding);
        load(soundExplosion);
        load(soundPurchase);
        load(soundWarning);
        load(soundPositive);
        load(soundNegative);
        load(soundConfirm);
        load(soundSwap);
        load(soundBonus);
        load(atlas);
        load(fadeShader);
        load(laser);
        load(laserShader);
        load(soundLaser);

        ParticleEffectLoader.ParticleEffectLoadParameter loaderParams = new ParticleEffectLoader.ParticleEffectLoadParameter(particles.getBatches());
        flame = new AssetDescriptor<>("projectiles/flame2.pfx", ParticleEffect.class, loaderParams);
        load(flame);
    }

    @Override
    public void finishedLoadingAssets() {
        atlas1 = Assets.asset(Assets.atlas);
        heartTexture = Assets.asset(Assets.atlas).findRegion("heart_icon");
        whitePixel = Assets.asset(Assets.atlas).findRegion("white_pixel");

        spaceShipModels.clear();
        for (int i = 0; i < spaceShipModelsAvailable; i++) {
            AssetDescriptor<Model> spaceShipAsset = new AssetDescriptor<>("spaceships/spaceship" + i + ".obj", Model.class);
            spaceShipModels.add(asset(spaceShipAsset));
        }

        bullets.clear();
        for (int i = 1; i < 11; i++) {
            bullets.add(atlas1.findRegion(String.format(Locale.US, "%02d", i)));
        }
    }

    public static <T> T asset(AssetDescriptor<T> assetDescriptor) {
        return instance.get(assetDescriptor.fileName, assetDescriptor.type, true);
    }
}