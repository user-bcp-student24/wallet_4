import { Component } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';

/** Estructura de un movimiento ficticio mostrado en el resumen. */
interface Movement {
  initials: string;
  name: string;
  detail: string;
  date: string;
  amount: number;
  incoming: boolean;
}

@Component({
  selector: 'app-root',
  standalone: true,
  // Los pipes se importan directamente porque este componente no usa NgModules.
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  isLoggedIn = false;
  userName = 'María Fernanda';
  readonly balance = 2450.8;
  readonly today = new Date();

  get userInitials(): string {
    return this.userName
      .split(/\s+/)
      .slice(0, 2)
      .map((part) => part.charAt(0).toUpperCase())
      .join('');
  }

  // Movimientos de ejemplo: incoming distingue abonos de pagos en la presentación.
  readonly movements: Movement[] = [
    {
      initials: 'CL',
      name: 'Carlos López',
      detail: 'Transferencia recibida',
      date: 'Hoy, 10:42 a. m.',
      amount: 180,
      incoming: true
    },
    {
      initials: 'CA',
      name: 'Café de Barrio',
      detail: 'Pago con QR',
      date: 'Hoy, 9:18 a. m.',
      amount: 18.5,
      incoming: false
    },
    {
      initials: 'AP',
      name: 'Ana Pérez',
      detail: 'Pago por contacto',
      date: 'Ayer, 6:32 p. m.',
      amount: 45,
      incoming: false
    }
  ];

  activeAction = '';

  completeOnboarding(event: Event, form: HTMLFormElement): void {
    event.preventDefault();
    if (!form.reportValidity()) return;

    const fullName = new FormData(form).get('fullName')?.toString().trim() ?? '';
    this.userName = fullName.split(/\s+/)[0] || 'Usuario';
    this.isLoggedIn = true;
  }

  restartDemo(): void {
    this.isLoggedIn = false;
    this.userName = 'María Fernanda';
    this.activeAction = '';
  }

  /** Muestra un aviso para indicar que las acciones aún no están conectadas. */
  selectAction(action: string): void {
    this.activeAction = action;
  }
}
