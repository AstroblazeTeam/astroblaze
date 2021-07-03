package com.astroblaze.Bonuses;

import com.astroblaze.*;
import com.astroblaze.Interfaces.*;
import com.astroblaze.Rendering.*;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class PlayerBonusLife implements IPlayerBonus {
    @Override
    public TextureAtlas.AtlasRegion getDecalTexture() {
        return Assets.asset(Assets.atlas).findRegion("life-upgrade");
    }

    @Override
    public float getDecalScale() {
        return 0.1f;
    }

    @Override
    public void applyBonus(PlayerShip playerShip) {
        AstroblazeGame.getSoundController().playBonusSound();
        playerShip.getScene().modLives(+1);
    }
}
