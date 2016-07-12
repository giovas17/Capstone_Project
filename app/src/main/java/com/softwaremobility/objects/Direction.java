package com.softwaremobility.objects;

import android.os.Parcel;
import android.os.Parcelable;

public class Direction implements Parcelable{
    private int sequence;
    private String direction;

    public Direction(){
        this.sequence = 0;
        this.direction = "";
    }

    protected Direction(Parcel in) {
        sequence = in.readInt();
        direction = in.readString();
    }

    public static final Creator<Direction> CREATOR = new Creator<Direction>() {
        @Override
        public Direction createFromParcel(Parcel in) {
            return new Direction(in);
        }

        @Override
        public Direction[] newArray(int size) {
            return new Direction[size];
        }
    };

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(sequence);
        dest.writeString(direction);
    }

}
