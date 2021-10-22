package com.morteza.screen.common;

import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import java.io.PrintWriter;

public class DisplayInfo implements Parcelable {

    public int x;
    public int y;
    public DisplayMetrics metrics;

    public DisplayInfo(WindowManager wm) {
        this.metrics = new DisplayMetrics();
        Point size = new Point();
        Display display = wm.getDefaultDisplay();
        display.getRealMetrics(metrics);
        display.getRealSize(size);
        this.x = size.x;
        this.y = size.y;
    }

    public DisplayInfo() {
        metrics = new DisplayMetrics();
    }

    public DisplayInfo(int x, int y, DisplayMetrics metrics) {
        this.x = x;
        this.y = y;
        this.metrics = metrics;
    }

    public DisplayInfo(@NonNull DisplayInfo src) {
        this.x = src.x;
        this.y = src.y;
        this.metrics = src.metrics;
    }

    /**
     * Set the DisplayInfo's x and y coordinates
     */
    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Negate the DisplayInfo's coordinates
     */
    public final void negate() {
        x = -x;
        y = -y;
    }

    /**
     * Offset the DisplayInfo's coordinates by dx, dy
     */
    public final void offset(int dx, int dy) {
        x += dx;
        y += dy;
    }

    /**
     * Returns true if the DisplayInfo's coordinates equal (x,y)
     */
    public final boolean equals(int x, int y) {
        return this.x == x && this.y == y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DisplayInfo displayInfo = (DisplayInfo) o;

        if (x != displayInfo.x) return false;
        if (y != displayInfo.y) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    @Override
    public String toString() {
        return "DisplayInfo(" + x + ", " + y + ")";
    }

    /**
     * @hide
     */
    public void printShortString(@NonNull PrintWriter pw) {
        pw.print("[");
        pw.print(x);
        pw.print(",");
        pw.print(y);
        pw.print("]");
    }

    /**
     * Parcelable interface methods
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Write this DisplayInfo to the specified parcel. To restore a DisplayInfo from
     * a parcel, use readFromParcel()
     *
     * @param out The parcel to write the DisplayInfo's coordinates into
     */
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(x);
        out.writeInt(y);
    }


    public static final @NonNull
    Parcelable.Creator<DisplayInfo> CREATOR = new Parcelable.Creator<DisplayInfo>() {
        /**
         * Return a new DisplayInfo from the data in the specified parcel.
         */
        @Override
        public DisplayInfo createFromParcel(Parcel in) {
            DisplayInfo r = new DisplayInfo();
            r.readFromParcel(in);
            return r;
        }

        /**
         * Return an array of rectangles of the specified size.
         */
        @Override
        public DisplayInfo[] newArray(int size) {
            return new DisplayInfo[size];
        }
    };

    /**
     * Set the DisplayInfo's coordinates from the data stored in the specified
     * parcel. To write a DisplayInfo to a parcel, call writeToParcel().
     *
     * @param in The parcel to read the DisplayInfo's coordinates from
     */
    public void readFromParcel(@NonNull Parcel in) {
        x = in.readInt();
        y = in.readInt();
    }

    /**
     * {@hide}
     */
    public static @NonNull
    DisplayInfo convert(@NonNull Size size) {
        return new DisplayInfo(size.getWidth(), size.getHeight(), new DisplayMetrics());
    }

    /**
     * {@hide}
     */
    public static @NonNull
    Size convert(@NonNull DisplayInfo displayInfo) {
        return new Size(displayInfo.x, displayInfo.y);
    }
}
