package jp.hackugyo.gatemail.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * stringをmd5に変換します
 * 
 */
public class MessageDigestUtils {

    /**
     * 引数をMD5に変換します
     * 
     * @param s
     * @return md5
     * @see <a
     *      href="http://androidsnippets.com/create-a-md5-hash-and-dump-as-a-hex-string">参考ページ</a>
     */
    public static String md5(String s) {
        if (s == null) s = "";
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
