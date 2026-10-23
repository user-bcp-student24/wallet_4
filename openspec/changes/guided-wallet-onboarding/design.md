# Design

## Context

Ver [proposal.md](proposal.md) para la motivación y [specs/wallet-onboarding/spec.md](specs/wallet-onboarding/spec.md) para los requisitos observables.

El frontend actual es una página estática en `index.html`: el formulario de registro valida campos con restricciones HTML, pero su submit solo muestra un aviso de demostración; no hace llamadas de red ni guarda estado. El README W1/W2 describe el uso directo desde el navegador del mock del grupo 4, mientras que `client/postman/wallet-grupo-4.postman_environment.json` define su `baseUrl`. La integración todavía no existe en el HTML: no hay `fetch` ni una constante `API_BASE`. Se establecerá una única configuración de URL que use el mock del grupo asignado y permita seleccionar el servicio local documentado en `docs/ENDPOINTS.md`, sin introducir un backend propio.

El servicio mock es externo, tiene persistencia SQLite compartida por el grupo y no ofrece idempotencia para altas KYC. Las nuevas wallets aprobadas tienen saldo cero; los escenarios `APPROVED`, `MANUAL_REVIEW`, `REJECTED` y `UNAVAILABLE` tienen resultados distintos. El contrato de rutas y payloads permanece como lo define `docs/ENDPOINTS.md`. La página también contiene un acceso de login de demostración, que no forma parte del onboarding de este cambio.

No existe un harness de pruebas frontend ni código backend KYC en este checkout. `docs/TESTING.md` describe pruebas Maven de autenticación, pero no hay `pom.xml` ni fuentes Java aquí; esa suite no caracteriza el onboarding. La colección Postman sí contiene pruebas del contrato del mock y comandos Newman documentados.

## Goals / Non-Goals

**Goals:**

- Añadir el flujo y los estados de onboarding dentro de la página existente, conservando su estructura general y el acceso de login fuera del flujo.
- Integrarse con el mock KYC existente y tratar sus respuestas como la fuente de verdad de los estados KYC y wallet.
- Mantener el progreso y los eventos de navegación solo en memoria.
- Poder verificar los escenarios de la spec en navegador y contra el contrato API sin cambiar el servidor.

**Non-Goals:**

- Añadir un backend, migrar a un framework de frontend o rediseñar las demás vistas.
- Cambiar rutas, payloads, validaciones, estados o códigos HTTP del mock.
- Enviar al mock nombre, correo, teléfono, contraseña o eventos de navegación; el payload se limita a los campos TEST aceptados por el contrato.
- Implementar pagos, saldo inicial, analítica persistente, autenticación real o idempotencia que el servicio no ofrece.

## Protected Contracts

- `POST /kyc/verifications` conserva `documentType`, `document` y el escenario documentado; responde con `id`, `wallet_id`, `scenario` y `status`, crea una wallet asociada y mantiene los códigos HTTP de `docs/ENDPOINTS.md`.
- `GET /kyc/verifications/{id}` y `GET /wallets/{id}` conservan campos y respuestas `200`/`404`. Los identificadores KYC y wallet se mantienen distintos; no se debe usar uno en la ruta del otro.
- `POST /kyc/verifications/{id}/decision` conserva `ACTIVE` y `REJECTED`, sus códigos HTTP y el flujo de revisión documentado.
- El acceso a pagos solo se presenta disponible con wallet `ACTIVE`. No se llama ni se modifica ningún endpoint de pagos en este cambio.
- Los datos de navegación no salen del navegador ni sobreviven a una recarga. Los datos KYC sí pueden persistir en el mock externo; son estados distintos y no deben confundirse.
- Usar solo documentos `TEST-...` sintéticos. No reiniciar el almacenamiento compartido ni usar datos personales reales.

## Components Affected

- `index.html`: presentación de pasos, validación existente, transición entre estados, solicitudes KYC y mensajes de resultado. Es el único componente funcional de este checkout que necesita cambiar.
- Mock Wallet externo del grupo asignado: consumidor de sus endpoints existentes, sin cambios de servidor.
- `client/openapi.json` y `client/postman/`: contrato y colección existentes para configurar y caracterizar las solicitudes; se reutilizan como referencias de prueba, sin requerir un cambio de contrato.
- `docs/ENDPOINTS.md`: baseline contractual, no se modifica para acomodar la interfaz.

