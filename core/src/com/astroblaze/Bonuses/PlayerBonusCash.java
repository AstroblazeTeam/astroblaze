package com.astroblaze.Bonuses;

import com.astroblaze.Assets;
import com.astroblaze.AstroblazeGame;
import com.astroblaze.Interfaces.IPlayerBonus;
import com.astroblaze.Rendering.PlayerShip;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class PlayerBonusCash implements IPlayerBonus {
    @Override
    public TextureAtlas.AtlasRegion getDecalTexture() {
        return Assets.asset(Assets.atlas).findRegion("money_drop");
    }

    @Override
    public float getDecalScale() {
        return 0.075f;
    }

    @Override
    public void applyBonus(PlayerShip playerShip) {
        AstroblazeGame.getSoundController().playBonusSound();
        AstroblazeGame.getPlayerState().modPlayerMoney(1000f);
    }
}
