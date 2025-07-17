package tw.com.ispan.eeit.model.dto.store;

import lombok.Data;

@Data
public class TimeSettingUpdateRequest {
    private Integer interval;
    private Integer mealTime;
}
