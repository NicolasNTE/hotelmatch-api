package pe.edu.upc.hotelmatch.hotel;

import java.time.LocalDate;

public record RoomFilter(String q, RoomType type, RoomStatus status, LocalDate checkIn, LocalDate checkOut,
                         Integer guests) {
}
