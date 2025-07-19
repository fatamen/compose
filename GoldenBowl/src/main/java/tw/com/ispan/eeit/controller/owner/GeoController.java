package tw.com.ispan.eeit.controller.owner;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/geo")
public class GeoController {

    @GetMapping("/latlng")
    public Map<String, Object> getLatLng(@RequestParam String address) {
        try {
            String url = "https://nominatim.openstreetmap.org/search?format=json&addressdetails=1&q=" + URLEncoder.encode(address, StandardCharsets.UTF_8);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url)) // ← 這裡要用 java.net.URI
                .header("User-Agent", "YourAppName/1.0 (your@email.com)") // 必須加
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONArray arr = new JSONArray(response.body());
            if (arr.length() > 0) {
                JSONObject first = arr.getJSONObject(0);
                return Map.of(
                        "success", true,
                        "lat", first.getString("lat"),
                        "lng", first.getString("lon"));
            } else {
                return Map.of("success", false, "message", "查無結果");
            }
        } catch (Exception e) {
            return Map.of("success", false, "message", "API錯誤: " + e.getMessage());
        }
    }
}
