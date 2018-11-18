package com.amyth_shekhar.spellotest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.view.View;

/**
 * Created by amyth_shekhar on 15-03-2018.
 */

public class BlurHelper {
    Context context;

    private BlurHelper(Context context) {
        this.context = context;
    }

    @NonNull
    public static BlurHelper builder(Context context){
        return new BlurHelper(context);
    }
    public Drawable blur(View backgroundView, View blurOnView, float radius) {
        View screenshot = backgroundView;
        View size = blurOnView;

        screenshot.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(
                screenshot.getDrawingCache()
                , (int) size.getX()
                , (int)size.getY()
                , size.getWidth()
                , size.getHeight());
        screenshot.setDrawingCacheEnabled(false);
        RenderScript rs = RenderScript.create(context);

        Allocation allocIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allocOut = Allocation.createFromBitmap(rs, bitmap);

        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(
                rs, Element.U8_4(rs));
        blur.setInput(allocIn);
        blur.setRadius(radius);
        blur.forEach(allocOut);
        bitmap.setHasAlpha(true);

        allocOut.copyTo(bitmap);
        rs.destroy();
        Drawable d = new BitmapDrawable(context.getResources(), bitmap);

        return d;
    }
}
