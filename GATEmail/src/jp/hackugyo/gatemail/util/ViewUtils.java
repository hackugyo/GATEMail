package jp.hackugyo.gatemail.util;

import java.lang.reflect.Method;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;

public class ViewUtils {
    /**
     * {@link View#setBackgroundDrawable(Drawable)}
     * がdeprecatedになったので，SDK_INTによって処理を変えます．
     * 
     * @param view
     * @param drawable
     * @return 第1引数のView
     */
    @SuppressWarnings("javadoc")
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

    /**
     * {@link Drawable}をrecycleします．<br>
     * これを使ったら，利用しているViewに対して直後に{@link #cleanupViewWithImage(View)}を呼んでください．<br>
     * 同じbitmapを使っているdrawableが複数存在する場合があるので，注意して呼び出してください．<br>
     * 
     * @param drawable
     */
    public static void recycleDrawable(Drawable drawable) {
        if (drawable == null) return;
        if (!(drawable instanceof BitmapDrawable)) return;
        BitmapDrawable bd = (BitmapDrawable) drawable;
        Bitmap bitmap = bd.getBitmap();
        if (bitmap != null) bitmap.recycle();
    }
}