## Decisions

### Mantener el frontend estático y el alcance de página

La navegación se modelará como estados explícitos del flujo en la página existente; no se migrará la aplicación ni se cambiará el login de demostración. El avance y los eventos vivirán en memoria, sin `localStorage`, `sessionStorage`, cookies ni parámetros de URL. Esto mantiene el cambio pequeño y satisface el descarte al recargar.

Alternativas consideradas: reemplazar la página con una SPA/framework o persistir el progreso localmente. La primera amplía innecesariamente el alcance; la segunda contradice la spec y puede hacer reaparecer eventos o pasos luego de una recarga.

### Consumir el mock documentado desde el navegador

Se usará el servicio del grupo ya configurado en el entorno Postman y descrito en README W1/W2. La URL base será una única configuración, no una URL repetida en cada solicitud. Se enviarán solo los campos `TEST` del contrato; los demás campos del formulario no se incorporarán al payload KYC. No se creará un servicio backend intermedio.

Alternativas consideradas: implementar endpoints locales o introducir un adaptador backend. No hay código de servidor KYC en este checkout y ambos enfoques duplicarían un contrato ya ofrecido por el mock. Antes de integrar, se debe comprobar que la URL seleccionada corresponde al grupo y al entorno previstos; los ejemplos `localhost:8080` y el host remoto son entornos, no contratos distintos.

### Usar las respuestas remotas como fuente de estado

El identificador de KYC y `wallet_id` devueltos por la creación se conservarán por separado durante la sesión. La consulta de estado usará el endpoint correspondiente para cada identificador. El estado de verificación no se convertirá por sí solo en estado de wallet, y el punto de entrada de pagos se habilitará solo al confirmar `ACTIVE` en la wallet.

Alternativas consideradas: inferir activación localmente al enviar el formulario o usar únicamente el estado KYC. Ambas pueden mostrar pagos como disponibles sin que la wallet esté activa y no verifican el comportamiento acordado. Ante `PENDING`, `REJECTED` o indisponibilidad del servicio, se mostrará el estado conocido y no se habilitará el acceso a pagos.

### Mantener las reglas de entrada dentro del contrato disponible

Se conservarán las restricciones HTML actuales para los campos que continúen en el flujo y se validarán los campos `documentType`/`document` exigidos por KYC antes de la solicitud. No se inventarán requisitos adicionales de identidad o credenciales. Los errores de validación deben dejar el paso actual visible y explicar la corrección; los errores HTTP deben distinguirse de una aprobación.

Alternativas consideradas: replicar en el cliente reglas KYC no documentadas o tratar la validación nativa del formulario como suficiente. La primera inventaría reglas; la segunda no valida el nuevo payload KYC ni cubre resultados del servicio. La validación del servidor sigue siendo autoritativa para el contrato API.

### Separar caracterización de navegador y contrato API

Se reutilizará Newman/Postman para las aserciones HTTP existentes. Para el comportamiento de página se añadirá una prueba de navegador aislada, de desarrollo solamente, antes de cambiar el submit; Playwright es la alternativa preferida frente a una suite manual permanente porque puede verificar validación, requests, recarga y estado visible sin añadir un framework de runtime. No se debe invocar la colección completa como prueba automática de este cambio.

Alternativas consideradas: probar solo la API con Newman, que no verifica pasos ni accesibilidad del navegador; o depender solo de pruebas manuales, que no detectan regresiones de forma repetible. Las comprobaciones manuales siguen siendo útiles para la revisión visual, pero no sustituyen los casos de caracterización indicados abajo.

## Incremental Strategy

