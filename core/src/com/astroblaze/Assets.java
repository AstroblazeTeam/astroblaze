package com.astroblaze;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;

public class Assets extends AssetManager {
    private static Assets instance;
    public final static Array<AssetDescriptor<Texture>> parallaxArray = new Array<>();
    public final static AssetDescriptor<Skin> uiSkin = new AssetDescriptor<>("ui/clean-crispy-ui.json", Skin.class);
    public final static AssetDescriptor<Texture> loadingImage = new AssetDescriptor<>("loading.png", Texture.class);
    public final static AssetDescriptor<Texture> parallax0 = new AssetDescriptor<>("parallax/parallax0.png", Texture.class);
    public final static AssetDescriptor<Texture> parallax1 = new AssetDescriptor<>("parallax/parallax1.png", Texture.class);
    public final static AssetDescriptor<Texture> parallax2 = new AssetDescriptor<>("parallax/parallax2.png", Texture.class);
    public final static AssetDescriptor<Texture> parallax3 = new AssetDescriptor<>("parallax/parallax3.png", Texture.class);
    public final static AssetDescriptor<Model> spaceShip1 = new AssetDescriptor<>("spaceships/spaceship1.obj", Model.class);
    public final static AssetDescriptor<Model> spaceShip2 = new AssetDescriptor<>("spaceships/spaceship2.obj", Model.class);
    public final static AssetDescriptor<Model> spaceShip3 = new AssetDescriptor<>("spaceships/spaceship3.obj", Model.class);

    static {
        parallaxArray.add(parallax0, parallax1, parallax2, parallax3);
    }

    public Assets() {
        instance = this;
    }

    public void loadAssets() {
        load(loadingImage);
        load(uiSkin);
        load(spaceShip1);
        load(spaceShip2);
        load(spaceShip3);
        load(parallax0);
        load(parallax1);
        load(parallax2);
        load(parallax3);
    }

    public static <T> T asset (AssetDescriptor<T> assetDescriptor) {
        return instance.get(assetDescriptor.fileName, assetDescriptor.type, true);
    }
}