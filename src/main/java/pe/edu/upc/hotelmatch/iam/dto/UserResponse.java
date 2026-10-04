package pe.edu.upc.hotelmatch.iam.dto;

import pe.edu.upc.hotelmatch.hotel.dto.HotelResponse;
import pe.edu.upc.hotelmatch.iam.Role;
import pe.edu.upc.hotelmatch.iam.User;

public record UserResponse(Long id, String name, String email, Role role, HotelResponse hotel) {

    public static UserResponse from(User user) {
        HotelResponse hotel = user.getHotel() != null ? HotelResponse.full(user.getHotel()) : null;
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), hotel);
    }
}
