package com.astroblaze;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g3d.Model;

public enum PlayerShipVariant {
    Scout(Assets.spaceShip1, 1f, 100f, 2, 3),
    Cruiser(Assets.spaceShip2, 0.25f, 150f, 4, 4),
    Destroyer(Assets.spaceShip2, 0.5f, 200f, 5, 8);

    public final AssetDescriptor<Model> modelDescriptor;
    public final float modelScale;
    public final float maxHp;
    public final int gunPorts;
    public final int missilePorts;

    PlayerShipVariant(AssetDescriptor<Model> modelDescriptor, float modelScale, float maxHp, int gunPorts, int missilePorts) {
        this.modelDescriptor = modelDescriptor;
        this.modelScale = modelScale;
        this.maxHp = maxHp;
        this.gunPorts = gunPorts;
        this.missilePorts = missilePorts;
    }
}
