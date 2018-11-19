package com.amyth_shekhar.spellotest;

import java.util.HashMap;
import java.util.Map;

public class SpelloData {
    static private Map<Integer, String> spellings ;
    static private Map<Integer, Integer> images ;

    private static int counter = 0;
    public static String getSpellings(){
        if(spellings == null)
            initialize();
        return spellings.get(counter);
    }

    public static int getImage(){
        if(images == null)
            initialize();
        return images.get(counter);
    }

    public static void updateCounter(){
        counter++;
        if(counter == spellings.size())
            counter =0;
    }

    private static void initialize(){
        spellings = new HashMap<>();
        spellings.put(0,"airplane");
        spellings.put(1,"alligator");
        spellings.put(2,"balloon");
        spellings.put(3,"camp");
        spellings.put(4,"candle");
        spellings.put(5,"dog");
        spellings.put(6,"doll");
        spellings.put(7,"egg");
        spellings.put(8,"elbow");
        spellings.put(9,"fat");
        spellings.put(10,"feather");
        spellings.put(11,"giraffe");
        spellings.put(12,"girl");
        spellings.put(13,"hen");
        spellings.put(14,"horse");
        spellings.put(15,"igloo");
        spellings.put(16,"ink");
        spellings.put(17,"jug");
        spellings.put(18,"jupiter");
        spellings.put(19,"kettle");
        spellings.put(20,"key");
        spellings.put(21,"lion");

        images = new HashMap<>();
        images.put(0,R.drawable.airplane);
        images.put(1,R.drawable.alligator);
        images.put(2,R.drawable.balloon);
        images.put(3,R.drawable.camp);
        images.put(4,R.drawable.candle);
        images.put(5,R.drawable.dog);
        images.put(6,R.drawable.doll);
        images.put(7,R.drawable.egg);
        images.put(8,R.drawable.elbow);
        images.put(9,R.drawable.fat);
        images.put(10,R.drawable.feather);
        images.put(11,R.drawable.giraffe);
        images.put(12,R.drawable.girl);
        images.put(13,R.drawable.hen);
        images.put(14,R.drawable.horse);
        images.put(15,R.drawable.igloo);
        images.put(16,R.drawable.ink);
        images.put(17,R.drawable.jug);
        images.put(18,R.drawable.jupiter);
        images.put(19,R.drawable.kettle);
        images.put(20,R.drawable.key);
        images.put(21,R.drawable.lion);
    }
}
