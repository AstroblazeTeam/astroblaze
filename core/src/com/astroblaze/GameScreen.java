package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.FloatCounter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameScreen extends ScreenAdapter {
    private final AstroblazeGame game;
    private final Stage stage;
    private final GLProfiler profiler;

    public GameScreen(AstroblazeGame game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        profiler = new GLProfiler(Gdx.graphics);
    }

    @Override
    public void render(float delta) {
        if (profiler.isEnabled()) {
            profiler.reset();
        }

        if (Gdx.input.isTouched(3)) {
            if (profiler.isEnabled()) {
                profiler.disable();
                DebugTextDrawer.setExtraReport("");
            } else {
                profiler.enable();
            }
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        this.stage.act(delta);
        game.getScene().act(delta);
        this.stage.draw();
        game.getScene().render();

        if (profiler.isEnabled()) {
            String extra = "draw calls: " + profiler.getDrawCalls() +
                    "\nGL calls: " + profiler.getCalls() +
                    "\ntexture bindings: " + profiler.getTextureBindings() +
                    "\nshader switches:  " + profiler.getShaderSwitches()+
                    "\nvertices: " + (int)profiler.getVertexCount().value;
            DebugTextDrawer.setExtraReport(extra);
        }
    }

    @Override
    public void resize(int width, int height) {
        game.getScene().resize(width, height);
    }

    @Override
    public void show() {
        this.stage.addAction(Actions.sequence(Actions.fadeOut(0f), Actions.fadeIn(1f)));
        this.stage.addActor(new ParallaxBackground(8f));
        this.stage.addActor(new DebugTextDrawer());
    }

    public void startGame() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                game.getScene().addActors();
            }
        });
    }

    public void stopGame() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                game.getScene().clearActors();
            }
        });
    }
}
