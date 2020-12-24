package com.crepass.restfulapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableWebMvc
@Import({SwaggerConfig.class})
public class WebConfig extends WebMvcConfigurerAdapter{

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		super.addInterceptors(registry);
		registry.addInterceptor(createCrePASSApiTokenInterceptor()).addPathPatterns("/api*/**");
	}
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
//		super.addResourceHandlers(registry);
		registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
		registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
	}
	
	@Override
	public void addCorsMappings(CorsRegistry registry) {
//		super.addCorsMappings(registry);
		registry.addMapping("/**");
	}
	
	@Bean
	public CrePASSApiTokenInterceptor createCrePASSApiTokenInterceptor() {
		return new CrePASSApiTokenInterceptor();
	}
}
