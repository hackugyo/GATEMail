package jp.hackugyo.gatemail.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import jp.hackugyo.gatemail.CustomApplication;
import junit.framework.Assert;
import android.annotation.SuppressLint;

import com.google.common.base.Objects;

public final class CalendarUtils {

    /**
     * Calendarのインスタンスを返します．<br>
     * タイムゾーンの扱いが重要になるので，{@link Calendar#getInstance()}を直接呼ばず，このメソッドを呼んでください．
     * 
     * @param hasJSTcontent
     *            本日のJSTをそのまま使う（true）か，unix time == 0のインスタンスを返す（false）か
     * @return Calendar
     */
    public static Calendar getInstance(boolean hasJSTcontent) {
        Calendar result = Calendar.getInstance(TimeZone.getTimeZone("GMT+09:00"));
        if (!hasJSTcontent) result.clear();
        return result;
    }

    public static Calendar getInstance(int y, int m, int d) {
        Calendar result = getInstance(true);
        result.set(Calendar.YEAR, y);
        result.set(Calendar.MONTH, m - 1);
        result.set(Calendar.DAY_OF_MONTH, d);
        return result;
    }

    public static Calendar getCleanInstance() {
        return getInstance(false);
    }

    public static Calendar getInstance(long millis) {
        Calendar result = getCleanInstance();
        result.setTimeInMillis(millis);
        return result;
    }

    public static Calendar getInstance(java.util.Date date) {
        if (date == null) return null;
        Calendar result = getCleanInstance();
        result.setTimeInMillis(date.getTime());
        return result;
    }

    public static Calendar copy(Calendar c) {
        Calendar result = getCleanInstance();
        result.setTimeInMillis(c.getTimeInMillis());
        return result;
    }

    /**
     * 翌月を取得します．月末の場合，翌月の月末となります．(01/31に対して取得すると，02/28となります．）
     * 
     * @param original
     * @return 翌月のインスタンス
     */
    public static Calendar getNextMonth(Calendar original) {
        Calendar next = CalendarUtils.add(CalendarUtils.copy(original), Calendar.MONTH, 1);
        return next;
    }

    public static Calendar getPrevMonth(Calendar original) {
        Calendar prev = CalendarUtils.add(copy(original), Calendar.MONTH, -1);
        return prev;
    }

    public static String getYYYYmm(Calendar calendar) {
        return getMonthString(calendar, "");
    }

    /**
     * 本日の日付をyyyyMMddの文字列にして返します．
     * 
     */
    public static String getDateString() {
        return getDateString(CalendarUtils.getInstance(true));
    }

    /**
     * 日付をyyyy/MM/dd hh:mmの文字列にして返します．
     * 
     * @param calendar
     * @return yyyy/MM/dd hh:mm
     */
    @SuppressLint("DefaultLocale")
    public static String getDateTimeString(Calendar calendar) {
        // Date と SimpleDateFormat を使うと簡潔に書けるが，DateクラスはJavaにおいてdeprecatedなので使用しない．
        final String slash = "/";
        String monthString = String.format("%1$02d", (calendar.get(Calendar.MONTH) + 1)); // 6を06にするような処理
        String dateString = String.format("%1$02d", calendar.get(Calendar.DATE));
        String hourString = String.format("%1$02d", calendar.get(Calendar.HOUR_OF_DAY));
        String minuteString = String.format("%1$02d", calendar.get(Calendar.MINUTE));
        StringBuilder dateStringBuilder = new StringBuilder();
        dateStringBuilder.append(calendar.get(Calendar.YEAR)).append(slash);
        dateStringBuilder.append(monthString).append(slash);
        dateStringBuilder.append(dateString).append(" ");
        dateStringBuilder.append(hourString).append(":");
        dateStringBuilder.append(minuteString);
        return dateStringBuilder.toString();
    }

    /**
     * 日付をyyyyMMddの文字列にして返します．
     * 
     * @param calendar
     * @return yyyyMMdd
     */
    public static String getDateString(Calendar calendar) {
        return getDateString(calendar, "");
    }

