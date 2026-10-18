package com.karkaror.shop.context;

/**
 * ThreadLocal-хранилище для JWT текущего HTTP-запроса.
 * <p>Заполняется в {@code AuthInterceptor} и читается в
 *    {@code JwtClientInterceptor} для передачи токена в gRPC metadata.</p>
 * <p>ThreadLocal обязателен: один поток обрабатывает один HTTP-запрос,
 *    а gRPC-вызов происходит в том же потоке.</p>
 */
public final class TokenContext {

    private static final ThreadLocal<String> TOKEN = new ThreadLocal<>();

    private TokenContext() {}

    public static void set(String token) { TOKEN.set(token); }
    public static String get() { return TOKEN.get(); }
    public static void clear() { TOKEN.remove(); }              // обязательно в afterCompletion
}
