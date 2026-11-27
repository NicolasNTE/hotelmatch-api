import { Component, inject, signal, provideZonelessChangeDetection } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';
import { HttpClient, HttpInterceptorFn, provideHttpClient, withInterceptors } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipSelectionChange, MatChipsModule } from '@angular/material/chips';
import { MatDialog } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MAT_FORM_FIELD_DEFAULT_OPTIONS, MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorIntl, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { firstValueFrom } from 'rxjs';
import { Admin } from './admin';
import { Chat } from './chat';
import { lang, t, label, toggleLanguage } from './i18n';
import { PaginatorText, ask } from './ui';

const API = (window as any).HOTELMATCH_API || '/api/v1';
const auth: HttpInterceptorFn = (req, next) => {
  const token = sessionStorage.getItem('hm-token');
  return next(req.url.startsWith(API + '/') ? req.clone({ setHeaders: {
    ...(token ? { Authorization: 'Bearer ' + token } : {}), 'Accept-Language': sessionStorage.getItem('hm-lang') || 'es-419',
  } }) : req);
};

@Component({
  selector: 'hm-app',
  imports: [CommonModule, FormsModule, MatButtonModule, MatCardModule, MatCheckboxModule, MatChipsModule,
    MatExpansionModule, MatFormFieldModule, MatInputModule, MatPaginatorModule, MatProgressBarModule,
    MatSelectModule, MatTabsModule, MatToolbarModule, Admin, Chat],
  templateUrl: './app.html',
})
class App {
  http = inject(HttpClient);
  dialog = inject(MatDialog);
  user = signal<any>(null); results = signal<any[]>([]); bookings = signal<any[]>([]);
  weather = signal<any>(null); detail = signal<any>(null); report = signal<any>(null); busy = signal(false); message = signal('');
  lang = lang; t = t; label = label; page = signal('search'); register = false; showAuth = false;
  email = ''; password = ''; name = ''; role = 'GUEST'; step = signal(1); amenities = signal<any[]>([]); selectedAmenities: number[] = [];
  hotel: any = { legalName: '', tradeName: '', address: '', city: '', district: '', latitude: null, longitude: null, sustainabilitySeal: false, sustainabilityDescription: '' };
  profile: any = { name: '', email: '', currentPassword: '', newPassword: '' };
  bookingPage = signal(0); bookingTotal = signal(0); bookingStatus = ''; bookingFrom = ''; bookingTo = '';
  travel = { checkIn: '', checkOut: '', guests: 1, maxBudgetPerNight: 350, purpose: 'TOURISM', priorities: ['QUIET'], city: '', freeText: '' };
  purposes = ['BUSINESS', 'TOURISM', 'COUPLE', 'FAMILY', 'EVENT', 'REST', 'OTHER'];
  priorities = ['PRIVACY', 'PRICE', 'COMFORT', 'SPACE', 'QUIET', 'VIEW', 'ENTERTAINMENT', 'WORK', 'LUXURY'];
  bookingStatuses = ['CONFIRMED', 'PENDING_PAYMENT', 'CANCELLED'];
  from = ''; to = '';
  suggestion = signal('');

