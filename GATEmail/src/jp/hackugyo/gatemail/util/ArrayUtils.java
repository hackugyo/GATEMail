package jp.hackugyo.gatemail.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Iterables;

public class ArrayUtils {

    /**
     * ArrayListインスタンスそのものは変更せず，中身だけをinitialArrayで完全に上書きします．
     * 
     * @param targetArray
     * @param initialArray
     * @return targetArray
     */
    public static <T> ArrayList<T> initArrayListWith(ArrayList<T> targetArray, ArrayList<T> initialArray) {
        if (targetArray == null) {
            LogUtils.v("  targetArray is Null. create new one( and return it).");
            targetArray = new ArrayList<T>();
        }
        // 同じインスタンスだった場合，前者をclearしてから後者を入れると，けっきょくぜんぶclearされてしまうので．
        if (targetArray == initialArray) return targetArray;

        targetArray.clear();
        if (initialArray != null) Iterables.addAll(targetArray, initialArray);
        return targetArray;
    }

    /**
     * 第1引数に第2引数を連結します．第1引数は破壊的に更新されます（インスタンスの参照を変えません）．
     * 
     * @param targetArray
     * @param initialArray
     * @return 第1引数と同じインスタンス
     */
    public static <T> List<T> concatList(List<T> targetArray, List<T> initialArray) {
        if (targetArray == null) {
            LogUtils.v("  targetArray is Null. create new one( and return it).");
            targetArray = new ArrayList<T>();
        }
        if (initialArray != null) Iterables.addAll(targetArray, initialArray);
        return targetArray;
    }

    /**
     * 
     * @param first
     * @param second
     * @return 結合した配列（引数の両方ともnullでない場合のみ）．どちらか片方でもnullであれば，null
     */
    public static <T> T[] concat(T[] first, T[] second) {
        if (first == null || second == null) return null;
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static boolean containTrue(boolean[] array) {
        for (boolean value : array) {
            if (value) return true;
        }
        return false;
    }

}
