import { Component, TemplateRef, inject, signal, viewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipSelectionChange, MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { Api } from './api';
import { t, label } from './i18n';
import { ask } from './ui';

@Component({
  selector: 'hm-admin',
  imports: [CommonModule, FormsModule, MatButtonModule, MatCardModule, MatChipsModule, MatDialogModule,
    MatFormFieldModule, MatInputModule, MatPaginatorModule, MatSelectModule, MatTableModule, MatTabsModule],
  templateUrl: './admin.html',
})
export class Admin {
  api = inject(Api);
  dialog = inject(MatDialog);
  roomDialog = viewChild.required<TemplateRef<unknown>>('roomDialog');
  private dialogRef?: MatDialogRef<unknown>;

  rooms = signal<any[]>([]); services = signal<any[]>([]); rates = signal<any[]>([]); days = signal<any[]>([]);
  roomId: number | null = null; page = signal(0); total = signal(0); q = ''; type = ''; status = '';
  room: any = this.emptyRoom(); service: any = { name: '', category: 'COMFORT', icon: '' }; serviceId: number | null = null;
  selectedRoom: number | '' = ''; from = ''; to = ''; availabilityStatus = 'BLOCKED';
  rate: any = { name: '', startDate: '', endDate: '', price: 100 }; rateId: number | null = null;
  types = ['STANDARD', 'SUPERIOR', 'DELUXE', 'SUITE', 'EXECUTIVE_SUITE']; beds = ['SINGLE', 'TWIN', 'DOUBLE', 'QUEEN', 'KING'];
  categories = ['COMFORT', 'WELLNESS', 'ENTERTAINMENT', 'WORK', 'FOOD', 'TRANSPORT'];
  experienceKeys = ['privacy', 'quiet', 'view', 'space'];
  roomColumns = ['name', 'type', 'capacity', 'price', 'status', 'actions'];
  dayColumns = ['date', 'status', 'actions'];
  t = t; label = label;

  constructor() { this.api.run(async () => { await this.load(); await this.loadServices(); }); }

  emptyRoom() {
    return { name: '', number: '', floor: 1, type: 'STANDARD', capacity: 2, bedType: 'QUEEN', basePrice: 150, description: '', imageUrl: '', status: 'ACTIVE', amenityIds: [] as number[], experience: { privacy: 3, quiet: 3, view: 3, space: 3 } };
  }
  async load() {
    const query = new URLSearchParams({ page: String(this.page()), size: '10', q: this.q });
    if (this.type) query.set('type', this.type);
    if (this.status) query.set('status', this.status);
    const r = await this.api.request('GET', '/rooms?' + query);
    this.rooms.set(r.content);
    this.total.set(r.totalElements);
  }
  async loadServices() { this.services.set(await this.api.request('GET', '/services')); }
  refresh() { this.page.set(0); this.api.run(() => this.load()); }
  paginate(event: PageEvent) { this.page.set(event.pageIndex); this.api.run(() => this.load()); }

  edit(r?: any) {
    this.roomId = r?.id ?? null;
    this.room = r ? { ...r, experience: { ...r.experience }, amenityIds: r.amenities.map((a: any) => a.amenityId) } : this.emptyRoom();
    this.dialogRef = this.dialog.open(this.roomDialog(), { width: '820px', maxWidth: '96vw', maxHeight: '94vh' });
  }
  closeEditor() { this.dialogRef?.close(); }
  chooseAmenity(id: number, event: MatChipSelectionChange) {
    if (!event.isUserInput) return;
    const others = this.room.amenityIds.filter((x: number) => x !== id);
    this.room.amenityIds = event.selected ? [...others, id] : others;
  }
  saveRoom() {
    this.api.run(async () => {
      await this.api.request(this.roomId ? 'PUT' : 'POST', '/rooms' + (this.roomId ? '/' + this.roomId : ''), this.room);
      this.closeEditor();
      await this.load();
      this.api.message.set(t('Habitación guardada.', 'Room saved.'));
    });
  }
  async removeRoom(r: any) {
    if (!await ask(this.dialog, t('¿Retirar ', 'Remove ') + r.name + t('? Se conservarán las reservas históricas.', '? Historical bookings are kept.'))) return;
    this.api.run(async () => { await this.api.request('DELETE', '/rooms/' + r.id + '?confirm=true'); await this.load(); });
  }

  editService(s: any) { this.serviceId = s.id; this.service = { name: s.name, category: s.category, icon: s.icon || '' }; }
  saveService(form: NgForm) {
    this.api.run(async () => {
      await this.api.request(this.serviceId ? 'PUT' : 'POST', '/services' + (this.serviceId ? '/' + this.serviceId : ''), this.service);
      this.resetService(form);
      await this.loadServices();
    });
  }
  // resetForm limpia también el estado "enviado", para que los campos vacíos no queden marcados como error.
  resetService(form: NgForm) {
    this.serviceId = null;
    this.service = { name: '', category: 'COMFORT', icon: '' };
    form.resetForm({ name: '', category: 'COMFORT', icon: '' });
  }
  async removeService(s: any) {
    if (!await ask(this.dialog, t('¿Retirar el servicio ', 'Remove the amenity ') + s.name + t('? No debe estar asociado a habitaciones.', '? It must not be linked to any room.'))) return;
    this.api.run(async () => { await this.api.request('DELETE', '/services/' + s.id); await this.loadServices(); });
  }

  chooseRoom() { this.rates.set([]); this.days.set([]); this.rateId = null; }
  path(part: string) {
    if (!this.selectedRoom) throw Error(t('Selecciona una habitación.', 'Select a room.'));
    return '/rooms/' + this.selectedRoom + '/' + part;
  }
  private async fetchDays() { this.days.set(await this.api.request('GET', this.path('availability') + '?from=' + this.from + '&to=' + this.to)); }
  loadCalendar() {
    this.api.run(async () => {
      if (!this.from || !this.to) throw Error(t('Selecciona ambas fechas.', 'Select both dates.'));
      await this.fetchDays();
    });
  }
  saveAvailability() {
    this.api.run(async () => {
      await this.api.request('POST', this.path('availability'), { startDate: this.from, endDate: this.to, status: this.availabilityStatus });
      await this.fetchDays();
    });
  }
  dayAction(date: string, remove = false) {
    this.api.run(async () => {
      await this.api.request(remove ? 'DELETE' : 'PUT', this.path('availability') + '/' + date, remove ? undefined : { status: this.availabilityStatus });
      await this.fetchDays();
    });
  }

  private async fetchRates() { this.rates.set(await this.api.request('GET', this.path('rates'))); }
  loadRates() { this.api.run(() => this.fetchRates()); }
  editRate(r: any) { this.rateId = r.id; this.rate = { name: r.name, startDate: r.startDate, endDate: r.endDate, price: r.price }; }
  newRate(form: NgForm) {
    this.rateId = null;
    this.rate = { name: '', startDate: '', endDate: '', price: 100 };
    form.resetForm({ name: '', start: '', end: '', price: 100 });
  }
  saveRate(form: NgForm) {
    this.api.run(async () => {
      await this.api.request(this.rateId ? 'PUT' : 'POST', this.path('rates') + (this.rateId ? '/' + this.rateId : ''), this.rate);
      this.newRate(form);
      await this.fetchRates();
    });
  }
  async removeRate(id: number) {
    if (!await ask(this.dialog, t('¿Eliminar esta tarifa y restituir el precio base?', 'Delete this rate and restore the base price?'))) return;
    this.api.run(async () => { await this.api.request('DELETE', this.path('rates') + '/' + id); await this.fetchRates(); });
  }
}
