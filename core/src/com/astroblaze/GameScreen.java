package com.astroblaze;

import com.badlogic.gdx.ScreenAdapter;

public class GameScreen extends ScreenAdapter {
    private final AstroblazeGame game;
    private final Scene3D scene;

    public GameScreen(AstroblazeGame game) {
        this.game = game;
        this.scene = new Scene3D(game);
    }

    @Override
    public void render(float delta) {
        this.scene.act(delta);
        this.scene.render();
    }

    @Override
    public void resize(int width, int height) {
        this.scene.resize(width, height);
    }

    @Override
    public void show() {
        scene.addShip();
    }
}
