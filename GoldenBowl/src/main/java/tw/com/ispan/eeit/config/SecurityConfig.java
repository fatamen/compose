package tw.com.ispan.eeit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
// ===== 預約系統 CORS 配置 - 開始 =====
// 以下 CORS 配置是為了讓前端 Vue.js 應用能夠訪問後端預約 API
// 如果不需要預約功能，可以移除這些 import 和相關配置
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
// ===== 預約系統 CORS 配置 - 結束 =====

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// ===== 預約系統 CORS 配置 - 開始 =====
				// 啟用 CORS 支持，允許前端訪問後端 API
				.cors().and()
				// ===== 預約系統 CORS 配置 - 結束 =====
				.csrf().disable()
				.authorizeHttpRequests(auth -> auth
						.anyRequest().permitAll())
				.oauth2Login(oauth2 -> oauth2
						.defaultSuccessUrl("/loginSuccess", true)
						.failureUrl("/loginFailure"));
		return http.build();
	}

	// ===== 預約系統 CORS 配置 - 開始 =====
	/**
	 * CORS 配置 Bean
	 * 用途：允許前端 Vue.js 應用訪問後端預約 API
	 * 如果不需要預約功能，可以移除此方法
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		// 允許所有來源（開發環境）
		configuration.setAllowedOriginPatterns(Arrays.asList("*"));
		// 允許的 HTTP 方法
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		// 允許的請求頭
		configuration.setAllowedHeaders(Arrays.asList("*"));
		// 允許發送憑證（cookies 等）
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
	// ===== 預約系統 CORS 配置 - 結束 =====
}
