# Dashboard de Mi Billetera (Angular)

Mock responsive de la pantalla principal posterior al inicio de sesión. Presenta el saludo al usuario, saldo actual, últimos movimientos y accesos a pago por contacto y pago por QR. Los datos son demostrativos; los accesos muestran un aviso y no ejecutan pagos ni se conectan a un backend.

## Estructura y responsabilidades:

- `src/index.html`: documento base que aloja el componente Angular.
- `src/main.ts`: registra la configuración regional peruana e inicia la aplicación.
- `src/app/app.component.ts`: datos de ejemplo y acciones de la pantalla.
- `src/app/app.component.html`: estructura y enlaces entre la vista y el componente.
- `src/app/app.component.css`: estilos del dashboard y sus variantes para pantallas pequeñas.
- `src/styles.css`: estilos globales, tipografía y colores compartidos.
- `angular.json`: define el proyecto, el build y el servidor local de Angular.
- `tsconfig.json` y `tsconfig.app.json`: opciones estrictas de TypeScript y archivos de entrada.
- `package.json`: comandos y dependencias directas; `package-lock.json` fija sus versiones resueltas.

Los archivos JSON y JSON de bloqueo se mantienen en formato válido para que npm y Angular puedan leerlos; su configuración se explica aquí en vez de insertar comentarios que puedan invalidarlos. `package-lock.json` es generado por npm y no se edita manualmente. Las carpetas `node_modules`, `.angular` y `dist` contienen dependencias o resultados generados, por lo que no se comentan ni se versionan.

## Ejecutar

Requiere Node.js 22.22.3, 24.15.0 o 26.0.0 en adelante (versiones admitidas por Angular CLI 22).

```powershell
cd frontend
npm install
npm start
```

Abre `http://localhost:4200`. Para generar el build de producción:

```powershell
npm run build
```

El nombre, el saldo y los movimientos de ejemplo se definen en `src/app/app.component.ts`.
