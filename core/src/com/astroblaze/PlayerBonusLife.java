package com.astroblaze;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class PlayerBonusLife implements IPlayerBonus {
    @Override
    public TextureAtlas.AtlasRegion getDecalTexture() {
        return Assets.asset(Assets.atlas).findRegion("life-upgrade");
    }

    @Override
    public void applyBonus(Ship ship) {
        ship.getScene().modLives(+1);
    }
}
