package com.pablito203.plugins.gallerycapacitorplugin.Utils;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class SelectedFile implements Parcelable {
    public int imageID;

    public SelectedFile(int imageID) {
        this.imageID = imageID;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(imageID);
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
    }
}
