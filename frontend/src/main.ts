import { registerLocaleData } from '@angular/common';
import localeEsPe from '@angular/common/locales/es-PE';
import { LOCALE_ID } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';

// Registra los nombres de meses y formatos de fecha usados en español de Perú.
registerLocaleData(localeEsPe);

// Establece es-PE como locale predeterminado para pipes como DatePipe y CurrencyPipe.
bootstrapApplication(AppComponent, {
  providers: [{ provide: LOCALE_ID, useValue: 'es-PE' }]
}).catch((error: unknown) => {
  // Hace explícito el error de arranque en la consola del navegador.
  console.error('No se pudo iniciar la aplicación de la billetera.', error);
});
