package com.mrshamshir.a2dhelicoptergame;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.test.suitebuilder.annotation.Smoke;

public class SmokePuff extends GameObject {

    public int r;

    public SmokePuff(int x, int y) {
        this.r = 5;
        super.x = x;
        super.y = y;
    }

    public void update() {
        x -= 10;
    }

    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(x - r, y - r, r, paint);
        canvas.drawCircle(x - r + 2, y - r - 2, r, paint);
        canvas.drawCircle(x - r + 4, y - r + 1, r, paint);
    }

}