1. Registrar el comportamiento actual del formulario, tabs, restricciones HTML y submit de demostración antes de alterarlo. Confirmar que el estado inicial no envía solicitudes ni persiste datos.
2. Introducir el progreso y el bloqueo/recuperación por validación en la página, conservando sus controles y restricciones actuales. Ejecutar primero las pruebas de navegador de la etapa 1.
3. Conectar la creación KYC usando una URL de grupo explícita y el payload mínimo documentado. Caracterizar `APPROVED` y consultar ambos recursos por sus identificadores respectivos.
4. Añadir presentación de estados no activos y habilitar el punto de entrada de pagos solo tras consultar una wallet `ACTIVE`. Confirmar errores de red/HTTP sin reinterpretarlos como éxito.
5. Verificar los escenarios de eventos en memoria: no generan requests de analítica y desaparecen al recargar. Ejecutar las verificaciones no destructivas y revisar visualmente escritorio y móvil.

## Characterization Tests Before Behavior Changes

- **Formulario y navegación actuales:** en navegador, confirmar que los campos requeridos, correo, patrón de teléfono y mínimo de contraseña bloquean entradas inválidas; confirmar selección de tabs, submit de demostración y que el mensaje actual no crea cuenta ni envía requests. Guardar estas expectativas como pruebas antes de sustituir el submit.
- **Sin efectos secundarios de telemetría:** observar las solicitudes de red durante el inicio, avance, cierre y recarga. La prueba debe fallar si aparece una llamada de analítica o si un evento sobrevive a una recarga.
- **Contrato de lectura:** ejecutar la carpeta Postman `01 - Servicio y preparacion` con el entorno configurado, que realiza consultas. Confirmar disponibilidad/configuración y no usar operaciones de reset.
- **Contrato de escritura KYC:** antes de conectar el formulario, caracterizar con la colección existente o Newman los resultados documentados para aprobación, rechazo, revisión manual y proveedor no disponible; validar respuesta, campos e IDs, luego consultar KYC y wallet. Estos casos escriben datos en el SQLite compartido y no son una prueba local aislada: ejecutarlos solo coordinando con el equipo y con documentos `TEST-` únicos. No automatizarlos como gate por defecto ni intentar limpiar el grupo con reset.
- **Payload y privacidad:** en prueba de navegador, interceptar la solicitud de creación y verificar que contiene solo `documentType`, `document` y `scenario`; comprobar que nombre, correo, teléfono, contraseña y eventos no se transmiten.
- **Harness:** no hay actualmente `package.json`, configuración Playwright ni pruebas web. Añadir solo la configuración mínima de test de navegador, sin dependencias de runtime; no reutilizar los tests Maven descritos en `docs/TESTING.md`, que pertenecen a otro proyecto/componente.

## Risks / Trade-offs

- **El README describe una integración que no está en el HTML actual** → tratar la llamada al mock como trabajo nuevo y confirmar grupo/base URL desde el entorno Postman antes de conectar; no asumir que `API_BASE` ya existe.
- **El mock conserva altas y no deduplica** → usar documentos `TEST-` únicos y evitar reintentos automáticos de `POST`; ante timeout ambiguo, no afirmar éxito ni repetir en segundo plano. Los registros pueden quedar en el grupo compartido.
- **Mock compartido o indisponible (`503`)** → mantener el estado como pendiente/no confirmado y no habilitar pagos; separar tests de lectura de tests que crean datos y coordinar las escrituras.
- **Wallet activa con saldo cero** → el onboarding confirma el estado de cuenta, no capacidad de pagar ni saldo disponible; no prometer pagos exitosos.
- **Confusión entre IDs o estados** → conservar `id` KYC y `wallet_id` por separado y cubrir las rutas de consulta con sus IDs correctos.
- **La automatización de navegador añade dependencia de test** → limitar Playwright al entorno de desarrollo/pruebas; no añadir framework ni dependencias al runtime estático.

## Rollback

El frontend es estático y no requiere migración de datos local: revertir la versión de `index.html` vuelve a la pantalla de demostración. El rollback de interfaz no elimina las verificaciones creadas en el mock, porque su almacenamiento es externo y compartido. No usar reset como mecanismo de rollback; coordinar cualquier limpieza soportada por el equipo del mock y dejar constancia de los IDs sintéticos creados. Mantener la suite de navegador separada del runtime permite retirarla sin afectar la página.