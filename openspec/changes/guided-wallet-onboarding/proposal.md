# Proposal

## Why

El Issue [#19 - W1: Registro y activación del MVP de Wallet](https://github.com/user-bcp-student19/workshop-auth-backend/issues/19) pide que una persona pueda completar el onboarding siguiendo pasos claros, corregir datos inválidos y entender cuándo puede empezar a pagar. El prototipo actual confirma el registro sin crear una cuenta, por lo que no satisface ese objetivo ni permite verificar la activación de la wallet.

## What Changes

- Incorporar un onboarding guiado que muestre la secuencia requerida, el paso actual y el siguiente paso esperado.
- Impedir avanzar cuando falte información obligatoria o sea inválida, indicando cómo corregirla.
- Al completar satisfactoriamente el onboarding, activar la wallet y ofrecer acceso al punto de entrada de pagos.
- En el mock, crear una wallet asociada a la verificación y permitir consultar ambos estados.
- Limitar los eventos de navegación a memoria: no enviarlos a la API y descartarlos al recargar.

### Comportamiento que se preserva

`docs/ENDPOINTS.md` sigue siendo el contrato del mock KYC y de la wallet: `POST /kyc/verifications` requiere `documentType` y `document`, acepta los escenarios documentados y crea la verificación con su wallet asociada; `GET /kyc/verifications/{id}` y `GET /wallets/{id}` mantienen sus rutas, datos y respuestas `200`/`404`. También se preservan los campos documentados de respuesta y los códigos de estado de creación y decisión. El Issue no solicita cambiar ese contrato.

### Fuera de alcance

- Cambiar requisitos KYC, decisiones de verificación o contratos HTTP existentes.
- Implementar el procesamiento de pagos; solo se cubre el acceso al punto de entrada después de activar la wallet.
- Enviar o persistir analítica, atribución de adquisición, métricas de MAU/CAC o eventos entre recargas.
- Diseñar autenticación/login o reglas de negocio no definidas en el Issue ni en el contrato documentado.

## Capabilities

### New Capabilities
- `wallet-onboarding`: flujo guiado, validación, activación de wallet, consulta de estados y eventos de navegación efímeros.

### Modified Capabilities
- Ninguna.

## Impact

La experiencia de registro existente en `index.html` y su interacción con el mock KYC/wallet descrito en `docs/ENDPOINTS.md`. Los criterios se limitan al comportamiento observable y no prescriben una arquitectura ni modifican el contrato de API.