package com.pablito203.plugins.gallerycapacitorplugin;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class SelectedFile implements Parcelable {
    public int imageID;
    public int gridPosition;
    public String fileName;

    SelectedFile(int imageID, int imageRotate, int gridPosition, String fileName) {
        this.imageID = imageID;
        this.gridPosition = gridPosition;
        this.fileName = fileName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(imageID);
        dest.writeInt(gridPosition);
        dest.writeString(fileName);
    }

    public static final Parcelable.Creator<SelectedFile> CREATOR = new Parcelable.Creator<SelectedFile>() {
        @Override
        public SelectedFile createFromParcel(Parcel source) {

            return new SelectedFile(source);
        }

        @Override
        public SelectedFile[] newArray(int size) {
            return new SelectedFile[size];
        }
    };

    public SelectedFile(Parcel data) {
        this.imageID = data.readInt();
        this.gridPosition = data.readInt();
        this.fileName = data.readString();
    }
}
