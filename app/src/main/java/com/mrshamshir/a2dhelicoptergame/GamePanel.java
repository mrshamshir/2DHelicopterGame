package com.mrshamshir.a2dhelicoptergame;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Random;


public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {

    public static final int WIDTH = 856;
    public static final int HEIGHT = 480;
    public static final int MOVESPEED = -5;

    private long smokeStartTime;
    private long missileStartTime;
    private MainThread thread;
    private Background bg;
    private Player player;
    private ArrayList<SmokePuff> smokePuffs;
    private ArrayList<Missile> missiles;
    private ArrayList<TopBorder> topBorders;
    private ArrayList<BotBorder> botBorders;
    private Random rand = new Random();
    private int maxBorderHeight;
    private int minBorderHeight;
    private boolean topDown = true;
    private boolean botDown = true;
    private boolean newGameCreated;

    //increase to slow down difficulty progression , decrease to speed up difficulty progression
    private int progressDenom = 20;


    public GamePanel(Context context) {
        super(context);

        //add the callback to the surfaceHolder to intercept events
        getHolder().addCallback(this);

        thread = new MainThread(getHolder(), this);

        //make focusable so it can handle events
        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.grassbg1));
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.helicopter), 65, 25, 3);
        smokePuffs = new ArrayList<SmokePuff>();
        missiles = new ArrayList<Missile>();
        topBorders = new ArrayList<TopBorder>();
        botBorders = new ArrayList<BotBorder>();

        smokeStartTime = System.nanoTime();
        missileStartTime = System.nanoTime();

        //we can safely start the game loop
        thread.setRunning(true);
        thread.start();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        int counter = 0;
        while (retry && counter < 1000) {
            counter++;
            try {
                thread.setRunning(false);
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!player.isPlaying()) {
                player.setPlaying(true);
                player.setUp(true);
            } else {
                player.setUp(true);
            }
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            player.setUp(false);
            return true;
        }


        return super.onTouchEvent(event);
    }

    public void update() {
        if (player.isPlaying()) {
            bg.update();
            player.update();

            //calculate the threshold of height the border can have based on the score
            //max and min border heart are updated , and the border switched direction when either max or
            //min is met

            maxBorderHeight = 30 + player.getScore() / progressDenom;
            //cap max border height so that borders can only take up a total of 1/2 the screen
            if (maxBorderHeight > HEIGHT / 4)
                maxBorderHeight = HEIGHT / 4;
            minBorderHeight = 5 + player.getScore() / progressDenom;

            //check bottom border collision
            for (int i = 0; i < botBorders.size(); i++) {
                if (collision(botBorders.get(i), player))
                    player.setPlaying(false);
            }

            //check top border collision
            for (int i = 0; i < topBorders.size(); i++) {
                if (collision(topBorders.get(i), player))
                    player.setPlaying(false);
            }

            //update top border
            this.updateTopBorder();

            //update bottom border
            this.updateBottomBorder();

            //add missiles on timer
            long missileElapsed = (System.nanoTime() - missileStartTime) / 1000000;
            if (missileElapsed > (2000 - player.getScore() / 4)) {
                //first missile always goes down the middle
                if (missiles.size() == 0) {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),
                            R.drawable.missile), WIDTH + 10, HEIGHT / 2, 45, 15, player.getScore(), 13));
                } else {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),
                            R.drawable.missile), WIDTH + 10, (int) (rand.nextDouble() * (HEIGHT - (maxBorderHeight * 2)) + maxBorderHeight),
                            45, 15, player.getScore(), 13));
                }
                //reset timer
                missileStartTime = System.nanoTime();
            }
            //loop though every missile and check collision and remove
            for (int i = 0; i < missiles.size(); i++) {
                //update missile
                missiles.get(i).update();

                if (collision(missiles.get(i), player)) {
                    missiles.remove(i);
                    player.setPlaying(false);
                    break;

                }
                //remove missile if it is way off the screen
                if (missiles.get(i).getX() < -100) {
                    missiles.remove(i);
                    break;
                }
            }

            //add smoke puffs on timer
            long elapsed = (System.nanoTime() - smokeStartTime) / 1000000;
            if (elapsed > 120) {
                smokePuffs.add(new SmokePuff(player.getX(), player.getY() + 10));
                smokeStartTime = System.nanoTime();
            }

            for (int i = 0; i < smokePuffs.size(); i++) {
                smokePuffs.get(i).update();
                if (smokePuffs.get(i).getX() < -10) {
                    smokePuffs.remove(i);
                }
            }
        } else {
            newGameCreated = false;
            if (!newGameCreated) {
                newGame();
            }
        }

    }

    private boolean collision(GameObject a, GameObject b) {
        if (Rect.intersects(a.getRectangle(), b.getRectangle())) {
            return true;
        }
        return false;
    }

    @Override
    public void draw(Canvas canvas) {
        final float scaleFactorX = getWidth() / (WIDTH * 1.f);
        final float scaleFactorY = getHeight() / (HEIGHT * 1.f);


        if (canvas != null) {
            final int savedState = canvas.save();


            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);
            player.draw(canvas);
            //draw smoke puffs
            for (SmokePuff sp : smokePuffs) {
                sp.draw(canvas);
            }
            //draw missiles
            for (Missile m : missiles) {
                m.draw(canvas);
            }

            //draw topBorder
            for (TopBorder tb : topBorders) {
                tb.draw(canvas);
            }

            //draw botBorder
            for (BotBorder bb : botBorders) {
                bb.draw(canvas);
            }
            canvas.restoreToCount(savedState);


        }

    }

    public void updateTopBorder() {

        //every 50 points, insert randomly placed top blocks that break the pattern
        if (player.getScore() % 50 == 0) {
            topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                    topBorders.get(topBorders.size() - 1).getX() + 20, 0,
                    (int) ((rand.nextDouble() * (maxBorderHeight)) + 1)));
        }

        for (int i = 0; i < topBorders.size(); i++) {
            topBorders.get(i).update();
            if (topBorders.get(i).getX() < -20) {
                topBorders.remove(i);
                //remove element of arrayList , replace it by adding a new one

                //calculate topDown which determines the direction the border is moving(up or down)
                if (topBorders.get(topBorders.size() - 1).getHeight() >= maxBorderHeight) {
                    topDown = false;
                }
                if (topBorders.get(topBorders.size() - 1).getHeight() <= minBorderHeight) {
                    topDown = true;
                }

                //new border added will have larger height
                if (topDown) {
                    topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick), topBorders.get(topBorders.size() - 1).getX() + 20,
                            0, topBorders.get(topBorders.size() - 1).getHeight() + 1));
                }
                //new border added will have smaller height
                else {
                    topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick), topBorders.get(topBorders.size() - 1).getX() + 20,
                            0, topBorders.get(topBorders.size() - 1).getHeight() - 1));
                }
            }
        }
    }

    public void updateBottomBorder() {

        //every 40 points, insert randomly placed bottom blocks that break the pattern
        if (player.getScore() % 40 == 0) {
            botBorders.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                    botBorders.get(botBorders.size() - 1).getX() + 20,
                    (int) ((rand.nextDouble() * maxBorderHeight) + (HEIGHT - maxBorderHeight))));
        }

        //update bottom border
        for (int i = 0; i < botBorders.size(); i++) {
            botBorders.get(i).update();

            //if border is moving off screen , remove it and add a corresponding new one
            if (botBorders.get(i).getX() < -20) {
                botBorders.remove(i);

                //determine if border will be moving up or down
                if (botBorders.get(botBorders.size() - 1).getY() <= HEIGHT - maxBorderHeight) {
                    botDown = true;
                }
                if (botBorders.get(botBorders.size() - 1).getY() >= HEIGHT - minBorderHeight) {
                    botDown = false;
                }

                if (botDown) {
                    botBorders.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            botBorders.get(botBorders.size() - 1).getX() + 20, botBorders.get(botBorders.size() - 1).getY() + 1));

                } else {
                    botBorders.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            botBorders.get(botBorders.size() - 1).getX() + 20, botBorders.get(botBorders.size() - 1).getY() - 1));
                }
            }
        }

    }

    public void newGame() {
        botBorders.clear();
        topBorders.clear();
        missiles.clear();
        smokePuffs.clear();


        minBorderHeight = 5;
        maxBorderHeight = 30;
        player.resetDY();
        player.resetScore();
        player.setY(HEIGHT / 2);

        //create initial borders

        //initial topBorders
        for (int i = 0; i * 20 < WIDTH + 40; i++) {

            //first top border create
            if (i == 0) {
                topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i * 20, 0, 10));
            } else {
                topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i * 20, 0, topBorders.get(i - 1).getHeight() + 1));
            }

        }

        //initial bottom border
        for (int i = 0; i * 20 < WIDTH + 40; i++) {

            //first border ever created
            if (i == 0) {
                botBorders.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i * 20, HEIGHT - minBorderHeight));
            }
            //adding borders until the initial screen is filled
            else {
                botBorders.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i * 20, botBorders.get(i - 1).getY() - 1));

            }
        }

        newGameCreated = true;


    }


}

