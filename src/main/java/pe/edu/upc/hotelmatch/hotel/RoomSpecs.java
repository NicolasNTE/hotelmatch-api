package pe.edu.upc.hotelmatch.hotel;

import java.util.Collection;
import org.springframework.data.jpa.domain.Specification;

final class RoomSpecs {

    private RoomSpecs() {
    }

    static Specification<Room> all() {
        return (root, query, cb) -> cb.conjunction();
    }

    static Specification<Room> hotelIs(Long hotelId) {
        return (root, query, cb) -> cb.equal(root.get("hotel").get("id"), hotelId);
    }

    static Specification<Room> hotelActive() {
        return (root, query, cb) -> cb.isTrue(root.get("hotel").get("active"));
    }

    static Specification<Room> statusIs(RoomStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    static Specification<Room> statusNot(RoomStatus status) {
        return (root, query, cb) -> cb.notEqual(root.get("status"), status);
    }

    static Specification<Room> typeIs(RoomType type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    static Specification<Room> nameContains(String text) {
        String pattern = "%" + text.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern);
    }

    static Specification<Room> capacityAtLeast(int guests) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("capacity"), guests);
    }

    static Specification<Room> idNotIn(Collection<Long> ids) {
        return (root, query, cb) -> cb.not(root.get("id").in(ids));
    }
}
