package tw.com.ispan.eeit.exception;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SpringbootConfig implements WebMvcConfigurer {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
                // registry.addMapping("/ajax/pages/products/**")
                // .allowedMethods("GET", "POST", "PUT", "DELETE");
                // registry.addMapping("/ajax/secure/login/**")
                // .allowedMethods("GET", "POST", "PUT", "DELETE");
                // registry.addMapping("/api/**")
                // .allowedMethods("GET", "POST", "PUT", "DELETE");
                // registry.addMapping("/**")
                // .allowedOrigins("http://localhost:5173", "http://localhost:5174",
                // "http://localhost:5175", "http://localhost:4173",
                // "http://localhost:8080")
                // .allowedMethods("GET", "POST", "PUT", "DELETE");
                registry.addMapping("/**")
                                .allowedOrigins("*") // 暫時改為 * 進行測試
                                .allowedMethods("GET", "POST", "PUT", "DELETE");
                // .allowedHeaders("*");
        }

}
