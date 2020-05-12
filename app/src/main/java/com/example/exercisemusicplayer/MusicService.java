package com.example.exercisemusicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import java.util.ArrayList;
import java.util.Random;
import android.content.ContentUris;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener{

    //media player
    private MediaPlayer player;
    //Daftar lagu
    private ArrayList<Song> songs;
    //current position
    private int songPos;
    private final IBinder musicBind = new MusicBinder();
    private String songTitle="";
    private static final int NOTIFY_ID=1;
    private boolean shuffle=false;
    private Random rand;

    public void onCreate(){
        // Membuat service
        super.onCreate();
        // Inisialisasi posisi
        songPos=0;
        //membuat tampilan
        player = new MediaPlayer();
        initMusicPlayer();

        rand=new Random();
    }

    // Untuk button shuflle
    public void setShuffle(){
        if(shuffle) shuffle=false;
        else shuffle=true;
    }

    public void initMusicPlayer(){
        // Mengatur properti yang dibutuhkan
        // Wake Lock memungkinkan bermain musik ketika dalam dalam mode siaga
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    // Untuk mengrimkan ke playlist
    public void setList(ArrayList<Song> theSongs){
        songs=theSongs;
    }


    // Memanggil method Musicservice
    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    // Memanggil method setForeground
    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(player.getCurrentPosition()>0){
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //mulai playbac
        mp.start();
    }

    public void setSong(int songIndex){
        songPos=songIndex;
    }

    public void playSong(){
        player.reset();
        //Mendapatkan lagu
        Song playSong = songs.get(songPos);
        //Mendapatkan id
        long currSong = playSong.getID();
        //Mengatur uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        // alternative jika url eror
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
    }

    // Method dibawah ini digunakan untuk control music
    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        player.start();
    }

    public void playPrev(){
        songPos--;
        if(songPos<0) songPos=songs.size()-1;
        playSong();
    }

    // Mengaktifkan pengaturan shuffle dan mematikannya
    public void playNext(){
        if(shuffle){
            int newSong = songPos;
            while(newSong==songPos){
                newSong=rand.nextInt(songs.size());
            }
            songPos=newSong;
        }
        else{
            songPos++;
            if(songPos>=songs.size()) songPos=0;
        }
        playSong();
    }

}
