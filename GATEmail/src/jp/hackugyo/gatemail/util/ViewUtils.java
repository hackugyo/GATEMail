package jp.hackugyo.gatemail.util;

import java.lang.reflect.Method;

import jp.hackugyo.gatemail.CustomApplication;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;

public class ViewUtils {

    /**
     * dipsからpxの値に変更します。
     * 
     * @param dip
     * @return pxに変換した値
     */
    public static int dipsToPixel(int dip) {
        float scale = CustomApplication.getAppContext().getResources().getDisplayMetrics().density;
        return (int) (dip * scale);
    }

    /**
     * {@link Activity#setContentView(int)}したViewを取得します．
     * 
     */
    public static View getContentView(Activity activity) {
        return ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
    }

    /***********************************************
     * idからViewに設定 *
     **********************************************/

    /**
     * ViewにOnClickListenerを設定します．
     * 
     * @param rootView
     * @param viewId
     * @param listener
     * @return View or null
     */
    public static View setOnClickListener(View rootView, int viewId, View.OnClickListener listener) {
        View v = rootView.findViewById(viewId);
        if (v != null) v.setOnClickListener(listener);
        return v;
    }

    /**
     * ViewにOnClickListenerを設定します．
     * 
     * @param activity
     * @param viewId
     * @param listener
     * @return View or null
     */
    public static View setOnClickListener(Activity activity, int viewId, View.OnClickListener listener) {
        View v = activity.findViewById(viewId);
        if (v != null) v.setOnClickListener(listener);
        return v;
    }

    /**
     * ViewにVisibility:GONEを設定します．
     * 
     * @param rootView
     * @param viewIds
     */
    public static void setVisibilityGone(View rootView, int... viewIds) {
        for (int viewId : viewIds) {
            View v = rootView.findViewById(viewId);
            if (v != null) v.setVisibility(View.GONE);
        }
    }

