package com.karkaror.shop.interceptor;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * gRPC ClientInterceptor: автоматически добавляет deadline ко всем исходящим вызовам.
 * <p>
 * <b>Deadline</b> — абсолютное время, к которому вызов должен завершиться.
 * gRPC передаёт его по сети в заголовке {@code grpc-timeout}. Если сервер
 * не ответил вовремя — клиент получает {@link Status.Code#DEADLINE_EXCEEDED}.
 * </p>
 * <p>
 * <b>Почему deadline, а не timeout:</b>
 * <ul>
 *     <li>Deadline — абсолютное время, «ответь до 14:30:05»;</li>
 *     <li>Timeout — относительное, «подожди 5 секунд».</li>
 * </ul>
 * Deadline «протекает» через цепочку сервисов: если у клиента 5 секунд,
 * и он вызвал сервис A, который вызвал сервис B — у B осталось меньше 5 секунд.
 * </p>
 * <p>
 * <b>Зачем нужен:</b>
 * <ul>
 *     <li>Защита от «зависших» вызовов (fail-fast);</li>
 *     <li>Защита от каскадных сбоев (ShopAPI не «съедает» потоки);</li>
 *     <li>Гарантия SLA (ответ в рамках обещанного времени).</li>
 * </ul>
 * </p>
 * <p>
 * <b>Приоритет:</b>
 * Если у вызова <b>уже</b> есть deadline (установлен явно через
 * {@code withDeadlineAfter()}), мы его <b>не перезаписываем</b>.
 * Это позволяет точечно увеличивать/уменьшать deadline при необходимости.
 * </p>
 */
@Component
@Slf4j
public class DeadlineClientInterceptor implements ClientInterceptor {

    /**
     * Deadline по умолчанию (мс) для всех gRPC-вызовов.
     * Читается из {@code application.properties}: {@code grpc.client.deadline-ms}.
     * <p>
     * Значение по умолчанию — 3000 мс (3 секунды):
     * <ul>
     *     <li>AuthService должен отвечать за ~300 мс (BCrypt + БД);</li>
     *     <li>3 секунды — с запасом на пиковую нагрузку;</li>
     *     <li>Если AuthService не отвечает за 3 секунды — что-то не так.</li>
     * </ul>
     * </p>
     */
    @Value("${shop.grpc.deadline-ms:3000}")
    private long deadlineMs;

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        // Если deadline уже установлен явно — не перезаписываем
        if (callOptions.getDeadline() != null) {
            log.debug("gRPC call {} already has deadline: {}",
                    method.getFullMethodName(), callOptions.getDeadline());
            return next.newCall(method, callOptions);
        }

        // Устанавливаем deadline по умолчанию
        CallOptions optionsWithDeadline = callOptions.withDeadlineAfter(deadlineMs, TimeUnit.MILLISECONDS);

        log.debug("Attached deadline {} ms to gRPC call: {}", deadlineMs, method.getFullMethodName());

        return next.newCall(method, optionsWithDeadline);
    }
}
