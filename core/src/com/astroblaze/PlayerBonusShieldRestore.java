package com.astroblaze;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class PlayerBonusShieldRestore implements IPlayerBonus {
    @Override
    public TextureAtlas.AtlasRegion getDecalTexture() {
        return Assets.asset(Assets.atlas).findRegion("shield-restore");
    }

    @Override
    public void applyBonus(Ship ship) {
        ship.modHp(ship.getMaxHp());
    }
}
