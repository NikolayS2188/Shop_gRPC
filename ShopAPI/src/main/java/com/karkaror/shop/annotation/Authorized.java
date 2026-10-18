package com.karkaror.shop.annotation;

import java.lang.annotation.*;

/**
 * Самописный атрибут авторизации.
 * <p>Ставится на метод контроллера, требующий аутентификации.
 *    Middleware ({@code AuthInterceptor}) проверяет наличие аннотации
 *    и валидирует JWT через gRPC-вызов AuthService.</p>
 * <p>Если токен невалиден — возвращается 401 и запрос не доходит до контроллера.</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Authorized {
}
