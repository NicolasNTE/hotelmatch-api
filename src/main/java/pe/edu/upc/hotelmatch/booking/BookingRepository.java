package pe.edu.upc.hotelmatch.booking;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    boolean existsByCode(String code);

    @Query("""
            select count(b) > 0 from Booking b
            where b.room.id = :roomId and b.status in :statuses
              and b.checkIn < :checkOut and b.checkOut > :checkIn
            """)
    boolean existsOverlap(@Param("roomId") Long roomId, @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut, @Param("statuses") Collection<BookingStatus> statuses);

    @Query("""
            select b from Booking b
            where b.room.id = :roomId and b.status in :statuses
              and b.checkIn < :checkOut and b.checkOut > :checkIn
            """)
    List<Booking> findOverlapping(@Param("roomId") Long roomId, @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut, @Param("statuses") Collection<BookingStatus> statuses);

    @Query("""
            select distinct b.room.id from Booking b
            where b.status in :statuses and b.checkIn < :checkOut and b.checkOut > :checkIn
            """)
    Set<Long> findBookedRoomIds(@Param("checkIn") LocalDate checkIn, @Param("checkOut") LocalDate checkOut,
            @Param("statuses") Collection<BookingStatus> statuses);
}
