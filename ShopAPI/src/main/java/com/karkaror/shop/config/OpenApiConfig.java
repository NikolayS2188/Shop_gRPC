package com.karkaror.shop.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация OpenAPI/Swagger UI.
 * <p>
 * Настраивает:
 * <ul>
 *     <li>Информацию об API (название, версия, описание);</li>
 *     <li>Security scheme для JWT — добавляет кнопку "Authorize" в Swagger UI.</li>
 * </ul>
 * </p>
 */
@Configuration
public class OpenApiConfig {

    /**
     * Имя схемы безопасности. Используется как ключ в {@link Components}
     * и в {@link SecurityRequirement}.
     */
    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI shopOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ShopAPI")
                        .description("REST API для магазина бытовой техники")
                        .version("1.0.0"))

                // 1. Регистрируем схему безопасности:
                //    HTTP Bearer с JWT-токеном.
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("""
                                                JWT access token.
                                                Получите его через POST /api/v1/auth (login)
                                                или POST /api/v1/register.
                                                Введите токен без префикса 'Bearer ' — Swagger добавит сам.
                                                """)))

                // 2. Применяем схему глобально:
                //    Swagger UI будет показывать поле ввода токена для всех эндпоинтов.
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
