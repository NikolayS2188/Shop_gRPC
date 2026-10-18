plugins {
    java
    id("org.springframework.boot") version "3.1.5"              // Spring Boot плагин для сборки fat JAR
    id("io.spring.dependency-management") version "1.1.5"       // Управление версиями зависимостей из Spring BOM
    id("com.google.protobuf") version "0.9.4"                   // Gradle-плагин, который добавляет в сборку задачи для обработки .proto-файлов (не умеет генерировать Java-код для gRPC). Его задача — найти .proto, вызвать protoc (умеет генерировать только классы сообщений (messages)) и передать ему нужные плагины-генераторы gRPC-стабов.

}

group = "com.karkaror"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starter Web - включает Spring MVC, Tomcat, Jackson и т.д.
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Database - для Spring Boot 3.x используем актуальные версии
    implementation("org.postgresql:postgresql:42.7.2")

    // OpenAPI / Swagger UI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // gRPC-клиент (Spring Boot starter)
    implementation("net.devh:grpc-client-spring-boot-starter:3.0.0.RELEASE")
    implementation("com.google.protobuf:protobuf-java:3.25.1")
    implementation("io.grpc:grpc-protobuf:1.60.0")
    implementation("io.grpc:grpc-stub:1.60.0")
    compileOnly("org.apache.tomcat:annotations-api:6.0.53")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Тестовые зависимости
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

// Настройка protobuf-плагина: где искать .proto и что генерировать
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.1"                  // Компилятор Protobuf
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.60.0"            // Плагин для генерации gRPC-стабов (клиентских и серверных классов Java)
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                create("grpc")                                          // Генерировать код для gRPC (если есть service)
            }
        }
    }
}

// Подключаем папку /proto как источник .proto-файлов
sourceSets {
    main {
        proto {
            srcDir("../proto")
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"                                      // Правильная обработка кириллицы
    options.release.set(21)                                         // Оптимизация под JDK 21
}

tasks.test {
    useJUnitPlatform()                                              // Использовать JUnit Platform для запуска тестов
    maxParallelForks = Runtime.getRuntime().availableProcessors()   // Параллельный запуск тестов
}

springBoot {
    mainClass.set("com.karkaror.shop.ShopApplication")
}
