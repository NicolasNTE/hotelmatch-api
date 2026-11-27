package pe.edu.upc.hotelmatch.hotel;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomAmenityRepository extends JpaRepository<RoomAmenity, Long> {

    boolean existsByAmenityId(Long amenityId);

    Optional<RoomAmenity> findByRoomIdAndAmenityId(Long roomId, Long amenityId);
}
