import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { Api } from './api';
import { t, label } from './i18n';

@Component({selector:'hm-admin',standalone:true,imports:[CommonModule,FormsModule,MatButtonModule],templateUrl:'./admin.html'})
export class Admin {
  api=inject(Api); tab=signal('rooms'); rooms=signal<any[]>([]); services=signal<any[]>([]); rates=signal<any[]>([]); days=signal<any[]>([]);
  editing=signal(false); roomId:number|null=null; page=signal(0); totalPages=signal(0); q=''; type=''; status='';
  room:any=this.emptyRoom(); service:any={name:'',category:'COMFORT',icon:''}; serviceId:number|null=null;
  selectedRoom=''; from=''; to=''; availabilityStatus='BLOCKED';
  rate:any={name:'',startDate:'',endDate:'',price:100}; rateId:number|null=null;
  types=['STANDARD','SUPERIOR','DELUXE','SUITE','EXECUTIVE_SUITE']; beds=['SINGLE','TWIN','DOUBLE','QUEEN','KING'];
  categories=['COMFORT','WELLNESS','ENTERTAINMENT','WORK','FOOD','TRANSPORT'];
  t=t; label=label;
  constructor(){this.api.run(async()=>{await this.load();await this.loadServices();});}
  emptyRoom(){return {name:'',number:'',floor:1,type:'STANDARD',capacity:2,bedType:'QUEEN',basePrice:150,description:'',imageUrl:'',status:'ACTIVE',amenityIds:[],experience:{privacy:3,quiet:3,view:3,space:3}};}
  async load(){const query=new URLSearchParams({page:String(this.page()),size:'10',q:this.q});if(this.type)query.set('type',this.type);if(this.status)query.set('status',this.status);const r=await this.api.request('GET','/rooms?'+query);this.rooms.set(r.content);this.totalPages.set(r.totalPages);}
  async loadServices(){this.services.set(await this.api.request('GET','/services'));}
  refresh(){this.page.set(0);this.api.run(()=>this.load());}
  paginate(delta:number){this.page.update(p=>p+delta);this.api.run(()=>this.load());}
  edit(r?:any){this.roomId=r?.id??null;this.room=r?{...r,experience:{...r.experience},amenityIds:r.amenities.map((a:any)=>a.amenityId)}:this.emptyRoom();this.editing.set(true);}
  toggle(id:number){this.room.amenityIds=this.room.amenityIds.includes(id)?this.room.amenityIds.filter((x:number)=>x!==id):[...this.room.amenityIds,id];}
  saveRoom(){this.api.run(async()=>{await this.api.request(this.roomId?'PUT':'POST','/rooms'+(this.roomId?'/'+this.roomId:''),this.room);this.editing.set(false);await this.load();this.api.message.set(t('Habitación guardada.','Room saved.'));});}
  removeRoom(r:any){if(!confirm(t('¿Retirar ','Remove ')+r.name+t('? Se conservarán las reservas históricas.','? Historical bookings are kept.')))return;this.api.run(async()=>{await this.api.request('DELETE','/rooms/'+r.id+'?confirm=true');await this.load();});}
  editService(s:any){this.serviceId=s.id;this.service={name:s.name,category:s.category,icon:s.icon||''};}
  saveService(){this.api.run(async()=>{await this.api.request(this.serviceId?'PUT':'POST','/services'+(this.serviceId?'/'+this.serviceId:''),this.service);this.resetService();await this.loadServices();});}
  resetService(){this.serviceId=null;this.service={name:'',category:'COMFORT',icon:''};}
  removeService(s:any){if(!confirm(t('¿Retirar el servicio ','Remove the amenity ')+s.name+t('? No debe estar asociado a habitaciones.','? It must not be linked to any room.')))return;this.api.run(async()=>{await this.api.request('DELETE','/services/'+s.id);await this.loadServices();});}
  path(part:string){if(!this.selectedRoom)throw Error(t('Selecciona una habitación.','Select a room.'));return '/rooms/'+this.selectedRoom+'/'+part;}
  loadCalendar(){this.api.run(async()=>{if(!this.from||!this.to)throw Error(t('Selecciona ambas fechas.','Select both dates.'));this.days.set(await this.api.request('GET',this.path('availability')+'?from='+this.from+'&to='+this.to));});}
  saveAvailability(){this.api.run(async()=>{await this.api.request('POST',this.path('availability'),{startDate:this.from,endDate:this.to,status:this.availabilityStatus});this.days.set(await this.api.request('GET',this.path('availability')+'?from='+this.from+'&to='+this.to));});}
  dayAction(date:string,remove=false){this.api.run(async()=>{await this.api.request(remove?'DELETE':'PUT',this.path('availability')+'/'+date,remove?undefined:{status:this.availabilityStatus});this.days.set(await this.api.request('GET',this.path('availability')+'?from='+this.from+'&to='+this.to));});}
  loadRates(){this.api.run(async()=>this.rates.set(await this.api.request('GET',this.path('rates'))));}
  editRate(r:any){this.rateId=r.id;this.rate={name:r.name,startDate:r.startDate,endDate:r.endDate,price:r.price};}
  saveRate(){this.api.run(async()=>{await this.api.request(this.rateId?'PUT':'POST',this.path('rates')+(this.rateId?'/'+this.rateId:''),this.rate);this.rateId=null;this.rate={name:'',startDate:'',endDate:'',price:100};this.rates.set(await this.api.request('GET',this.path('rates')));});}
  removeRate(id:number){if(!confirm(t('¿Eliminar esta tarifa y restituir el precio base?','Delete this rate and restore the base price?')))return;this.api.run(async()=>{await this.api.request('DELETE',this.path('rates')+'/'+id);this.rates.set(await this.api.request('GET',this.path('rates')));});}
}
