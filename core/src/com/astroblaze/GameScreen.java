package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameScreen extends ScreenAdapter {
    private final AstroblazeGame game;
    private final PerspectiveCamera camera = new PerspectiveCamera();
    private final Stage stage;
    private final Environment environment = new Environment();
    private float dip;
    private final Model model;
    private final ModelInstance modelInstance;

    public GameScreen(AstroblazeGame game) {
        this.game = game;

        stage = new Stage(new ScreenViewport(camera));
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.2f, 0.2f, 0.2f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));


        ObjLoader objLoader = new ObjLoader();
        model = objLoader.loadModel(Gdx.files.internal("tank/tank-model.obj"));
        modelInstance = new ModelInstance(model);
        modelInstance.transform.translate(10f, 0f, -5f);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        ScreenUtils.clear(0, 0, 0.2f, 1);

        modelInstance.transform.rotate(0f, 1f, 0f, 10f * delta);

        game.batch.begin(camera);
        game.batch.render(modelInstance, environment);
        game.batch.end();
//
//        game.batch.begin(camera);
//        stage.act();
//        stage.draw();
//        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        dip = Gdx.graphics.getDensity();
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.fieldOfView = 60;
        camera.position.set(10f, 10f, 10f);
        camera.lookAt(0, 0, 0);
        camera.near = 1f;
        camera.far = 300f;
        camera.update();
    }

    @Override
    public void dispose() {
        model.dispose();
    }
}
