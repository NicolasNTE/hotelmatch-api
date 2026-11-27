import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'dist/hotelmatch/browser');
const types={'.html':'text/html; charset=utf-8','.js':'text/javascript; charset=utf-8','.css':'text/css; charset=utf-8','.svg':'image/svg+xml','.png':'image/png','.jpg':'image/jpeg','.webp':'image/webp','.ico':'image/x-icon'};
http.createServer((req,res)=>{
  if(req.url.startsWith('/api/')){
    const proxy=http.request({hostname:'127.0.0.1',port:8080,path:req.url,method:req.method,headers:req.headers},up=>{res.writeHead(up.statusCode,up.headers);up.pipe(res)});
    proxy.on('error',()=>{res.writeHead(502,{'Content-Type':'application/problem+json'});res.end(JSON.stringify({detail:'No se pudo conectar con la API HotelMatch en el puerto 8080.'}));});req.pipe(proxy);return;
  }
  let requested;try{requested=decodeURIComponent(new URL(req.url,'http://localhost').pathname);}catch{res.writeHead(400);res.end();return;}
  let file=path.resolve(root,'.'+requested);if(!file.startsWith(root+path.sep)&&file!==root){res.writeHead(403);res.end();return;}
  if(!fs.existsSync(file)||fs.statSync(file).isDirectory())file=path.join(root,'index.html');
  fs.readFile(file,(err,data)=>{if(err){res.writeHead(404);res.end('Primero ejecuta npm run build.');return;}res.writeHead(200,{'Content-Type':types[path.extname(file)]||'application/octet-stream','Cache-Control':'no-store'});res.end(data);});
}).listen(4200,'127.0.0.1',()=>console.log('HotelMatch: http://127.0.0.1:4200'));
