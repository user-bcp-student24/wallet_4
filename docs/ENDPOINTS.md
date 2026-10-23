# API Wallet 4

URL Base:

```text
http://localhost:8080
```

Todas las respuestas se sirven en formato JSON.

---

# W1 · Registro y Activación

Esta API simula el proceso de validación de identidad (KYC) y la creación de una wallet digital.

## Flujo de Registro

1. Crear la verificación KYC:

```http
POST /kyc/verifications
```

Ejemplo:

```json
{
  "documentType": "TEST",
  "document": "TEST-001",
  "scenario": "APPROVED"
}
```

2. Guardar:

- id
- wallet_id

3. Consultar estado de verificación:

```http
GET /kyc/verifications/{id}
```

4. Consultar wallet:

```http
GET /wallets/{id}
```

5. Para revisiones manuales:

```http
GET /support/cases
```

Resolver decisión:

```http
POST /kyc/verifications/{id}/decision
```

```json
{
  "decision": "ACTIVE"
}
```

o

```json
{
  "decision": "REJECTED"
}
```

---

## POST /kyc/verifications

Crea una verificación KYC y una wallet asociada.

### Request

```json
{
  "documentType": "TEST",
  "document": "TEST-001",
  "scenario": "APPROVED"
}
```

### Campos

| Campo | Tipo | Requerido | Valores |
|---------|---------|---------|---------|
| documentType | string | Sí | TEST |
| document | string | Sí | TEST-* |
| scenario | string | No | APPROVED, REJECTED, MANUAL_REVIEW, UNAVAILABLE |

### Response

```json
{
  "id": "kyc-123",
  "wallet_id": "wallet-123",
  "scenario": "APPROVED",
  "status": "ACTIVE"
}
```

### HTTP Status

```text
201 Created
400 Bad Request
503 Service Unavailable
413 Payload Too Large
429 Too Many Requests
```

---

## GET /kyc/verifications/{id}

Consulta una verificación KYC.

### Response

```json
{
  "id": "kyc-123",
  "wallet_id": "wallet-123",
  "scenario": "APPROVED",
  "status": "ACTIVE"
}
```

### HTTP Status

```text
200 OK
404 Not Found
```

---

## POST /kyc/verifications/{id}/decision

Resuelve una verificación pendiente.

### Request

```json
{
  "decision": "ACTIVE"
}
```

### Valores permitidos

```text
ACTIVE
REJECTED
```

### HTTP Status

```text
200 OK
400 Bad Request
404 Not Found
409 Conflict
413 Payload Too Large
429 Too Many Requests
```

---

## GET /wallets/{id}

Consulta una wallet.

### Response

```json
{
  "id": "wallet-demo",
  "status": "ACTIVE",
  "balance": 100000,
  "reserved": 0,
  "available": 100000,
  "currency": "PEN"
}
```

### HTTP Status

```text
200 OK
404 Not Found
```

---

# W2 · Pago a Contacto

Permite enviar dinero a un contacto externo y consultar su estado posterior.

## Flujo de Pago

### Consultar Wallet

```http
GET /wallets/wallet-demo
```

### Crear Pago

```http
POST /payments
```

Headers:

```http
Content-Type: application/json
Idempotency-Key: contacto-001
```

Body:

```json
{
  "walletId": "wallet-demo",
  "recipient": "contact-demo",
  "amountCents": 1500,
  "currency": "PEN",
  "scenario": "APPROVED"
}
```

### Consultar Estado

```http
GET /payments/{id}
```

### Obtener Comprobante

```http
GET /payments/{id}/receipt
```

### Consultar Historial

```http
GET /payments?walletId=wallet-demo
```

---

## POST /payments

Crea una intención de pago y reserva saldo.

### Request

```json
{
  "walletId": "wallet-demo",
  "recipient": "contact-demo",
  "amountCents": 1500,
  "currency": "PEN",
  "scenario": "APPROVED"
}
```

### Campos

| Campo | Tipo | Requerido |
|---------|---------|---------|
| walletId | string | Sí |
| recipient | string | Sí |
| amountCents | integer | Sí |
| currency | string | No |
| scenario | string | No |
| qrId | string | No |

### Escenarios

```text
APPROVED
REJECTED
PENDING
DELAYED_APPROVAL
TIMEOUT_AFTER_PROCESSING
PROVIDER_ERROR
```

### Destinatarios válidos

```text
contact-demo
merchant-demo
```

### Response

```json
{
  "id": "pay-123",
  "wallet_id": "wallet-demo",
  "recipient": "contact-demo",
  "amount": 1500,
  "status": "PENDING",
  "scenario": "APPROVED"
}
```

### HTTP Status

```text
200 OK
202 Accepted
400 Bad Request
404 Not Found
409 Conflict
410 Gone
503 Service Unavailable
504 Gateway Timeout
413 Payload Too Large
429 Too Many Requests
```

---

## GET /payments

Obtiene el historial de pagos de una wallet.

### Request

```http
GET /payments?walletId=wallet-demo
```

### Response

```json
[
  {
    "id": "pay-123",
    "wallet_id": "wallet-demo",
    "recipient": "contact-demo",
    "amount": 1500,
    "status": "SUCCEEDED"
  }
]
```

---

## GET /payments/{id}

Consulta el estado de un pago.

### HTTP Status

```text
200 OK
404 Not Found
```

---

## GET /payments/{id}/receipt

Obtiene el comprobante de un pago completado.

### Response

```json
{
  "receiptId": "rcp-123",
  "paymentId": "pay-123",
  "amountCents": 1500,
  "currency": "PEN",
  "recipient": "contact-demo",
  "status": "SUCCEEDED"
}
```

### HTTP Status

```text
200 OK
404 Not Found
409 Conflict
```

---

## GET /provider/payments/{id}

Consulta el estado del proveedor simulado.

### Response

```json
{
  "id": "pay-123",
  "amount": 1500,
  "status": "SUCCEEDED"
}
```

### HTTP Status

```text
200 OK
404 Not Found
```

---

# Datos de Prueba

## Wallet precargada

```text
wallet-demo
```

Saldo inicial:

```text
100000 céntimos
S/ 1,000.00
```

## Contactos de prueba

```text
contact-demo
merchant-demo
```

## Documentos de prueba

```text
TEST-001
TEST-002
TEST-003
```

## Escenarios KYC

```text
APPROVED
REJECTED
MANUAL_REVIEW
UNAVAILABLE
```

## Escenarios de Pago

```text
APPROVED
REJECTED
PENDING
DELAYED_APPROVAL
TIMEOUT_AFTER_PROCESSING
PROVIDER_ERROR
```