  constructor() {
    const today = new Date();
    const local = (d: Date) => new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0, 10);
    this.from = local(new Date(today.getFullYear(), today.getMonth(), 1));
    this.to = local(new Date(today.getFullYear(), today.getMonth() + 1, 1));
    if (sessionStorage.getItem('hm-token')) {
      this.run(async () => { this.user.set(await this.get('/users/me')); await this.catalog(); });
    }
  }

  // Selección en las listas de chips: solo se atiende lo que pulsa la persona.
  choosePurpose(purpose: string, event: MatChipSelectionChange) {
    if (!event.isUserInput) return;
    if (event.selected) this.travel.purpose = purpose;
    else if (this.travel.purpose === purpose) event.source.selected = true;
  }
  choosePriority(key: string, event: MatChipSelectionChange) {
    if (!event.isUserInput) return;
    const others = this.travel.priorities.filter(p => p !== key);
    this.travel.priorities = event.selected ? [...others, key].slice(0, 3) : others;
  }
  chooseAmenity(id: number, event: MatChipSelectionChange) {
    if (!event.isUserInput) return;
    const others = this.selectedAmenities.filter(x => x !== id);
    this.selectedAmenities = event.selected ? [...others, id] : others;
  }

  next() {
    const trip = this.travel;
    if (!trip.checkIn || !trip.checkOut || trip.checkOut <= trip.checkIn || trip.guests < 1 || trip.maxBudgetPerNight <= 0) {
      this.message.set(t('Revisa las fechas, los huéspedes y el presupuesto.', 'Check dates, guests and budget.'));
      return;
    }
    this.step.update(x => Math.min(3, x + 1));
    this.message.set('');
  }
  previous() { this.step.update(x => Math.max(1, x - 1)); }
  restart() { this.step.set(1); this.results.set([]); this.detail.set(null); }

  async catalog() { this.amenities.set(await this.get('/services')); }
  openProfile() {
    this.profile = { name: this.user().name, email: this.user().email, currentPassword: '', newPassword: '' };
    this.hotel = { ...this.user().hotel };
    this.page.set('profile');
  }
  saveProfile() {
    this.run(async () => {
      const body: any = { name: this.profile.name, email: this.profile.email };
      if (this.profile.newPassword) { body.currentPassword = this.profile.currentPassword; body.newPassword = this.profile.newPassword; }
      this.user.set(await firstValueFrom(this.http.put(API + '/users/me', body)));
      this.profile.currentPassword = ''; this.profile.newPassword = '';
      this.message.set(t('Perfil guardado.', 'Profile saved.'));
    });
  }
  saveHotel() {
    this.run(async () => {
      await firstValueFrom(this.http.put(API + '/hotels/' + this.user().hotel.id, this.hotel));
      this.user.set(await this.get('/users/me'));
      this.message.set(t('Hotel actualizado.', 'Hotel updated.'));
    });
  }
  async deactivateHotel() {
    if (!await ask(this.dialog, t('¿Dar de baja el hotel? Sus habitaciones dejarán de aparecer en las búsquedas.', 'Deactivate the hotel? Its rooms will be removed from searches.'))) return;
    this.run(async () => {
      await firstValueFrom(this.http.delete(API + '/hotels/' + this.user().hotel.id + '?confirm=true'));
      this.user.set(await this.get('/users/me'));
    });
  }
  async deactivateAccount() {
    if (!await ask(this.dialog, t('¿Dar de baja tu cuenta? Se conservarán los registros históricos.', 'Deactivate your account? Historical records are preserved.'))) return;
    this.run(async () => { await firstValueFrom(this.http.delete(API + '/users/me')); this.logout(); });
  }

  changeStatus(id: number, status: string) {
    this.run(async () => { await firstValueFrom(this.http.put(API + '/bookings/' + id + '/status', { status })); await this.fetchBookings(); });
  }
  async fetchBookings() {
    const q = new URLSearchParams({ page: String(this.bookingPage()), size: '10' });
    if (this.bookingStatus) q.set('status', this.bookingStatus);
    if (this.bookingFrom) q.set('from', this.bookingFrom);
    if (this.bookingTo) q.set('to', this.bookingTo);
    const r = await this.get('/bookings?' + q);
    this.bookings.set(r.content);
    this.bookingTotal.set(r.totalElements);
  }
  turnBookings(event: PageEvent) { this.bookingPage.set(event.pageIndex); this.run(() => this.fetchBookings()); }
  loadBookings() { this.page.set('bookings'); this.bookingPage.set(0); this.run(() => this.fetchBookings()); }
  async cancel(id: number) {
    if (!await ask(this.dialog, t('¿Cancelar esta reserva?', 'Cancel this booking?'))) return;
    this.run(async () => { await firstValueFrom(this.http.delete(API + '/bookings/' + id)); await this.fetchBookings(); });
  }

  language() { toggleLanguage(); }
  get(path: string) { return firstValueFrom(this.http.get<any>(API + path)); }
  async run(work: () => Promise<void>) {
    if (this.busy()) return;
    this.busy.set(true); this.message.set('');
    try { await work(); } catch (e: any) {
      if (e.status === 401) this.logout();
      this.message.set(e.status === 409 ? t('La habitación ya no está disponible. Busca otra opción.', 'This room is no longer available. Search again.')
        : e.error?.detail || e.message || t('No se pudo completar la operación.', 'The operation failed.'));
    } finally { this.busy.set(false); }
  }
  login() {
    this.run(async () => {
      if (this.register) {
        const body = { name: this.name, email: this.email, password: this.password, role: this.role, ...(this.role === 'ADMIN' ? { hotel: this.hotel } : {}) };
        await firstValueFrom(this.http.post(API + '/auth/register', body));
      }
      const r: any = await firstValueFrom(this.http.post(API + '/auth/login', { email: this.email, password: this.password }));
      this.register = false;
      sessionStorage.setItem('hm-token', r.accessToken);
      this.user.set(r.user); this.showAuth = false; this.password = '';
      await this.catalog();
    });
  }
  logout() {
    sessionStorage.removeItem('hm-token');
    this.user.set(null); this.results.set([]); this.bookings.set([]); this.detail.set(null); this.report.set(null);
    this.page.set('search'); this.step.set(1); this.suggestion.set('');
  }

  interpret() {
    const text = this.travel.freeText.trim();
    if (!text) { this.suggestion.set(t('Escribe algo sobre tu viaje primero.', 'Write something about your trip first.')); return; }
    this.run(async () => {
      this.suggestion.set(t('Analizando tu texto…', 'Reading your text…'));
      const r: any = await firstValueFrom(this.http.post(API + '/recommendations/interpret', { text }));
      if (r.mode === 'NONE') { this.suggestion.set(t('No encontré preferencias claras; elígelas en el siguiente paso.', 'No clear preferences found; pick them in the next step.')); return; }
      if (r.priorities.length) this.travel.priorities = r.priorities;
      if (r.purpose) this.travel.purpose = r.purpose;
      const source = r.mode === 'AI' ? t('Sugerencia generada con IA', 'AI-generated suggestion') : t('Sugerencia por palabras clave', 'Keyword-based suggestion');
      this.suggestion.set(source + ': ' + [r.purpose, ...r.priorities].filter(Boolean).map((k: string) => label(k)).join(', ') + '. ' + t('Puedes cambiarla.', 'You can change it.'));
    });
  }
  search() {
    this.run(async () => {
      if (this.travel.priorities.length < 1 || this.travel.priorities.length > 3) throw Error(t('Elige entre 1 y 3 prioridades.', 'Choose 1 to 3 priorities.'));
      const r: any = await firstValueFrom(this.http.post(API + '/recommendations', { ...this.travel, amenityIds: this.selectedAmenities }));
      this.results.set(r.results); this.step.set(4); this.detail.set(null);
      if (!r.results.length) this.message.set(t('Sin opciones: cambia fechas o presupuesto.', 'No matches: change dates or budget.'));
    });
  }
  show(id: number) {
    this.run(async () => {
      const room = await this.get('/rooms/' + id);
      this.detail.set({ ...room, hotel: await this.get('/hotels/' + room.hotelId), match: this.results().find(r => r.roomId === id) });
      this.weather.set(null);
      setTimeout(() => document.getElementById('room-detail')?.scrollIntoView({ behavior: 'smooth', block: 'start' }));
      try { this.weather.set(await this.get('/hotels/' + room.hotelId + '/weather')); } catch { this.weather.set({ available: false }); }
    });
  }
  reserve(id: number) {
    this.run(async () => {
      const r: any = await firstValueFrom(this.http.post(API + '/bookings', { roomId: id, checkIn: this.travel.checkIn, checkOut: this.travel.checkOut, guests: this.travel.guests }));
      this.message.set(t('Reserva confirmada: ', 'Booking confirmed: ') + r.code);
      this.results.set([]); this.detail.set(null);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    });
  }
  loadReport() { this.run(async () => this.report.set(await this.get('/reports?from=' + this.from + '&to=' + this.to))); }
  download() {
    this.run(async () => {
      const b = await firstValueFrom(this.http.get(API + '/reports/pdf?from=' + this.from + '&to=' + this.to, { responseType: 'blob' }));
      const u = URL.createObjectURL(b);
      const a = document.createElement('a');
      a.href = u; a.download = 'hotelmatch-report.pdf'; a.click();
      setTimeout(() => URL.revokeObjectURL(u), 1000);
    });
  }
}

bootstrapApplication(App, {
  providers: [
    provideZonelessChangeDetection(),
    provideHttpClient(withInterceptors([auth])),
    { provide: MAT_FORM_FIELD_DEFAULT_OPTIONS, useValue: { appearance: 'outline', subscriptSizing: 'dynamic' } },
    { provide: MatPaginatorIntl, useClass: PaginatorText },
  ],
}).catch(console.error);
