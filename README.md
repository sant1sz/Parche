# Parche

Plataforma web para restaurantes y cafeterías de Medellín: pedido a la mesa por QR o NFC, cuenta dividida por persona, pantalla de cocina en tiempo real, panel del dueño, registro de clientes, reactivación por WhatsApp y reportes con preguntas en lenguaje natural.

> **Datos de demostración.** El negocio que trae el proyecto, **Café La Ceiba**, es ficticio. Sus mesas, productos y precios son simulados y la API lo marca con `"demo": true`.

## Estado

El proyecto se construye por fases. Fase actual: **0. Base** (v0.1).

| Fase | Qué incluye | Versión |
| --- | --- | --- |
| **0. Base** | Monorepo, backend y web compilando, PostgreSQL con Flyway, menú de Café La Ceiba, CI | v0.1 |
| 1. Pedido a la mesa (MVP) | Menú por QR, carrito por persona, cocina en tiempo real, llamar al mesero, cuenta dividida con QR de Bre-B, login del personal | v0.2 |
| 2. Panel del dueño | CRUD de menú, mesas, QR imprimibles en PDF, usuarios y roles | v0.3 |
| 3. Clientes y reactivación | Registro por código de WhatsApp, consentimiento, regla de 21 días, mensajes | v0.4 |
| 4. Reportes e IA | Historial simulado, panel de ventas, reporte semanal, preguntas a los datos | v0.5 |
| 5. Demo pública | Despliegue, modo demo, video, README final | v1.0 |

## Stack

| Parte | Tecnología |
| --- | --- |
| Backend | Java 21, Spring Boot 4.2 (Web MVC, Data JPA, Validation), Maven |
| Base de datos | PostgreSQL 18; el esquema se maneja solo con migraciones de Flyway (`ddl-auto=validate`) |
| Web | Next.js 16 con TypeScript, App Router y Tailwind CSS 4 |
| Pruebas | JUnit 5 y Testcontainers con PostgreSQL real |
| CI | GitHub Actions |
| Local | PostgreSQL con Docker Compose |

Llegan en fases siguientes: Spring Security con JWT y WebSocket/STOMP (fase 1), y Spring AI con Gemini (fase 4).

## Estructura

```
parche/
├── backend/                 Spring Boot (paquete com.sant1sz.parche)
│   └── src/main/
│       ├── java/.../        controller, dto, exception, model, repository, service
│       └── resources/db/
│           ├── migration/   esquema: V1__esquema_inicial.sql, ...
│           └── demo/        datos simulados: R__demo_cafe_la_ceiba.sql (solo perfil demo)
├── web/                     Next.js
├── docker-compose.yml       PostgreSQL local
├── .github/workflows/ci.yml
└── .env.example
```

## Cómo correrlo localmente

### Requisitos

- Java 21 (no hace falta instalar Maven: el proyecto trae el wrapper `mvnw`)
- Node.js 26 LTS
- Docker Desktop, abierto
- Git

### 1. Variables de entorno

Copia `.env.example` como `.env` en la raíz y cambia las contraseñas. El archivo `.env` no se sube al repositorio.

```powershell
Copy-Item .env.example .env
```

Docker Compose y el backend leen el mismo `.env`. Por defecto, `SPRING_PROFILES_ACTIVE=demo` carga los datos de Café La Ceiba.

### 2. Base de datos

```powershell
docker compose up -d
```

Levanta PostgreSQL 18 en el puerto 5432, con un volumen persistente.

### 3. Backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

En macOS o Linux: `./mvnw spring-boot:run`. Al arrancar, Flyway aplica las migraciones y, con el perfil `demo`, los datos de ejemplo. La API queda en `http://localhost:8080`.

Si no quieres usar Docker Compose, `TestParcheApplication` (en `src/test`) arranca el backend con un PostgreSQL de Testcontainers:

```powershell
.\mvnw.cmd spring-boot:test-run "-Dspring-boot.run.profiles=demo"
```

