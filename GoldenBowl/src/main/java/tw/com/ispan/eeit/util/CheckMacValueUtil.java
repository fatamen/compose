package tw.com.ispan.eeit.util;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CheckMacValueUtil {
    public static String generate(Map<String, String> params, String hashKey, String hashIV) throws Exception {
        // Step 1: 按字母排序
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys, String.CASE_INSENSITIVE_ORDER);

        // Step 2: 組合字串
        StringBuilder sb = new StringBuilder();
        sb.append("HashKey=").append(hashKey);
        for (String k : keys) {
            sb.append("&").append(k).append("=").append(params.get(k));
        }
        sb.append("&HashIV=").append(hashIV);

        // Step 3: URL encode
        String urlEncoded = URLEncoder.encode(sb.toString(), "UTF-8")
            .replace("%21", "!")
            .replace("%28", "(")
            .replace("%29", ")")
            .replace("%2a", "*")
            .replace("%2d", "-")
            .replace("%2e", ".")
            .replace("%5f", "_");

        // Step 4: 轉小寫
        urlEncoded = urlEncoded.toLowerCase();

        // Step 5: MD5
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(urlEncoded.getBytes("UTF-8"));
        StringBuilder result = new StringBuilder();
        for (byte b : digest) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }
    public static boolean verify(Map<String, String> payload, String hashKey, String hashIV) throws Exception {
        String receivedMac = payload.get("CheckMacValue");
        payload.remove("CheckMacValue"); // 避免參與簽章計算

        String generatedMac = generate(payload, hashKey, hashIV);
        return receivedMac != null && receivedMac.equalsIgnoreCase(generatedMac);
    }
}
