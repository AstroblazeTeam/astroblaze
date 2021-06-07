package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;

public class LevelController extends Actor {
    private final Scene3D scene;
    private float spawnInterval;
    private float spawnTimer = 3f;

    public LevelController(Scene3D scene, float spawnInterval) {
        this.scene = scene;
        this.spawnInterval = spawnInterval;
    }

    public void runTutorial() {
        float defaultDelay = 3f;
        this.addAction(Actions.sequence(
                Actions.delay(defaultDelay),
                showText("Touch the screen to move!"),
                new Action() {
                    @Override
                    public boolean act(float delta) {
                        return !scene.getPlayer().getMoveVector().isZero();
                    }
                },
                showText("Primary weapon is always active, just aim and kill!"),
                spawnEnemyAndWaitDeath(EnemyType.TrainingDummy),
                showText("Dodge the bullets and enemies!"),
                Actions.delay(defaultDelay),
                showText("Use buttons below to fire missiles"),
                Actions.delay(defaultDelay),
                showText("")
        ));
    }

    private RunnableAction showText(final String txt) {
        RunnableAction r = new RunnableAction();
        r.setRunnable(new Runnable() {
            @Override
            public void run() {
                AstroblazeGame.getInstance().renderText(0, txt,
                        Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 4f);
            }
        });
        return r;
    }

    private Action spawnEnemyAndWaitDeath(final EnemyType type) {
        RunnableAction r = new RunnableAction();
        final Enemy[] enemy = new Enemy[1]; // array wrapper for closure
        r.setRunnable(new Runnable() {
            @Override
            public void run() {
                Vector3 spawnPos = scene.getPlayer().getPosition().cpy();
                // spawn away by mirrored x axis just in case
                spawnPos.add(
                        Math.copySign(scene.gameBounds.max.x * 0.75f, -spawnPos.x),
                        0f,
                        scene.gameBounds.max.z * 0.75f);

                enemy[0] = scene.enemyPool.obtain();
                enemy[0].setType(type);
                enemy[0].setPosition(spawnPos);
            }
        });
        return Actions.sequence(r, new Action() {
            @Override
            public boolean act(float delta) {
                return enemy[0].hp <= 0f;
            }
        });
    }

    public void setLevel(int level) {
        Gdx.app.log("LevelController", "Set level to " + level);

        if (level == 0) {
            runTutorial();
            return;
        } // else generate waves to spawn

        spawnInterval = 3f / level;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        spawnTimer -= delta * scene.getTimeScale();
        if (spawnTimer < 0f) {
            spawnTimer = spawnInterval;

            if (!this.hasActions()) { // spawn random
                Enemy enemy = scene.enemyPool.obtain();
                enemy.setType(EnemyType.random());
            } // otherwise spawn actions are handled by scenario
        }
    }
}
