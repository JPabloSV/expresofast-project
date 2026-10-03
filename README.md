# ExpresoFast - Consola Logística (Laboratorio 11)

Universidad de Costa Rica, Sede del Atlántico, Recinto de Paraíso.
Carrera de Informática Empresarial. Curso IF0009 - Desarrollo de Software IV. Semestre II-2026.

Estudiante: Juan Pablo Solano Vásquez
Carnet: C5K023

**Laboratorio 11: Formularios Reactivos Avanzados y Consolidación Full-Stack.** Continúa el Laboratorio 10 (SPA en Angular Standalone) y lo reconecta a la base de datos SQL Server de los laboratorios 5 a 9 (`ExpresoFastC5K023_II2026`). Un envío (un número de tracking) ahora puede contener varios paquetes, y se registra junto con todos sus paquetes en una sola operación transaccional.

## Estructura del repositorio

    expresofast-project/
    ├── database/
    │   ├── expresofast_esquema_base.sql   Esquema de la BD de los labs 5 a 9 (referencia)
    │   └── lab11_paquetes.sql             Script del Lab 11: tabla PAQUETES y fechas del envío
    ├── expresofast-backend/               Spring Boot 3.5, Java 21, SQL Server
    └── expresofast-frontend/              Angular 22 Standalone, Reactive Forms

## Requisitos

- JDK 21 o superior. No hace falta instalar Maven: se usa el Maven Wrapper.
- Microsoft SQL Server con la base `ExpresoFastC5K023_II2026` de los laboratorios 5 a 9, con TCP/IP habilitado en el puerto 1433.
- Node.js 20 o superior y Angular CLI (`npm install -g @angular/cli`).

## 1. Base de datos (SQL Server)

1. La base `ExpresoFastC5K023_II2026` debe existir con las tablas y los datos de los laboratorios 5 a 9 (scripts `01` a `06` del repositorio `expresofast-lab6-c5k023`). El archivo `database/expresofast_esquema_base.sql` documenta ese esquema.
2. En SSMS, ejecute **`database/lab11_paquetes.sql`**. El script:
   - crea la tabla `PAQUETES` con la relación 1:N hacia `dbo.Envio` (`ON DELETE CASCADE`);
   - agrega a `dbo.Envio` las columnas `fecha_despacho` y `fecha_entrega_estimada`;
   - agrega la restricción `CK_Envio_Fechas` (entrega estimada > despacho).

   Es idempotente: se puede ejecutar más de una vez sin errores.

**Adaptación del script oficial.** El enunciado referencia `ENVIOS(id)` con `envio_id BIGINT`. En esta base la tabla se llama `dbo.Envio` y su llave primaria es `envio_id INT`. SQL Server exige que una llave foránea tenga el mismo tipo que la columna que referencia, así que en `PAQUETES` la columna `envio_id` es `INT` y referencia a `dbo.Envio(envio_id)`. El resto de la tabla (`id BIGINT IDENTITY`, `descripcion VARCHAR(255)`, `peso_kg DECIMAL(5,2)`) es igual al script del enunciado.

## 2. Backend (puerto 8080)

La contraseña del usuario `sa` no se guarda en el repositorio. Se lee de la variable de entorno `DB_PASSWORD`.

PowerShell (Windows):

    cd expresofast-backend
    $env:DB_PASSWORD = "su_contraseña"
    .\mvnw.cmd spring-boot:run

Linux o macOS: `DB_PASSWORD=su_contraseña ./mvnw spring-boot:run`.

Variables opcionales: `DB_USER` (por defecto `sa`) y `DB_URL`. Si su instancia es `SQLEXPRESS` y no escucha en el puerto 1433, use por ejemplo:

    $env:DB_URL = "jdbc:sqlserver://localhost\SQLEXPRESS;databaseName=ExpresoFastC5K023_II2026;encrypt=true;trustServerCertificate=true"

`spring.jpa.hibernate.ddl-auto=validate`: al arrancar, Hibernate comprueba que las entidades coincidan con las tablas creadas por los scripts y **nunca** modifica el esquema. Si falta ejecutar `lab11_paquetes.sql`, la aplicación no arranca e indica la tabla o columna faltante.

Pruebas automatizadas (no necesitan SQL Server; usan H2 en modo de compatibilidad `MSSQLServer` con una réplica del esquema):

    .\mvnw.cmd test

## 3. Frontend (puerto 4200)

    cd expresofast-frontend
    npm install
    ng serve

Abra `http://localhost:4200/nuevo-envio`.

## API REST

| Método | Ruta | Descripción | Respuestas |
|--------|------|-------------|------------|
| GET | `/api/envios` | Lista de envíos con sus paquetes. Filtro opcional `?estado=PENDIENTE`. | 200 |
| GET | `/api/envios/rastreo/{codigo}` | Detalle de un envío por número de tracking. | 200, 404 |
| GET | `/api/envios/check-tracking/{trackingNumber}` | `{"numeroTracking":"EXP-1001","existe":true}`. Lo consume el validador asíncrono. | 200 |
| POST | `/api/envios` | Registra un envío con su lista de paquetes (`EnvioRegistroDTO`). | 201, 400, 409 |
| PATCH | `/api/envios/{id}/estado` | Cambia el estado. Cuerpo: `{ "estado": "EN_TRANSITO" }`. | 200, 400, 404 |
| GET | `/api/vehiculos` | Catálogo de vehículos (selector del formulario). | 200 |
| GET | `/api/conductores` | Conductores activos (selector del formulario). | 200 |

