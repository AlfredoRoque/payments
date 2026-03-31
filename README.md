# 💳 Payments Microservice

Microservicio de gestión de **suscripciones y pagos** basado en la API de [Stripe](https://stripe.com). Permite crear planes de suscripción, gestionar suscripciones de usuarios y procesar eventos de pago en tiempo real mediante webhooks.

---

## 🛠️ Stack Tecnológico

| Tecnología           | Versión   |
|----------------------|-----------|
| Java                 | 17        |
| Spring Boot          | 3.5.11    |
| Spring Security      | (incluido)|
| Spring Data JPA      | (incluido)|
| PostgreSQL           | (runtime) |
| Stripe Java SDK      | 24.9.0    |
| JJWT                 | 0.11.5    |
| SpringDoc OpenAPI    | 2.8.16    |
| Lombok               | (optional)|
| Docker               | Multi-stage|

---

## 📋 Requisitos Previos

- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- Cuenta de Stripe con acceso a claves de API y webhook

---

## ⚙️ Configuración

El archivo de configuración se encuentra en `src/main/resources/application.yaml`:

```yaml
spring:
  datasource:
    url: 'jdbc:postgresql://localhost:5432/pagos'
    username: 'postgres'
    password: '123456'

server:
  port: 8084

jwt:
  secret: "<tu-jwt-secret>"

stripe:
  secret:
    key: "sk_test_<tu-stripe-secret-key>"
  webhook:
    secret: "whsec_<tu-webhook-secret>"

stripe-urls:
  success-payment:
    path: "http://localhost:4200/payment-success"
  failure-payment:
    path: "http://localhost:4200/payment-failure"
  change-card-return:
    path: "http://localhost:4200/profile"
```

> ⚠️ **Importante:** En producción reemplaza todos los secrets por variables de entorno y nunca expongas claves reales en el repositorio.

---

## 🗄️ Modelo de Base de Datos

| Tabla              | Descripción                                                                 |
|--------------------|-----------------------------------------------------------------------------|
| `plan`             | Planes de suscripción disponibles (nombre, precio, ID de precio en Stripe)  |
| `detalle_plan`     | Características/funcionalidades incluidas en cada plan (icono + descripción)|
| `parametro_plan`   | Límites operativos del plan (pacientes, balances, signos vitales, etc.)     |
| `suscripcion`      | Suscripciones activas de usuarios con su estado y datos de Stripe           |
| `evento_stripe`    | Registro de eventos de Stripe procesados (idempotencia)                     |

### Diagrama simplificado

```
Plan (1) ──── (N) DetailPlan
Plan (1) ──── (1) ParametersPlan
Plan ◄──── priceId ──── Subscription
```

---

## 🔐 Seguridad

El microservicio utiliza **JWT stateless** para autenticación:

- Todas las peticiones deben incluir el header:
  - `Authorization: Bearer <token>`
  - `X-SESSION-USER: <json-con-datos-del-usuario>`
- Los endpoints **públicos** (sin autenticación) son:
  - `GET /swagger-ui.html` y derivados
  - `GET /v3/api-docs/**`
  - `POST /api/payments/webhook`

### Roles de usuario

| Rol      | Descripción                          |
|----------|--------------------------------------|
| `ADMIN`  | Acceso completo al sistema           |
| `PATIENT`| Acceso limitado a sus propios datos  |

---

## 📡 Endpoints REST

### 📦 Planes — `/api/plans`

| Método | Ruta        | Descripción                        | Auth |
|--------|-------------|------------------------------------|------|
| GET    | `/api/plans` | Obtiene todos los planes disponibles | ✅   |

---

### 🔄 Suscripciones — `/api/subscriptions`

| Método | Ruta                              | Descripción                                               | Auth |
|--------|-----------------------------------|-----------------------------------------------------------|------|
| POST   | `/api/subscriptions/create`       | Crea una sesión de Stripe Checkout para nueva suscripción | ✅   |
| POST   | `/api/subscriptions/cancel`       | Cancela la suscripción al final del período actual        | ✅   |
| POST   | `/api/subscriptions/change-plan`  | Cambia el plan de suscripción (proration automática)      | ✅   |
| POST   | `/api/subscriptions/change-cards` | Actualiza el método de pago del usuario                   | ✅   |
| GET    | `/api/subscriptions/exist-subscription` | Verifica si el usuario autenticado tiene suscripción | ✅   |
| GET    | `/api/subscriptions/users/active` | Obtiene la suscripción activa de un usuario por ID        | ✅   |

#### Parámetros

- `POST /create` → `?priceId=price_xxx` — ID del precio en Stripe
- `POST /change-plan` → `?newPriceId=price_xxx` — ID del nuevo precio en Stripe
- `GET /users/active` → `?userId=123` — ID del usuario

---

### 🪝 Webhook — `/api/payments`

| Método | Ruta                     | Descripción                            | Auth |
|--------|--------------------------|----------------------------------------|------|
| POST   | `/api/payments/webhook`  | Recibe y procesa eventos de Stripe     | ❌ Público |

#### Eventos de Stripe procesados

| Evento                          | Acción                                           |
|---------------------------------|--------------------------------------------------|
| `checkout.session.completed`    | Crea la suscripción en base de datos             |
| `customer.subscription.updated` | Actualiza estado/fechas del período              |
| `customer.subscription.deleted` | Marca la suscripción como cancelada              |
| `invoice.payment_succeeded`     | Confirma el pago y actualiza el período          |
| `invoice.payment_failed`        | Marca la suscripción con estado FALLIDO          |

---

## 📊 Estados de Pago (`PaymentStatus`)

| Estado               | Descripción                                      |
|----------------------|--------------------------------------------------|
| `PAGADO`             | Suscripción activa y pago confirmado             |
| `CANCELADO`          | Suscripción cancelada definitivamente            |
| `PENDIENTE_CANCELAR` | Cancelación programada al final del período      |
| `PENDIENTE_CONFIRMAR`| Suscripción creada, esperando confirmación       |
| `FALLIDO`            | El pago falló                                    |

---

## 🚀 Ejecución

### Local con Maven

```bash
# Compilar y ejecutar
./mvnw spring-boot:run

# Solo compilar
./mvnw clean package -DskipTests
```

### Con Docker

```bash
# Construir imagen
docker build -t payments:1.0.0 .

# Ejecutar contenedor
docker run -p 8084:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/pagos \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=123456 \
  -e JWT_SECRET=<tu-secret> \
  -e STRIPE_SECRET_KEY=sk_test_<tu-key> \
  -e STRIPE_WEBHOOK_SECRET=whsec_<tu-secret> \
  payments:1.0.0
```

> El Dockerfile utiliza un build multi-stage:
> - **Stage 1 (builder):** `maven:3.9.6-eclipse-temurin-17` — compila el proyecto
> - **Stage 2 (runtime):** `eclipse-temurin:17-jre-alpine` — imagen ligera con solo el JRE

---

## 📖 Documentación API (Swagger)

Una vez levantada la aplicación, accede a la documentación interactiva en:

```
http://localhost:8084/swagger-ui.html
```

---

## 🔁 Flujo de Suscripción

```
Usuario ──► POST /subscriptions/create?priceId=xxx
              │
              ▼
        Stripe Checkout Session creada
              │
              ▼
        Usuario completa pago en Stripe
              │
              ▼
        Stripe envía webhook: checkout.session.completed
              │
              ▼
        Suscripción guardada en DB (estado: PAGADO)
              │
              ├── Renovación: invoice.payment_succeeded → actualiza fechas
              ├── Fallo de pago: invoice.payment_failed → estado FALLIDO
              ├── Cambio de plan: POST /change-plan → proration en Stripe
              ├── Cambio de tarjeta: POST /change-cards → Billing Portal
              └── Cancelación: POST /cancel → estado PENDIENTE_CANCELAR
```

---

## 📁 Estructura del Proyecto

```
src/main/java/com/rorideas/payments/
├── config/
│   ├── SecurityConfig.java       # Configuración Spring Security + JWT
│   └── StripeConfig.java         # Inicialización del SDK de Stripe
├── controller/
│   ├── PlanController.java       # Endpoints de planes
│   ├── SubscriptionController.java # Endpoints de suscripciones
│   └── StripeWebhookController.java # Endpoint del webhook
├── dto/
│   ├── PlanDto.java
│   ├── DetailPlanDto.java
│   ├── ParametersPlanDto.java
│   ├── SubscriptionDto.java
│   ├── PaymentSubscriptionResponseDto.java
│   └── UserSessionModel.java
├── entity/
│   ├── Plan.java
│   ├── DetailPlan.java
│   ├── ParametersPlan.java
│   ├── Subscription.java
│   └── StripeEvent.java
├── enums/
│   ├── PaymentStatus.java
│   └── UserRol.java
├── repository/
│   ├── PlanRepository.java
│   ├── SubscriptionRepository.java
│   └── StripeEventRepository.java
├── security/
│   ├── JwtFilter.java            # Filtro de validación JWT
│   └── JwtUtil.java              # Utilidades JWT
├── service/
│   ├── PlanService.java
│   ├── SubscriptionService.java  # Lógica de negocio + webhook
│   └── StripeSubscriptionService.java # Integración directa con Stripe API
└── util/
    ├── Constants.java            # Constantes de la aplicación
    ├── SecurityUtils.java        # Utilidades del contexto de seguridad
    └── Utility.java
```

---

## 🧪 Tests

```bash
./mvnw test
```

---

## 📝 Notas Adicionales

- El microservicio **no gestiona usuarios** directamente; recibe la sesión del usuario a través del header `X-SESSION-USER` (JSON serializado) propagado por un API Gateway o servicio de autenticación.
- La **idempotencia** de eventos de Stripe está garantizada mediante la tabla `evento_stripe`, que almacena los IDs de eventos ya procesados.
- El campo `zona` del usuario se almacena en el metadata de Stripe para mantener el contexto de zona horaria en cada suscripción.

