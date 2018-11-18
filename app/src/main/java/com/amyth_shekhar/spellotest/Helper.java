package com.amyth_shekhar.spellotest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.support.v4.content.ContextCompat;

/**
 * Created by amyth_shekhar on 22-02-2018.
 */

public class Helper {
    public static final String PLAYGROUP="Playgroup - Regular";
    public static final String NURSERY="Nursery - Regular";
    public static final String JUNIOR_KG="Junior KG - Regular";
    public static final String SENIOR_KG="Senior KG - Regular";
    public static final String EDIT_CHILD = "EDITCHILD";


    public static final int color_skg_picture_scene = Color.parseColor("#a443ff");
    public static final int color_skg_word_spelling = Color.parseColor("#0087ff");
    public static final int color_skg_phonics_exercise = Color.parseColor("#00b7c2");

    public static final int color_nur_pattern_tracing = Color.parseColor("#00b7c2");
    public static final int color_nur_letter_tracing = Color.parseColor("#ff7179");
    public static final int color_nur_phonics_exercise = Color.parseColor("#a443ff");
    public static final int color_nur_letter_recognition = Color.parseColor("#0087ff");

    public static final int color_pg_pattern_tracing= Color.parseColor("#00b7c2");
    public static final int color_pg_letter_recognition = Color.parseColor("#0087ff");

    public static final int color_jkg_word_spelling= Color.parseColor("#0087ff");
    public static final int color_jkg_picture_scene= Color.parseColor("#ff7179");
    public static final int color_jkg_phonics_exercise= Color.parseColor("#a443ff");
    public static final int color_jkg_pattern_tracing= Color.parseColor("#a443ff");
    public static final int color_jkg_letter_tracing= Color.parseColor("#b15eff");


    public static Drawable getImageResource(Context context, String imageName) throws Exception {
        return ContextCompat.getDrawable(context, context.getResources().getIdentifier(imageName, "drawable", context.getPackageName()));
    }

    public static int getSound(Context context , String soundName) {
        return context.getResources().getIdentifier(soundName , "raw",context.getPackageName());
    }

    public static void playSound(Context context , String soundName) throws Exception {
        MediaPlayer mp = MediaPlayer.create(context , context.getResources().getIdentifier(soundName , "raw", context.getPackageName()));
        mp.start();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
    }

    public static int getClassId(String className){
        if(className.equals(PLAYGROUP))
            return 0;
        else if(className.equals(NURSERY))
            return 1;
        else if(className.equals(JUNIOR_KG))
            return 2;
        else if(className.equals(SENIOR_KG))
            return 3;
        else
            return -1;
    }

    public static void showInformation(Context context, String info){
        new AlertDialog.Builder(context)
                .setMessage(info)
                .setTitle("Instructions")
                .setPositiveButton("OK, I GOT IT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }
}
