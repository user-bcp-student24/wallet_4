# Tasks

## 1. Caracterización previa

- [ ] 1.1 Añadir el harness mínimo de Playwright solo para pruebas y caracterizar `index.html` antes de cambiar su submit: restricciones actuales de campos, cambio entre tabs, aviso de demostración y ausencia de solicitudes o persistencia; verificar con `npx playwright test`.
- [ ] 1.2 Ejecutar la carpeta Postman `01 - Servicio y preparacion` con el entorno del grupo configurado, sin reset ni requests de escritura, y registrar el resultado como baseline de lectura; verificar con el comando Newman documentado en `README_W1_W2.md`.

## 2. Navegación guiada y validación

- [ ] 2.1 Añadir los estados de pasos y el indicador de progreso en el registro existente, conservando el acceso de login; cubrir inicio, paso actual y avance con datos válidos, y verificar con Playwright.
- [ ] 2.2 Bloquear avance ante campos obligatorios vacíos o inválidos y mostrar cómo corregirlos sin perder el paso actual; cubrir correo, teléfono, clave y los campos KYC requeridos, y verificar con Playwright.

## 3. Integración KYC y estados de wallet

- [ ] 3.1 Configurar una sola URL base para el mock del grupo asignado y conectar la creación KYC con solo `documentType`, `document` y `scenario`; interceptar la solicitud y verificar URL, método y ausencia de nombre, correo, teléfono, clave o eventos en el payload con Playwright.
- [ ] 3.2 Tras crear KYC, conservar separadamente `id` y `wallet_id`, consultar cada estado en su endpoint y mostrar acceso a pagos solo cuando la consulta de wallet confirme `ACTIVE`; cubrir el escenario `APPROVED` y verificar requests, IDs y estado visible con Playwright.
- [ ] 3.3 Presentar estados `PENDING`/`REJECTED` y errores HTTP o de red sin habilitar pagos ni interpretar un resultado desconocido como éxito; verificar con pruebas de navegador que no se repite automáticamente el `POST` ante un timeout.

## 4. Eventos efímeros y verificación integral

- [ ] 4.1 Registrar inicio, avance y cierre solo en memoria, sin requests de analítica ni almacenamiento entre recargas; verificar con Playwright que no sale tráfico de telemetría y que los eventos se descartan al recargar.
- [ ] 4.2 Ejecutar la suite de navegador completa y revisar el recorrido en escritorio y móvil; verificar todos los criterios de la spec sin cambiar el contrato API. Cualquier prueba que cree KYC contra el mock compartido debe usar documentos `TEST-` únicos y ejecutarse solo tras coordinar con el equipo; no usar reset ni incorporarla como gate automático.