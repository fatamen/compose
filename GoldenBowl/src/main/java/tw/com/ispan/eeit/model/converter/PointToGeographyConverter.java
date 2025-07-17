package tw.com.ispan.eeit.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKBReader;

@Converter(autoApply = false) // 建議先不要 autoApply，有多個 Point 欄位時容易衝突
public class PointToGeographyConverter implements AttributeConverter<Point, String> {

    @Override
    public String convertToDatabaseColumn(Point point) {
        if (point == null)
            return null;
        return new WKTWriter().write(point);
    }

    @Override
    public Point convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        try {
            String data = dbData.trim();
            // 檢查是不是 HEX 字串（只有 0~9A~F）
            if (data.matches("^[0-9A-Fa-f]+$")) {
                byte[] bytes = hexStringToByteArray(data);
                WKBReader wkbReader = new WKBReader();
                return (Point) wkbReader.read(bytes);
            } else {
                WKTReader reader = new WKTReader();
                return (Point) reader.read(data);
            }
        } catch (Exception e) {
            System.err.println("【PointToGeographyConverter 轉換失敗】dbData=" + dbData);
            e.printStackTrace();
            throw new RuntimeException("無法將字串轉換為 Point: " + dbData, e);
        }
    }

    // HEX 字串轉 byte[]
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}