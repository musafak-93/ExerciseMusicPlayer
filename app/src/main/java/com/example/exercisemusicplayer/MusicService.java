package com.example.exercisemusicplayer;

import android.app.Service;
import android.media.MediaPlayer;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener{

}
