package com.astroblaze.Rendering;

import com.astroblaze.AstroblazeGame;
import com.astroblaze.Interfaces.ITargetable;
import com.astroblaze.PlayerState;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public abstract class SpaceShip extends Renderable implements ITargetable {
    protected final Vector3 moveVector = new Vector3();
    protected final Scene3D scene;
    protected final PlayerState playerState;
    protected final AstroblazeGame game;
    protected final Array<DecalController.DecalInfo> exhaustDecals = new Array<>(8);

    protected float hp;
    protected float gunClock = 0f;
    protected float turretClock = 0f;

    protected SpaceShip(Scene3D scene) {
        this.scene = scene;
        this.game = AstroblazeGame.getInstance();
        this.playerState = AstroblazeGame.getPlayerState();
    }

    public Scene3D getScene() {
        return this.scene;
    }

    public float getHitpoints() {
        return this.hp;
    }

    public abstract float getMaxHitpoints();

    @Override
    public float distanceSquaredTo(Vector3 pos) {
        return position.dst2(pos);
    }
}
