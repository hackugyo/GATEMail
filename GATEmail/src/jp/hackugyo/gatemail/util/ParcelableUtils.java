package jp.hackugyo.gatemail.util;

import java.util.ArrayList;

import android.os.Parcelable;

public class ParcelableUtils {
    /**
     * Parcelableで，ArrayListを読む際には，このメソッドを利用してください．<br>
     * 単純にキャストしてもだめです．<br>
     * 
     * @param result
     * @param parcelableArray
     * @see <a
     *      href="http://stackoverflow.com/questions/10071502/how-to-read-write-array-of-parcelable-objects-android">参考ページ</a>
     * @return ArrayList
     */
    @SuppressWarnings("unchecked")
    public static <T> ArrayList<T> readParcelableArray(ArrayList<T> result, Parcelable[] parcelableArray) {

        if (parcelableArray != null) {
            if (result == null) result = new ArrayList<T>();
            result.clear();
            for (int i = 0; i < parcelableArray.length; ++i) {
                result.add((T) parcelableArray[i]);
            }
        } else {
            result = null;
        }
        return result;
    }
}