Ejemplo de cuerpo para `POST /api/envios`:

```json
{
  "numeroTracking": "EXP-2001",
  "destinatario": "Ana Rodríguez",
  "direccionDestino": "Cartago, Paraíso centro",
  "montoFlete": 9500.00,
  "fechaDespacho": "2026-10-10",
  "fechaEntregaEstimada": "2026-10-12",
  "vehiculoId": 1,
  "conductorId": 1,
  "paquetes": [
    { "descripcion": "Caja de libros", "pesoKg": 12.50 },
    { "descripcion": "Monitor", "pesoKg": 7.25 }
  ]
}
```

Swagger UI: `http://localhost:8080/swagger-ui.html`.

## Implementación

### Back-End

- **Relación 1:N bidireccional.** `Envio` tiene `@OneToMany(mappedBy = "envio", cascade = CascadeType.ALL, orphanRemoval = true) List<Paquete> paquetes`. `Paquete` tiene `@ManyToOne @JoinColumn(name = "envio_id")`. `Paquete` es el lado dueño porque su tabla tiene la llave foránea. El método `Envio.agregarPaquete()` sincroniza ambos lados.
- **DTOs.** `EnvioRegistroDTO` recibe `List<@Valid PaqueteDTO> paquetes` (`@NotEmpty`). Cada `PaqueteDTO` valida descripción y peso (0.01 a 999.99, igual que `DECIMAL(5,2)`).
- **Transaccionalidad.** `EnvioServiceImpl.registrar()` es `@Transactional`: valida las reglas de negocio, arma el agregado `Envio` → `Paquete` y hace un único `saveAndFlush`. Por la cascada, se inserta el envío y luego cada paquete. Si un INSERT falla, Spring hace rollback y no queda nada guardado. La prueba `siUnPaqueteFallaNoSeGuardaNiElEnvioNiLosDemasPaquetes` lo demuestra.
- **Reglas en el servidor.** Un tracking duplicado devuelve 409. Una entrega que no sea posterior al despacho, un conductor inactivo o un peso total mayor que la capacidad del vehículo devuelven 400. El backend no confía en las validaciones del cliente.
- `peso_kg` de `Envio` (columna `NOT NULL` del esquema original) se calcula como la suma de los pesos de los paquetes.

### Front-End (`EnvioAvanzadoFormComponent`, ruta `/nuevo-envio`)

- **Typed Forms.** `FormGroup<EnvioAvanzadoForm>` construido con `NonNullableFormBuilder`. `pesoKg` es `FormControl<number | null>`: `setValue('diez')` produce el error de compilación TS2345.
- **FormArray.** `paquetes: FormArray<FormGroup<PaqueteForm>>`, recorrido con `@for` y `[formGroupName]="i"`. "+ Añadir Paquete" hace `push(crearPaquete())`. El botón "X" hace `removeAt(i)` y se deshabilita cuando queda un solo paquete.
- **Validación cruzada síncrona.** `fechaEntregaPosteriorValidator` (`validators/fechas.validator.ts`) se aplica al FormGroup completo y produce `{ fechasInvalidas: true }`. Se muestra un mensaje global y el botón de envío queda deshabilitado (`form.invalid`).
- **Validación asíncrona.** `trackingDisponibleValidator` (`validators/tracking-disponible.validator.ts`) es un `AsyncValidatorFn` que espera 400 ms (`timer`), consulta `check-tracking` y devuelve `{ trackingTomado: true }` cuando el número existe. Se muestra "Este número de rastreo ya está en uso". Mientras la consulta está en curso, el control queda `PENDING` y el botón de envío sigue deshabilitado.
- No se usa `[(ngModel)]` en ningún componente: la búsqueda de rastreo también usa Reactive Forms.

## Fundamentación teórica

### 1. UX y escalabilidad: `FormArray` frente a 10 campos estáticos ocultos

Con 10 pares de campos escritos a mano en el HTML y ocultos con CSS o `*ngIf`, el formulario tiene una **cardinalidad fija codificada en la vista**. Un envío de 11 paquetes es imposible sin modificar el código. Los campos ocultos además siguen existiendo:

- Si tienen `required`, invalidan el formulario aunque el usuario no los vea. Si no lo tienen, hay que activar y desactivar validadores a mano según cuántos estén visibles.
- El componente necesita variables auxiliares (`paquete7Visible`, contadores, etc.).
- La carga útil llega con entradas vacías que hay que filtrar antes de enviarla al backend.
- Agregar un campo nuevo al paquete (por ejemplo, dimensiones) obliga a repetir el cambio 10 veces en la plantilla y en el código. Esa duplicación es la fuente típica de errores de copiar y pegar.

En los formularios reactivos la **fuente de la verdad es el modelo en TypeScript**, y la plantilla es solo una proyección de ese modelo:

