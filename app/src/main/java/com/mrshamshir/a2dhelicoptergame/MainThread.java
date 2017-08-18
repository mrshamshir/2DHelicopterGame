package com.mrshamshir.a2dhelicoptergame;

import android.graphics.Canvas;
import android.provider.Settings;
import android.view.SurfaceHolder;

/**
 * Created by MRSH74 on 8/4/2017.
 */

public class MainThread extends Thread {
    private int FPS = 30;
    private double avarageFPS;
    private SurfaceHolder surfaceHolder;
    private GamePanel gamePanel;
    private boolean running;
    public static Canvas canvas;


    public MainThread(SurfaceHolder surfaceHolder, GamePanel gamePanel) {
        super();
        this.surfaceHolder = surfaceHolder;
        this.gamePanel = gamePanel;
    }

    @Override
    public void run() {
        long startTime;
        long timeMillis;
        long waitTime;
        long totalTime = 0;
        int frameCount = 0;
        long targetTime = 1000 / FPS;

        while (running) {
            startTime = System.nanoTime();
            canvas = null;

            //try locking the canvas for pixel editing
            try {
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    this.gamePanel.update();
                    this.gamePanel.draw(canvas);
                }
            } catch (Exception e) {
            }
            finally {
                if ( canvas != null){
                    try{
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception e){e.printStackTrace();}
                }
            }


            timeMillis = (System.nanoTime() - startTime) / 1000000;
            waitTime = targetTime - timeMillis ;

            try{
                this.sleep(waitTime);
            }catch (Exception e){}

            totalTime += System.nanoTime() - startTime ;
            frameCount++ ;
            if ( frameCount == FPS){
                avarageFPS = 1000 / ((totalTime / frameCount)/1000000);
                frameCount = 0 ;
                totalTime = 0 ;
//                System.out.println(avarageFPS);
            }

        }
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
