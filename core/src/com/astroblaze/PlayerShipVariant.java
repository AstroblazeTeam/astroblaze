package com.astroblaze;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g3d.Model;

public enum PlayerShipVariant {
    Scout(0, Assets.spaceShip1, 0.5f, 100f, 0f, 2, 3),
    Cruiser(1, Assets.spaceShip2, 0.5f, 150f, 10000f, 4, 4),
    Destroyer(2, Assets.spaceShip3, 0.5f, 200f, 200000f, 5, 8);

    public final int id;
    public final AssetDescriptor<Model> modelDescriptor;
    public final float modelScale;
    public final float maxHp;
    public final float price;
    public final int gunPorts;
    public final int missilePorts;

    PlayerShipVariant(int id, AssetDescriptor<Model> modelDescriptor, float modelScale, float maxHp,
                      float price, int gunPorts, int missilePorts) {
        this.id = id;
        this.modelDescriptor = modelDescriptor;
        this.modelScale = modelScale;
        this.maxHp = maxHp;
        this.price = price;
        this.gunPorts = gunPorts;
        this.missilePorts = missilePorts;
    }
}
