import { Component, inject, signal } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Api } from './api';
import { t } from './i18n';

@Component({
  selector: 'hm-chat',
  imports: [FormsModule, MatButtonModule, MatCardModule, MatFormFieldModule, MatInputModule],
  template: `
<aside class="chat">
  @if(open()){
  <mat-card appearance="raised" [attr.aria-label]="t('Asistente HotelMatch','HotelMatch assistant')">
    <mat-card-content>
      <h2>{{t('¿En qué te ayudamos?','How can we help?')}}</h2>
      <p class="muted">{{t('Orientación sobre el uso de HotelMatch. No ejecuta reservas.','Guidance on using HotelMatch. It does not make bookings.')}}</p>
      <div class="chat-messages" role="log" aria-live="polite">
        @for(m of messages();track $index){<p [class.chat-user]="m.role==='user'"><strong>{{m.role==='user'?t('Tú','You'):'HotelMatch'}}</strong><br>{{m.text}}</p>}
      </div>
      <form #chatForm="ngForm" (ngSubmit)="send(chatForm)" class="stack">
        <mat-form-field><mat-label>{{t('Tu consulta','Your question')}}</mat-label><input matInput name="message" [(ngModel)]="message" required maxlength="500" [placeholder]="t('¿Cómo cancelo una reserva?','How do I cancel a booking?')"></mat-form-field>
        <button mat-flat-button type="submit" [disabled]="busy()">{{busy()?t('Consultando…','Asking…'):t('Enviar','Send')}}</button>
      </form>
      <small>{{mode()==='AI'?t('Respuesta generada con IA. Verifica los datos en la aplicación.','AI-generated answer. Check the details in the app.'):t('Modo de preguntas frecuentes. La IA requiere configurar un modelo Ollama.','FAQ mode. AI requires a configured Ollama model.')}}</small>
    </mat-card-content>
  </mat-card>
  }
  <button mat-fab extended class="chat-toggle" (click)="open.set(!open())" [attr.aria-expanded]="open()">✦ {{t('Asistente HotelMatch','HotelMatch assistant')}}</button>
</aside>`,
})
export class Chat {
  t = t;
  api = inject(Api);
  open = signal(false); busy = signal(false); mode = signal('FAQ'); message = '';
  messages = signal<{ role: string, text: string }[]>([]);

  async send(form: NgForm) {
    if (this.busy() || !this.message.trim()) return;
    const message = this.message.trim();
    form.resetForm({ message: '' });
    this.messages.update(x => [...x.slice(-18), { role: 'user', text: message }]);
    this.busy.set(true);
    try {
      const r = await this.api.request('POST', '/chat', { message });
      this.mode.set(r.mode);
      this.messages.update(x => [...x, { role: 'assistant', text: r.answer }]);
    } catch {
      this.messages.update(x => [...x, { role: 'assistant', text: t('No se pudo consultar el asistente. Intenta de nuevo.', 'The assistant could not be reached. Try again.') }]);
    } finally {
      this.busy.set(false);
    }
  }
}
