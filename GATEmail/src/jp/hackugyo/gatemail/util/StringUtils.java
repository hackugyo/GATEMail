package jp.hackugyo.gatemail.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文字列 ユーティリティクラス.
 * 
 * @author User
 */
public final class StringUtils {
    /** 行頭、行末の BR タグにマッチするパターン. */
    private static final Pattern TRIMBRTAG_PATTERN = Pattern.compile("(^<br\\s*[/]*>|<br\\s*[/]*>$)", Pattern.CASE_INSENSITIVE);
    /** Solr クエリーでエスケープが必要な文字にマッチするパターン. */
    private static final Pattern SOLR_ESCAPE_PATTERN = Pattern.compile("[¥+\\-¥!¥(¥)¥{¥}\\[\\]¥^~¥*¥?:\"]|&{2}|\\|{2}|[\\\\]", Pattern.UNIX_LINES);
    /** 日時指定フォーマット */
    public static final String DATE_PATTERN = "yyyy/MM/dd HH:mm:ss";
    public static final String DATE_PATTERN_SHORT = "yyyy/MM/dd";

    /**
     * 数値を，3桁区切りの金額文字列に変換します．1000 -> 1,000<br>
     * nullの場合，"-"を返します．
     * 
     */
    public static String parseIntToMoneyText(Integer i) {
        if (i == null) return "-"; // "null"のかわりに"-"を返す
        return String.format("%1$,3d", i);
    }

    /**
     * 数値を，3桁区切りの金額文字列に変換します．1000 -> 1,000<br>
     * nullの場合，"-"を返します．
     * 
     */
    public static String parseLongToMoneyText(Long i) {
        if (i == null) return "-"; // "null"のかわりに"-"を返す
        return String.format("%1$,3d", i);
    }

    /**
     * 数値の文字列を，3桁区切りの金額文字列に変換します．1000.00 -> 1,000.00 1000 -> 1,000
     * 
     */
    public static String parseStringToMoneyText(String doubleString) {
        String integerPart, decimalPart;
        String[] split = doubleString.split(Pattern.quote("."), -1);
        if (split.length <= 0 || 2 < split.length) throw new IllegalArgumentException("cannot parse: " + doubleString);
        integerPart = split[0];
        decimalPart = (split.length == 2 ? split[1] : "");
        return joinStrings(parseLongToMoneyText(Long.valueOf(integerPart)), decimalPart, ".");
    }

    /**
     * 文字列が空文字かどうかを取得します. NULL または、空文字の場合は true を、それ以外の場合は false を返します.
     * 
     * @param str
     *            判定する文字列
     * @return NULL または、空文字の場合は true
     */
    public static boolean isEmpty(CharSequence str) {
        if (str == null || str.length() <= 0) return true;
        return false;
    }

    /**
     * 文字列から、全角・半角スペースを取り除きます。
     * 
     * @param str
     */
    public static String trimSpace(String str) {
        if (str == null) return null;

        return str.replaceAll("　| ", "");
    }

    /**
     * 文字列が空文字かどうかを取得します. NULL または、空文字の場合は false を、それ以外の場合は true を返します.
     * 
     * @param str
     *            判定する文字列
     * @return NULL または、空文字の場合は false
     */
    public static boolean isPresent(CharSequence str) {
        return !isEmpty(str);
    }

    /**
     * 行頭・行末のBRタグを削除します.
     * 
     * @param tag
     *            対象の文字列
     * @return タグ削除後の文字列
     */
    public static String trimBRTag(CharSequence tag) {
        if (tag == null) return null;
        Matcher matcher = TRIMBRTAG_PATTERN.matcher(tag);
        return matcher.replaceAll("");
    }

    /**
     * 先頭の文字を大文字に、それ以降を小文字にした文字列を返します.
     * 
     * @param str
     *            文字列
     * @return 先頭大文字、先頭以外小文字の文字列
     */
    public static String capitalize(CharSequence str) {
        if (isEmpty(str)) return null;
        String tmp = str.toString();
        return tmp.substring(0, 1).toUpperCase() + tmp.substring(1).toLowerCase();
    }

    /**
     * 先頭の文字を小文字にした文字列を返します.
     * 
     * @param str
     *            文字列
     * @return 先頭を小文字にした文字列
     */
    public static String uncapitalize(CharSequence str) {
        if (isEmpty(str)) return null;
        String tmp = str.toString();
        return tmp.substring(0, 1).toLowerCase() + tmp.substring(1);
    }

