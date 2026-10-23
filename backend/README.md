# Wallet Backend

Base Java 21 con Quarkus 3.27.2, Maven y enfoque contract-first. El frontend no forma parte de esta entrega. No hay persistencia, autenticacion ni logica de pagos implementada todavia.

## Dependencias

- Quarkus REST Jackson: servidor HTTP sobre el stack reactivo de Quarkus.
- Quarkus REST Client Jackson: cliente no bloqueante sobre Vert.x con conexiones reutilizables.
- Hibernate Validator: validacion Jakarta Bean Validation de los modelos del servidor.
- SmallRye OpenAPI: documentacion de los recursos implementados y Swagger UI en desarrollo.
- SmallRye Health: liveness y readiness tecnicos.
- JUnit 5, Quarkus Test y REST Assured: pruebas unitarias, HTTP y del artefacto empaquetado.
- OpenAPI Generator 7.16.0: interfaces Jakarta REST y modelos Jackson derivados del contrato.

Las versiones de extensiones y librerias administradas se alinean mediante el BOM de Quarkus. Se fijan las versiones de Quarkus y del generador para reproducibilidad; revisar sus parches de mantenimiento antes de desplegar en produccion.

## Estructura

```text
backend/
  pom.xml
  mvnw / mvnw.cmd
  .mvn/wrapper/
  Dockerfile
  Dockerfile.dockerignore
  src/main/java/pe/wallet/
    domain/                        # Espacio para futuras reglas de negocio
    application/health/            # Servicio de aplicacion de ejemplo
    infrastructure/rest/           # Recursos que implementan interfaces generadas
    infrastructure/client/         # Fachada CDI, cabeceras y errores del SDK
  src/main/openapi-templates/       # Plantilla del cliente MicroProfile reactivo
  src/main/resources/
    application.properties
  src/test/java/pe/wallet/
    application/health/             # Pruebas unitarias
    contract/                      # Compatibilidad JSON del codigo generado
    infrastructure/rest/           # Pruebas HTTP y de integracion empaquetada
    infrastructure/client/         # REST Client contra un servidor HTTP local
  src/test/resources/
  target/generated-sources/openapi/src/main/java/pe/wallet/contract/
    api/                           # Interfaces generadas; no editar
    model/                         # DTO y enums generados; no editar
  target/generated-sources/wallet-client/src/main/java/pe/wallet/infrastructure/client/generated/
    api/                           # Ocho interfaces reactivas generadas; no editar
    model/                         # DTO del cliente saliente; no editar
```

## Requisitos

- JDK 21 con `JAVA_HOME` apuntando al JDK, no a su subcarpeta `bin`.
- Maven Wrapper incluido; no requiere una instalacion global de Maven. La primera ejecucion descarga Maven 3.9.11 y las dependencias.
- Docker opcional para construir y ejecutar el contenedor.

Los ejemplos usan Git Bash en Windows o una terminal Bash en Linux/macOS. Se utiliza `sh ./mvnw` para no depender del permiso de ejecucion del wrapper.

Comprobar el JDK utilizado por Maven desde la raiz del repositorio:

```bash
cd backend
sh ./mvnw --version
```

El resultado debe indicar Java 21. Si `java --version` y Maven usan JDK diferentes, corregir `JAVA_HOME` antes de construir.

## Compilar

Los siguientes comandos se ejecutan desde `backend`, en Git Bash o Bash. Si la terminal esta en la raiz del repositorio, entrar primero con `cd backend`.

Para limpiar y compilar el proyecto:

```bash
sh ./mvnw clean compile
```

Este comando genera automaticamente las interfaces y modelos del servidor y del cliente HTTP desde el contrato OpenAPI, incluido el paquete `pe.wallet.infrastructure.client.generated`. La fase `compile` no ejecuta pruebas.

Para compilar, ejecutar las pruebas y crear el artefacto ejecutable:

```bash
sh ./mvnw clean package
```

Para incluir tambien las pruebas de integracion del JAR empaquetado, usar `clean verify "-DskipITs=false"`, como se indica en la seccion de pruebas.

## Levantar en desarrollo

Desde `backend`, en Git Bash o Bash:

```bash
HTTP_PORT=8089 sh ./mvnw compile quarkus:dev
```

El paso `compile` garantiza que se generen las clases antes de iniciar el modo desarrollo. El servidor queda disponible en `http://localhost:8089`. Mantener la terminal abierta y usar `Ctrl+C` para detenerlo.

