# Parche

Plataforma web para restaurantes y cafeterías de Medellín: pedido a la mesa por QR o NFC, cuenta dividida por persona, pantalla de cocina en tiempo real, panel del dueño, registro de clientes, reactivación por WhatsApp y reportes con preguntas en lenguaje natural.

Es un proyecto de negocio en validación y, al mismo tiempo, una pieza de portafolio. Todo debe verse y funcionar de forma profesional aunque nunca tenga clientes reales.

## Estado

Se construye por fases. No avances a la siguiente fase sin que el usuario lo pida.

| Fase | Qué incluye | Versión |
| --- | --- | --- |
| 0. Base | Monorepo, backend y web compilando, PostgreSQL con Flyway, menú de Café La Ceiba, CI | v0.1 |
| 1. Pedido a la mesa (MVP) | Menú por QR, carrito por persona, cocina en tiempo real, llamar al mesero, cuenta dividida con QR de Bre-B, login del personal | v0.2 |
| 2. Panel del dueño | CRUD de menú, mesas, QR imprimibles en PDF, usuarios y roles | v0.3 |
| 3. Clientes y reactivación | Registro por código de WhatsApp, consentimiento, regla de 21 días, mensajes | v0.4 |
| 4. Reportes e IA | Historial simulado, panel de ventas, reporte semanal, preguntas a los datos | v0.5 |
| 5. Demo pública | Despliegue, modo demo, video, README final | v1.0 |

Fase actual: **0. Base**

## Stack

- **Backend:** Java 21, Spring Boot 4 (Web, Data JPA, Validation, Security, WebSocket), Maven.
- **Base de datos:** PostgreSQL. El esquema se maneja SOLO con migraciones de Flyway (`spring.jpa.hibernate.ddl-auto=validate`).
- **Frontend:** Next.js con TypeScript, App Router, Tailwind CSS. Es una PWA.
- **Tiempo real:** WebSocket con STOMP.
- **Autenticación:** personal con correo y contraseña (JWT); clientes con código por WhatsApp (fase 3).
- **IA (fase 4):** Spring AI + Gemini.
- **Pruebas:** JUnit 5 y Testcontainers (PostgreSQL real) en el backend.
- **CI:** GitHub Actions.
- **Local:** PostgreSQL con Docker Compose.

## Estructura

```
parche/
├── backend/            Spring Boot (paquete base com.sant1sz.parche)
│   └── src/main/java/com/sant1sz/parche/
│       ├── config/
│       ├── controller/
│       ├── dto/        peticiones (*Request) y respuestas (*Response), como records
│       ├── exception/
│       ├── model/
│       ├── repository/
│       └── service/
├── web/                Next.js
├── docker-compose.yml  PostgreSQL local
├── .github/workflows/
├── .env.example
├── CLAUDE.md
└── README.md
```

## Comandos

- Base de datos local: `docker compose up -d`
- Backend: `cd backend` y luego `./mvnw spring-boot:run` (en Windows, `.\mvnw.cmd`)
- Pruebas del backend: `./mvnw verify`
- Web: `cd web`, `npm install`, `npm run dev`
- Lint de la web: `npm run lint`

El usuario trabaja en Windows con VS Code. Da los comandos para PowerShell cuando sean distintos.

## Convenciones

- **Idioma:** el dominio va en español (`Negocio`, `Mesa`, `Pedido`, `SesionMesa`, `Comensal`). Lo técnico de Spring y Next.js queda en inglés (`Controller`, `Repository`, `page.tsx`). Comentarios, mensajes de error al usuario y textos de la interfaz en español de Colombia.
- **API:** prefijo `/api`. Los controladores devuelven DTOs, nunca entidades JPA. Validación con `@Valid` y manejo de errores centralizado con `@RestControllerAdvice` y un cuerpo de error uniforme.
- **Multi-negocio:** casi todas las tablas tienen `id_negocio`. Ninguna consulta debe mezclar datos de dos negocios.
- **Base de datos:** nunca cambiar el esquema a mano ni con `ddl-auto=update`. Cada cambio es una migración nueva `V{n}__descripcion.sql`. No editar migraciones ya aplicadas.
- **Dinero:** montos en pesos colombianos como enteros (`long` o `BIGINT`), sin decimales.
- **Credenciales:** solo en variables de entorno. Mantener `.env.example` al día. Nunca escribir contraseñas, tokens ni claves en el código ni en los commits.
- **Git:** una rama por función (`feat/...`, `fix/...`), pull request a `main`. Mensajes de commit en español, en imperativo. Cada fase cierra con una etiqueta (`v0.1`, `v0.2`, ...).
- **Commits sin atribución de IA:** no agregar `Co-Authored-By` ni ninguna mención a Claude o a herramientas de IA en commits, pull requests ni archivos.
- **No hacer `git push`** ni crear pull requests sin que el usuario lo pida.

## Frontend y diseño

- Mobile first: el menú del cliente se usa en el celular, con una mano, en un restaurante.
- La pantalla de cocina se usa en una tablet horizontal y se lee a distancia: letra grande y estados con color.
- Usar el skill **frontend-design** para cualquier pantalla nueva o rediseño. Nada de plantillas genéricas.
- Accesibilidad básica: contraste suficiente, botones de al menos 44 px, textos alternativos.
- La identidad visual (colores, tipografía, logo) se define al inicio de la fase 1. Mientras tanto, interfaz neutra y limpia.

## Datos de ejemplo

- El negocio de demostración es **Café La Ceiba**, un café-restaurante ficticio de Laureles con 12 mesas y unos 30 productos en 5 categorías.
- Todo dato de ejemplo es simulado y debe identificarse como tal (por ejemplo, un aviso de "Demo" en la interfaz). Nunca presentar datos inventados como datos reales de un negocio.
- Los datos de ejemplo se cargan con una migración o un perfil `demo` separado, nunca mezclados con la lógica del negocio.

## Modelo de datos (referencia)

Negocio, Usuario (dueño, mesero, cocina), Mesa (token único para QR/NFC), Categoria, Producto, SesionMesa (abierta, cuenta pedida, cerrada), Comensal, Pedido, ItemPedido (apunta a un producto y a un comensal), Llamado, Pago, Cliente (teléfono, consentimiento con fecha), Campana y Mensaje.

En la fase 0 solo se crean: Negocio, Usuario, Mesa, Categoria y Producto.

## Calidad

- Ninguna fase se da por terminada sin pruebas pasando, CI en verde y README actualizado.
- Prueba de integración para cada endpoint nuevo.
- Si algo del plan no es posible o hay una mejor opción, decirlo antes de implementarlo.
- Preguntar antes de agregar dependencias grandes o cambiar algo del stack.