    /**
     * 日付をyyyyMMddの文字列にして返します．
     * 
     * @param calendar
     * @param spacer
     *            （yyyy, MM, ddの間に入れる）
     * @return yyyyMMdd
     */
    public static String getDateString(Calendar calendar, String spacer) {
        // Date と SimpleDateFormat を使うと簡潔に書けるが，DateクラスはJavaにおいてdeprecatedなので使用しない．
        String monthString = String.format("%1$02d", (calendar.get(Calendar.MONTH) + 1));
        String dateString = String.format("%1$02d", calendar.get(Calendar.DATE));
        StringBuilder dateStringBuilder = new StringBuilder();
        return dateStringBuilder.append(calendar.get(Calendar.YEAR)).append(spacer).append(monthString).append(spacer).append(dateString).toString();
    }

    /**
     * 日付をddの文字列にして返します．
     * 
     * @param calendar
     * @return dd
     */
    public static String getMonthlyDateString(Calendar calendar) {
        // Date と SimpleDateFormat を使うと簡潔に書けるが，DateクラスはJavaにおいてdeprecatedなので使用しない．
        String dateString = String.format("%d", calendar.get(Calendar.DATE));
        StringBuilder dateStringBuilder = new StringBuilder();
        return dateStringBuilder.append(dateString).toString();
    }

    /**
     * 
     * @param calendar
     * @param spacer
     * @return yyyyMM
     */
    @SuppressLint("DefaultLocale")
    public static String getMonthString(Calendar calendar, String spacer) {
        // Date と SimpleDateFormat を使うと簡潔に書けるが，DateクラスはJavaにおいてdeprecatedなので使用しない．
        String yearString = String.format("%1$04d", (calendar.get(Calendar.YEAR)));
        String monthString = String.format("%1$02d", (calendar.get(Calendar.MONTH) + 1));
        StringBuilder dateStringBuilder = new StringBuilder();
        return dateStringBuilder.append(yearString).append(spacer).append(monthString).toString();
    }

