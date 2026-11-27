package pe.edu.upc.hotelmatch.inventory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    Optional<Availability> findByRoomIdAndDate(Long roomId, LocalDate date);

    List<Availability> findByRoomIdAndDateBetweenOrderByDate(Long roomId, LocalDate from, LocalDate to);

    boolean existsByRoomIdAndDateBetween(Long roomId, LocalDate from, LocalDate to);

    @Query("""
            select count(a) > 0 from Availability a
            where a.room.id = :roomId and a.status = :status and a.date >= :from and a.date < :toExclusive
            """)
    boolean existsInRange(@Param("roomId") Long roomId, @Param("status") AvailabilityStatus status,
            @Param("from") LocalDate from, @Param("toExclusive") LocalDate toExclusive);

    @Query("""
            select distinct a.room.id from Availability a
            where a.status = :status and a.date >= :from and a.date < :toExclusive
            """)
    Set<Long> findRoomIdsWithStatusInRange(@Param("status") AvailabilityStatus status,
            @Param("from") LocalDate from, @Param("toExclusive") LocalDate toExclusive);
}
