package com.example.exercisemusicplayer;

public class Song {

    // Variable untuk data yang ingin disimpan untuk setiap lagunya
    private long id;
    private String title;
    private String artist;

    // Konstruktor untuk memanggil variable
    public Song(long songID, String songTitle, String songArtist) {
        id=songID;
        title=songTitle;
        artist=songArtist;
    }

    // Untuk mengembalikan nilai variable yang ada
    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
}
