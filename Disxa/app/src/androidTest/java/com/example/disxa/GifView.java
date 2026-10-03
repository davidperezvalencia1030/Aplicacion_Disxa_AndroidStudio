package com.example.disxa;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import java.io.InputStream;

public class GifView extends View {

    private Movie movie;
    private long movieStart = 0;

    public GifView(Context context) {
        super(context);
    }

    public GifView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GifView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setGifResource(int resourceId) {
        InputStream is = getResources().openRawResource(resourceId);
        movie = Movie.decodeStream(is);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (movie == null) return;

        long now = SystemClock.uptimeMillis();
        if (movieStart == 0) movieStart = now;

        int duration = movie.duration();
        if (duration == 0) duration = 1000;

        int relTime = (int) ((now - movieStart) % duration);
        movie.setTime(relTime);

        // Escalar el GIF para que llene la vista
        float scaleX = (float) getWidth()  / movie.width();
        float scaleY = (float) getHeight() / movie.height();
        float scale  = Math.min(scaleX, scaleY);

        float left = (getWidth()  - movie.width()  * scale) / 2f;
        float top  = (getHeight() - movie.height() * scale) / 2f;

        canvas.save();
        canvas.translate(left, top);
        canvas.scale(scale, scale);
        movie.draw(canvas, 0, 0);
        canvas.restore();

        // Redibujar continuamente para animar
        postInvalidateDelayed(16);
    }
}