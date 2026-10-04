package pe.edu.upc.hotelmatch.hotel;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.edu.upc.hotelmatch.IntegrationTestSupport;

class RoomCrudIntegrationTest extends IntegrationTestSupport {

    private int createRoom(String token, String number) throws Exception {
        return read(postJson("/api/v1/rooms", token, roomBody(number, "210.00", 2))
                .andExpect(status().isCreated()), "$.id");
    }

    @Test
    @DisplayName("US18: solo el administrador crea habitaciones y se valida precio, capacidad y número único")
    void createsRoomWithValidations() throws Exception {
        String admin = adminToken();
        String number = unique("N");

        postJson("/api/v1/rooms", guestToken(), roomBody(number, "210.00", 2)).andExpect(status().isForbidden());
        postJson("/api/v1/rooms", null, roomBody(number, "210.00", 2)).andExpect(status().isUnauthorized());

        postJson("/api/v1/rooms", admin, roomBody(number, "210.00", 2))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.experience.privacy").value(4));

        postJson("/api/v1/rooms", admin, roomBody(number, "210.00", 2))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("room.numberTaken"));
        postJson("/api/v1/rooms", admin, roomBody(unique("N"), "0", 2)).andExpect(status().isBadRequest());
        postJson("/api/v1/rooms", admin, roomBody(unique("N"), "100", 0)).andExpect(status().isBadRequest());

        Map<String, Object> outOfScale = new HashMap<>(roomBody(unique("N"), "100", 2));
        outOfScale.put("experience", Map.of("privacy", 6, "quiet", 4, "view", 3, "space", 3));
        postJson("/api/v1/rooms", admin, outOfScale).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Lista con búsqueda, filtro por tipo y paginación; el huésped solo ve habitaciones activas")
    void listsWithFiltersAndPagination() throws Exception {
        String admin = adminToken();
        String number = unique("L");
        int id = createRoom(admin, number);

        getJson("/api/v1/rooms?q=" + number + "&type=SUPERIOR", admin)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].number").value(number));
        getJson("/api/v1/rooms?size=3&page=0&sort=name", admin)
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.size").value(3));
        getJson("/api/v1/rooms?q=" + number + "&type=SUITE", admin)
                .andExpect(jsonPath("$.totalElements").value(0));

