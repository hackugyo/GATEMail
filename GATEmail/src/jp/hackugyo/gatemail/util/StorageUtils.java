package jp.hackugyo.gatemail.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

import android.os.Environment;

/**
 * SDカードのパスを返すメソッドです．
 * 
 * 
 */
public class StorageUtils {
    /** vold.fstabファイルのリスト */
    private static final String[] FSTAB_FILES = { "/system/etc/vold.fstab", "/etc/vold.fstab" };

    /***********************************************
     * public methods *
     ***********************************************/

    /**
     * マウント中の外部ストレージのファイルオブジェクトを返します。
     */
    public static File getMountedExternalStorage() {
        File file = null;
        String path = getMountedExternalStoragePath();
        if (StringUtils.isPresent(path)) file = new File(path);

        return file;
    }

    /**
     * マウント中の外部ストレージのパスを返します。
     */
    public static String getMountedExternalStoragePath() {
        String path = "";
        ArrayList<String> paths = getMountedStoragePathsWithoutInnerStorage();

        if (path != null && !paths.isEmpty()) {
            path = paths.get(0);
        }

        return path;
    }

    /***********************************************
     * private methods*
     ***********************************************/
    /**
     * ストレージのパスの一覧を取得します
     */
    private static ArrayList<String> getStoragePaths() {
        ArrayList<String> mountList = new ArrayList<String>();

        Scanner scanner = null;
        try {
            File fstabFile = getFStabFile();
            if (fstabFile == null) throw new FileNotFoundException();

            scanner = new Scanner(new FileInputStream(fstabFile));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                // dev_mountのある行にストレージがあるのが普通だが，
                // Xperia VL SOL21ではfuse_mountにあるようなので修正
                // http://d.tflare.com/2013/01/04/125653/
                if (line.startsWith("dev_mount") || line.startsWith("fuse_mount")) {
                    // dev_mount のパスを登録する (同じパスは登録しない)
                    String path = line.split(" ")[2];

                    // "/mnt/sdcard:/mnt/usbdisk:/mnt/sdcard/external_sd"となっているのを"mnt/sdcard"に絞るための処理
                    // 例えばIS12Mでは， /mnt/sdcard-ext:none:lun1 が入っている
                    // http://blog.tappli.com/article/44620525.html
                    path = path.replaceAll(":.*$", "");

                    LogUtils.v(">>>>>>>>>>>> storage path : " + path);
                    if (!mountList.contains(path)) {
                        File dir = new File(path + "/");
                        if (dir.canRead()) mountList.add(path);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            LogUtils.w(e.toString());
            // vold.fstabが見つからない場合は，何もせずあきらめる
            // mountList.clear();
            // mountList.add(Environment.getExternalStorageDirectory().getAbsolutePath());
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }

        return mountList;
    }

    /**
     * "/system/etc/vold.fstab"ファイルが存在しない場合がある(AOKP等のカスタムROMを導入している場合に該当)ので，
     * その場合"/etc/vold.fstab"も探しにいくようにした
     * 
     * @see <a href="http://hascha.blogspot.jp/2013/01/blog-post.html">参考ページ</a>
     */
    private static File getFStabFile() {
        File fstabFile = null;

        for (int i = 0; i < FSTAB_FILES.length; i++) {
            fstabFile = new File(FSTAB_FILES[i]);

            if (fstabFile.exists()) {
                break;
            }
        }
        return fstabFile;
    }

    /**
     * 内蔵ストレージを除いた、ストレージのパスの一覧を取得します。
     */
    private static ArrayList<String> getStoragePathsWithoutInnerStorage() {
        ArrayList<String> mountList = getStoragePaths();

        // ストレージのパスが2つ以上見つかった場合は、内蔵ストレージの方をカットする。
        // 
        // NOTE: 
        // getExternalStorageDirectoryは内蔵ストレージと外付けSDカードが存在する場合は、
        // 内蔵ストレージのパスを取得する。
        if (mountList.size() >= 2) {
            mountList.remove(Environment.getExternalStorageDirectory().getPath());
        }

        for (String path : mountList) {
            LogUtils.v(">>>>>>>>>>>>>> mount Path : " + path);
        }

        return mountList;
    }

    /**
     * マウント中のストレージの一覧を取得します
     */
    @SuppressWarnings("unused")
    private static ArrayList<String> getMountedStoragePaths() {
        ArrayList<String> mountList = getStoragePaths();

        // return trimUnmountStoragePaths(mountList);
        return trimUnmountStoragePathsPlusUnRegisteredButMounted(mountList);
    }

    /**
     * 内蔵ストレージを除いた、マウント中のストレージのパスの一覧を取得します
     */
    private static ArrayList<String> getMountedStoragePathsWithoutInnerStorage() {
        ArrayList<String> mountList = getStoragePathsWithoutInnerStorage();

        // return trimUnmountStoragePaths(mountList);
        return trimUnmountStoragePathsPlusUnRegisteredButMounted(mountList);
    }

    @SuppressWarnings("unused")
    private static ArrayList<String> trimUnmountStoragePaths(ArrayList<String> storagePaths) {
        for (int i = 0; i < storagePaths.size(); i++) {
            if (!isMounted(storagePaths.get(i))) {
                storagePaths.remove(i--);
            }
        }

        return storagePaths;
    }

    private static boolean isMounted(String path) {
        boolean isMounted = false;

        Scanner scanner = null;
        File file = new File("/proc/mounts");
        try {
            scanner = new Scanner(new FileInputStream(file));
            while (scanner.hasNextLine()) {
                if (scanner.nextLine().contains(path)) {
                    isMounted = true;
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }

        return isMounted;
    }

    /**
     * storagePathsから，マウントされているものだけを取得して返します．
     * ただし，マウントされているのにstoragePathsに入ってないパスがあった場合，
     * デフォルトストレージ直下の非隠しファイルの場合のみ，追加します．
     * 
     * @param storagePaths
     */
    private static ArrayList<String> trimUnmountStoragePathsPlusUnRegisteredButMounted(ArrayList<String> storagePaths) {
        ArrayList<String> mountedPaths = new ArrayList<String>();
        BufferedReader reader = null;

        try {
            Process process = Runtime.getRuntime().exec("mount");
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;

            while ((line = reader.readLine()) != null) {
                String mountPoint = line.split("\\s+")[1]; // 任意の空白文字（スペース、タブ、改行、復帰）の繰り返し
                if (storagePaths.contains(mountPoint)) {
                    mountedPaths.add(mountPoint);
                } else if (isDirectlyUnderDefaultStorage(mountPoint)) {
                    mountedPaths.add(mountPoint);
                }
            }
            process.waitFor();
        } catch (IOException e) {
            LogUtils.w(e.toString());
        } catch (InterruptedException e) {
            LogUtils.w(e.toString());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
        return mountedPaths;
    }

    /**
     * 指定されたマウントパスが，「デフォルトストレージの直下（隠しファイルでない）」であるかどうかを返します．
     * 
     * @param mountPoint
     * @return true: デフォルトストレージの直下
     */
    private static boolean isDirectlyUnderDefaultStorage(String mountPoint) {
        String defPath = Environment.getExternalStorageDirectory().toString();
        return FileUtils.isDirectlyUnder(mountPoint, defPath);
    }

    /**
     * 環境変数を使って取得した外部ストレージのパスを返します．
     */
    @SuppressWarnings("unused")
    private static String getExternalStoragePathByEnviromentVariables() {

        String path = null;
        // MOTOROLA Photon ISW11M 対応
        path = System.getenv("EXTERNAL_ALT_STORAGE");
        if (path != null) return path;
        // Sumsung 対応
        path = System.getenv("EXTERNAL_STORAGE2");
        if (path != null) return path;
        // ISW13HT対応
        File file = new File(System.getenv("EXTERNAL_STORAGE") + "/ext_sd");
        if (file.exists()) path = file.getPath();
        return path;
    }
}
