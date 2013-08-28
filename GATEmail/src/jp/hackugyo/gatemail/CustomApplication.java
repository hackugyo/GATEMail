package jp.hackugyo.gatemail;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

import jp.hackugyo.gatemail.ui.view.ImageCacheManager;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class CustomApplication extends Application {

    public static final String REQUEST_TASK_CLEANUP_SCHEDULES = "REQUEST_TASK_CLEANUP_SCHEDULES";
    /** コンテキスト. */
    private static Context _context;
    /** プリファレンス. */
    private static SharedPreferences _sharedPreferences;
    /** 画像キャッシュ */
    private static jp.hackugyo.gatemail.ui.view.ImageCacheManager sImageCacheManager;

    /**
     * どこにも出力しない PrintStream<br>
     * {@link Exception#printStackTrace()}などがLogCatに漏れないようにする
     */
    private final PrintStream mEmptyStream = new PrintStream(new OutputStream() {
        @Override
        public void write(int oneByte) throws IOException {
            // do nothing
        }
    });

    /***********************************************
     * Life Cycle *
     ***********************************************/
    @Override
    public void onCreate() {
        super.onCreate();

        _context = getApplicationContext();
        initImageLoader(getApplicationContext());

        createStream();
    }

    /***********************************************
     * Other methods *
     ***********************************************/

    /**
     * アプリケーションコンテキストを取得します.
     * 
     * @return Application Context
     */
    public static Context getAppContext() {
        return _context;
    }

    /**
     * アプリケーションで利用するプリファレンスを取得します. 共有モードは Private となります.
     * 
     * @return SharedPreferences
     */
    public static SharedPreferences getSharedPreferences() {
        if (_sharedPreferences == null) {
            _sharedPreferences = getAppContext().getSharedPreferences(Defines.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        }
        return _sharedPreferences;
    }

    public static void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them, 
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory().discCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO).enableLogging() // Not necessary in common
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }

    public static Resources getResource() {
        return _context.getResources();
    }

    /**
     * {@link TypedArray}を取得して返します． 使い終わったら{@link TypedArray#recycle()}してください．
     * 
     * @param typedArrayId
     * @return TypedArray
     */
    @SuppressLint("Recycle")
    public static TypedArray getTypedArrayById(int typedArrayId) {
        return _context.getResources().obtainTypedArray(typedArrayId);
    }

    public static String getStringById(int stringId) {
        return _context.getResources().getString(stringId);
    }

    public static int getColorIntByColorId(int colorId) {
        return _context.getResources().getColor(colorId);
    }

    public static Drawable getDrawableById(int id) {
        return _context.getResources().getDrawable(id);
    }

    public static float getDimenById(int id) {
        return _context.getResources().getDimension(id);
    }

    public static int getDimensionPixelSizeById(int id) {
        return _context.getResources().getDimensionPixelSize(id);
    }

    public static Object getSystemServiceOf(String name) {
        return _context.getSystemService(name);
    }

    public static ArrayList<String> getStringArrayById(int id) {
        return new ArrayList<String>(Arrays.asList(CustomApplication.getAppContext().getResources().getStringArray(id)));
    }

    public static int getIntegerById(int id) {
        try {
            return _context.getResources().getInteger(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * アプリバージョンを返します．
     * 
     */
    public static String getAppVersionName() {
        String versionName = null;
        try {
            PackageInfo packageInfo = getAppContext().getPackageManager().getPackageInfo(getAppContext().getPackageName(), PackageManager.GET_ACTIVITIES);
            versionName = packageInfo.versionName;
        } catch (NameNotFoundException ignore) {
        }
        return versionName;
    }

    /**
     * アプリ全体で利用する画像キャッシュを返します．
     * この画像キャッシュはアプリアイコンだけを保存する想定のため，キーは，アプリのsearchKeyとします．
     * 
     * @return 画像キャッシュ（不変インスタンス）
     */
    public static ImageCacheManager getImageCacheManager() {
        return sImageCacheManager;
    }

    /**
     * スタックトレースがリリース後にLogCatに流出しないよう，出力先をハンドリングします．
     * 
     * @see <a href="http://www.jssec.org/dl/android_securecoding.pdf">参考ページ</a>
     */
    private void createStream() {
        // System.out/err の本来のストリームを退避する
        PrintStream savedOut = System.out;
        PrintStream savedErr = System.err;
        // 一旦、System.out/err をどこにも出力しない PrintStream にリダイレクトする
        System.setOut(mEmptyStream);
        System.setErr(mEmptyStream);
        // デバッグ時のみ本来のストリームに戻す(リリースビルドでは下記 1行が無視される)
        if (BuildConfig.DEBUG) resetStreams(savedOut, savedErr);
    }

    private void resetStreams(PrintStream savedOut, PrintStream savedErr) {
        System.setOut(savedOut);
        System.setErr(savedErr);
    }
}