- **Cardinalidad dinámica y real.** El `FormArray` contiene exactamente los `FormGroup` que el usuario creó. `push()` y `removeAt()` modifican el modelo y `@for` vuelve a dibujar la vista. No existen controles fantasma, y `removeAt` reindexa los elementos sin lógica adicional.
- **Una sola definición por paquete.** La fábrica `crearPaquete()` define los controles y validadores de un paquete en un único lugar. Un campo nuevo se agrega una sola vez, en la interfaz `PaqueteForm` y en la fábrica.
- **Agregación automática del estado.** La validez, `pending`, `dirty` y `touched` se propagan de cada paquete al `FormArray` y del `FormArray` al `FormGroup` raíz. Basta un `[disabled]="form.invalid || form.pending"` para bloquear el envío si *cualquier* paquete es inválido, sin recorrer campos a mano.
- **Contrato tipado con el backend.** `getRawValue()` devuelve `{ descripcion: string; pesoKg: number | null }[]`, que se mapea directamente a `PaqueteDTO[]`. Gracias a los Typed Forms, un error de tipo (un string en `pesoKg`, un campo mal escrito) se detecta **al compilar** y no en producción.
- **Mejor UX.** El operador ve solo los paquetes que necesita y recibe retroalimentación inmediata por paquete, con mensajes por campo y el peso total en vivo. Además, el DOM no carga nodos inútiles.
- **Mantenibilidad y pruebas.** La lógica vive en la clase del componente y se puede probar unitariamente sin DOM (por ejemplo, "agregar tres paquetes, eliminar uno y verificar el payload"). Con campos estáticos, la lógica queda repartida en la plantilla.

### 2. Ciclo de eventos: validador síncrono frente a validador asíncrono

JavaScript ejecuta el código de la página en **un solo hilo** con una pila de llamadas (*call stack*). Las operaciones lentas (temporizadores, red) las atiende el navegador fuera de ese hilo (*Web APIs*). Cuando terminan, sus callbacks se encolan: los temporizadores y eventos van a la cola de tareas (*macrotasks*) y la resolución de promesas va a la cola de *microtasks*. El **Event Loop** toma un callback de esas colas solo cuando la pila queda vacía, y entre tareas el navegador puede volver a pintar la pantalla.

**Validador cruzado de fechas (síncrono).** Cuando el usuario cambia una fecha, el evento `input` ejecuta, *en la misma pila de llamadas*, la cadena `setValue → updateValueAndValidity` del control y luego la del FormGroup padre, que invoca `fechaEntregaPosteriorValidator`. La función compara dos valores y **retorna de inmediato** `{ fechasInvalidas: true }` o `null`. Cuando el manejador del evento termina, el estado del formulario ya es definitivo (`VALID` o `INVALID`) y el siguiente renderizado muestra el error. Por eso un validador síncrono debe ser rápido y puro, sin E/S: si tardara, bloquearía el único hilo y la interfaz se congelaría.

**Validador de tracking (asíncrono).** Angular lo ejecuta solo si los validadores síncronos del control pasaron (obligatorio y formato `EXP-1234`). En ese momento pone el control en estado `PENDING`, invoca el validador y se suscribe al `Observable` que este devuelve. La función retorna **sin haber consultado nada todavía**, y la pila queda libre para que el usuario siga escribiendo:

1. `timer(400)` registra un temporizador en las Web APIs. Su callback llega 400 ms después como *macrotask*.
2. `switchMap` lanza la petición HTTP con `HttpClient` (`withFetch`). La red la resuelve el navegador, fuera del hilo de JavaScript.
3. Cuando llega la respuesta, la promesa de `fetch` se resuelve (*microtask*). `map` la convierte en `{ trackingTomado: true }` o `null`, el Observable emite y se completa, y Angular aplica el resultado al control, que pasa de `PENDING` a `VALID` o `INVALID`. En ese momento se actualizan el mensaje y el botón.

Si el usuario modifica el valor antes de que todo eso termine, Angular **cancela la suscripción anterior** (`unsubscribe`), lo que descarta el temporizador o aborta la petición. Así una respuesta vieja nunca sobrescribe el resultado del valor actual. Eso evita *race conditions* y además hace que el `timer` funcione como *debounce*: no se envía una petición por cada tecla.

**¿Por qué Angular exige un `Observable` o una `Promise`?** Porque cuando la función del validador retorna, el resultado todavía **no existe**: depende de una respuesta de red que llegará en un turno futuro del Event Loop. Para devolver un `ValidationErrors` directamente, la función tendría que bloquear el hilo hasta recibir la respuesta (como el XHR síncrono, obsoleto), congelando la interfaz. `Promise` y `Observable` son el contrato estándar para representar un **valor futuro**: Angular se suscribe, deja el control en `PENDING` y aplica el resultado cuando llega. Angular combina los validadores asíncronos con `forkJoin`, por lo que el Observable debe **completarse**, y `HttpClient` lo hace tras su única respuesta. Se prefiere `Observable` sobre `Promise` porque es **cancelable**, que es justamente lo que permite descartar las consultas obsoletas.