### 4. Web

```powershell
cd web
npm install
npm run dev
```

La web queda en `http://localhost:3000`.

## API

Todas las rutas llevan el prefijo `/api` y devuelven JSON. Los montos van en pesos colombianos, como enteros.

### `GET /api/negocios/{id}/menu`

Devuelve las categorías activas del negocio, en orden, con sus productos disponibles. Las categorías que no tienen productos disponibles no aparecen.

```powershell
curl.exe http://localhost:8080/api/negocios/1/menu
```

```json
{
  "negocio": { "id": 1, "nombre": "Café La Ceiba", "barrio": "Laureles", "ciudad": "Medellín", "demo": true },
  "categorias": [
    {
      "id": 1,
      "nombre": "Desayunos",
      "productos": [
        { "id": 1, "nombre": "Calentado paisa", "descripcion": "Frijoles y arroz calentados, huevo frito, chorizo, arepa y quesito", "precio": 18000 }
      ]
    }
  ]
}
```

En una base nueva, Café La Ceiba tiene el id 1.

### `GET /api/mesas/{token}`

Resuelve el token del QR o NFC de una mesa y devuelve el negocio y el número de la mesa. En la fase 1 lo va a usar el QR.

```powershell
curl.exe http://localhost:8080/api/mesas/hGo0Aj_ObuejR_d4UPJumA
```

```json
{ "negocio": { "id": 1, "nombre": "Café La Ceiba", "barrio": "Laureles", "ciudad": "Medellín", "demo": true }, "numero": 1 }
```

Los tokens de las 12 mesas de la demo están en [`R__demo_cafe_la_ceiba.sql`](backend/src/main/resources/db/demo/R__demo_cafe_la_ceiba.sql).

### Errores

Todos los errores usan el mismo cuerpo ([RFC 9457](https://www.rfc-editor.org/rfc/rfc9457), `application/problem+json`), con textos en español:

```json
{
  "title": "Solicitud inválida",
  "status": 400,
  "detail": "Algunos datos de la solicitud no son válidos.",
  "instance": "/api/negocios/abc/menu",
  "codigo": "VALIDACION",
  "marcaTiempo": "2026-11-26T22:26:56.838Z",
  "errores": [{ "campo": "id", "mensaje": "Debe ser un número entero." }]
}
```

`codigo` es estable y sirve para que el cliente decida qué hacer: `RECURSO_NO_ENCONTRADO`, `VALIDACION`, `METODO_NO_PERMITIDO`, `ERROR_INTERNO`, etc. `errores` solo aparece en los errores de validación.

## Base de datos y datos de ejemplo

- El esquema se define **solo** con migraciones versionadas en `db/migration` (`V{n}__descripcion.sql`). Las migraciones ya aplicadas no se editan.
- Los datos simulados de Café La Ceiba viven aparte, en `db/demo/R__demo_cafe_la_ceiba.sql`. Es una migración repetible de Flyway que se carga solo con el perfil `demo`. Es idempotente: si cambias el archivo, Flyway la vuelve a ejecutar y solo agrega lo que falte, sin duplicar ni pisar datos.
- Sin el perfil `demo`, la base queda con el esquema vacío.

## Pruebas

```powershell
cd backend
.\mvnw.cmd verify
```

Son pruebas de integración contra un PostgreSQL 18 real en Testcontainers, así que Docker tiene que estar corriendo. Cubren los dos endpoints (éxito, 404, validaciones, negocios y mesas inactivos, aislamiento entre negocios), el cuerpo de error uniforme y el resultado de las migraciones.

Para la web:

```powershell
cd web
npm run lint
npm run build
```

## CI

[`.github/workflows/ci.yml`](.github/workflows/ci.yml) corre en cada push y pull request, con dos jobs: el backend (`./mvnw verify`, con Testcontainers) y la web (`npm ci`, lint y build).

## Licencia

Código bajo la [licencia MIT](LICENSE). © 2026 Santiago Sánchez Ramírez.
