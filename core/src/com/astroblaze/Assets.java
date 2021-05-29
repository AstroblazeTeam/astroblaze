package com.astroblaze;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class Assets extends AssetManager {
    private static Assets instance;
    public final static AssetDescriptor<Texture> loadingImage = new AssetDescriptor<>("loading.png", Texture.class);
    public final static AssetDescriptor<Skin> uiSkin = new AssetDescriptor<>("ui/clean-crispy-ui.json", Skin.class);
    public final static AssetDescriptor<Model> spaceShip1 = new AssetDescriptor<>("spaceships/spaceship1.obj", Model.class);
    public final static AssetDescriptor<Model> spaceShip2 = new AssetDescriptor<>("spaceships/spaceship2.obj", Model.class);
    public final static AssetDescriptor<Model> spaceShip3 = new AssetDescriptor<>("spaceships/spaceship3.obj", Model.class);

    public Assets() {
        instance = this;
    }

    public void loadAssets() {
        load(loadingImage);
        load(uiSkin);
        load(spaceShip1);
        load(spaceShip2);
        load(spaceShip3);
    }

    public static <T> T asset (AssetDescriptor<T> assetDescriptor) {
        return instance.get(assetDescriptor.fileName, assetDescriptor.type, true);
    }
}