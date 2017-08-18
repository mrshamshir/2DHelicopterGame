package com.mrshamshir.a2dhelicoptergame;

import android.graphics.Bitmap;

/**
 * Created by MRSH74 on 8/12/2017.
 */

public class Animation {
    private Bitmap[] frames ;
    private int currentFrame ;
    private long startTime ;
    private long delay ;
    private boolean playedOnce ;


    public void setFrames(Bitmap[] frames) {
        this.frames = frames;
        currentFrame = 0 ;
        startTime = System.nanoTime();
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public void setFrame(int i ){
        currentFrame = i ;
    }

    public void update(){
        long elapsed = (System.nanoTime()-startTime)/1000000;

        if ( elapsed > delay ){
            currentFrame++;
            startTime = System.nanoTime();
        }

        if ( currentFrame == frames.length){
            currentFrame = 0 ;
            playedOnce = true ;
        }
    }

    public Bitmap getImage(){
        return frames[currentFrame];
    }

    public int getFrame(){
        return currentFrame ;
    }

    public boolean isPlayedOnce() {
        return playedOnce;
    }
}
