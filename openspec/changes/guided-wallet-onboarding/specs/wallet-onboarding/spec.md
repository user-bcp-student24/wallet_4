# Spec Delta

## Purpose

Esta capacidad permite completar el registro de una wallet mediante un flujo guiado, comprobar su estado de activación y conocer cuándo se habilita el acceso al flujo de pagos.

## ADDED Requirements

### Requirement: El onboarding muestra el progreso y el siguiente paso

El flujo MUST mostrar la secuencia de pasos requeridos, identificar inequívocamente el paso actual y comunicar la acción necesaria para continuar.

#### Scenario: La persona inicia el onboarding
- **WHEN** la persona comienza el flujo de registro
- **THEN** ve la secuencia requerida, el paso actual y la acción para avanzar

#### Scenario: La persona completa un paso
- **WHEN** la persona completa los datos válidos del paso actual y avanza
- **THEN** el flujo identifica el nuevo paso actual y comunica la siguiente acción

### Requirement: Los datos incompletos o inválidos bloquean el avance

El flujo MUST impedir el avance si falta información obligatoria o si algún dato no cumple las validaciones aplicables, y MUST indicar cómo corregirlo.

#### Scenario: Falta información obligatoria
- **WHEN** la persona intenta avanzar sin completar un dato obligatorio
- **THEN** permanece en el paso actual, se identifica el dato faltante y se explica cómo completarlo

#### Scenario: Un dato no es válido
- **WHEN** la persona intenta avanzar con un dato inválido
- **THEN** permanece en el paso actual, se identifica el dato que debe corregir y se muestra una indicación accionable

### Requirement: El onboarding aprobado activa la wallet y habilita el acceso a pagos

El mock MUST crear una verificación y una wallet asociada al completar correctamente el onboarding. El acceso al flujo de pagos MUST presentarse como disponible solo cuando el estado de la wallet sea `ACTIVE`.

#### Scenario: El mock aprueba la verificación
- **WHEN** la persona completa el onboarding con datos válidos y el escenario de verificación es `APPROVED`
- **THEN** el mock devuelve una verificación y el identificador de su wallet asociada, la wallet queda en estado `ACTIVE` y se ofrece el punto de entrada al flujo de pagos

#### Scenario: La verificación aún no habilita pagos
- **WHEN** el estado consultado de la wallet todavía no es `ACTIVE`
- **THEN** el flujo no presenta el acceso a pagos como disponible y muestra el estado de la verificación o de la wallet para orientar el siguiente paso

### Requirement: Los estados de verificación y wallet se pueden consultar

La persona MUST poder consultar el estado de la verificación y de la cuenta creada mediante el mock, usando el identificador devuelto durante el onboarding.

#### Scenario: Se consultan los estados de una wallet creada
- **WHEN** la persona consulta una verificación y su wallet mediante sus identificadores
- **THEN** obtiene los estados y datos definidos por `GET /kyc/verifications/{id}` y `GET /wallets/{id}` en `docs/ENDPOINTS.md`

### Requirement: Se preserva el contrato documentado de KYC y wallet

El onboarding MUST conservar las rutas, campos, valores permitidos y códigos HTTP documentados en `docs/ENDPOINTS.md`; este cambio no altera ese contrato.

#### Scenario: Se crea una verificación KYC
- **WHEN** el mock recibe `POST /kyc/verifications` con `documentType` y `document` válidos
- **THEN** conserva los campos de respuesta `id`, `wallet_id`, `scenario` y `status`, la creación de una wallet asociada y los códigos documentados `201`, `400`, `503`, `413` y `429`

#### Scenario: Se consultan una verificación o una wallet
- **WHEN** se consulta `GET /kyc/verifications/{id}` o `GET /wallets/{id}`
- **THEN** se conservan las respuestas y campos documentados, incluidos `200` para un recurso existente y `404` para uno inexistente

#### Scenario: Se resuelve una verificación pendiente
- **WHEN** se usa `POST /kyc/verifications/{id}/decision` para resolver una verificación
- **THEN** se conservan los valores permitidos `ACTIVE` y `REJECTED` y los códigos documentados `200`, `400`, `404`, `409`, `413` y `429`

### Requirement: Los eventos de navegación son efímeros y no se envían a la API

El flujo MUST registrar los eventos de navegación solo en memoria; no MUST enviarlos a la API ni conservarlos después de recargar.

#### Scenario: Se navega por el onboarding
- **WHEN** la persona inicia, completa pasos o cierra el flujo durante una sesión
- **THEN** los eventos de navegación existen solo en memoria y no se envía ninguna solicitud de analítica a la API

#### Scenario: Se recarga la interfaz
- **WHEN** se recarga la interfaz después de generar eventos de navegación
- **THEN** los eventos anteriores ya no están disponibles