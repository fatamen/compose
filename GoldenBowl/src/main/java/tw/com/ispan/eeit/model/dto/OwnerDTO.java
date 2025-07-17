package tw.com.ispan.eeit.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class OwnerDTO {
    private Integer id;
    private String email;
    private String name;
    private String phone;
    private LocalDateTime signupDate;
    private LocalDateTime lastLogin;
    private Boolean isActive;
    private Boolean isVerify;
    // 建議只傳 store id + name，避免關聯物件過深
    private List<StoreSimpleDTO> stores;

    // 內部 class：只放你想給前端的 Store 資訊
    @Data
    @NoArgsConstructor
    public static class StoreSimpleDTO {
        private Integer id;
        private String name;
    }
}
