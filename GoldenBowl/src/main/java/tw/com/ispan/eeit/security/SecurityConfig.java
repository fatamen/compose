//package tw.com.ispan.eeit.security;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//            .csrf(csrf -> csrf.disable())          // ✅ 關掉 CSRF
//            .authorizeHttpRequests(auth -> auth
//                .anyRequest().permitAll()          // ✅ 所有 API 放行
//            )
//            .formLogin(form -> form.disable())     // ❗ 關閉預設登入頁
//            .httpBasic(basic -> basic.disable());  // ❗ 關閉基本認證
//
//        return http.build();
//    }
//}
