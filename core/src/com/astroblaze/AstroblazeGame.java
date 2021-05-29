package com.astroblaze;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.Random;

public class AstroblazeGame extends Game {
	public final static int PIXELS_PER_METER = 50;

	public Skin skin;
	public ModelBatch batch;
	public GameScreen gameScreen;
	public InputMultiplexer inputMux;
	private static Preferences prefs;
	public final static Random rng = new Random();
	public static Preferences getPrefs() { return prefs; }

	@Override
	public void create () {
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		this.prefs = Gdx.app.getPreferences("AnnelidWar");
		this.skin = new Skin(Gdx.files.internal("ui/clean-crispy-ui.json"));
		this.batch = new ModelBatch();
		this.inputMux = new InputMultiplexer();
		this.gameScreen = new GameScreen(this);

		Gdx.input.setInputProcessor(inputMux);

		this.setScreen(gameScreen);
	}

	@Override
	public void pause(){
	}

	@Override
	public void resume()
	{
	}

	@Override
	public void dispose () {
		batch.dispose();
	}
}
