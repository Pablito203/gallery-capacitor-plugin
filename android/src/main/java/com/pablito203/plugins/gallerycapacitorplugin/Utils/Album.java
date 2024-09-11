package com.pablito203.plugins.gallerycapacitorplugin.Utils;

public class Album {
    public int AlbumID;
    public String AlbumName;
    public int ThumbnailID;
    public int ThumbnailRotate;

    public Album(int albumID, String albumName, int thumbnailID, int thumbnailRotate) {
        AlbumID = albumID;
        AlbumName = albumName;
        ThumbnailID = thumbnailID;
        ThumbnailRotate = thumbnailRotate;
    }
}
