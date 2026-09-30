# ExpresoFast - Consola Logística (Laboratorio 10)

Universidad de Costa Rica, Sede del Atlántico, Recinto de Paraíso.
Carrera de Informática Empresarial. Curso IF0009 - Desarrollo de Software IV. Semestre II-2026.

Estudiante: Juan Pablo Solano Vásquez
Carnet: C5K023

Migración de la consola logística ExpresoFast a una Single Page Application en Angular Standalone, conectada a un backend RESTful por capas en Spring Boot. Es un proyecto independiente de los laboratorios 5 a 9.

## Estructura del repositorio

    expresofast-project/
    ├── expresofast-backend/     Spring Boot 3.5.16, Java 21, H2 en archivo
    └── expresofast-frontend/    Angular 22 Standalone

## Requisitos

- JDK 21 o superior (probado con JDK 25; el proyecto compila con `release 21`).
- Node.js 18 o superior y Angular CLI (`npm install -g @angular/cli`).
- No se necesita instalar Maven (se usa el Maven Wrapper) ni una base de datos (H2 es embebida).

## Ejecución

Se necesitan dos terminales abiertas al mismo tiempo. Arranque primero el backend.

### 1. Backend (puerto 8080)

    cd expresofast-backend
    .\mvnw.cmd spring-boot:run

En Linux o macOS: `./mvnw spring-boot:run`. La primera vez Maven descarga las dependencias.

Al arrancar por primera vez se cargan 10 envíos de ejemplo. Los datos se guardan en `expresofast-backend/data/` y persisten entre reinicios. Para reiniciar la base, detenga el backend y borre esa carpeta.

### 2. Frontend (puerto 4200)

    cd expresofast-frontend
    npm install
    ng serve

Abra `http://localhost:4200`. La aplicación redirige a `/envios`.

## Vistas de la aplicación

| Ruta | Componente | Descripción |
|------|------------|-------------|
| `/envios` | `EnvioListComponent` | Tabla de envíos con insignia de color por estado y selector para cambiar el estado. |
| `/nuevo-envio` | `EnvioFormComponent` | Formulario con validación para registrar un envío. |
| `/rastreo` | `EnvioTrackingComponent` | Búsqueda por código de rastreo y ficha con barra de progreso. |

## API REST

Ruta base: `/api/v1/envios`

| Método | Ruta | Descripción | Respuestas |
|--------|------|-------------|------------|
| GET | `/api/v1/envios` | Lista todos los envíos. Filtro opcional `?estado=PENDIENTE`. | 200 |
| GET | `/api/v1/envios/rastreo/{codigo}` | Detalle de un envío por código de rastreo. | 200, 404 |
| POST | `/api/v1/envios` | Registra un envío (destinatario, direccionDestino, montoFlete). Genera el código `EXP-AAAA-NNNN`. | 201, 400 |
| PATCH | `/api/v1/envios/{id}/estado` | Actualiza el estado. Cuerpo: `{ "estado": "EN_TRANSITO" }`. | 200, 400, 404 |

Estados válidos: `PENDIENTE`, `EN_TRANSITO`, `ENTREGADO`, `CANCELADO`.

El controlador autoriza el origen `http://localhost:4200` con `@CrossOrigin`.

## Herramientas de desarrollo

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Consola H2: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:file:./data/expresofast`
  - Usuario: `sa`, contraseña vacía.

## Arquitectura del backend

Paquete base `cr.ac.ucr.c5k023.lab10`:

- `model`: entidad JPA `Envio`.
- `repository`: `EnvioRepository` (consultas derivadas y JPQL).
- `dto`: `EnvioDTO`, `CrearEnvioDTO`, `ActualizarEstadoDTO`, `ErrorDTO`.
- `service`: `EnvioService` y `EnvioServiceImpl` (lógica de negocio).
- `controller`: `EnvioController` y `GlobalExceptionHandler`.
- `config`: `DatosIniciales` (datos de ejemplo).
- `exception`: `EnvioNoEncontradoException`.

## Arquitectura del frontend

Angular Standalone, con la API configurada en `src/environments/environment.ts` y `provideHttpClient(withFetch())` en `app.config.ts`.

- `models/envio.model.ts`: interfaces `Envio` y `CrearEnvioPayload`.
- `services/envio.service.ts`: métodos HTTP `obtenerEnvios`, `obtenerPorRastreo`, `crearEnvio` y `actualizarEstado`.
- `components/`: los tres componentes enrutables.
- `app.routes.ts`: tabla de rutas con redirección por defecto a `/envios`.