Endpoints disponibles:

- `http://localhost:8089/health`: health funcional del backend. Devuelve HTTP `200 OK` y JSON con `status: "ok"` y `storage: "memory"`.
- `http://localhost:8089/q/health/live`: liveness tecnico.
- `http://localhost:8089/q/health/ready`: readiness tecnico; devuelve HTTP `200 OK` y `status: "UP"`. No comprueba base de datos ni proveedores porque aun no hay dependencias operativas.
- `http://localhost:8089/q/swagger-ui`: Swagger UI en desarrollo.
- `http://localhost:8089/q/openapi`: descripcion de recursos implementados, no promesa de que todo el contrato este implementado.
- `http://localhost:8089/contracts/openapi.json`: contrato original completo, publicado sin alterar sus servidores del mock.

Verificar el status HTTP y el cuerpo desde otra terminal Bash:

```bash
curl -i http://localhost:8089/health
curl -i http://localhost:8089/q/health/ready
```

`storage=memory` en `/health` identifica esta base sin persistencia; no significa que existan wallets o pagos almacenados. Las operaciones de negocio todavia responden 404 al no tener recursos implementados. No apuntar la suite completa de Postman al backend esperando el comportamiento del mock.

## Contract-first

La unica fuente es [client/openapi.json](../client/openapi.json). No se mantiene una segunda copia editable en el backend.

1. Cambiar primero el contrato y validar su compatibilidad con los consumidores.
2. Regenerar modelos e interfaces:

```bash
sh ./mvnw generate-sources
```

3. Implementar o ajustar los recursos en `infrastructure/rest` usando las interfaces de `pe.wallet.contract.api` y delegar los casos de uso en `application`.
4. Agregar pruebas y ejecutar `clean verify`.

La generacion se ejecuta automaticamente durante `compile`, `test`, `package` y `verify`. `target` no se versiona y nunca debe editarse manualmente. Si cambia el contrato durante una sesion de desarrollo, reiniciar con `compile quarkus:dev` para regenerar y volver a copiar el contrato estatico.

Las interfaces usan `Uni<Response>` para permitir composicion no bloqueante y devolver los codigos de respuesta previstos. Los metodos con entrada/salida bloqueante que se agreguen en el futuro requieren ejecucion apropiada fuera del event loop; usar Quarkus reactivo no convierte automaticamente una llamada JDBC o de archivos en no bloqueante.

El contrato actual tiene cuatro operaciones sin `operationId`; el generador les asigna nombres automaticamente y muestra avisos. Si se modifican esas rutas, revisar los nombres generados. `Payment.fingerprint` permite texto o arreglo: se mapea el esquema inline `Payment_fingerprint` a `Object` porque el generador no representa correctamente esa union. Dos pruebas verifican ambos formatos. Este atributo es interno del mock y no debe formar parte del dominio de negocio.

## REST Client

El cliente se genera desde `../client/openapi.json` durante `generate-sources`, separado de las interfaces del servidor. Cubre las 32 operaciones del contrato en ocho interfaces MicroProfile sobre Quarkus REST Client/Vert.x. Cada operacion expone un metodo tipado `Uni<T>` y una variante `...Response()` con `Uni<Response>`. La plantilla local evita dependencias HTTP adicionales y registra los proveedores de cabeceras y errores.

`WalletMockClient` es una fachada CDI. Se inyecta con `@Inject`, sin `@RestClient`; este ultimo se utiliza internamente para las interfaces generadas. No se llama al mock al arrancar el backend.

```java
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import pe.wallet.infrastructure.client.WalletMockClient;
import pe.wallet.infrastructure.client.generated.model.Wallet;

@ApplicationScoped
public class WalletQueryService {
  @Inject
  WalletMockClient client;

  public Uni<Wallet> findWallet(String walletId) {
    return client.onboarding().getWalletsByid(walletId);
  }
}
```

La fachada ofrece `service()`, `onboarding()`, `payments()`, `qr()`, `callbacks()`, `failures()`, `reconciliation()` y `controls()`. Los modelos salientes pertenecen a `pe.wallet.infrastructure.client.generated.model`, no a `pe.wallet.contract.model`. Para crear pagos, `payments().postPayments(idempotencyKey, request)` recibe explicitamente la clave de idempotencia y un `PostPaymentsRequest`. No bloquear con `await()` en recursos reactivos; componer el `Uni`.

