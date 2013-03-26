package jp.hackugyo.gatemail.ui.view;

import jp.hackugyo.gatemail.CustomApplication;
import jp.hackugyo.gatemail.util.ImageUtils;
import jp.hackugyo.gatemail.util.LogUtils;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;


/**
 * 画像をLRUキャッシュするクラスです．
 * 
 * @author kwatanabe
 * 
 */
public class ImageCacheManager {
    @SuppressWarnings("unused")
    private final ImageCacheManager self = this;

    /**
     * 画像キャッシュ
     */
    private LruCache<String, Bitmap> mBitmapMemoryCache;

    private LruCache<String, Bitmap> getMemoryCache() {
        if (mBitmapMemoryCache == null) {
            final int memClass = ((ActivityManager) CustomApplication.getAppContext().getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();

            // Use 1/8th of the available memory for this memory cache.  
            final int cacheSize = 1024 * 1024 * memClass / 8;

            mBitmapMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // The cache size will be measured in bytes rather than number  of items.  
                    return bitmap.getRowBytes() * bitmap.getHeight(); // bitmap.getByteCount();はAPI 12以降なので、そののかわり
                }
            };
        }
        return mBitmapMemoryCache;
    }

    /**
     * バナーキャッシュ保存
     * 
     * @param key
     *            :アプリのsearchKeyを用いる
     * @param bitmap
     */
    public void putBitmapToMemoryCache(String key, Bitmap bitmap) {
        LogUtils.v("save cache: " + key + ", size: " + ImageUtils.bitmapToByte(bitmap).length);
        getMemoryCache().put(key, bitmap);
    }

    /**
     * バナーキャッシュ取り出し
     * 
     * @param key
     *            : バナーのダウンロードに用いたloaderId番号を用いる
     */
    public Bitmap getBitmapFromMemCache(String key) {
        if (key == null) return null;
        Bitmap bitmap = getMemoryCache().get(key);
        if (bitmap == null) return null;
        LogUtils.v("load cache: " + key + ", size: " + ImageUtils.bitmapToByte(bitmap).length);
        return bitmap;
    }


    public static void clearMemoryCache() {
        CustomApplication.getImageCacheManager().getMemoryCache().evictAll();
    }

    public static void clearDiskCache() {
        // TODO ResourceDao dao = new ResourceDao();
        // TODO dao.deleteAll();
    }
}
