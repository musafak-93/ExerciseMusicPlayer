package com.example.exercisemusicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;

import com.example.exercisemusicplayer.MusicService.MusicBinder;

public class MainActivity extends AppCompatActivity implements MediaController.MediaPlayerControl {

    //List variable lagu
    private ArrayList<Song> songList;
    private ListView songView;
    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound=false;
    private MusicController controller;
    private boolean paused=false, playbackPaused=false;
    public static final int PERMISSIONS_READ_EXTERNAL = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkPermissionREAD_EXTERNAL_STORAGE(this)) {
            songView = findViewById(R.id.song_list);
            songList = new ArrayList<Song>();
            getSongList();

            Collections.sort(songList, new Comparator<Song>(){
                @Override
                public int compare(Song a, Song b) {
                    return a.getTitle().compareTo(b.getTitle());
                }
            });
        }

        SongAdapter songAdapter = new SongAdapter(this, songList);
        songView.setAdapter(songAdapter);
        setController();
    }

    //Untuk memanggil menu di activity main
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // Action untuk menu shuffle dan end
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                //shuffle
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicService=null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getSongList() {
        // Untuk mengambil info lagu
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        @SuppressLint("Recycle") Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if(musicCursor!=null && musicCursor.moveToFirst()){
            // Menngambil column untuk lagu
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);

            // Menambahkan lagu pada listview
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }

    }

    // Menghubungkan ke service music
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            // Mengambil service
            musicService = binder.getService();
            // Mengambil lagu
            musicService.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    // Untuk memulai objek ketika objek dimulai
    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    // Mengambil lagu
    public void songPicked(View view){
        musicService.setSong(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    // Untuk mengakhiri aplikasi
    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicService=null;
        super.onDestroy();
    }

    // Mengatur controller music
    private void setController(){
        //set the controller up
        controller = new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            // Action ketika pengguna menakan tombol next
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            // Action ketika pengguna menekan tombol previous
            public void onClick(View v) {
                playPrev();
            }
        });

        // Tempat tampilan controller di panggil
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
    }

    // Untuk melewati lagu ke selanjutnya
    private void playNext(){
        musicService.playNext();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    // Untuk memutar music sebelumnya
    private void playPrev(){
        musicService.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }


    // Untuk menjeda music
    @Override
    protected void onPause(){
        super.onPause();
        paused=true;
    }


    // Untuk melanjutkan music kembali
    @Override
    protected void onResume(){
        super.onResume();
        if(paused){
            setController();
            paused=false;
        }
    }

    // Untuk Alert dialog izin aplikasi untuk akses memori external device
    public void showDialog(String msg, final Context context, final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        sb.append(" permission is necessary");
        alertBuilder.setMessage(sb.toString());

        alertBuilder.setPositiveButton(getCurrentPosition(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, MainActivity.PERMISSIONS_READ_EXTERNAL);
            }
        });

        alertBuilder.create().show();
    }

    // Untuk mengecek permission
    public boolean checkPermissionREAD_EXTERNAL_STORAGE(Context context) {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        String str = "android.permission.READ_EXTERNAL_STORAGE";
        if (ContextCompat.checkSelfPermission(context, str) == 0) {
            return true;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, str)) {
            showDialog("External storage", context, str);
        } else {
            ActivityCompat.requestPermissions((Activity) context, new String[]{str}, PERMISSIONS_READ_EXTERNAL);
        }
        return false;
    }

    // Menyembuyikan controller
    @Override
    protected void onStop() {
        controller.hide();
        super.onStop();
    }

    // akses ke musiservice
    @Override
    public void start() {
        musicService.go();
    }

    @Override
    public void pause() {
        playbackPaused=true;
        musicService.pausePlayer();
    }

    @Override
    public void seekTo(int pos) {
        musicService.seek(pos);
    }

    // Untuk mengetahui durasi musik yang diputar
    @Override
    public int getDuration() {
        if(musicService!=null && musicBound && musicService.isPng())
            return musicService.getDur();
        else return 0;
    }

    //Untuk mendapatkan posisi music saat ini
    @Override
    public int getCurrentPosition() {
        if(musicService!=null && musicBound && musicService.isPng())
            return musicService.getPosn();
        else return 0;
    }

    // action ketika  music di play
    @Override
    public boolean isPlaying() {
        if(musicService!=null && musicBound)
            return musicService.isPng();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

}