La configuracion limita conexion a 2 segundos, respuesta a 5 segundos y el pool a 20 conexiones; reutiliza conexiones y no registra cuerpos. Todas las solicitudes envian `X-Mock-Latency-Ms`. `/groups` y `/usage/all` se dirigen a la URL raiz; las demas operaciones usan la URL del grupo. No hay reintentos automaticos a operaciones financieras.

Las respuestas HTTP de error se convierten en `WalletApiException`: `getStatusCode()` conserva el codigo y `getApiError()` contiene el error del contrato, incluidos `processed`, `paymentId` y `status` cuando estan presentes. Un 504 con `processed=true` no implica que el pago deba enviarse nuevamente. Si la respuesta no tiene un error JSON interpretable, se utiliza `UNEXPECTED_HTTP_ERROR` conservando el codigo HTTP. Los errores de transporte siguen siendo fallos del cliente HTTP, no errores de negocio.

Para operaciones con distintos esquemas de exito, usar la variante `...Response()`. Por ejemplo, `failures().postPaymentsByidResolveResponse(id, null)` permite distinguir el 200 con `Payment` del 202 con `PendingResolution`. Consumir la entidad segun el estado y cerrar siempre el `Response`, tambien en rutas de fallo durante su procesamiento.

La validacion Bean Validation de los DTO salientes esta desactivada: el generador aplica incorrectamente restricciones de longitud a algunos enums. El servidor remoto valida las solicitudes; la validacion del servidor backend permanece habilitada. Los DTO generados no sustituyen las reglas de negocio de la capa de aplicacion.

| Variable de entorno | Valor predeterminado |
| --- | --- |
| `HTTP_PORT` | `8080` |
| `WALLET_ENVIRONMENT` | `local` |
| `WALLET_MOCK_URL` | `https://bcp-wallet-mock.yefri.workers.dev/groups/4` |
| `WALLET_MOCK_ROOT_URL` | `https://bcp-wallet-mock.yefri.workers.dev` |
| `WALLET_MOCK_LATENCY_MS` | `0` |

Las pruebas fijan la URL configurada a una direccion local no operativa y la prueba del cliente usa su propio servidor HTTP local. Ninguna prueba consulta o modifica datos del grupo compartido.

Ejecutar solo las pruebas del cliente:

```bash
sh ./mvnw "-Dtest=WalletMockClientTest,WalletGeneratedClientContractTest" test
```

Diez pruebas HTTP verifican deserializacion, rutas, query parameters, JSON, idempotencia, latencia, errores 404/503/504 sin reintentos y la respuesta pendiente 202. Otra prueba compara todas las rutas y verbos del contrato con los metodos reactivos tipados y completos del SDK.

## Pruebas y empaquetado

Desde `backend`:

```bash
sh ./mvnw test
sh ./mvnw clean verify "-DskipITs=false"
```

`test` ejecuta pruebas unitarias, de contrato, HTTP con `@QuarkusTest` y del cliente. `verify -DskipITs=false` agrega las pruebas `*IT` con `@QuarkusIntegrationTest`, arrancando el JAR empaquetado. Estas pruebas no necesitan Docker.

Ejecutar el artefacto construido:

```bash
java -jar target/quarkus-app/quarkus-run.jar
```

El formato es fast-jar: para desplegar debe copiarse toda la carpeta `target/quarkus-app`, no solamente `quarkus-run.jar`.

## Docker

Desde la raiz del repositorio, no desde `backend`, porque el build tambien necesita el contrato en `client`:

```bash
docker build -f backend/Dockerfile -t wallet-backend:local .
docker run --rm -p 8080:8080 -e WALLET_ENVIRONMENT=docker wallet-backend:local
```

El Dockerfile compila dentro de una imagen Maven/JDK 21 y copia el fast-jar a una imagen JRE 21. La imagen final no lleva Maven ni fuentes y se ejecuta con UID/GID `10001`. El archivo Dockerfile.dockerignore restringe el contexto al POM, fuentes y contrato. Las pruebas se ejecutan antes del build mediante `verify`; el build de imagen las omite para no duplicarlas.

Docker debe validarse en una maquina con el motor instalado y activo. Esta base no incluye imagen nativa, Kubernetes, base de datos, seguridad ni despliegue productivo.