    /**
     * 与えられた文字列が，hhmmとしてふさわしいかどうか判定します．
     * 
     * @param hhmm
     * @return true / false
     */
    @SuppressLint("DefaultLocale")
    public static boolean validatehhmm(String hhmm) {
        if (hhmm == null) return false;
        if (hhmm.length() != 4) return false;
        Calendar c = getCleanInstance();
        c.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hhmm.substring(0, 2)));
        c.set(Calendar.MINUTE, Integer.valueOf(hhmm.substring(2, 4)));
        String hourString = String.format("%1$02d", c.get(Calendar.HOUR_OF_DAY));
        String minuteString = String.format("%1$02d", c.get(Calendar.MINUTE));
        return Objects.equal(hhmm, hourString + minuteString);
    }

    public static boolean checkValidityOfDate(String yyyyMMdd) {
        if (yyyyMMdd.length() != 8) return false;
        return checkValidityOfDate(yyyyMMdd.substring(0, 4), yyyyMMdd.substring(4, 6), yyyyMMdd.substring(6, 8));
    }

    public static boolean checkValidityOfDate(String yyyyMMddWithSeparator, String separator) {
        if (StringUtils.isEmpty(yyyyMMddWithSeparator)) return false;
        if (StringUtils.isEmpty(separator)) throw new IllegalArgumentException("separator is null.");
        String[] separated = yyyyMMddWithSeparator.split(separator, -1);
        if (separated.length != 3) return false;
        return checkValidityOfDate(separated[0], separated[1], separated[2]);

    }

    public static boolean checkValidityOfDate(String yyyy, String MM, String dd) {
        int year, month, day;
        try {
            year = Integer.valueOf(yyyy);
            month = Integer.valueOf(MM);
            day = Integer.valueOf(dd);
        } catch (NumberFormatException e) {
            return false;
        }
        return checkValidityOfDate(year, month, day);
    }

    /**
     * 指定された日付が有効なものであるかどうか判定します．
     * 
     * @param year
     * @param month
     *            from 1 to 12
     * @param day
     */
    public static boolean checkValidityOfDate(int year, int month, int day) {
        month = month - 1;
        Calendar targetDate = CalendarUtils.getCleanInstance();
        targetDate.set(year, month, day);
        if (targetDate.get(Calendar.YEAR) != year) return false;
        if (targetDate.get(Calendar.MONTH) != month) return false;
        if (targetDate.get(Calendar.DAY_OF_MONTH) != day) return false;
        return true;
    }

    /**
     * 与えられた文字列が，ddとしてふさわしいかどうか判定します．
     * 
     * @param dd
     * @return 01〜31のときtrue
     */
    public static boolean validatedd(String dd) {
        return StringUtils.validateIntString(dd, 2, 1, 31);
    }

    /**
     * yyyyMMddで表現された日付を，Calendarオブジェクトにして返します．
     * 
     * @param yyyyMMdd
     *            または yyyy/MM/dd
     * @return 年月日以外が0にリセットされたCalendar
     */
    public static Calendar parseGregorianDate(String yyyyMMdd) {
        if (StringUtils.isEmpty(yyyyMMdd)) return null;
        if (yyyyMMdd.contains("/")) yyyyMMdd = yyyyMMdd.replace("/", "");
        return parseGregorianDate(yyyyMMdd, true);
    }

    public static Calendar parseGregorianDate(String yyyyMMdd, boolean isLenient) {
        Calendar calendar = CalendarUtils.getCleanInstance();
        calendar.setLenient(isLenient); // 20110230を2012年03月2日と好意的に解釈しない
        try {
            StringBuilder dateStringBuilder = new StringBuilder(yyyyMMdd);
            String yyyy = dateStringBuilder.substring(0, 3 + 1);
            String MM = dateStringBuilder.substring(4, 5 + 1);
            String dd = dateStringBuilder.substring(6, 7 + 1);
            calendar.set(Integer.valueOf(yyyy), Integer.valueOf(MM) - 1, Integer.valueOf(dd));
        } catch (StringIndexOutOfBoundsException e) {
            LogUtils.w("not valid date: " + yyyyMMdd);
            throw new IllegalArgumentException("not valid date: " + yyyyMMdd);
        }
        // if (calendar.get(Calendar.HOUR_OF_DAY) != 0) throw new IllegalArgumentException();
        return calendar;
    }

    /**
     * 指定された日の指定日後を返します．
     * 
     * @param date
     * @param amount
     * @return dateがnullの場合null
     */
    public static java.util.Date getDatePlus(java.util.Date date, int amount) {
        if (date == null) {
            return null;
            //dateがnullの場合、nullを返す
        }

        try {
            Calendar now = Calendar.getInstance();
            //Calendarクラスのインスタンスを生成

            now.setTime(date);
            //Date型からカレンダー型に変換

            now.add(Calendar.DATE, amount);
            //指定された日時に、指定された日にちを足す

            Date time = now.getTime();
            //カレンダー型からDate型に変換

            return time;
        } catch (Exception e) {
            return null;
        }

    }

    public static Calendar getInstance(String yyyyMMddWithSeparator, String separator) {
        if (yyyyMMddWithSeparator == null) return null;
        String yyyyMMdd = StringUtils.valueOf(yyyyMMddWithSeparator);
        if (yyyyMMddWithSeparator.contains(separator)) yyyyMMdd = yyyyMMddWithSeparator.replace(separator, "");
        return parseGregorianDate(yyyyMMdd);
    }

    public static boolean isFromBeforeTo(int hhFrom, int mmFrom, int hhTo, int mmTo) {
        if (hhFrom > hhTo) {
            return false;
        } else if (hhFrom == hhTo && mmFrom > mmTo) {
            return false;
        }

        return true;
    }

    /**
     * 日付がセットされたカレンダーに時刻をセットします．
     * 
     * @param targetFrom
     * @param fromHourAndMinute
     * @param targetTo
     * @param toHourAndMinute
     * @return 開始日時カレンダー・終了日時カレンダー（秒の部分は，開始が0秒，終了が59秒としています）
     */
    public static Calendar[] getDateTimeFromTo(Calendar targetFrom, Integer[] fromHourAndMinute, Calendar targetTo, Integer[] toHourAndMinute) {
        final int fromDay = targetFrom.get(Calendar.DAY_OF_MONTH);
        final int toDay = targetTo.get(Calendar.DAY_OF_MONTH);

        Calendar[] result = new Calendar[2];

        // Calendar.HOURは使わないこと．10を午後に対してsetすると22時に，午前にsetすると10時になってしまう．
        targetFrom.set(Calendar.HOUR_OF_DAY, fromHourAndMinute[0]);
        targetFrom.set(Calendar.MINUTE, fromHourAndMinute[1]);
        targetFrom.set(Calendar.SECOND, 0);
        targetTo.set(Calendar.HOUR_OF_DAY, toHourAndMinute[0]);
        targetTo.set(Calendar.MINUTE, toHourAndMinute[1]);
        targetTo.set(Calendar.SECOND, 59);

        result[0] = targetFrom;
        result[1] = targetTo;

        {
            Assert.assertTrue(targetFrom.get(Calendar.DAY_OF_MONTH) == fromDay);
            Assert.assertTrue(targetTo.get(Calendar.DAY_OF_MONTH) == toDay);
        }

        return result;
    }

    public static Calendar add(Calendar c, int field, int value) {
        if (field >= Calendar.FIELD_COUNT) throw new IllegalArgumentException("unknown calendar field: " + field);
        c.add(field, value);
        return c;
    }

    public static Calendar add(java.util.Date d, int field, int value) {
        return add(CalendarUtils.getInstance(d.getTime()), field, value);
    }

    public static boolean isToday(java.util.Date date, Calendar today) {
        Assert.assertNotNull(date);
        if (today == null) today = CalendarUtils.getInstance(true);

        return CalendarUtils.isSameDateAs(CalendarUtils.getInstance(date.getTime()), today);
    }

    public static boolean isSameDateAs(Calendar instance, Calendar other) {
        return isSameAs(Calendar.YEAR, instance, other) //
                && isSameAs(Calendar.MONTH, instance, other) //
                && isSameAs(Calendar.DAY_OF_MONTH, instance, other);
    }

    public static boolean isSameDateAs(java.util.Date instance, java.util.Date other) {
        return isSameDateAs(CalendarUtils.getInstance(instance), CalendarUtils.getInstance(other));
    }

    public static boolean isSameDateAs(Calendar instance, java.util.Date other) {
        return isSameDateAs(instance, CalendarUtils.getInstance(other));
    }

    public static boolean isSameDateAs(java.util.Date instance, Calendar other) {
        return isSameDateAs(CalendarUtils.getInstance(instance), other);
    }

    public static boolean isSameAs(int field, Calendar instance, Calendar other) {
        if (instance == null || other == null) return false;
        return instance.get(field) == other.get(field);
    }

    public static boolean isToday(java.util.Date instance) {
        return isSameAs(Calendar.DATE, CalendarUtils.getInstance(instance), getInstance(true));
    }

    /**
     * 端末の日付形式設定を反映した日付文字列を返します．<br>
     * 日付形式設定は，端末のロケールを上書きできるため，必要な場合はこちらを使ってください．
     * 
     * @param instance
     * @return yyyy/MM/ddなど
     * @see <a href="http://qiita.com/items/508304558078203fe24b">参考ページ</a>
     */
    public static String getLocalizedDateString(java.util.Date instance) {
        java.text.DateFormat format = android.text.format.DateFormat.getDateFormat(CustomApplication.getAppContext());
        return format.format(instance);
    }

    /**
     * 端末の日付形式設定を反映した日付文字列を返します．<br>
     * 日付形式設定は，端末のロケールを上書きできるため，必要な場合はこちらを使ってください．
     * 
     * @param calendar
     * @return yyyy/MM/ddなど
     * @see <a href="http://qiita.com/items/508304558078203fe24b">参考ページ</a>
     */
    public static String getLocalizedDateString(Calendar calendar) {
        return getLocalizedDateString(calendar.getTime());
    }
}
