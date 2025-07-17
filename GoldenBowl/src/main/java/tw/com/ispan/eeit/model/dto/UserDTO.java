package tw.com.ispan.eeit.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class UserDTO {
    private Integer id;
    private String email;
    private String name;
    private String password;//
    private String phone;
    private Boolean isActive;
    private Boolean isVerify;
    private LocalDateTime signupDate;
    private LocalDateTime lastLogin;
    private LocalDateTime hideUntil;
}
