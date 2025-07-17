package tw.com.ispan.eeit.model.entity.emailVerify;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "user_token")
@NoArgsConstructor
public class UserTokenBean {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "user_id")
	private Integer userid;

	@Column(name = "email")
	private String email;

	@Column(name = "verify_code")
	private String verifyCode;

	@Column(name = "expiration")
	private LocalDateTime expiration;

	@Column(name = "is_used")
	private boolean used;
}
