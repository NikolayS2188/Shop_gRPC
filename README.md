# gRPC-приложение ShopAPI + AuthService

## Описание

Микросервисная система для магазина бытовой техники, состоящая из двух приложений:

**ShopAPI** (REST API, порт 8080):
- управление клиентами (создание, удаление, поиск, пагинация, смена адреса);
- управление товарами (создание, удаление, получение, уменьшение стока);
- управление поставщиками (создание, удаление, получение, смена адреса);
- управление изображениями товаров (загрузка, замена, удаление, скачивание);
- регистрация, логин, сброс пароля (публичные эндпоинты);
- авторизация всех защищённых эндпоинтов через middleware + gRPC;
- хранение данных в PostgreSQL (`shop_db`, порт 5433);
- документация OpenAPI/Swagger.

**AuthService** (gRPC-сервер, порт 9090):
- регистрация пользователей (`Register`);
- аутентификация (`Login`);
- проверка JWT (`ValidateToken`);
- смена пароля (`ChangePassword`);
- сброс пароля (`ResetPassword`);
- хранение данных в PostgreSQL (`auth_db`, порт 5434);
- хэширование паролей BCrypt (соль внутри хэша);
- JWT HS256.

**Взаимодействие:** ShopAPI → gRPC (HTTP/2) → AuthService. JWT передаётся в metadata.

### Стек технологий
- **Java 21**, **Spring Boot 3.1.5**
- **Spring Data JPA**, **Hibernate 6.2**
- **PostgreSQL**
- **gRPC** (Netty, HTTP/2), **Protobuf**
- **JWT** (Nimbus JOSE+JWT, HS256)
- **BCrypt** (spring-security-crypto)
- **Springdoc OpenAPI** (Swagger UI)
- **Lombok**
- **Gradle**

---

<a id="содержание"></a>
## Содержание

