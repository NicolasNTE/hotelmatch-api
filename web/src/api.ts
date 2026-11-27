import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { t } from './i18n';
export const API = (window as any).HOTELMATCH_API || '/api/v1';
@Injectable({providedIn:'root'})
export class Api {
  private http=inject(HttpClient);
  busy=signal(false); message=signal('');
  request<T=any>(method:string,path:string,body?:unknown){return firstValueFrom(this.http.request<T>(method,API+path,{body}));}
  async run(task:()=>Promise<void>){if(this.busy())return;this.busy.set(true);this.message.set('');try{await task();}catch(e:any){this.message.set(e.status===409?t('El registro cambió o ya existe. Actualiza e intenta de nuevo.','The record changed or already exists. Refresh and try again.'):e.error?.detail || e.message || t('No se pudo completar la operación.','The operation failed.'));}finally{this.busy.set(false);}}
}
