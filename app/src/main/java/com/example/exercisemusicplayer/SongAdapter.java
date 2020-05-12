package com.example.exercisemusicplayer;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

// Songadapter digunakan untuk memetakan lagu
public class SongAdapter extends BaseAdapter {

    // Untuk daftar lagu dan tata letak lagunya
    private ArrayList<Song> songs;
    private LayoutInflater songInf;

    //constructor
    public SongAdapter(Context c, ArrayList<Song> theSongs) {
        songs = theSongs;
        songInf = LayoutInflater.from(c);
    }


    //Method dibawah ini digunakan untuk memetakan lagu
    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Untuk tata letak lagu
        LinearLayout songLay = (LinearLayout)songInf.inflate
                (R.layout.song, parent, false);
        // Mendapatkan judul dan tampilan artis
        TextView songView = (TextView)songLay.findViewById(R.id.song_title);
        TextView artistView = (TextView)songLay.findViewById(R.id.song_artist);
        //Mendapatkan lagu menggunakan posisi
        Song currSong = songs.get(position);
        //megubah judul dan artis menjadi string
        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());
        // Mengatur posisi sebagai tag
        songLay.setTag(position);
        return songLay;
    }
}
