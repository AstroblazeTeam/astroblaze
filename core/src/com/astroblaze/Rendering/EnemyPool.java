package com.astroblaze.Rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Pool;

public class EnemyPool extends Pool<EnemyShip> {
    private final Scene3D scene;

    public EnemyPool(Scene3D scene) {
        super(0, 256);
        this.scene = scene;
    }

    @Override
    protected void reset(EnemyShip enemyShip) {
        // super.reset(pfx); - no need, default implementation only handles Poolable
        enemyShip.reset(scene.getGameBounds());
    }

    @Override
    public EnemyShip obtain() {
        EnemyShip enemyShip = super.obtain();
        Gdx.app.debug("EnemyPool", "Added enemy to scene");
        scene.addActor(enemyShip);
        return enemyShip;
    }

    @Override
    public void free(EnemyShip enemyShip) {
        super.free(enemyShip);
        Gdx.app.debug("EnemyPool", "Removed enemy from scene");
        scene.removeActor(enemyShip);
    }

    @Override
    protected EnemyShip newObject() {
        EnemyShip enemyShip = new EnemyShip(scene);
        reset(enemyShip);
        return enemyShip;
    }
}