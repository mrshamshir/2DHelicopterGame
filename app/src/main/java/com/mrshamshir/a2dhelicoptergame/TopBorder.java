package com.mrshamshir.a2dhelicoptergame;

import android.graphics.Bitmap;
import android.graphics.Canvas;



public class TopBorder extends GameObject {
    private Bitmap image;

    public TopBorder(Bitmap res, int x, int y, int h) {
        height = h;
        width = 20;

        super.x = x;
        super.y = y;

        dx = GamePanel.MOVESPEED;
        image = Bitmap.createBitmap(res, 0, 0, width, height);

    }

    public void update() {
        x += dx;
    }

    public void draw(Canvas canvas) {
        try{
            canvas.drawBitmap(image , x, y, null);

        }catch (Exception e){}
    }

}
