import { Component, inject, signal, provideZonelessChangeDetection } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';
import { HttpClient, HttpInterceptorFn, provideHttpClient, withInterceptors } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { firstValueFrom } from 'rxjs';
import { Admin } from './admin';
import { Chat } from './chat';
import { lang, t, label, toggleLanguage } from './i18n';

const API = (window as any).HOTELMATCH_API || '/api/v1';
const auth: HttpInterceptorFn = (req, next) => {
  const token = sessionStorage.getItem('hm-token');
  return next(req.url.startsWith(API + '/') ? req.clone({setHeaders: {
    ...(token ? {Authorization: 'Bearer ' + token} : {}), 'Accept-Language': sessionStorage.getItem('hm-lang') || 'es-419'
  }}) : req);
};
@Component({selector:'hm-app', standalone:true, imports:[CommonModule, FormsModule, MatButtonModule, Admin, Chat], templateUrl:'./app.html'})
class App {
  http = inject(HttpClient); user = signal<any>(null); results = signal<any[]>([]); bookings = signal<any[]>([]);
  weather=signal<any>(null); detail = signal<any>(null); report = signal<any>(null); busy = signal(false); message = signal('');
  lang = lang; t = t; label = label; page = signal('search'); register = false; showAuth = false;
  email=''; password=''; name=''; role='GUEST'; step=signal(1); amenities=signal<any[]>([]); selectedAmenities:number[]=[];
  hotel:any={legalName:'',tradeName:'',address:'',city:'',district:'',latitude:null,longitude:null,sustainabilitySeal:false,sustainabilityDescription:''};
  profile:any={name:'',email:'',currentPassword:'',newPassword:''}; bookingPage=signal(0); bookingPages=signal(0); bookingStatus=''; bookingFrom=''; bookingTo='';
  togglePriority(key:string){const selected=this.travel.priorities;if(selected.includes(key))this.travel.priorities=selected.filter(p=>p!==key);else if(selected.length<3)this.travel.priorities=[...selected,key];}
  toggleAmenity(id:number){this.selectedAmenities=this.selectedAmenities.includes(id)?this.selectedAmenities.filter(x=>x!==id):[...this.selectedAmenities,id];}
  next(){if(!this.travel.checkIn||!this.travel.checkOut||this.travel.checkOut<=this.travel.checkIn||this.travel.guests<1||this.travel.maxBudgetPerNight<=0){this.message.set(this.t('Revisa las fechas, los huéspedes y el presupuesto.','Check dates, guests and budget.'));return;}this.step.update(x=>Math.min(3,x+1));this.message.set('');}
  async catalog(){this.amenities.set(await this.get('/services'));}
  openProfile(){this.profile={name:this.user().name,email:this.user().email,currentPassword:'',newPassword:''};this.hotel={...this.user().hotel};this.page.set('profile');}
  saveProfile(){this.run(async()=>{const body:any={name:this.profile.name,email:this.profile.email};if(this.profile.newPassword){body.currentPassword=this.profile.currentPassword;body.newPassword=this.profile.newPassword;}this.user.set(await firstValueFrom(this.http.put(API+'/users/me',body)));this.profile.currentPassword='';this.profile.newPassword='';this.message.set(this.t('Perfil guardado.','Profile saved.'));});}
  saveHotel(){this.run(async()=>{await firstValueFrom(this.http.put(API+'/hotels/'+this.user().hotel.id,this.hotel));this.user.set(await this.get('/users/me'));this.message.set(this.t('Hotel actualizado.','Hotel updated.'));});}
  deactivateHotel(){if(!confirm(this.t('¿Dar de baja el hotel? Sus habitaciones dejarán de aparecer en las búsquedas.','Deactivate the hotel? Its rooms will be removed from searches.')))return;this.run(async()=>{await firstValueFrom(this.http.delete(API+'/hotels/'+this.user().hotel.id+'?confirm=true'));this.user.set(await this.get('/users/me'));});}
  deactivateAccount(){if(!confirm(this.t('¿Dar de baja tu cuenta? Se conservarán los registros históricos.','Deactivate your account? Historical records are preserved.')))return;this.run(async()=>{await firstValueFrom(this.http.delete(API+'/users/me'));this.logout();});}
  changeStatus(id:number,status:string){this.run(async()=>{await firstValueFrom(this.http.put(API+'/bookings/'+id+'/status',{status}));await this.fetchBookings();});}
  async fetchBookings(){const q=new URLSearchParams({page:String(this.bookingPage()),size:'10'});if(this.bookingStatus)q.set('status',this.bookingStatus);if(this.bookingFrom)q.set('from',this.bookingFrom);if(this.bookingTo)q.set('to',this.bookingTo);const r=await this.get('/bookings?'+q);this.bookings.set(r.content);this.bookingPages.set(r.totalPages);}
  turnBookings(delta:number){this.bookingPage.update(x=>x+delta);this.run(()=>this.fetchBookings());}
  travel = {checkIn:'',checkOut:'',guests:1,maxBudgetPerNight:350,purpose:'TOURISM',priorities:['QUIET'],city:'',freeText:''};
  purposes=['BUSINESS','TOURISM','COUPLE','FAMILY','EVENT','REST','OTHER'];
  priorities=['PRIVACY','PRICE','COMFORT','SPACE','QUIET','VIEW','ENTERTAINMENT','WORK','LUXURY'];
  from=''; to=''; adminResource='rooms'; adminId=''; adminRoomId=''; adminJson=''; adminRows = signal<any>(null);
  constructor() {const today=new Date();const local=(d:Date)=>new Date(d.getTime()-d.getTimezoneOffset()*60000).toISOString().slice(0,10);this.from=local(new Date(today.getFullYear(),today.getMonth(),1));this.to=local(new Date(today.getFullYear(),today.getMonth()+1,1));if(sessionStorage.getItem('hm-token')) this.run(async()=>{this.user.set(await this.get('/users/me'));await this.catalog();});}
  previousStep=(n:number)=>Math.max(1,n-1);
  language() { toggleLanguage(); }
  get(path:string) {return firstValueFrom(this.http.get<any>(API+path));}
  async run(work:()=>Promise<void>) { if(this.busy())return; this.busy.set(true);this.message.set('');try{await work();}catch(e:any){
    if(e.status===401)this.logout();
    this.message.set(e.status===409?this.t('La habitación ya no está disponible. Busca otra opción.','This room is no longer available. Search again.'):
      e.error?.detail || e.message || this.t('No se pudo completar la operación.','The operation failed.'));
  }finally{this.busy.set(false);}}
  login() {this.run(async()=>{const body=this.register?{name:this.name,email:this.email,password:this.password,role:this.role,...(this.role==='ADMIN'?{hotel:this.hotel}:{})}:{email:this.email,password:this.password};
    if(this.register)await firstValueFrom(this.http.post(API+'/auth/register',body));
    const r:any=await firstValueFrom(this.http.post(API+'/auth/login',{email:this.email,password:this.password}));this.register=false;sessionStorage.setItem('hm-token',r.accessToken);this.user.set(r.user);this.showAuth=false;this.password='';await this.catalog();});}
  logout(){sessionStorage.removeItem('hm-token');this.user.set(null);this.results.set([]);this.bookings.set([]);this.detail.set(null);this.report.set(null);this.adminRows.set(null);}
  suggestion=signal('');
  interpret(){const text=this.travel.freeText.trim();if(!text){this.suggestion.set(t('Escribe algo sobre tu viaje primero.','Write something about your trip first.'));return;}
    this.run(async()=>{this.suggestion.set(t('Analizando tu texto…','Reading your text…'));const r:any=await firstValueFrom(this.http.post(API+'/recommendations/interpret',{text}));
    if(r.mode==='NONE'){this.suggestion.set(t('No encontré preferencias claras; elígelas en el siguiente paso.','No clear preferences found; pick them in the next step.'));return;}
    if(r.priorities.length)this.travel.priorities=r.priorities;if(r.purpose)this.travel.purpose=r.purpose;
    const source=r.mode==='AI'?t('Sugerencia generada con IA','AI-generated suggestion'):t('Sugerencia por palabras clave','Keyword-based suggestion');
    this.suggestion.set(source+': '+[r.purpose,...r.priorities].filter(Boolean).map((k:string)=>label(k)).join(', ')+'. '+t('Puedes cambiarla.','You can change it.'));});}
  search(){this.run(async()=>{if(this.travel.priorities.length<1||this.travel.priorities.length>3)throw Error(this.t('Elige entre 1 y 3 prioridades.','Choose 1 to 3 priorities.'));
    const r:any=await firstValueFrom(this.http.post(API+'/recommendations',{...this.travel,amenityIds:this.selectedAmenities}));this.results.set(r.results);this.step.set(4);this.detail.set(null);if(!r.results.length)this.message.set(this.t('Sin opciones: cambia fechas o presupuesto.','No matches: change dates or budget.'));});}
  show(id:number){this.run(async()=>{const room=await this.get('/rooms/'+id);this.detail.set({...room,hotel:await this.get('/hotels/'+room.hotelId),match:this.results().find(r=>r.roomId===id)});this.weather.set(null);try{this.weather.set(await this.get('/hotels/'+room.hotelId+'/weather'));}catch{this.weather.set({available:false});}});}
  reserve(id:number){this.run(async()=>{const r:any=await firstValueFrom(this.http.post(API+'/bookings',{roomId:id,checkIn:this.travel.checkIn,checkOut:this.travel.checkOut,guests:this.travel.guests}));this.message.set(this.t('Reserva confirmada: ','Booking confirmed: ')+r.code);this.results.set([]);this.detail.set(null);});}
  loadBookings(){this.page.set('bookings');this.bookingPage.set(0);this.run(()=>this.fetchBookings());}
  cancel(id:number){if(!confirm(this.t('¿Cancelar esta reserva?','Cancel this booking?')))return;this.run(async()=>{await firstValueFrom(this.http.delete(API+'/bookings/'+id));await this.fetchBookings();});}
  loadReport(){this.run(async()=>this.report.set(await this.get('/reports?from='+this.from+'&to='+this.to)));}
  download(){this.run(async()=>{const b=await firstValueFrom(this.http.get(API+'/reports/pdf?from='+this.from+'&to='+this.to,{responseType:'blob'}));const u=URL.createObjectURL(b);const a=document.createElement('a');a.href=u;a.download='hotelmatch-report.pdf';a.click();setTimeout(()=>URL.revokeObjectURL(u),1000);});}
  adminPath(){if(['availability','rates'].includes(this.adminResource)){if(!/^\d+$/.test(this.adminRoomId))throw Error(t('Indica el ID de la habitación.','Room ID required.'));return '/rooms/'+this.adminRoomId+'/'+this.adminResource;}return '/'+this.adminResource;}
  adminAction(method:string){this.run(async()=>{let path=this.adminPath();if(this.adminId)path+='/'+encodeURIComponent(this.adminId);
    if(method==='DELETE'){if(!this.adminId)throw Error('ID required');if(!confirm(this.t('¿Eliminar este registro?','Delete this record?')))return;if(this.adminResource==='rooms')path+='?confirm=true';}
    if(method==='PUT'&&!this.adminId)throw Error('ID required');
    if(method==='GET'&&this.adminResource==='availability')path+='?from='+this.from+'&to='+this.to;
    const body=['POST','PUT'].includes(method)?JSON.parse(this.adminJson):undefined;
    const r=await firstValueFrom(this.http.request(method,API+path,{body}));this.adminRows.set(r);this.message.set(this.t('Operación completada.','Operation complete.'));});}
}
bootstrapApplication(App,{providers:[provideZonelessChangeDetection(),provideHttpClient(withInterceptors([auth]))]}).catch(console.error);