1. [Описание](#описание)
2. [Архитектура](#архитектура)
3. [Запуск приложений](#запуск-приложений)
    - [Требования для запуска](#требования-для-запуска)
    - [Переменные окружения](#переменные-окружения)
    - [1. Запустить PostgreSQL](#1-запустить-postgresql)
    - [2. Настроить application.properties](#2-настроить-applicationproperties)
    - [3. Запустить AuthService](#3-запустить-authservice)
    - [4. Запустить ShopAPI](#4-запустить-shopapi)
4. [API](#api)
    - [ShopAPI (REST)](#shopapi-rest)
    - [AuthService (gRPC)](#authservice-grpc)
5. [Примеры curl-запросов](#примеры-curl-запросов)
    - [1. Аутентификация](#1-аутентификация)
        - [1.1. Регистрация пользователя](#11-регистрация-пользователя)
        - [1.2. Логин](#12-логин)
        - [1.3. Сброс пароля](#13-сброс-пароля)
        - [1.4. Ошибка: повторная регистрация](#14-ошибка-повторная-регистрация)
        - [1.5. Ошибка: неверные учётные данные](#15-ошибка-неверные-учётные-данные)
        - [1.6. Ошибка: запрос без токена](#16-ошибка-запрос-без-токена)
        - [1.7. Ошибка: невалидный токен](#17-ошибка-невалидный-токен)
    - [2. Клиенты](#2-клиенты)
        - [2.1. Создать клиента (новый адрес)](#21-создать-клиента-новый-адрес)
        - [2.2. Создать клиента (существующий адрес)](#22-создать-клиента-существующий-адрес)
        - [2.3. Ошибка: оба поля addressId и address заданы](#23-ошибка-оба-поля-addressid-и-address-заданы)
        - [2.4. Получить всех клиентов](#24-получить-всех-клиентов)
        - [2.5. Получить всех клиентов с пагинацией](#25-получить-всех-клиентов-с-пагинацией)
        - [2.6. Получить клиентов по имени и фамилии](#26-получить-клиентов-по-имени-и-фамилии)
        - [2.7. Изменить адрес клиента](#27-изменить-адрес-клиента)
        - [2.8. Удалить клиента](#28-удалить-клиента)
        - [2.9. Ошибка: клиент не найден](#29-ошибка-клиент-не-найден)
        - [2.10. Ошибка: невалидные параметры пагинации](#210-ошибка-невалидные-параметры-пагинации)
    - [3. Товары](#3-товары)
        - [3.1. Создать товар](#31-создать-товар)
        - [3.2. Получить товар по id](#32-получить-товар-по-id)
        - [3.3. Получить все доступные товары](#33-получить-все-доступные-товары)
        - [3.4. Уменьшить количество товара](#34-уменьшить-количество-товара)
        - [3.5. Ошибка: недостаточно товара на складе](#35-ошибка-недостаточно-товара-на-складе)
        - [3.6. Ошибка: некорректное значение amount](#36-ошибка-некорректное-значение-amount)
        - [3.7. Удалить товар](#37-удалить-товар)
    - [4. Поставщики](#4-поставщики)
        - [4.1. Создать поставщика (новый адрес)](#41-создать-поставщика-новый-адрес)
        - [4.2. Создать поставщика (существующий адрес)](#42-создать-поставщика-существующий-адрес)
        - [4.3. Получить всех поставщиков](#43-получить-всех-поставщиков)
        - [4.4. Получить поставщика по id](#44-получить-поставщика-по-id)
        - [4.5. Изменить адрес поставщика](#45-изменить-адрес-поставщика)
        - [4.6. Удалить поставщика](#46-удалить-поставщика)
        - [4.7. Ошибка: поставщик не найден](#47-ошибка-поставщик-не-найден)
    - [5. Изображения](#5-изображения)
        - [5.1. Добавить изображение товару](#51-добавить-изображение-товару)
        - [5.2. Заменить изображение по id](#52-заменить-изображение-по-id)
        - [5.3. Получить изображение по id товара](#53-получить-изображение-по-id-товара)
        - [5.4. Получить изображение по id изображения](#54-получить-изображение-по-id-изображения)
        - [5.5. Удалить изображение по id](#55-удалить-изображение-по-id)
        - [5.6. Ошибка: изображение не найдено](#56-ошибка-изображение-не-найдено)
    - [6. Документация](#6-документация)
        - [6.1. Swagger UI](#61-swagger-ui)
        - [6.2. OpenAPI JSON](#62-openapi-json)
    - [7. gRPC AuthService](#7-grpc-authservice)
        - [7.1. Прямой вызов через grpcurl](#71-прямой-вызов-через-grpcurl)
        - [7.2. Проверка токена через gRPC](#72-проверка-токена-через-grpc)
6. [Очистка базы данных](#очистка-базы-данных)
    - [Просмотр данных](#просмотр-данных)
    - [Удаление всех клиентов](#удаление-всех-клиентов)
    - [Удаление всех товаров](#удаление-всех-товаров)
    - [Удаление всех поставщиков](#удаление-всех-поставщиков)
    - [Удаление всех изображений](#удаление-всех-изображений)
    - [Очистка auth_db](#очистка-auth_db)
    - [Полная очистка всех таблиц](#полная-очистка-всех-таблиц)
    - [Полное удаление контейнеров и данных](#полное-удаление-контейнеров-и-данных)
7. [Лицензия](#лицензия)

---

<a id="архитектура"></a>
## Архитектура

```
┌──────────────────────────────┐         ┌──────────────────────────────┐
│   ShopAPI (REST)             │         │   AuthService (gRPC)         │
│   ─────────────────────      │         │   ─────────────────────      │
│   Порт: 8080 (HTTP/1.1)      │         │   Порт: 9090 (HTTP/2)        │
│   БД: shop_db (5433)         │         │   БД: auth_db (5434)         │
│                              │         │                              │
│   Controllers + Middleware   │──gRPC──▶│   ┌────────────────────┐     │
│   Services (interfaces)      │         │   │ Register           │     │
│   Repositories (JPA)         │         │   │ Login              │     │
│   DTO + Mappers              │         │   │ ValidateToken      │     │
│                              │         │   │ ChangePassword     │     │
│   JWT в metadata ────────────┼─────────┼──▶│ ResetPassword      │     │
│   Deadline (3s) ─────────────┼─────────┼──▶│                    │     │
│                              │         │   └────────────────────┘     │
│                              │         │                              │
│                              │         │   BCrypt (cost=12) + соль    │
│                              │         │   JWT HS256                  │
└──────────────────────────────┘         └──────────────────────────────┘
```
> REST-клиент → `ShopAPI` (`AuthInterceptor` проверяет `@Authorized` → gRPC `ValidateToken` → `AuthService`) → если токен валиден, запрос идёт в контроллер → сервис → репозиторий → `shop_db`.  
> JWT передаётся в gRPC metadata автоматически через `JwtClientInterceptor`;  
> deadline — через `DeadlineClientInterceptor`.  
> Ошибки AuthService маппятся из gRPC-статусов в HTTP-коды через `AuthClientServiceImpl` и `GlobalExceptionHandler`.

**Ключевые компоненты:**

| Компонент | Где | Что делает |
|-----------|-----|-----------|
| `AuthInterceptor` (HandlerInterceptor) | ShopAPI | Middleware: проверяет `@Authorized`, вызывает gRPC |
| `JwtClientInterceptor` (ClientInterceptor) | ShopAPI | Добавляет JWT в metadata gRPC |
| `DeadlineClientInterceptor` (ClientInterceptor) | ShopAPI | Добавляет deadline (3 сек) |
| `AuthServerInterceptor` (ServerInterceptor) | AuthService | Читает JWT из metadata |
| `GrpcExceptionHandler` (ServerInterceptor) | AuthService | Маппит Java-исключения в gRPC-статусы |
| `TokenContext` (ThreadLocal) | ShopAPI | Передаёт токен между middleware и gRPC-клиентом |

---

<a id="запуск-приложений"></a>
## Запуск приложений

<a id="требования-для-запуска"></a>
### Требования для запуска
- **JDK 21**
- **Docker** и **Docker Compose** (для хранения данных двух сервисов в PostgreSQL)
- **Gradle**
- Свободные порты: `8080` (ShopAPI), `9090` (AuthService), `5433`/`5434` (PostgreSQL)

<a id="переменные-окружения"></a>
### Переменные окружения

| Переменная `application.properties` | Описание | Значение по умолчанию (dev)         | Сервис |
|------------------------------------|----------|-------------------------------------|--------|
| `shop.grpc.deadline-ms` | Таймаут gRPC-вызовов к AuthService | `3000`                              | ShopAPI |
| `jwt.secret` | Base64-секрет для HS256 | \*пример в `application.properties` | AuthService |
| `jwt.access-ttl-minutes` | Время жизни JWT | `60`                                | AuthService |
| `bcrypt.cost` | Стоимость BCrypt | `12`                                | AuthService |
| `grpc.client.auth-service.address` | Адрес AuthService | `static://localhost:9090`           | ShopAPI |

> \***Секреты в проде** должны передаваться через переменные окружения, а не храниться в репозитории.

### 1. Запустить PostgreSQL

**В контейнерах Docker (две БД: `shop_db` на 5433, `auth_db` на 5434):**

```bash
docker compose up -d
```

**Вариант Б. Локально:**

```bash
psql -U postgres
CREATE DATABASE shop_db;
CREATE DATABASE auth_db;
\q
```

### 2. Настроить application.properties

**ShopAPI/src/main/resources/application.properties:**
```properties
server.port=8080

spring.datasource.url=jdbc:postgresql://localhost:5433/shop_db
spring.datasource.username=postgres
spring.datasource.password=postgres

# gRPC-клиент → AuthService
grpc.client.auth-service.address=static://localhost:9090
grpc.client.auth-service.negotiationType=PLAINTEXT
shop.grpc.deadline-ms=3000

springdoc.swagger-ui.path=/swagger/index.html
springdoc.api-docs.path=/v3/api-docs
```

**AuthService/src/main/resources/application.properties:**
```properties
grpc.server.port=9090

spring.datasource.url=jdbc:postgresql://localhost:5434/auth_db
spring.datasource.username=postgres
spring.datasource.password=postgres

jwt.secret=ZGV2LW9ubHktc2VjcmV0LWNoYW5nZS1tZS1pbi1wcm9kdWN0aW9uLTEyMzQ1Njc4OTBhYmNkZWZnaGlqa2xtbm9wcXJzdHV2d3h5eg==
jwt.access-ttl-minutes=60

bcrypt.cost=12
```

### 3. Запустить AuthService

**Терминал 1:**
```bash
cd AuthService
./gradlew bootRun
```

gRPC-сервер на порту **9090**.

### 4. Запустить ShopAPI

**Терминал 2:**
```bash
cd ShopAPI
./gradlew bootRun
```

Tomcat на порту **8080**.

Приложение доступно: `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger/index.html`

Ручная проверка выполняется через Swagger/curl (автотесты не предусмотрены).

[↑ К содержанию](#содержание)

---

<a id="api"></a>
## API

<a id="shopapi-rest"></a>
### ShopAPI (REST)

**Публичные (без `@Authorized`):**
```
POST    /api/v1/register            - регистрация (возвращает JWT)
POST    /api/v1/auth                - логин (возвращает JWT)
POST    /api/v1/reset               - сброс пароля (новый пароль в консоль AuthService)
```

**Защищённые (`@Authorized` — требуют `Authorization: Bearer <token>`):**

Клиенты:
```
POST    /api/v1/clients
GET     /api/v1/clients
GET     /api/v1/clients/search?name={name}&surname={surname}
PATCH   /api/v1/clients/{id}/address
DELETE  /api/v1/clients/{id}
```

Товары:
```
POST    /api/v1/products
GET     /api/v1/products
GET     /api/v1/products/{id}
PATCH   /api/v1/products/{id}/decrease-stock?amount={amount}
DELETE  /api/v1/products/{id}
```

Поставщики:
```
POST    /api/v1/suppliers
GET     /api/v1/suppliers
GET     /api/v1/suppliers/{id}
PATCH   /api/v1/suppliers/{id}/address
DELETE  /api/v1/suppliers/{id}
```

Изображения:
```
POST    /api/v1/images/product/{productId}    - multipart/form-data
PUT     /api/v1/images/{imageId}              - multipart/form-data
GET     /api/v1/images/product/{productId}    - octet-stream (attachment)
GET     /api/v1/images/{imageId}              - octet-stream (attachment)
DELETE  /api/v1/images/{imageId}
```

Документация:
```
GET     /swagger/index.html
GET     /v3/api-docs
```

<a id="authservice-grpc"></a>
### AuthService (gRPC)

**Порт:** 9090 (HTTP/2, `PLAINTEXT` в dev)

**Методы:**
```
com.karkaror.auth.AuthService/Register
com.karkaror.auth.AuthService/Login
com.karkaror.auth.AuthService/ValidateToken
com.karkaror.auth.AuthService/ChangePassword
com.karkaror.auth.AuthService/ResetPassword
```

**JWT передаётся в metadata:** `authorization: Bearer <token>`.

[↑ К содержанию](#содержание)

---

<a id="примеры-curl-запросов"></a>
## Примеры curl-запросов

> *Примечания:*
>
> Базовый URL ShopAPI: `http://localhost:8080`.
> Для **Windows PowerShell** используйте `curl.exe` с флагом `--%` (отключает обработку спецсимволов).
>
> Во всех примерах:
> - `TOKEN` — accessToken, полученный после логина/регистрации. Подставляется в заголовок `Authorization: Bearer <TOKEN>`.
> - `CLIENT_ID`, `PRODUCT_ID`, `SUPPLIER_ID`, `IMAGE_ID`, `ADDRESS_ID` — UUID соответствующих сущностей.

---

<a id="1-аутентификация"></a>
### 1. Аутентификация

<a id="11-регистрация-пользователя"></a>
#### 1.1. Регистрация пользователя

**Mac / Linux / cmd:**
```bash
curl -v -X POST http://localhost:8080/api/v1/register -H "Content-Type: application/json" -d "{\"email\":\"user@example.com\",\"firstName\":\"Ivan\",\"lastName\":\"Ivanov\",\"phoneNumber\":\"+7-999-123-45-67\",\"password\":\"password123\"}"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X POST http://localhost:8080/api/v1/register -H "Content-Type: application/json" -d "{\"email\":\"user@example.com\",\"firstName\":\"Ivan\",\"lastName\":\"Ivanov\",\"phoneNumber\":\"+7-999-123-45-67\",\"password\":\"password123\"}"
```

**Ответ (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "userId": "11111111-1111-1111-1111-111111111111"
}
```

[↑ К содержанию](#содержание)

---

<a id="12-логин"></a>
#### 1.2. Логин

**Mac / Linux / cmd:**
```bash
curl -v -X POST http://localhost:8080/api/v1/auth -H "Content-Type: application/json" -d "{\"email\":\"user@example.com\",\"password\":\"password123\"}"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X POST http://localhost:8080/api/v1/auth -H "Content-Type: application/json" -d "{\"email\":\"user@example.com\",\"password\":\"password123\"}"
```

**Ответ (200 OK):** аналогично 1.1.

[↑ К содержанию](#содержание)

---

<a id="13-сброс-пароля"></a>
#### 1.3. Сброс пароля

**Mac / Linux / cmd:**
```bash
curl -v -X POST http://localhost:8080/api/v1/reset -H "Content-Type: application/json" -d "{\"email\":\"user@example.com\"}"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X POST http://localhost:8080/api/v1/reset -H "Content-Type: application/json" -d "{\"email\":\"user@example.com\"}"
```

**Ответ (200 OK):**
```json
{"success": true}
```

**В консоли AuthService появится новый пароль:**
```
========================================
 EMAIL STUB — Password Reset
 To: user@example.com
 New password: aB3$xY9pQ2mN
========================================
```

[↑ К содержанию](#содержание)

---

<a id="14-ошибка-повторная-регистрация"></a>
#### 1.4. Ошибка: повторная регистрация

**Mac / Linux / cmd:**
```bash
curl -v -X POST http://localhost:8080/api/v1/register -H "Content-Type: application/json" -d "{\"email\":\"user@example.com\",\"firstName\":\"Ivan\",\"lastName\":\"Ivanov\",\"phoneNumber\":\"+7-999-123-45-67\",\"password\":\"password123\"}"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X POST http://localhost:8080/api/v1/register -H "Content-Type: application/json" -d "{\"email\":\"user@example.com\",\"firstName\":\"Ivan\",\"lastName\":\"Ivanov\",\"phoneNumber\":\"+7-999-123-45-67\",\"password\":\"password123\"}"
```

**Ответ (409 Conflict):**
```json
{
  "timestamp": "2026-10-07T12:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "User with email 'user@example.com' already exists",
  "path": "/api/v1/register"
}
```

[↑ К содержанию](#содержание)

---

<a id="15-ошибка-неверные-учётные-данные"></a>
#### 1.5. Ошибка: неверные учётные данные

**Mac / Linux / cmd:**
```bash
curl -v -X POST http://localhost:8080/api/v1/auth -H "Content-Type: application/json" -d "{\"email\":\"user@example.com\",\"password\":\"wrong_password\"}"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X POST http://localhost:8080/api/v1/auth -H "Content-Type: application/json" -d "{\"email\":\"user@example.com\",\"password\":\"wrong_password\"}"
```

**Ответ (401 Unauthorized):**
```json
{
  "timestamp": "2026-10-07T12:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/api/v1/auth"
}
```

[↑ К содержанию](#содержание)

---

<a id="16-ошибка-запрос-без-токена"></a>
#### 1.6. Ошибка: запрос без токена

**Mac / Linux / cmd:**
```bash
curl -v -X GET http://localhost:8080/api/v1/clients
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X GET http://localhost:8080/api/v1/clients
```

**Ответ (401 Unauthorized):**
```json
{"error":"Missing or invalid Authorization header"}
```

[↑ К содержанию](#содержание)

---

<a id="17-ошибка-невалидный-токен"></a>
#### 1.7. Ошибка: невалидный токен

**Mac / Linux / cmd:**
```bash
curl -v -X GET http://localhost:8080/api/v1/clients -H "Authorization: Bearer invalid.token.here"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X GET http://localhost:8080/api/v1/clients -H "Authorization: Bearer invalid.token.here"
```

**Ответ (401 Unauthorized):**
```json
{"error":"Invalid or expired token"}
```

[↑ К содержанию](#содержание)

---

<a id="2-клиенты"></a>
### 2. Клиенты

> Все запросы требуют `Authorization: Bearer <TOKEN>`.

<a id="21-создать-клиента-новый-адрес"></a>
#### 2.1. Создать клиента (новый адрес)

**Mac / Linux / cmd:**
```bash
curl -v -X POST http://localhost:8080/api/v1/clients -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" -d "{\"clientName\":\"Ivan\",\"clientSurname\":\"Ivanov\",\"birthday\":\"1990-01-15\",\"gender\":\"MALE\",\"address\":{\"country\":\"Russia\",\"city\":\"Moscow\",\"street\":\"Tverskaya 1\"}}"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X POST http://localhost:8080/api/v1/clients -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" -d "{\"clientName\":\"Ivan\",\"clientSurname\":\"Ivanov\",\"birthday\":\"1990-01-15\",\"gender\":\"MALE\",\"address\":{\"country\":\"Russia\",\"city\":\"Moscow\",\"street\":\"Tverskaya 1\"}}"
```

[↑ К содержанию](#содержание)

---

<a id="22-создать-клиента-существующий-адрес"></a>
#### 2.2. Создать клиента (существующий адрес)

**Mac / Linux / cmd:**
```bash
curl -v -X POST http://localhost:8080/api/v1/clients -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" -d "{\"clientName\":\"Petr\",\"clientSurname\":\"Petrov\",\"birthday\":\"1985-05-20\",\"gender\":\"MALE\",\"addressId\":\"550e8400-e29b-41d4-a716-446655440000\"}"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X POST http://localhost:8080/api/v1/clients -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" -d "{\"clientName\":\"Petr\",\"clientSurname\":\"Petrov\",\"birthday\":\"1985-05-20\",\"gender\":\"MALE\",\"addressId\":\"550e8400-e29b-41d4-a716-446655440000\"}"
```

[↑ К содержанию](#содержание)

---

<a id="23-ошибка-оба-поля-addressid-и-address-заданы"></a>
#### 2.3. Ошибка: оба поля addressId и address заданы

**Mac / Linux / cmd:**
```bash
curl -v -X POST http://localhost:8080/api/v1/clients -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" -d "{\"clientName\":\"Test\",\"clientSurname\":\"Test\",\"birthday\":\"1990-01-15\",\"gender\":\"MALE\",\"addressId\":\"550e8400-e29b-41d4-a716-446655440000\",\"address\":{\"country\":\"Russia\",\"city\":\"Moscow\",\"street\":\"Tverskaya 1\"}}"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X POST http://localhost:8080/api/v1/clients -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" -d "{\"clientName\":\"Test\",\"clientSurname\":\"Test\",\"birthday\":\"1990-01-15\",\"gender\":\"MALE\",\"addressId\":\"550e8400-e29b-41d4-a716-446655440000\",\"address\":{\"country\":\"Russia\",\"city\":\"Moscow\",\"street\":\"Tverskaya 1\"}}"
```

**Ответ (400 Bad Request):**
```json
{"status":400,"error":"Bad Request","message":"addressProvided: Either addressId or address must be provided, but not both",...}
```

[↑ К содержанию](#содержание)

---

<a id="24-получить-всех-клиентов"></a>
#### 2.4. Получить всех клиентов

**Mac / Linux / cmd:**
```bash
curl -v -X GET http://localhost:8080/api/v1/clients -H "Authorization: Bearer TOKEN"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X GET http://localhost:8080/api/v1/clients -H "Authorization: Bearer TOKEN"
```

[↑ К содержанию](#содержание)

---

<a id="25-получить-всех-клиентов-с-пагинацией"></a>
#### 2.5. Получить всех клиентов с пагинацией

**Mac / Linux / cmd:**
```bash
curl -v -X GET "http://localhost:8080/api/v1/clients?limit=10&offset=0" -H "Authorization: Bearer TOKEN"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X GET "http://localhost:8080/api/v1/clients?limit=10&offset=0" -H "Authorization: Bearer TOKEN"
```

[↑ К содержанию](#содержание)

---

<a id="26-получить-клиентов-по-имени-и-фамилии"></a>
#### 2.6. Получить клиентов по имени и фамилии

**Mac / Linux / cmd:**
```bash
curl -v -X GET "http://localhost:8080/api/v1/clients/search?name=Ivan&surname=Ivanov" -H "Authorization: Bearer TOKEN"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X GET "http://localhost:8080/api/v1/clients/search?name=Ivan&surname=Ivanov" -H "Authorization: Bearer TOKEN"
```

[↑ К содержанию](#содержание)

---

<a id="27-изменить-адрес-клиента"></a>
#### 2.7. Изменить адрес клиента

**Mac / Linux / cmd:**
```bash
curl -v -X PATCH http://localhost:8080/api/v1/clients/CLIENT_ID/address -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" -d "{\"country\":\"Russia\",\"city\":\"SPb\",\"street\":\"Nevsky 1\"}"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X PATCH http://localhost:8080/api/v1/clients/CLIENT_ID/address -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" -d "{\"country\":\"Russia\",\"city\":\"SPb\",\"street\":\"Nevsky 1\"}"
```

[↑ К содержанию](#содержание)

---

<a id="28-удалить-клиента"></a>
#### 2.8. Удалить клиента

**Mac / Linux / cmd:**
```bash
curl -v -X DELETE http://localhost:8080/api/v1/clients/CLIENT_ID -H "Authorization: Bearer TOKEN"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X DELETE http://localhost:8080/api/v1/clients/CLIENT_ID -H "Authorization: Bearer TOKEN"
```

**Ответ (204 No Content).**

[↑ К содержанию](#содержание)

---

<a id="29-ошибка-клиент-не-найден"></a>
#### 2.9. Ошибка: клиент не найден

**Mac / Linux / cmd:**
```bash
curl -v -X DELETE http://localhost:8080/api/v1/clients/00000000-0000-0000-0000-000000000000 -H "Authorization: Bearer TOKEN"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X DELETE http://localhost:8080/api/v1/clients/00000000-0000-0000-0000-000000000000 -H "Authorization: Bearer TOKEN"
```

**Ответ (404 Not Found).**

[↑ К содержанию](#содержание)

---

<a id="210-ошибка-невалидные-параметры-пагинации"></a>
#### 2.10. Ошибка: невалидные параметры пагинации

**Mac / Linux / cmd:**
```bash
curl -v -X GET "http://localhost:8080/api/v1/clients?limit=-5&offset=0" -H "Authorization: Bearer TOKEN"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X GET "http://localhost:8080/api/v1/clients?limit=-5&offset=0" -H "Authorization: Bearer TOKEN"
```

**Ответ (400 Bad Request).**

[↑ К содержанию](#содержание)

---

<a id="3-товары"></a>
### 3. Товары

<a id="31-создать-товар"></a>
#### 3.1. Создать товар

**Mac / Linux / cmd:**
```bash
curl -v -X POST http://localhost:8080/api/v1/products -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" -d "{\"name\":\"Samsung Galaxy S24\",\"category\":\"Smartphones\",\"price\":79999.99,\"availableStock\":50,\"supplierId\":\"SUPPLIER_ID\"}"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X POST http://localhost:8080/api/v1/products -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" -d "{\"name\":\"Samsung Galaxy S24\",\"category\":\"Smartphones\",\"price\":79999.99,\"availableStock\":50,\"supplierId\":\"SUPPLIER_ID\"}"
```

[↑ К содержанию](#содержание)

---

<a id="32-получить-товар-по-id"></a>
#### 3.2. Получить товар по id

**Mac / Linux / cmd:**
```bash
curl -v -X GET http://localhost:8080/api/v1/products/PRODUCT_ID -H "Authorization: Bearer TOKEN"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X GET http://localhost:8080/api/v1/products/PRODUCT_ID -H "Authorization: Bearer TOKEN"
```

[↑ К содержанию](#содержание)

---

<a id="33-получить-все-доступные-товары"></a>
#### 3.3. Получить все доступные товары

**Mac / Linux / cmd:**
```bash
curl -v -X GET http://localhost:8080/api/v1/products -H "Authorization: Bearer TOKEN"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X GET http://localhost:8080/api/v1/products -H "Authorization: Bearer TOKEN"
```

[↑ К содержанию](#содержание)

---

<a id="34-уменьшить-количество-товара"></a>
#### 3.4. Уменьшить количество товара

**Mac / Linux / cmd:**
```bash
curl -v -X PATCH "http://localhost:8080/api/v1/products/PRODUCT_ID/decrease-stock?amount=5" -H "Authorization: Bearer TOKEN"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X PATCH "http://localhost:8080/api/v1/products/PRODUCT_ID/decrease-stock?amount=5" -H "Authorization: Bearer TOKEN"
```

[↑ К содержанию](#содержание)

---

<a id="35-ошибка-недостаточно-товара-на-складе"></a>
#### 3.5. Ошибка: недостаточно товара на складе

**Mac / Linux / cmd:**
```bash
curl -v -X PATCH "http://localhost:8080/api/v1/products/PRODUCT_ID/decrease-stock?amount=99999" -H "Authorization: Bearer TOKEN"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X PATCH "http://localhost:8080/api/v1/products/PRODUCT_ID/decrease-stock?amount=99999" -H "Authorization: Bearer TOKEN"
```

**Ответ (400 Bad Request):**
```json
{"status":400,"error":"Bad Request","message":"Not enough stock: available=50, requested=99999",...}
```

[↑ К содержанию](#содержание)

---

<a id="36-ошибка-некорректное-значение-amount"></a>
#### 3.6. Ошибка: некорректное значение amount

**Mac / Linux / cmd:**
```bash
curl -v -X PATCH "http://localhost:8080/api/v1/products/PRODUCT_ID/decrease-stock?amount=0" -H "Authorization: Bearer TOKEN"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X PATCH "http://localhost:8080/api/v1/products/PRODUCT_ID/decrease-stock?amount=0" -H "Authorization: Bearer TOKEN"
```

**Ответ (400 Bad Request):**
```json
{"status":400,"error":"Bad Request","message":"decreaseStock.amount: must be greater than or equal to 1",...}
```

[↑ К содержанию](#содержание)

---

<a id="37-удалить-товар"></a>
#### 3.7. Удалить товар

**Mac / Linux / cmd:**
```bash
curl -v -X DELETE http://localhost:8080/api/v1/products/PRODUCT_ID -H "Authorization: Bearer TOKEN"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X DELETE http://localhost:8080/api/v1/products/PRODUCT_ID -H "Authorization: Bearer TOKEN"
```

**Ответ (204 No Content).**

[↑ К содержанию](#содержание)

---

<a id="4-поставщики"></a>
### 4. Поставщики

<a id="41-создать-поставщика-новый-адрес"></a>
#### 4.1. Создать поставщика (новый адрес)

**Mac / Linux / cmd:**
```bash
curl -v -X POST http://localhost:8080/api/v1/suppliers -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" -d "{\"name\":\"TechSupplier Company\",\"phoneNumber\":\"+7-999-123-45-67\",\"address\":{\"country\":\"Russia\",\"city\":\"Moscow\",\"street\":\"Lenina 5\"}}"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X POST http://localhost:8080/api/v1/suppliers -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" -d "{\"name\":\"TechSupplier Company\",\"phoneNumber\":\"+7-999-123-45-67\",\"address\":{\"country\":\"Russia\",\"city\":\"Moscow\",\"street\":\"Lenina 5\"}}"
```

[↑ К содержанию](#содержание)

---

<a id="42-создать-поставщика-существующий-адрес"></a>
#### 4.2. Создать поставщика (существующий адрес)

**Mac / Linux / cmd:**
```bash
curl -v -X POST http://localhost:8080/api/v1/suppliers -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" -d "{\"name\":\"AnotherSupplier\",\"phoneNumber\":\"+7-999-000-11-22\",\"addressId\":\"44444444-4444-4444-4444-444444444444\"}"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X POST http://localhost:8080/api/v1/suppliers -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" -d "{\"name\":\"AnotherSupplier\",\"phoneNumber\":\"+7-999-000-11-22\",\"addressId\":\"44444444-4444-4444-4444-444444444444\"}"
```

[↑ К содержанию](#содержание)

---

<a id="43-получить-всех-поставщиков"></a>
#### 4.3. Получить всех поставщиков

**Mac / Linux / cmd:**
```bash
curl -v -X GET http://localhost:8080/api/v1/suppliers -H "Authorization: Bearer TOKEN"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X GET http://localhost:8080/api/v1/suppliers -H "Authorization: Bearer TOKEN"
```

[↑ К содержанию](#содержание)

---

<a id="44-получить-поставщика-по-id"></a>
#### 4.4. Получить поставщика по id

**Mac / Linux / cmd:**
```bash
curl -v -X GET http://localhost:8080/api/v1/suppliers/SUPPLIER_ID -H "Authorization: Bearer TOKEN"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X GET http://localhost:8080/api/v1/suppliers/SUPPLIER_ID -H "Authorization: Bearer TOKEN"
```

[↑ К содержанию](#содержание)

---

<a id="45-изменить-адрес-поставщика"></a>
#### 4.5. Изменить адрес поставщика

**Mac / Linux / cmd:**
```bash
curl -v -X PATCH http://localhost:8080/api/v1/suppliers/SUPPLIER_ID/address -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" -d "{\"country\":\"Russia\",\"city\":\"Kazan\",\"street\":\"Baumana 10\"}"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X PATCH http://localhost:8080/api/v1/suppliers/SUPPLIER_ID/address -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" -d "{\"country\":\"Russia\",\"city\":\"Kazan\",\"street\":\"Baumana 10\"}"
```

[↑ К содержанию](#содержание)

---

<a id="46-удалить-поставщика"></a>
#### 4.6. Удалить поставщика

**Mac / Linux / cmd:**
```bash
curl -v -X DELETE http://localhost:8080/api/v1/suppliers/SUPPLIER_ID -H "Authorization: Bearer TOKEN"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X DELETE http://localhost:8080/api/v1/suppliers/SUPPLIER_ID -H "Authorization: Bearer TOKEN"
```

**Ответ (204 No Content).** Связанные товары получат `supplier_id = NULL`.

[↑ К содержанию](#содержание)

---

<a id="47-ошибка-поставщик-не-найден"></a>
#### 4.7. Ошибка: поставщик не найден

**Mac / Linux / cmd:**
```bash
curl -v -X GET http://localhost:8080/api/v1/suppliers/00000000-0000-0000-0000-000000000000 -H "Authorization: Bearer TOKEN"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X GET http://localhost:8080/api/v1/suppliers/00000000-0000-0000-0000-000000000000 -H "Authorization: Bearer TOKEN"
```

**Ответ (404 Not Found).**

[↑ К содержанию](#содержание)

---

<a id="5-изображения"></a>
### 5. Изображения

<a id="51-добавить-изображение-товару"></a>
#### 5.1. Добавить изображение товару

**Mac / Linux / cmd:**
```bash
curl -v -X POST http://localhost:8080/api/v1/images/product/PRODUCT_ID -H "Authorization: Bearer TOKEN" -F "file=@/path/to/image.jpg"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X POST http://localhost:8080/api/v1/images/product/PRODUCT_ID -H "Authorization: Bearer TOKEN" -F "file=@C:\path\to\image.jpg"
```

**Ответ (200 OK):**
```json
{"imageId": "55555555-5555-5555-5555-555555555555"}
```

[↑ К содержанию](#содержание)

---

<a id="52-заменить-изображение-по-id"></a>
#### 5.2. Заменить изображение по id

**Mac / Linux / cmd:**
```bash
curl -v -X PUT http://localhost:8080/api/v1/images/IMAGE_ID -H "Authorization: Bearer TOKEN" -F "file=@/path/to/new-image.jpg"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X PUT http://localhost:8080/api/v1/images/IMAGE_ID -H "Authorization: Bearer TOKEN" -F "file=@C:\path\to\new-image.jpg"
```

[↑ К содержанию](#содержание)

---

<a id="53-получить-изображение-по-id-товара"></a>
#### 5.3. Получить изображение по id товара

**Mac / Linux / cmd:**
```bash
curl -v -OJ http://localhost:8080/api/v1/images/product/PRODUCT_ID -H "Authorization: Bearer TOKEN"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -OJ http://localhost:8080/api/v1/images/product/PRODUCT_ID -H "Authorization: Bearer TOKEN"
```

**Ответ (200 OK):**
- `Content-Type: application/octet-stream`
- `Content-Disposition: attachment; filename="image-<UUID>.bin"`
- Тело — сырые байты.

[↑ К содержанию](#содержание)

---

<a id="54-получить-изображение-по-id-изображения"></a>
#### 5.4. Получить изображение по id изображения

**Mac / Linux / cmd:**
```bash
curl -v -OJ http://localhost:8080/api/v1/images/IMAGE_ID -H "Authorization: Bearer TOKEN"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -OJ http://localhost:8080/api/v1/images/IMAGE_ID -H "Authorization: Bearer TOKEN"
```

[↑ К содержанию](#содержание)

---

<a id="55-удалить-изображение-по-id"></a>
#### 5.5. Удалить изображение по id

**Mac / Linux / cmd:**
```bash
curl -v -X DELETE http://localhost:8080/api/v1/images/IMAGE_ID -H "Authorization: Bearer TOKEN"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X DELETE http://localhost:8080/api/v1/images/IMAGE_ID -H "Authorization: Bearer TOKEN"
```

**Ответ (204 No Content).**

[↑ К содержанию](#содержание)

---

<a id="56-ошибка-изображение-не-найдено"></a>
#### 5.6. Ошибка: изображение не найдено

**Mac / Linux / cmd:**
```bash
curl -v -X GET http://localhost:8080/api/v1/images/00000000-0000-0000-0000-000000000000 -H "Authorization: Bearer TOKEN"
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X GET http://localhost:8080/api/v1/images/00000000-0000-0000-0000-000000000000 -H "Authorization: Bearer TOKEN"
```

**Ответ (404 Not Found).**

[↑ К содержанию](#содержание)

---

<a id="6-документация"></a>
### 6. Документация

<a id="61-swagger-ui"></a>
#### 6.1. Swagger UI

**Mac / Linux / cmd:**
```bash
open http://localhost:8080/swagger/index.html
```

**Windows (PowerShell):**
```powershell
Start-Process "http://localhost:8080/swagger/index.html"
```

**Как пользоваться:**
1. Открыть Swagger UI.
2. Нажать **Authorize** (замок в правом верхнем углу).
3. Вставить JWT-токен (без `Bearer `).
4. Нажать **Authorize**, закрыть диалог.
5. Теперь все защищённые эндпоинты автоматически получают заголовок.

[↑ К содержанию](#содержание)

---

<a id="62-openapi-json"></a>
#### 6.2. OpenAPI JSON

**Mac / Linux / cmd:**
```bash
curl -v -X GET http://localhost:8080/v3/api-docs
```

**Windows (PowerShell):**
```powershell
curl.exe --% -v -X GET http://localhost:8080/v3/api-docs
```

[↑ К содержанию](#содержание)

---

<a id="7-grpc-authservice"></a>
### 7. gRPC AuthService

> Требуется `grpcurl` — CLI для gRPC. Установка: https://github.com/fullstorydev/grpcurl

<a id="71-прямой-вызов-через-grpcurl"></a>
#### 7.1. Прямой вызов через grpcurl

**Регистрация:**
```bash
grpcurl -plaintext -d "{\"email\":\"user@example.com\",\"first_name\":\"Ivan\",\"last_name\":\"Ivanov\",\"phone_number\":\"+7-999-123-45-67\",\"password\":\"password123\"}" localhost:9090 com.karkaror.auth.AuthService/Register
```

**Ответ:**
```json
{"token": "eyJhbGciOiJIUzI1NiJ9...", "userId": "..."}
```

**Логин:**
```bash
grpcurl -plaintext -d "{\"email\":\"user@example.com\",\"password\":\"password123\"}" localhost:9090 com.karkaror.auth.AuthService/Login
```

[↑ К содержанию](#содержание)

---

<a id="72-проверка-токена-через-grpc"></a>
#### 7.2. Проверка токена через gRPC

**Важно:** JWT передаётся в **metadata**, а не в теле.

```bash
grpcurl -plaintext -H "authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." -d "{}" localhost:9090 com.karkaror.auth.AuthService/ValidateToken
```

**Ответ:**
```json
{"valid": true, "userId": "11111111-1111-1111-1111-111111111111"}
```

[↑ К содержанию](#содержание)

---

<a id="очистка-базы-данных"></a>
## Очистка базы данных

### Просмотр данных

**Mac / Linux / cmd:**
```bash
docker exec -it shop_db psql -U postgres -d shop_db -c "SELECT * FROM client;"
docker exec -it auth_db psql -U postgres -d auth_db -c "SELECT * FROM users;"
```

**Windows (PowerShell):**
```powershell
docker exec -it shop_db psql -U postgres -d shop_db -c "SELECT * FROM client;"
docker exec -it auth_db psql -U postgres -d auth_db -c "SELECT * FROM users;"
```

### Удаление всех клиентов

**Mac / Linux / cmd:**
```bash
docker exec -it shop_db psql -U postgres -d shop_db -c "DELETE FROM client;"
```

**Windows (PowerShell):**
```powershell
docker exec -it shop_db psql -U postgres -d shop_db -c "DELETE FROM client;"
```

### Удаление всех товаров

**Mac / Linux / cmd:**
```bash
docker exec -it shop_db psql -U postgres -d shop_db -c "DELETE FROM product;"
```

**Windows (PowerShell):**
```powershell
docker exec -it shop_db psql -U postgres -d shop_db -c "DELETE FROM product;"
```

### Удаление всех поставщиков

**Mac / Linux / cmd:**
```bash
docker exec -it shop_db psql -U postgres -d shop_db -c "DELETE FROM supplier;"
```

**Windows (PowerShell):**
```powershell
docker exec -it shop_db psql -U postgres -d shop_db -c "DELETE FROM supplier;"
```

### Удаление всех изображений

**Mac / Linux / cmd:**
```bash
docker exec -it shop_db psql -U postgres -d shop_db -c "DELETE FROM images;"
```

**Windows (PowerShell):**
```powershell
docker exec -it shop_db psql -U postgres -d shop_db -c "DELETE FROM images;"
```

### Очистка auth_db

**Mac / Linux / cmd:**
```bash
docker exec -it auth_db psql -U postgres -d auth_db -c "DELETE FROM users;"
```

**Windows (PowerShell):**
```powershell
docker exec -it auth_db psql -U postgres -d auth_db -c "DELETE FROM users;"
```

### Полная очистка всех таблиц

**Mac / Linux / cmd:**
```bash
docker exec -it shop_db psql -U postgres -d shop_db -c "TRUNCATE TABLE product, client, supplier, images, category, address CASCADE;"
docker exec -it auth_db psql -U postgres -d auth_db -c "TRUNCATE TABLE users CASCADE;"
```

**Windows (PowerShell):**
```powershell
docker exec -it shop_db psql -U postgres -d shop_db -c "TRUNCATE TABLE product, client, supplier, images, category, address CASCADE;"
docker exec -it auth_db psql -U postgres -d auth_db -c "TRUNCATE TABLE users CASCADE;"
```

### Полное удаление контейнеров и данных

**Mac / Linux / cmd:**
```bash
docker compose down -v
docker compose up -d
```

**Windows (PowerShell):**
```powershell
docker compose down -v
docker compose up -d
```

[↑ К содержанию](#содержание)

---

<a id="лицензия"></a>
## Лицензия

Проект распространяется под лицензией MIT. Подробности — в файле LICENSE.