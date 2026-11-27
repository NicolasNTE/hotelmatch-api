-- Regla anti doble reserva garantizada por la base de datos, además del bloqueo de fila del servicio:
-- dos reservas no canceladas de una misma habitación no pueden compartir noches. El rango excluye el día de salida.
create extension if not exists btree_gist;

alter table bookings
    add constraint bookings_sin_solape
    exclude using gist (room_id with =, daterange(check_in, check_out) with &&)
    where (status <> 'CANCELLED');
