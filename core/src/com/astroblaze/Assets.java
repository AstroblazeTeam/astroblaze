package com.astroblaze;

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
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;

public class Assets extends AssetManager {
    private static Assets instance;

    // these 3 assets loaded first to show loading screen
    public final static AssetDescriptor<Skin> uiSkin = new AssetDescriptor<>("ui/clean-crispy-ui.json", Skin.class);
    public final static AssetDescriptor<Texture> logo = new AssetDescriptor<>("logo.png", Texture.class);
    public final static AssetDescriptor<Music> uiMusic = new AssetDescriptor<>("music/ObservingTheStar.ogg", Music.class);

    public final static Array<AssetDescriptor<Texture>> parallaxArray = new Array<>();
    public final static AssetDescriptor<Music> gameMusic = new AssetDescriptor<>("music/space_flight.ogg", Music.class);
    public final static AssetDescriptor<Texture> parallax0 = new AssetDescriptor<>("parallax/parallax0.png", Texture.class);
    public final static AssetDescriptor<Texture> parallax1 = new AssetDescriptor<>("parallax/parallax1.png", Texture.class);
    public final static AssetDescriptor<Texture> parallax2 = new AssetDescriptor<>("parallax/parallax2.png", Texture.class);
    public final static AssetDescriptor<Texture> parallax3 = new AssetDescriptor<>("parallax/parallax3.png", Texture.class);
    public final static AssetDescriptor<Model> spaceShip1 = new AssetDescriptor<>("spaceships/spaceship1.obj", Model.class);
    public final static AssetDescriptor<Model> spaceShip2 = new AssetDescriptor<>("spaceships/spaceship2.obj", Model.class);
    public final static AssetDescriptor<Model> spaceShip3 = new AssetDescriptor<>("spaceships/spaceship3.obj", Model.class);
    public final static AssetDescriptor<Model> missile = new AssetDescriptor<>("projectiles/missile.obj", Model.class);
    public final static AssetDescriptor<TextureAtlas> atlas = new AssetDescriptor<>("projectiles/projectiles.atlas", TextureAtlas.class);
    public final static AssetDescriptor<Sound> explosion = new AssetDescriptor<>("sfx/explosion1.ogg", Sound.class);

    // these are loaded late, don't rely on them available at start!
    public static AssetDescriptor<ParticleEffect> flame;
    public static AssetDescriptor<ParticleEffect> flame2;

    static {
        parallaxArray.add(parallax0, parallax1, parallax2, parallax3);
    }

    public Assets() {
        instance = this;
    }

    public static Assets getInstance() { return instance; }

    public void loadAssets(ParticleSystem particles) {
        load(uiSkin);
        load(uiMusic);
        load(logo);
        load(spaceShip1);
        load(spaceShip2);
        load(spaceShip3);
        load(parallax0);
        load(parallax1);
        load(parallax2);
        load(parallax3);
        load(missile);
        load(atlas);
        load(gameMusic);
        load(explosion);

        ParticleEffectLoader.ParticleEffectLoadParameter loaderParams = new ParticleEffectLoader.ParticleEffectLoadParameter(particles.getBatches());
        flame = new AssetDescriptor<>("particles/flame.pfx", ParticleEffect.class, loaderParams);
        flame2 = new AssetDescriptor<>("projectiles/flame2.pfx", ParticleEffect.class, loaderParams);
        load(flame);
        load(flame2);
    }

    public static <T> T asset (AssetDescriptor<T> assetDescriptor) {
        return instance.get(assetDescriptor.fileName, assetDescriptor.type, true);
    }
}