    /***********************************************
     * Viewサイズ設定 *
     **********************************************/
    /**
     * {@link ViewGroup#setLayoutParams(android.view.ViewGroup.LayoutParams)}
     * がClassCastExceptionを起こしがちなので，<br>
     * このメソッド経由で呼んでください．<br>
     * 
     * @param view
     * @param w
     *            nullのときは，元の値をそのまま使う
     * @param h
     *            nullのときは，元の値をそのまま使う
     */
    public static void setLayoutParams(View view, Integer w, Integer h) {
        if (view == null) return;
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params != null) {
            if (w != null) params.width = w;
            if (h != null) params.height = h;
        } else {
            params = new ViewGroup.LayoutParams(w, h);
        }
        try {
            view.setLayoutParams(params);
        } catch (ClassCastException e) {
            LogUtils.e("cannot set LayoutParams." + e.toString());
        }
    }

    /***********************************************
     * Viewサイズ取得 *
     **********************************************/
    /**
     * {@link Display#getWidth()} がdeprecatedになったので，SDK_INTによって処理を変えます．
     * 
     * @param display
     * @return displayの幅（displayがnullのとき0)
     */
    @SuppressWarnings({ "deprecation", "javadoc" })
    public static int getDisplayWidth(Display display) {
        if (display == null) return 0;
        Point outSize = new Point();
        try {
            // test for new method to trigger exception
            @SuppressWarnings("rawtypes")
            Class pointClass = Class.forName("android.graphics.Point");
            Method newGetSize = Display.class.getMethod("getSize", new Class[] { pointClass });

            // no exception, so new method is available, just use it
            newGetSize.invoke(display, outSize);
        } catch (Exception ex) {
            // new method is not available, use the old ones
            outSize.x = display.getWidth();
        }
        return outSize.x;
    }

    /**
     * {@link Display#getHeight()} がdeprecatedになったので，SDK_INTによって処理を変えます．
     * 
     * @param display
     * @return displayの幅（displayがnullのとき0)
     */
    @SuppressWarnings({ "deprecation", "javadoc" })
    public static int getDisplayHeight(Display display) {
        if (display == null) return 0;
        Point outSize = new Point();
        try {
            // test for new method to trigger exception
            @SuppressWarnings("rawtypes")
            Class pointClass = Class.forName("android.graphics.Point");
            Method newGetSize = Display.class.getMethod("getSize", new Class[] { pointClass });

            // no exception, so new method is available, just use it
            newGetSize.invoke(display, outSize);
        } catch (Exception ex) {
            // new method is not available, use the old ones
            outSize.y = display.getHeight();
        }
        return outSize.y;
    }

    /**
     * 
     * @param display
     * @return density (引数がnullの場合0f);
     */
    public static float getDisplayDensity(Display display) {
        if (display == null) return 0f;
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics.density;
    }

    /**
     * {@link View#setBackgroundDrawable(Drawable)}
     * がdeprecatedになったので，SDK_INTによって処理を変えます．
     * 
     * @param view
     * @param drawable
     * @return 第1引数のView
     */
    public static View setBackgroundDrawable(View view, int drawableId) {
        return setBackgroundDrawable(view, CustomApplication.getDrawableById(drawableId));
    }

    /**
     * {@link View#setBackgroundDrawable(Drawable)}
     * がdeprecatedになったので，SDK_INTによって処理を変えます．
     * 
     * @param view
     * @param drawable
     * @return 第1引数のView
     */
    public static View setBackgroundDrawable(View view, Drawable drawable) {
        if (drawable == null) {
            view.setBackgroundResource(0);
            return view;
        }

        String methodName = (Build.VERSION.SDK_INT >= 16 ? "setBackground" : "setBackgroundDrawable");
        Method setBackground;
        try {
            Class<?> partypes[] = new Class[1];
            partypes[0] = Drawable.class;
            setBackground = ImageView.class.getMethod(methodName, partypes);
            setBackground.invoke(view, new Object[] { drawable });
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.w("ImageView#" + methodName + "(Drawable) isn't available in this devices api");
        }
        return view;
    }

    /***********************************************
     * メモリ解放 *
     **********************************************/

    /**
     * 指定したビュー階層内の{@link Drawable}をクリアします． {@link Bitmap#recycle}
     * 
     * @param view
     */
    public static final void cleanupView(View view) {
        cleanupViewWithImage(view);
        cleanUpOnClickListeners(view);
        cleanUpAdapter(view);
        // 再帰的に処理
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            int size = vg.getChildCount();
            for (int i = 0; i < size; i++) {
                cleanupView(vg.getChildAt(i));
            }
        }
    }

    /**
     * Viewをきれいにする．（Image・Backgroundなど）<br>
     * {@link Drawable}のrecycleはしていません．
     * 
     * @param view
     * @return 引数のviewを返す
     */
    public static View cleanupViewWithImage(View view) {
        if (view instanceof ImageButton) {
            ImageButton ib = (ImageButton) view;
            ib.setImageDrawable(null);
        } else if (view instanceof ImageView) {
            ImageView iv = (ImageView) view;
            iv.setImageDrawable(null);
        } else if (view instanceof SeekBar) {
            SeekBar sb = (SeekBar) view;
            sb.setProgressDrawable(null);
            sb.setThumb(null);
            // } else if(view instanceof( xxxx )) {  -- 他にもDrawableを使用するUIコンポーネントがあれば追加 
        }
        return setBackgroundDrawable(view, null);
    }

    /**
     * OnClickListenerを解放する．
     * 
     * @param view
     * @see <a
     *      href="http://htomiyama.blogspot.jp/2012/08/androidoutofmemoryerror.html">参考ページ</a>
     */
    @SuppressWarnings({ "rawtypes" })
    private static void cleanUpOnClickListeners(View view) {
        if (view instanceof AdapterView) {
            // AdapterView（ListViewのような）に対しては，setOnClickListener()を呼んではいけない．
            ((AdapterView) view).setOnItemClickListener(null);
            ((AdapterView) view).setOnItemLongClickListener(null);
            ((AdapterView) view).setOnItemSelectedListener(null);
        } else {
            view.setOnClickListener(null);
            view.setOnLongClickListener(null);
            view.setOnTouchListener(null);
        }
    }

    /**
     * Adapterを解放する．
     * 
     * @param view
     * @see <a
     *      href="http://htomiyama.blogspot.jp/2012/08/androidoutofmemoryerror.html">参考ページ</a>
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void cleanUpAdapter(View view) {
        if (view instanceof AdapterView) {
            try {
                ((AdapterView) view).setAdapter(null);
            } catch (IllegalArgumentException e) {
                LogUtils.v(e.toString()); // CustomなAdapterの場合など，nullをセットすると失敗する場合があるので
            } catch (NullPointerException e) {
                LogUtils.v(e.toString()); // CustomなAdapterの場合など，nullをセットすると失敗する場合があるので
            }
        }
    }

    public static void recycleImageView(ImageView view) {
        Drawable toRecycle = view.getDrawable();
        recycleDrawable(toRecycle);
        view.setImageBitmap(null);
    }

    /**
     * {@link Drawable}をrecycleします．<br>
     * これを使ったら，利用しているViewに対して直後に{@link #cleanupViewWithImage(View)}を呼んでください．<br>
     * 同じbitmapを使っているdrawableが複数存在する場合があるので，注意して呼び出してください．<br>
     * 
     * @param toRecycle
     */
    public static void recycleDrawable(Drawable toRecycle) {
        Bitmap image = getBitmapFrom(toRecycle);
        if (image != null) image.recycle();
    }

    /**
     * drawableからBitmapを取得します．drawableは{@link BitmapDrawable}とは限らないので，<br>
     * このメソッド経由で取得してください．
     * 
     * @param drawable
     * @return bitmap or null
     */
    public static Bitmap getBitmapFrom(Drawable drawable) {
        if (drawable == null) return null;
        Bitmap image = null;
        if (drawable instanceof StateListDrawable) {
            // StateListViewに設定されているdrawableが画像であることを前提にしている
            // そうでない場合失敗するので再帰
            image = getBitmapFrom(((StateListDrawable) drawable).getCurrent());
        } else if (drawable instanceof BitmapDrawable) {
            image = ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof NinePatchDrawable) {
            LogUtils.v("I am an NinePathcDrawable. Nothing to do to fit width.");
        } else {
            LogUtils.v("I am an " + drawable.getClass().getCanonicalName() + ". Nothing to do to fit width.");
        }
        return image;
    }

    /***********************************************
     * 画面高さ *
     **********************************************/

    public static int getStatusBarHeight(Activity activity) {
        Rect outRect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);
        return outRect.top;
    }

    public static int getTitleBarHeight(Activity activity) {
        int contentViewTop = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int titleBarHeight = contentViewTop - getStatusBarHeight(activity);
        return titleBarHeight;
    }

    public static int getNavigationBarHeight(Activity activity) {
        return Math.max(0, //
                getRealHeight(activity) - getDisplayHeightWithoutNavigationBar(activity));
    }

    /**
     * ステータスバー，タイトルバー，ナビゲーションバーを除いた部分の高さを返します．
     * 
     * @param activity
     * @return 描画領域の高さ
     */
    public static int getContentRootHeight(Activity activity) {
        return getContentView(activity).getHeight();
    }

    public static int getDensityDpi(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.densityDpi;
    }

    /***********************************************
     * private methods*
     **********************************************/

    /**
     * ステータスバー+タイトルバー+コンテンツ領域 を合わせた高さを返します
     * 
     * @param activity
     * @return ステータスバー+タイトルバー+コンテンツ領域 を合わせた高さ
     */
    private static int getDisplayHeightWithoutNavigationBar(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        int height = getDisplayHeight(display);
        return height;
    }

    /**
     * 画面全体の高さ（ハードウェアサイズ）を返します．
     * 
     * @param activity
     * @return 画面全体の高さ
     */
    private static int getRealHeight(Activity activity) {

        Display display = activity.getWindowManager().getDefaultDisplay();
        Point real = new Point(0, 0);

        if (Build.VERSION.SDK_INT >= 17) {
            // Android 4.2以上
            try {
                // test for new method to trigger exception
                @SuppressWarnings("rawtypes")
                Class pointClass = Class.forName("android.graphics.Point");
                Method newGetSize = Display.class.getMethod("getRealSize", new Class[] { pointClass });
                newGetSize.invoke(display, real); // display.getRealSize(real);
                return real.y;
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }

        } else if (Build.VERSION.SDK_INT >= 13) {
            // Android 3.2以上
            try {
                Method getRawWidth = Display.class.getMethod("getRawWidth");
                Method getRawHeight = Display.class.getMethod("getRawHeight");
                int width = (Integer) getRawWidth.invoke(display);
                int height = (Integer) getRawHeight.invoke(display);
                real.set(width, height);
                return real.y;
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
        }

        return real.y;
    }
}
