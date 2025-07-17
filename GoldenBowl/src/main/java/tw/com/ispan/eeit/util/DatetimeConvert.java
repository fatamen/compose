package tw.com.ispan.eeit.util;



import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DatetimeConvert {

//	fromat 格式 yyyy-MM-dd HH:mm:ss
//  M月d日 EEEE HH時mm分
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
	
//	fromat 格式 yyyy-MM-dd HH:mm:ss
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
}
