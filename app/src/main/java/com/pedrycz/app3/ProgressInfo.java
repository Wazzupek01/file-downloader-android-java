package com.pedrycz.app3;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class ProgressInfo implements Parcelable {

    public int downloaded;
    public int size;

    public int progress;
    public String status;

    public ProgressInfo(Parcel parcel) {
        downloaded = parcel.readInt();
        size = parcel.readInt();
    }

    public ProgressInfo(){
        this.downloaded = 0;
        this.size = 0;
        this.progress = 0;
        this.status = "";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(downloaded);
        parcel.writeInt(size);
    }

    public static final Parcelable.Creator<ProgressInfo> CREATOR = new Parcelable.Creator<>() {
        @Override
        public ProgressInfo createFromParcel(Parcel parcel) {
            return new ProgressInfo(parcel);
        }

        @Override
        public ProgressInfo[] newArray(int i) {
            return new ProgressInfo[i];
        }
    };
}
