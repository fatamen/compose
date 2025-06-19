package tw.com.ispan;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SpringbootConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/ajax/pages/products/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE");
        registry.addMapping("/ajax/secure/login/**")
        		.allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