    /**
     * 文字列のリストを区切り文字で結合して返します.
     * 
     * @param parts
     *            文字列のリスト
     * @param separator
     *            区切り文字
     * @return 結合後の文字列
     */
    public static String join(List<String> parts, String separator) {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        int length = parts.size() - 1;
        for (String str : parts) {
            builder.append(str);
            if (index < length) builder.append(separator);
            index++;
        }
        return builder.toString();
    }

    /**
     * 文字列を結合して返します.
     * 
     * @param args
     *            文字列のリスト
     * @return 結合後の文字列
     */
    public static String join(String... args) {
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            sb.append(arg);
        }
        return sb.toString();
    }

    /**
     * Solrの特殊文字をエスケープして返します.
     * 
     * @param str
     *            クエリー文字列
     * @return エスケープ後の文字列
     */
    public static String escapeSolrQueryString(String str) {
        if (isEmpty(str)) return null;
        Matcher m = SOLR_ESCAPE_PATTERN.matcher(str);
        return m.replaceAll("\\\\$0");
    }

    /**
     * date型をString型に変換します．
     * 
     * @param date
     * @return 変換後の文字列
     */
    public static String getDateStr(Date date) {
        return getDateStr(date, DATE_PATTERN);
    }

    /**
     * date型をString型に変換します．フォーマットが指定できます．
     * 
     * @param date
     * @param format
     * @return 変換後の文字列
     */
    public static String getDateStr(Date date, String format) {
        final SimpleDateFormat sdf = new SimpleDateFormat(format);
        String term = sdf.format(date);
        return term;
    }

    /**
     * 最後の文字を除き，伏せ字にします．
     * 
     * @param string
     */
    public static String hideAllButLast(String string) {
        if (string == null) return "";
        return hideAllButLast(string, string.length(), Math.min(string.length(), 1));
    }

    /**
     * 後ろから何文字かを伏せ字にします．"123456", 3, 1が引数の場合，"123**6"を返します．
     * 
     * @param string
     * @param hideCharsFromLast
     *            : 後ろから何文字ぶんを伏せ字にするか指定します．
     * @param showCharsFromLast
     *            : 後ろから何文字ぶんは伏せ字にしないかを指定します．
     */
    public static String hideAllButLast(String string, final int hideCharsFromLast, final int showCharsFromLast) {
        if (isEmpty(string)) return "";

        final int length = string.length();
        if (showCharsFromLast < 0) throw new IllegalArgumentException("cannot show last chars: " + showCharsFromLast);
        if (hideCharsFromLast < showCharsFromLast) throw new IllegalArgumentException("hideCharsFromLast: " + hideCharsFromLast + " < showCharsFromLast: " + showCharsFromLast);
        if (string.length() < hideCharsFromLast) throw new IllegalArgumentException(string + " .length() < hideCharsFromLast: " + hideCharsFromLast);
        final String lastHalf = string.substring(length - hideCharsFromLast);// 後ろhideCharsFromLast文字ぶんを取り出す
        final String firstHalf = string.substring(0, length - hideCharsFromLast); // 後ろhideCharsFromLast文字より前の部分を取り出す

        String hidden = lastHalf.replaceAll(".", "*"); // いったん全部伏せ字に
        StringBuilder second = new StringBuilder(hidden);
        int hiddenLength = hidden.length();
        for (int i = showCharsFromLast; 1 <= i; i--) {
            // 一部を元に戻す
            second.setCharAt(hiddenLength - i, lastHalf.charAt(hiddenLength - i));
        }
        return firstHalf + second.toString();
    }

    /**
     * 文字列をjointで結合します．ただし左右どちらかが空文字列の場合，jointは使いません．
     * 
     * @param string1
     * @param string2
     * @param joint
     */
    public static String joinStrings(String string1, String string2, String joint) {
        if (string1 == null) string1 = "";
        if (string2 == null) string2 = "";

        if (StringUtils.isEmpty(string1) || StringUtils.isEmpty(string2)) joint = "";

        StringBuilder sb = new StringBuilder();
        sb.append(string1).append(joint).append(string2);
        return sb.toString();
    }

    /**
     * 文字列を結合します．ただし片方が空文字列の場合，空文字列を返します．
     * 
     * @param string1
     * @param string2
     */
    public static String concatStrings(String string1, String string2) {
        if (StringUtils.isEmpty(string1) || StringUtils.isEmpty(string2)) return "";
        return (string1 + string2);
    }

    /**
     * 文字列と単位とを結合します．ただし片方が空文字列または"-"の場合，単位なしで返します．
     * 
     * @param string
     * @param unit
     */
    public static String concatStringWithUnit(String string, String unit) {
        if (isEmpty(string) || "-".equals(string)) return string;
        return (string + unit);
    }

    /**
     * 与えられた文字列を，半角max文字以下に切り詰めます。切り捨て部分がある場合，"…"を末尾に追加します。
     * 
     * @param detail
     * @param max
     */
    public static String ellipsize(String detail, int max) {
        return ellipsize(detail, max, false);
    }

    /**
     * 与えられた文字列を，半角max文字以下に切り詰めます。切り捨て部分がある場合，"…"を末尾に追加します。
     * 
     * @param input
     * @param max
     * @param padLeft
     *            : trueの場合，末尾に空白を入れて，max文字ちょうどになるようにします．
     */
    public static String ellipsize(String input, int max, boolean padLeft) {
        if (input == null || input.length() <= max / 2) { // max / 2未満だったら全角でも確実に入るので，処理スキップ．
            return (padLeft ? padLeft(input, max) : input);
        }
        StringBuilder result = new StringBuilder();
        int currentLength = 0;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            currentLength += (isHalfWidth(c) ? 1 : 2);

            if (currentLength >= max) {
                if (currentLength == max && i == input.length() - 1) {
                    result.append(c); // これで最後の文字なのであれば，追加して終了
                } else {
                    result.append(ELLIPSIZER);
                }
                break;
            } else {
                result.append(c);
            }
        }
        return (padLeft ? padLeft(result.toString(), max) : result.toString());
    }

    public static String ellipsizeMiddle(String input, int max, boolean padLeft) {
        if (input == null || input.length() < max) {
            return (padLeft ? padLeft(input, max) : input);
        }
        int charactersAfterEllipsis = max / 2;
        int charactersBeforeEllipsis = max - ELLIPSIZER.length() - charactersAfterEllipsis;
        return input.substring(0, charactersBeforeEllipsis) + ELLIPSIZER + input.substring(input.length() - charactersAfterEllipsis);
    }

    public static String getCRLF() {
        return System.getProperty("line.separator");
    }

    public static final String ELLIPSIZER = "…"; // "..."ではなく"…"がAndroid推奨

    public static boolean isHalfWidth(char c) {
        return (c <= '\u007e') || // 英数字
                (c == '\u00a5') || // \記号
                (c == '\u203e') || // ~記号
                (c >= '\uff61' && c <= '\uff9f'); // 半角カナ
    }

    public static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }

    public static String padLeft(String s, int n) {
        return String.format("%1$" + n + "s", s);
    }

    /**
     * 与えられた引数が候補リストに含まれるかどうかを返します．
     * 
     * @param str
     * @param array
     */
    public static boolean isKnownByList(String str, String[] array) {
        return Arrays.asList(array).contains(str); // containsはequalsで比較するため，別インスタンスのStringも等しいものと判定される
    }

    /**
     * 与えられた文字列が，ホワイトリスト内にある文字列で始まるかかどうかを返します． リストが["aa", "ab"]のとき，"aab"はtrue,
     * "a"はfalse
     * 
     * @param str
     * @param whiteList
     */
    public static boolean isStartedByList(String str, ArrayList<String> whiteList) {
        for (String white : whiteList) {
            if (str.startsWith(white)) return true;
        }
        return false;
    }

    public static boolean isSame(String targ, String comp) {
        if (targ == null) {
            return comp == null;
        } else {
            return targ.equals(comp);
        }
    }

    /**
     * 与えられた文字列が，としてふさわしいかどうか判定します．
     * 
     * @return 桁数・最小・最大の制約を満たしたときtrue
     */
    public static boolean validateIntString(String dd, int length, int min, int max) {
        if (dd == null) return false;
        if (dd.length() != length) return false;
        int day;
        try {
            day = Integer.valueOf(dd);
        } catch (NumberFormatException e) {
            return false;
        }
        if (day < min || max < day) return false;
        return true;
    }

    /**
     * StringBuilderにnullを読ませると"null"という文字列になってしまうので，フィルタします．
     * 
     * @param text
     * @return String
     * @see com.google.common.base.Strings#nullToEmpty
     */
    public static String nullToEmpty(String text) {
        return (text == null ? "" : text);
    }

    public static String nullToEmpty(Double text) {
        return (text == null ? "" : String.valueOf(text));
    }

    /**
     * {@link String#valueOf(Object)}はnullを"null"に変えてしまうので，<br>
     * nullの場合はnullのまま返すメソッドを提供します．
     * 
     * @param object
     * @return String or null
     */
    public static String valueOf(Object object) {
        if (object == null) return null;
        return String.valueOf(object);
    }

    public static String map2String(Map<String, String> map) {
        if (map == null) return null;
        StringBuilder sb = new StringBuilder();
        Iterator<Entry<String, String>> itr = map.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<String, String> entry = itr.next();
            if (sb.length() > 0) sb.append("&");
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }

    /**
     * @see <a
     *      href="http://yuki312.blogspot.jp/2013/05/androidtolowercasetouppercase.html">参考ページ</a>
     * @param str
     */
    public static String toLowerCase(String str) {
        // ロケールを指定しない場合，大文字-小文字対応が英語と異なっている言語に対応できないので
        return str.toLowerCase(Locale.ENGLISH);
    }

    public static String toUpperase(String str) {
        // ロケールを指定しない場合，大文字-小文字対応が英語と異なっている言語に対応できないので
        return str.toUpperCase(Locale.ENGLISH);
    }

    /**
     * 遅いので，文字列結合には用いないこと．
     * 
     * @param format
     * @param args
     * @return string
     */
    public static String format(String format, Object... args) {
        return String.format(Locale.ENGLISH, format, args);
    }

    private static final String DEFAULT_REPLACER_FOR_CRLF = " ";
    /** 先頭の改行コードは削除する */
    @SuppressWarnings("unused")
    private static final Pattern PATTERN_CRLF = Pattern.compile("^[" + Pattern.quote(getCRLF()) + "]*");

    /**
     * 改行を取り除いて返します．
     * 
     * @param str
     *            the String to delete CRLF from, may be <code>null</code>
     * @param replacer
     *            the String to replace CRLFs.<br>
     *            default is <code>" "</code>
     * @return the String without CRLF.
     */
    public static String deleteCRLF(String str, String replacer) {
        if (str == null) return null;
        if (replacer == null) replacer = DEFAULT_REPLACER_FOR_CRLF;
        // return str.replaceAll(PATTERN_CRLF.pattern(), "").replaceAll(getCRLF(), replacer);
        return str.replaceAll(getCRLF(), replacer);
    }

    /**
     * <p>
     * Deletes all whitespaces from a String as defined by
     * {@link Character#isWhitespace(char)}.
     * </p>
     * 
     * <pre>
     * StringUtils.deleteWhitespace(null) = null
     * StringUtils.deleteWhitespace("") = ""
     * StringUtils.deleteWhitespace("abc") = "abc"
     * StringUtils.deleteWhitespace(" ab c ") = "abc"
     * </pre>
     * 
     * {@literal org.apache.commons.lang.StringUtils}
     * 
     * @param str
     *            the String to delete whitespace from, may be null
     * @return the String without whitespaces, <code>null</code> if null String
     *         input
     */
    public static String deleteWhitespace(String str) {
        if (isEmpty(str)) {
            return str;
        }
        int sz = str.length();
        char[] chs = new char[sz];
        int count = 0;
        for (int i = 0; i < sz; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                chs[count++] = str.charAt(i);
            }
        }
        if (count == sz) {
            return str;
        }
        return new String(chs, 0, count);
    }

    /**
     * 先頭のホワイトスペースのみ削除します．
     */
    public static String deleteHeadWhitespace(String str) {
        if (isEmpty(str)) {
            return str;
        }
        int sz = str.length();
        char[] chs = new char[sz];
        int count = 0;
        boolean hasFoundChar = false;
        for (int i = 0; i < sz; i++) {
            if (hasFoundChar || !Character.isWhitespace(str.charAt(i))) {
                chs[count++] = str.charAt(i);
                hasFoundChar = true;
            }
        }
        if (count == sz) {
            return str;
        }
        return new String(chs, 0, count);
    }

}
