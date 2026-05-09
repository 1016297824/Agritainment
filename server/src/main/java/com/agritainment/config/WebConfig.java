package com.agritainment.config;

import com.agritainment.interceptor.AuthInterceptor;
import com.agritainment.interceptor.LoggingInterceptor;
import com.agritainment.interceptor.RoleInterceptor;
import com.agritainment.logging.RequestLoggingFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;
    private final AuthInterceptor authInterceptor;
    private final RoleInterceptor roleInterceptor;

    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;

    public WebConfig(LoggingInterceptor loggingInterceptor, AuthInterceptor authInterceptor, RoleInterceptor roleInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
        this.authInterceptor = authInterceptor;
        this.roleInterceptor = roleInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/api/v1/**")
                .order(1);
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns("/api/v1/auth/login", "/api/v1/auth/register",
                        "/api/v1/auth/admin-login", "/api/v1/auth/sms-code",
                        "/api/v1/journals/shared", "/api/v1/products",
                        "/api/v1/products/{id}", "/api/v1/dining/dishes")
                .order(2);
        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/api/v1/**")
                .order(3);
    }

    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilterRegistration() {
        FilterRegistrationBean<RequestLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestLoggingFilter());
        registration.addUrlPatterns("/api/v1/*");
        registration.setOrder(1);
        return registration;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/**")
                .allowedOriginPatterns(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
