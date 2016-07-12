package com.softwaremobility.objects;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.ByteArrayOutputStream;

/**
 * Created by darkgeat on 5/5/16.
 */
public class Complete_Image implements Parcelable{
    private byte[] imageInBytes;

    public Complete_Image(byte[] imageInBytes) {
        this.imageInBytes = imageInBytes;
    }

    public Complete_Image(Bitmap bitmap, Bitmap.CompressFormat format){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(format, 100, stream);
        imageInBytes = stream.toByteArray();
    }

    protected Complete_Image(Parcel in) {
        imageInBytes = in.createByteArray();
    }

    public static final Creator<Complete_Image> CREATOR = new Creator<Complete_Image>() {
        @Override
        public Complete_Image createFromParcel(Parcel in) {
            return new Complete_Image(in);
        }

        @Override
        public Complete_Image[] newArray(int size) {
            return new Complete_Image[size];
        }
    };

    public byte[] getImageInBytes() {
        return imageInBytes;
    }

    public void setImageInBytes(byte[] imageInBytes) {
        this.imageInBytes = imageInBytes;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(imageInBytes);
    }
}
