package com.amyth_shekhar.spellotest;

import java.util.HashMap;
import java.util.Map;

public class SpelloData {
    static private Map<Integer, String> spellings = new HashMap<>();
    static private Map<Integer, Integer> images = new HashMap<>();

    private static int counter = 0;
    public static String getSpellings(){
        return spellings.get(counter);
    }

    public static int getImage(){
        return images.get(counter);
    }

    public static void updateCounter(){
        counter++;
        if(counter == spellings.size())
            counter =0;
    }
}