        putJson("/api/v1/rooms/" + id, admin, withStatus(roomBody(number, "210.00", 2), "INACTIVE"))
                .andExpect(status().isOk());
        getJson("/api/v1/rooms?q=" + number, guestToken()).andExpect(jsonPath("$.totalElements").value(0));
        getJson("/api/v1/rooms/" + id, guestToken()).andExpect(status().isNotFound());
        getJson("/api/v1/rooms?status=INACTIVE&q=" + number, admin)
                .andExpect(jsonPath("$.content[*].status", everyItem(is("INACTIVE"))));
    }

    @Test
    @DisplayName("US19: actualiza la habitación y la elimina de forma lógica solo con confirmación explícita")
    void updatesAndLogicallyDeletes() throws Exception {
        String admin = adminToken();
        String number = unique("U");
        int id = createRoom(admin, number);

        Map<String, Object> changed = new HashMap<>(roomBody(number, "275.50", 3));
        changed.put("name", "Nombre editado");
        putJson("/api/v1/rooms/" + id, admin, changed)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nombre editado"))
                .andExpect(jsonPath("$.basePrice").value(275.50));
        putJson("/api/v1/rooms/" + id, guestToken(), changed).andExpect(status().isForbidden());

        deleteJson("/api/v1/rooms/" + id, admin).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("confirm.required"));
        deleteJson("/api/v1/rooms/" + id + "?confirm=true", admin).andExpect(status().isNoContent());

        getJson("/api/v1/rooms/" + id, admin).andExpect(status().isNotFound());
        postJson("/api/v1/rooms", admin, roomBody(number, "210.00", 2)).andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Un administrador de otro hotel no puede modificar habitaciones ajenas")
    void otherHotelAdminCannotModify() throws Exception {
        int id = createRoom(adminToken(), unique("O"));
        String email = unique("otro.") + "@example.com";
        Map<String, Object> hotel = Map.of("legalName", "Otro Hotel S.A.C.", "tradeName", "Otro Hotel",
                "address", "Jr. Ayacucho 50", "city", "Arequipa");
        postJson("/api/v1/auth/register", null, Map.of("name", "Otro Admin", "email", email,
                "password", "Contraseña123", "role", "ADMIN", "hotel", hotel)).andExpect(status().isCreated());
        String other = login(email, "Contraseña123");

        putJson("/api/v1/rooms/" + id, other, roomBody(unique("X"), "100", 2)).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("room.forbidden"));
        deleteJson("/api/v1/rooms/" + id + "?confirm=true", other).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Catálogo y servicios de habitación: crear, asociar, evitar duplicados y proteger el retiro")
    void managesAmenities() throws Exception {
        String admin = adminToken();
        String name = unique("Servicio ");
        int amenityId = read(postJson("/api/v1/services", admin, Map.of("name", name, "category", "WELLNESS"))
                .andExpect(status().isCreated()), "$.id");
        postJson("/api/v1/services", admin, Map.of("name", name.toUpperCase(), "category", "WELLNESS"))
                .andExpect(status().isConflict());
        postJson("/api/v1/services", guestToken(), Map.of("name", unique("Otro "), "category", "FOOD"))
                .andExpect(status().isForbidden());
        getJson("/api/v1/services", guestToken()).andExpect(status().isOk());

        int roomId = createRoom(admin, unique("S"));
        postJson("/api/v1/rooms/" + roomId + "/services", admin,
                Map.of("amenityId", amenityId, "detail", "Para dos personas"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.name").value(name));
        postJson("/api/v1/rooms/" + roomId + "/services", admin, Map.of("amenityId", amenityId))
                .andExpect(status().isConflict());
        putJson("/api/v1/rooms/" + roomId + "/services/" + amenityId, admin, Map.of("detail", "Nuevo detalle"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.detail").value("Nuevo detalle"));
        getJson("/api/v1/rooms/" + roomId + "/services", admin).andExpect(jsonPath("$", hasSize(1)));

        deleteJson("/api/v1/services/" + amenityId, admin).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("amenity.inUse"));

        deleteJson("/api/v1/rooms/" + roomId + "/services/" + amenityId, admin).andExpect(status().isNoContent());
        getJson("/api/v1/rooms/" + roomId + "/services", admin).andExpect(jsonPath("$", hasSize(0)));
        deleteJson("/api/v1/services/" + amenityId, admin).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("US20: disponibilidad por rango (bloqueo, consulta día a día, cambio y retiro)")
    void managesAvailability() throws Exception {
        String admin = adminToken();
        int roomId = createRoom(admin, unique("A"));
        String from = today().plusDays(10).toString();
        String to = today().plusDays(12).toString();
        String url = "/api/v1/rooms/" + roomId + "/availability";

        postJson(url, admin, Map.of("startDate", from, "endDate", to, "status", "BLOCKED"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$", hasSize(3)));
        postJson(url, admin, Map.of("startDate", to, "endDate", to, "status", "BLOCKED"))
                .andExpect(status().isConflict());
        postJson(url, admin, Map.of("startDate", from, "endDate", from, "status", "BOOKED"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("availability.invalidStatus"));
        postJson(url, admin, Map.of("startDate", to, "endDate", from, "status", "BLOCKED"))
                .andExpect(status().isUnprocessableEntity());

        getJson(url + "?from=" + from + "&to=" + today().plusDays(13), guestToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].status").value("BLOCKED"))
                .andExpect(jsonPath("$[3].status").value("AVAILABLE"));

        putJson(url + "/" + from, admin, Map.of("status", "AVAILABLE")).andExpect(status().isOk());
        deleteJson(url + "/" + to, admin).andExpect(status().isNoContent());
        deleteJson(url + "/" + to, admin).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("US20: tarifas por fecha sin cruces, con actualización y eliminación")
    void managesRates() throws Exception {
        String admin = adminToken();
        int roomId = createRoom(admin, unique("T"));
        String url = "/api/v1/rooms/" + roomId + "/rates";
        String start = today().plusDays(20).toString();
        String end = today().plusDays(25).toString();

        int rateId = read(postJson(url, admin, Map.of("name", "Feriado", "startDate", start, "endDate", end,
                "price", "300.00")).andExpect(status().isCreated()), "$.id");
        postJson(url, admin, Map.of("name", "Cruce", "startDate", end, "endDate", today().plusDays(30).toString(),
                "price", "310.00")).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("rate.overlap"));
        postJson(url, admin, Map.of("name", "Mala", "startDate", end, "endDate", start, "price", "100.00"))
                .andExpect(status().isUnprocessableEntity());

        getJson(url + "?from=" + start + "&to=" + start, guestToken()).andExpect(jsonPath("$", hasSize(1)));
        putJson(url + "/" + rateId, admin, Map.of("name", "Feriado largo", "startDate", start, "endDate", end,
                "price", "320.00")).andExpect(status().isOk()).andExpect(jsonPath("$.price").value(320.00));

        deleteJson(url + "/" + rateId, admin).andExpect(status().isNoContent());
        getJson(url, admin).andExpect(jsonPath("$", hasSize(0)));
    }

    private Map<String, Object> withStatus(Map<String, Object> body, String status) {
        Map<String, Object> copy = new HashMap<>(body);
        copy.put("status", status);
        return copy;
    }
}
