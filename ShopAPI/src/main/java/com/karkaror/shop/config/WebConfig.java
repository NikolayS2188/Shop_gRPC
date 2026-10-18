package com.karkaror.shop.config;

import com.karkaror.shop.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Регистрация middleware ({@link AuthInterceptor}) в Spring MVC.
 * <p>Interceptor применяется ко всем {@code /api/v1/**}, но внутри
 *    сам решает, нужна ли авторизация (по наличию {@code @Authorized}).</p>
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/v1/**");
    }
}
