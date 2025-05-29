
# URL Shortener

## Описание

Проект — сервис сокращения URL с поддержкой:

- Генерации коротких ссылок
- Хранения данных в PostgreSQL и Redis (кэш с TTL)
- Ограничения количества переходов (maxVisit)
- Автоматического удаления просроченных ссылок из базы и кэша
- Фоновой синхронизации счетчиков из Redis в PostgreSQL
- Очереди на модерацию ссылок через RabbitMQ с автоматическим одобрением
- Аутентификации и регистрации пользователей (Spring Security)

---

## Технологии

- Java 21
- Spring Boot
- Maven
- PostgreSQL
- Redis
- RabbitMQ
- Spring Security
- JPA / Hibernate

---

## Запуск проекта

1. Настройте подключение к базе данных, Redis и RabbitMQ в `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/yourdb
    username: youruser
    password: yourpassword

  redis:
    host: localhost
    port: 6379

  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

2. Соберите и запустите проект:

```bash
mvn clean install
mvn spring-boot:run
```

---

## REST API

### Регистрация пользователя

`POST /auth/register`

**Тело запроса:**

```json
{
  "username": "user1",
  "email": "user1@example.com"
}
```

---

### Создание короткой ссылки

`POST /makeurl`

**Тело запроса:**

```json
{
  "originalUrl": "https://example.com",
  "maxVisit": 10,
  "ttlDays": 10
}
```

**Ответ:**

```
QlWIQK
```

---

### Редирект по короткой ссылке

`GET /makeurl/{code}`

Пример:

```bash
curl -v http://localhost:8080/makeurl/QlWIQK
```

---

### Одобрение ссылки (модерация)

`POST /makeurl/{code}/approve`

---

## Фоновые задачи

- Очистка просроченных ссылок (ежедневно)
- Синхронизация счетчиков посещений из Redis в PostgreSQL

---

## Модерация

Ссылки автоматически отправляются в очередь RabbitMQ для модерации, где слушатель проверяет и одобряет ссылки, удовлетворяющие условиям (например, возраст ссылки).

---

## Дальнейшие планы

- Добавление метрик (Micrometer + Prometheus)
- Контейнеризация и оркестрация (Docker + Kubernetes)
- Панель администратора для просмотра/модерации ссылок
- Email- или Telegram-уведомления при создании новых ссылок
- Реферальная система, ограничение по IP, бан по жалобе

---
