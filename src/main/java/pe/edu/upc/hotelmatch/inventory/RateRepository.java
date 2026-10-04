package pe.edu.upc.hotelmatch.inventory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RateRepository extends JpaRepository<Rate, Long> {

    Optional<Rate> findByIdAndRoomId(Long id, Long roomId);

    @Query("""
            select r from Rate r
            where r.room.id = :roomId and r.startDate <= :to and r.endDate >= :from
            order by r.startDate
            """)
    List<Rate> findOverlapping(@Param("roomId") Long roomId, @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
