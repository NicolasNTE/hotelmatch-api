package pe.edu.upc.hotelmatch.hotel;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {

    boolean existsByHotelIdAndNumberIgnoreCaseAndStatusNot(Long hotelId, String number, RoomStatus status);

    boolean existsByHotelIdAndNumberIgnoreCaseAndStatusNotAndIdNot(Long hotelId, String number, RoomStatus status,
            Long id);

    /** Bloquea la fila de la habitación para serializar las reservas concurrentes sobre ella. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r where r.id = :id")
    Optional<Room> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select distinct r from Room r
              join fetch r.hotel h
              left join fetch r.amenities ra
              left join fetch ra.amenity
            where r.status = :status and h.active = true and r.capacity >= :guests
            """)
    List<Room> findBookableCandidates(@Param("status") RoomStatus status, @Param("guests") int guests);
}
