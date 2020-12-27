package com.github.barteksc.sample.PDFUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

import com.github.barteksc.sample.R;

import java.io.ByteArrayOutputStream;
import java.util.Random;

public class PublicFunction {

    public static int getRandomNumber() {

        int min = 10000;
        int max = 50000;

        Random rand = new Random();

        return rand.nextInt((max - min) + 1) + min;
    }

    public static byte[] getByteFromDrawable(Context context, int resDrawable) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resDrawable);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static byte[] generateCommentBox1(Context context) {

        float boxStart = 0;
        float boxWidth = 300;
        float boxHeight = 100;

        Bitmap bitmapMain = Bitmap.createBitmap((int) boxWidth, (int) boxHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapMain);

        Paint basePaint = new Paint();
        basePaint.setColor(Color.rgb(7, 143, 82));
        basePaint.setStyle(Paint.Style.FILL);

        Paint gradientPaint = new Paint();
        Shader shader = new LinearGradient(0, 0, 0, 100,
                Color.rgb(0, 62, 52),
                Color.rgb(68, 115, 108),
                Shader.TileMode.CLAMP);
        gradientPaint.setShader(shader);

        canvas.drawRect(new RectF(boxStart, boxStart, boxWidth, boxHeight), gradientPaint);

        basePaint.setColor(Color.LTGRAY);
        canvas.drawLine(100, 20, 100, 80, basePaint);

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_logo);
        canvas.drawBitmap(bitmap, null, new RectF(30, 30, 80, 80), null);

        Paint mTextPaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(pxFromDp(context, 10));

        canvas.drawText("1399/03/23", 130, 45, mTextPaint);
        canvas.drawText("10:31 PM", 130, 75, mTextPaint);

        mTextPaint.setColor(Color.rgb(225, 124, 49));
        canvas.drawCircle(75, 30, 15, mTextPaint);

        mTextPaint.setColor(Color.WHITE);
        canvas.drawText("3", 70, 36, mTextPaint);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmapMain.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        bitmapMain.recycle();

        return byteArray;

    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

}
