package tw.com.ispan.eeit.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class DatetimeConvert {

	// fromat 格式 yyyy-MM-dd HH:mm:ss
	// M月d日 EEEE HH時mm分
	public static String toString(LocalDateTime datetime, String format) {

		if (datetime == null) {
			return ""; // 如果日期時間為 null，返回空字串
		}
		try {
			// DateTimeFormatter 是執行緒安全的
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
			return datetime.format(formatter);
		} catch (IllegalArgumentException e) {
			// 如果格式字串不合法，捕獲異常並處理
			System.err.println("無效的日期格式字串: " + format);
			e.printStackTrace();
			return ""; // 或者拋出一個自定義的 RuntimeException
		} catch (Exception e) {
			// 捕獲其他潛在異常
			System.err.println("格式化日期時發生錯誤: " + datetime + ", 格式: " + format);
			e.printStackTrace();
			return "";
		}
	}

	// fromat 格式 yyyy-MM-dd HH:mm:ss
	public static LocalDateTime parse(String datetimeStr, String format) {
		if (datetimeStr == null || datetimeStr.trim().isEmpty()) {
			return null; // 如果輸入字串為 null 或空，返回 null
		}
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
			return LocalDateTime.parse(datetimeStr, formatter);
		} catch (DateTimeParseException e) {
			// 針對日期時間解析錯誤的特定異常
			System.err.println("解析日期時間字串失敗: '" + datetimeStr + "'，預期格式: '" + format + "'");
			e.printStackTrace();
			return null; // 解析失敗返回 null
		} catch (IllegalArgumentException e) {
			// 如果格式字串不合法
			System.err.println("無效的日期格式字串: " + format);
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			// 捕獲其他潛在異常
			System.err.println("解析日期字串時發生未知錯誤: " + datetimeStr + ", 格式: " + format);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 將 LocalDate 轉換為指定格式的字串
	 * 
	 * @param date   LocalDate 物件
	 * @param format 日期格式，例如 "yyyy-MM-dd"
	 * @return 格式化後的日期字串，如果輸入為 null 則返回空字串
	 */
	public static String toString(LocalDate date, String format) {
		if (date == null) {
			return "";
		}
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
			return date.format(formatter);
		} catch (IllegalArgumentException e) {
			System.err.println("無效的日期格式字串: " + format);
			e.printStackTrace();
			return "";
		} catch (Exception e) {
			System.err.println("格式化 LocalDate 時發生錯誤: " + date + ", 格式: " + format);
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * 將字串解析為 LocalDate
	 * 
	 * @param dateStr 日期字串
	 * @param format  日期格式，例如 "yyyy-MM-dd"
	 * @return 解析後的 LocalDate 物件，如果解析失敗則返回 null
	 */
	public static LocalDate parseLocalDate(String dateStr, String format) {
		if (dateStr == null || dateStr.trim().isEmpty()) {
			return null;
		}
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
			return LocalDate.parse(dateStr, formatter);
		} catch (DateTimeParseException e) {
			System.err.println("解析 LocalDate 字串失敗: '" + dateStr + "'，預期格式: '" + format + "'");
			e.printStackTrace();
			return null;
		} catch (IllegalArgumentException e) {
			System.err.println("無效的日期格式字串: " + format);
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			System.err.println("解析 LocalDate 字串時發生未知錯誤: " + dateStr + ", 格式: " + format);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 將 LocalTime 轉換為指定格式的字串
	 * 
	 * @param time   LocalTime 物件
	 * @param format 時間格式，例如 "HH:mm"
	 * @return 格式化後的時間字串，如果輸入為 null 則返回空字串
	 */
	public static String toString(LocalTime time, String format) {
		if (time == null) {
			return "";
		}
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
			return time.format(formatter);
		} catch (IllegalArgumentException e) {
			System.err.println("無效的時間格式字串: " + format);
			e.printStackTrace();
			return "";
		} catch (Exception e) {
			System.err.println("格式化 LocalTime 時發生錯誤: " + time + ", 格式: " + format);
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * 將字串解析為 LocalTime
	 * 
	 * @param timeStr 時間字串
	 * @param format  時間格式，例如 "HH:mm"
	 * @return 解析後的 LocalTime 物件，如果解析失敗則返回 null
	 */
	public static LocalTime parseLocalTime(String timeStr, String format) {
		if (timeStr == null || timeStr.trim().isEmpty()) {
			return null;
		}
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
			return LocalTime.parse(timeStr, formatter);
		} catch (DateTimeParseException e) {
			System.err.println("解析 LocalTime 字串失敗: '" + timeStr + "'，預期格式: '" + format + "'");
			e.printStackTrace();
			return null;
		} catch (IllegalArgumentException e) {
			System.err.println("無效的時間格式字串: " + format);
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			System.err.println("解析 LocalTime 字串時發生未知錯誤: " + timeStr + ", 格式: " + format);
			e.printStackTrace();
			return null;
		}
	}

	// 格式： 2025/07/16（三） 14:00
	public static String toString(LocalDateTime time, String pattern, Locale locale) {
		if (time == null)
			return "";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, locale);
		return time.format(formatter);
	}
}
