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
    private int level;

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
                spawnWallOfEnemiesAndWaitDeath(EnemyType.Rammer, 9, 2),
                showText("Use buttons below to fire missiles"),
                new RunnableAction() {
                    @Override
                    public void run() {
                        scene.getPlayer().modMissileSalvos(1);
                    }
                },
                new Action() {
                    @Override
                    public boolean act(float delta) {
                        return scene.getPlayer().getMissileSalvos() < 1;
                    }
                },
                showText("Tutorial complete"),
                finishLevel()
        ));
    }

    private Action finishLevel() {
        return Actions.sequence(
                new RunnableAction() {
                    @Override
                    public void run() {
                        scene.getPlayer().setNoControlTime(100f);
                        scene.getPlayer().setGodModeTimer(100f);
                        scene.getPlayer().setMoveVector(new Vector3(0f, 0f, 0f), true);
                    }
                },
                Actions.delay(2f),
                new RunnableAction() {
                    @Override
                    public void run() {
                        scene.getPlayer().setMoveVector(new Vector3(0f, 0f, 300f), true);
                    }
                },
                new Action() {
                    @Override
                    public boolean act(float delta) {
                        Ship player = scene.getPlayer();
                        player.setPosition(player.getPosition().cpy().add(0f, 0f, 500f * delta));
                        return !scene.gameBounds.contains(player.getPosition());
                    }
                },
                new RunnableAction() {
                    @Override
                    public void run() {
                        AstroblazeGame game = AstroblazeGame.getInstance();
                        game.setMaxLevel(Math.max(level + 1, game.getMaxLevel()));
                        AstroblazeGame.getInstance().getGuiRenderer().backToLevelSelect();
                    }
                });
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

    private Action spawnWallOfEnemiesAndWaitDeath(final EnemyType type, final int count, final int removeMiddle) {
        RunnableAction r = new RunnableAction();
        final Enemy[] enemy = new Enemy[count]; // array wrapper for closure
        r.setRunnable(new Runnable() {
            @Override
            public void run() {
                float spawnZoneX = scene.gameBounds.max.x - scene.gameBounds.min.x;
                for (int i = 0; i < count; i++) {
                    if (i > count / 2 - removeMiddle && i < count / 2 + removeMiddle) {
                        enemy[i] = null;
                        continue;
                    }
                    // spawn away by mirrored x axis just in case
                    Vector3 spawnPos = new Vector3(
                            spawnZoneX * (((i + 0.5f) / count) - 0.5f),
                            0f,
                            scene.gameBounds.max.z);

                    enemy[i] = scene.enemyPool.obtain();
                    enemy[i].setType(type);
                    enemy[i].setPosition(spawnPos);
                }
            }
        });
        return Actions.sequence(r, new Action() {
            @Override
            public boolean act(float delta) {
                int alive = 0;
                for (Enemy value : enemy) {
                    if (value != null && value.hp > 0f
                            && scene.gameBounds.contains(value.position))
                        alive++;
                }
                return alive <= 0;
            }
        });
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
        this.level = level;
        Gdx.app.log("LevelController", "Set level to " + level);

        if (level == 0 || level == 1) {
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
