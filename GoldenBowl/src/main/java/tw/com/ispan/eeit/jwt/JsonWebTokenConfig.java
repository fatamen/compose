package tw.com.ispan.eeit.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class JsonWebTokenConfig implements WebMvcConfigurer {
	@Autowired
	private JsonWebTokenInterceptor jsonWebTokenInterceptor;

//	@Override
//	public void addInterceptors(InterceptorRegistry registry) {
//		registry.addInterceptor(jsonWebTokenInterceptor).addPathPatterns(
//				"/ajax/pages/products/**",
//				"/rest/pages/products/**");
//	}

}
