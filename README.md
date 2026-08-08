# B2B Micro-Payment Transaction Engine

![Java](https://img.shields.io/badge/Java-21+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3+-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)

Un motor de procesamiento de micro-pagos diseñado para entornos B2B de alta exigencia. Esta API RESTful orquesta transacciones financieras garantizando consistencia absoluta de datos, control de duplicidad mediante llaves de idempotencia y validación estricta de estados.

El diseño arquitectónico está preparado para integrarse con pasarelas tradicionales o servir como capa de infraestructura para automatización financiera y liquidaciones con *stablecoins*.

## 🏗️ Arquitectura y Patrones Core

*   **Idempotency Engine:** Escudo de base de datos que intercepta peticiones de red duplicadas o reintentos fallidos, cacheando el código HTTP y el payload original en formato `jsonb` para evitar dobles cobros.
*   **Finite State Machine (FSM):** Modelo de dominio rico encapsulado. Las transacciones de los pagos (ej. `CREATED` -> `AUTHORIZED` -> `CONFIRMED`) están gobernadas por reglas matemáticas inmutables que rechazan cualquier salto de estado ilegal.
*   **ACID Transactions & Optimistic Locking:** Uso estricto de orquestación `@Transactional` y versionado `@Version` de Hibernate. O la mutación del pago y el historial de idempotencia se guardan juntos, o la base de datos ejecuta un *rollback* total.
*   **Global Exception Handling:** Interceptor global (`@RestControllerAdvice`) que atrapa excepciones de negocio (pagos no encontrados, transacciones ilegales) y las transforma en respuestas JSON estructuradas y estandarizadas (RFC 7807).

## 🚀 Instalación y Despliegue

### Prerrequisitos
*   Java JDK 17 o superior.
*   PostgreSQL corriendo en el puerto `5432`.
*   Maven.

### Configuración
Configura tus credenciales de base de datos en el archivo `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/tu_base_de_datos
spring.datasource.username=postgres
spring.datasource.password=tu_password
spring.jpa.hibernate.ddl-auto=update
``````

### Ejecución

Compila y levanta la aplicación:

```bash
./mvnw spring-boot:run

```

El servidor estará escuchando por defecto en `http://localhost:8080`.

## 🔌 API Endpoints & Flujo de Uso

### 1. Inicializar un Pago (Sembrar)

Crea una nueva intención de pago. El sistema la registrará en estado `CREATED`.

**POST** `/api/v1/payments`

```json
{
  "amount": 1500.50,
  "currency": "USD",
  "idempotencyKey": "init-key-001"
}

```

**Respuesta (201 Created):**

```json
{
  "status": "SUCCESS",
  "paymentId": 1
}

```

### 2. Orquestar Estado (Webhook)

Simula la respuesta de una pasarela o red financiera para avanzar el estado del pago. Si envías la misma `idempotencyKey` dos veces, el motor devolverá la respuesta cacheada sin tocar la máquina de estados.

**POST** `/api/v1/payments/webhooks`

```json
{
  "paymentId": 1,
  "newState": "AUTHORIZED",
  "idempotencyKey": "webhook-auth-key-001"
}

```

**Respuesta (200 OK):**

```json
{
  "status": "SUCCESS",
  "paymentId": 1
}

```

*(Si intentas enviar un estado ilegal, como pasar directamente a `RECONCILED` saltando pasos, el API devolverá un **409 Conflict** con un mensaje de error estructurado).*

### 3. Consultar Estado

Obtén el estado inmutable actual del pago.

**GET** `/api/v1/payments/{paymentId}`

**Respuesta (200 OK):**

```json
"AUTHORIZED"

```
