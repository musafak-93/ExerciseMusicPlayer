package com.example.exercisemusicplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.MediaController;


/* untuk control music seperti tombol play/pause,rewind,fast-forward
 dan skip (previous/next)*/
public class MusicController extends MediaController {
    public MusicController(Context context) {
        super(context);
    }

    public void hide(){}
}