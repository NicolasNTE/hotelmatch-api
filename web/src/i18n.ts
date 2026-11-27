import { signal } from '@angular/core';

/** Idioma de la aplicación (es-419 por defecto, en-US). Lo comparten todas las pantallas. */
export const lang = signal(sessionStorage.getItem('hm-lang') || 'es-419');
document.documentElement.lang = lang();

export function t(es: string, en: string): string {
  return lang() === 'es-419' ? es : en;
}

export function toggleLanguage(): void {
  lang.set(lang() === 'es-419' ? 'en-US' : 'es-419');
  sessionStorage.setItem('hm-lang', lang());
  document.documentElement.lang = lang();
}

const LABELS: Record<string, [string, string]> = {
  // Prioridades y motivos de viaje
  PRIVACY: ['Privacidad', 'Privacy'], PRICE: ['Precio', 'Price'], COMFORT: ['Comodidad', 'Comfort'],
  SPACE: ['Espacio', 'Space'], QUIET: ['Silencio', 'Quiet'], VIEW: ['Vista', 'View'],
  ENTERTAINMENT: ['Entretenimiento', 'Entertainment'], WORK: ['Trabajo', 'Work'], LUXURY: ['Lujo', 'Luxury'],
  BUSINESS: ['Trabajo', 'Business'], TOURISM: ['Turismo', 'Tourism'], COUPLE: ['En pareja', 'Couple'],
  FAMILY: ['Familia', 'Family'], EVENT: ['Evento', 'Event'], REST: ['Descanso', 'Rest'], OTHER: ['Otro', 'Other'],
  // Reservas, disponibilidad y estado de habitación
  CONFIRMED: ['Confirmada', 'Confirmed'], CANCELLED: ['Cancelada', 'Cancelled'],
  PENDING_PAYMENT: ['Pendiente de pago', 'Pending payment'], AVAILABLE: ['Disponible', 'Available'],
  BOOKED: ['Reservada', 'Booked'], BLOCKED: ['Bloqueada', 'Blocked'], ACTIVE: ['Activa', 'Active'],
  INACTIVE: ['Inactiva', 'Inactive'], MAINTENANCE: ['Mantenimiento', 'Maintenance'],
  // Tipos de habitación, camas y categorías de servicio
  STANDARD: ['Estándar', 'Standard'], SUPERIOR: ['Superior', 'Superior'], DELUXE: ['Deluxe', 'Deluxe'],
  SUITE: ['Suite', 'Suite'], EXECUTIVE_SUITE: ['Suite ejecutiva', 'Executive suite'],
  SINGLE: ['Cama individual', 'Single bed'], TWIN: ['Dos camas individuales', 'Twin beds'],
  DOUBLE: ['Cama doble', 'Double bed'], QUEEN: ['Cama queen', 'Queen bed'], KING: ['Cama king', 'King bed'],
  WELLNESS: ['Bienestar', 'Wellness'], FOOD: ['Alimentación', 'Food'], TRANSPORT: ['Transporte', 'Transport'],
};

export function label(key: string): string {
  return LABELS[key]?.[lang() === 'es-419' ? 0 : 1] || key;
}
