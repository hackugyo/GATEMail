package jp.hackugyo.gatemail.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.support.v4.app.Fragment;

public class BitmapUtils {
    /**
     * 指定したサイズでbitmapを読み込みます．
     * 
     * @param imgLocation
     * @param targetWidth
     * @param targetHeight
     * @throws IOException
     * @see <a
     *      href="http://lablog.lanche.jp/archives/192">http://lablog.lanche.jp/archives/192</a>
     */
    public static Bitmap getBitmapFrom(Uri imgLocation, ContentResolver contentResolver, int targetWidth, int targetHeight) throws IOException {
        Bitmap resultBitmap = null;

        BitmapFactory.Options opt = new BitmapFactory.Options();
        // オプション設定用のオブジェクト
        opt.inJustDecodeBounds = true;
        // 実際の画像本体は読まずにサイズ情報のみ取得するフラグをセット

        InputStream is = null;
        try {
            is = contentResolver.openInputStream(imgLocation);
            BitmapFactory.decodeStream(is, null, opt);
        } catch (FileNotFoundException e) {
            LogUtils.w("file not found.");
        } finally {
            if (is != null) is.close();
        }
        // サイズ情報を取得する
        // 高さ
        int scaleW = opt.outWidth / targetWidth;
        int scaleH = opt.outHeight / targetHeight;
        // 取得した画像サイズは
        // BitmapFactory.Options#outWidth
        // BitmapFactory.Options#outHeight
        // にそれぞれ入り、表示サイズで割ることで縮小時の整数比率を求める

        int sampleSize = Math.max(scaleW, scaleH);
        opt.inSampleSize = sampleSize;
        // 画像を何分の1で取得するかを求めます
        // 2の乗数に丸められるのでここでは3で指定しても2と同様に扱われる
        opt.inJustDecodeBounds = false;

        // 今度は実際に画像を取得するためのフラグをセット

        Bitmap bmp = null;
        try {
            is = contentResolver.openInputStream(imgLocation);
            bmp = BitmapFactory.decodeStream(is, null, opt);
        } catch (FileNotFoundException e) {
            LogUtils.w("file not found.");
        } finally {
            if (is != null) is.close();
        }

        if (bmp == null) {
            LogUtils.e("not found: " + imgLocation);
            return null;
        }

        // inSampleSizeに3を指定し、処理としては2の乗数に丸められるので2
        // つまり元画像の1/2で読み込まれる
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        float scale = Math.min((float) targetWidth / w, (float) targetHeight / h);
        // 最終的なサイズにするための縮小率を求める
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        // 画像変形用のオブジェクトに拡大・縮小率をセットし
        if (bmp != null) resultBitmap = Bitmap.createBitmap(bmp, 0, 0, w, h, matrix, true);
        // 取得した画像を元にして変形画像を生成・再設定

        return resultBitmap;
    }

    public static Bitmap getBitmapFromDrawableId(Context context, int drawableId) {
        return BitmapFactory.decodeResource(context.getResources(), drawableId);
    }

    public static Bitmap getBitmapFromDrawableId(Fragment fragment, int drawableId) {
        return BitmapFactory.decodeResource(fragment.getResources(), drawableId);
    }
}
