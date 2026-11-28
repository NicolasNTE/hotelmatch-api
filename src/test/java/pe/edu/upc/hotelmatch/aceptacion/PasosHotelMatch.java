package pe.edu.upc.hotelmatch.aceptacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.spring.CucumberContextConfiguration;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.ResultActions;
import pe.edu.upc.hotelmatch.IntegrationTestSupport;
import pe.edu.upc.hotelmatch.iam.UserRepository;

/**
 * Pasos de las pruebas de aceptación. Cada escenario corresponde a un criterio de aceptación de las historias de
 * usuario y habla con la API como lo haría un cliente. Cada escenario crea su propio hotel en una ciudad única, de
 * modo que el ranking solo contiene sus habitaciones.
 */
@CucumberContextConfiguration
public class PasosHotelMatch extends IntegrationTestSupport {

    private static final String PASSWORD = "Aceptacion-2027";

    @Autowired
    private UserRepository users;

    private final String city = unique("Ciudad");
    private final LocalDate checkIn = today().plusDays(ThreadLocalRandom.current().nextInt(60, 300));
    private final List<Integer> roomIds = new ArrayList<>();
    private ResultActions last;
    private String email;
    private String guest;
    private String admin;
    private int hotelId;
    private int bookingId;
    private String bookingFrom;
    private String bookingTo;

    // ---------- utilidades ----------

    private MockHttpServletResponse response() {
        return last.andReturn().getResponse();
    }

    private <T> T json(String path) throws Exception {
        return JsonPath.read(response().getContentAsString(StandardCharsets.UTF_8), path);
    }

    private String register(String role, Map<String, Object> hotel) throws Exception {
        String mail = unique("acepta") + "@example.test";
        Map<String, Object> body = new HashMap<>(Map.of("name", "Persona de prueba", "email", mail,
                "password", PASSWORD, "role", role));
        if (hotel != null) {
            body.put("hotel", hotel);
        }
        last = postJson("/api/v1/auth/register", null, body);
        assertThat(response().getStatus()).isEqualTo(201);
        return mail;
    }

    private Map<String, Object> room(String name, String price) {
        Map<String, Object> body = new HashMap<>(roomBody(unique("A"), price, 2));
        body.put("name", name);
        return body;
    }

    private int createRoom(Map<String, Object> body) throws Exception {
        last = postJson("/api/v1/rooms", admin, body);
        assertThat(response().getStatus()).isEqualTo(201);
        return json("$.id");
    }

    private Map<String, Object> search(Object budget) {
        return new HashMap<>(Map.of("checkIn", checkIn.toString(), "checkOut", checkIn.plusDays(2).toString(),
                "guests", 2, "maxBudgetPerNight", budget, "purpose", "COUPLE", "priorities", List.of("VIEW"),
                "city", city));
    }

    private void recommend(Map<String, Object> body) throws Exception {
        if (guest == null) {
            guest = login(register("GUEST", null), PASSWORD);
        }
        last = postJson("/api/v1/recommendations", guest, body);
    }

    private List<Map<String, Object>> results() throws Exception {
        assertThat(response().getStatus()).isEqualTo(200);
        return json("$.results");
    }

    private Map<String, Object> resultFor(int roomId) throws Exception {
        recommend(search(5000));
        return results().stream().filter(r -> ((Number) r.get("roomId")).intValue() == roomId).findFirst()
                .orElse(null);
    }

    private static double number(Object value) {
        return ((Number) value).doubleValue();
    }

    private void book(String from, String to) throws Exception {
        bookingFrom = from;
        bookingTo = to;
        last = postJson("/api/v1/bookings", guest,
                Map.of("roomId", roomIds.get(0), "checkIn", from, "checkOut", to, "guests", 2));
        if (response().getStatus() == 201) {
            bookingId = json("$.id");
        }
    }

    // ---------- autenticación (US01 a US03) ----------

    @Cuando("me registro con un correo nuevo")
    public void registroNuevo() throws Exception {
        email = register("GUEST", null);
    }

    @Dado("que ya existe una cuenta")
    public void cuentaExistente() throws Exception {
        email = register("GUEST", null);
    }

    @Entonces("la contraseña queda guardada cifrada")
    public void contrasenaCifrada() {
        String stored = users.findByEmailIgnoreCase(email).orElseThrow().getPasswordHash();
        assertThat(stored).startsWith("$2").doesNotContain(PASSWORD);
    }

    @Cuando("me registro otra vez con el mismo correo")
    public void registroRepetido() throws Exception {
        last = postJson("/api/v1/auth/register", null,
                Map.of("name", "Otra", "email", email, "password", PASSWORD, "role", "GUEST"));
    }

    @Cuando("inicio sesión con la contraseña correcta")
    public void loginCorrecto() throws Exception {
        last = postJson("/api/v1/auth/login", null, Map.of("email", email, "password", PASSWORD));
    }

    @Cuando("inicio sesión con una contraseña incorrecta")
    public void loginIncorrecto() throws Exception {
        last = postJson("/api/v1/auth/login", null, Map.of("email", email, "password", "otra-clave-mala"));
    }

    @Entonces("la respuesta es {int}")
    public void estado(int status) {
        assertThat(response().getStatus()).isEqualTo(status);
    }

    @Entonces("recibo un token con el rol {string}")
    public void tokenConRol(String role) throws Exception {
        assertThat(this.<String>json("$.accessToken")).isNotBlank();
        assertThat(this.<String>json("$.user.role")).isEqualTo(role);
    }

    @Entonces("no recibo ningún token")
    public void sinToken() throws Exception {
        assertThat(response().getContentAsString()).doesNotContain("accessToken");
    }

    @Dado("que soy un administrador con mi propio hotel")
    public void administradorPropio() throws Exception {
        admin = login(register("ADMIN", Map.of("legalName", "Prueba S.A.C.", "tradeName", unique("Hotel"),
                "address", "Av. Prueba 1", "city", city)), PASSWORD);
    }

    @Dado("que soy un huésped")
    public void huesped() throws Exception {
        guest = login(register("GUEST", null), PASSWORD);
    }

    @Cuando("consulto el reporte del hotel")
    public void consultarReporte() throws Exception {
        String token = admin != null ? admin : guest;
        last = getJson("/api/v1/reports?from=" + today() + "&to=" + today().plusDays(30), token);
    }

    // ---------- perfil y ranking (US05 a US14) ----------

    @Dado("un hotel con habitaciones de {int}, {int} y {int} soles por noche")
    public void hotelConHabitaciones(int first, int second, int third) throws Exception {
        administradorPropio();
        for (int price : new int[] {first, second, third}) {
            roomIds.add(createRoom(room("Habitación " + price, price + ".00")));
        }
        hotelId = this.<Integer>json("$.hotelId");
    }

    @Cuando("pido el ranking con un presupuesto de {int} soles")
    public void rankingConPresupuesto(int budget) throws Exception {
        recommend(search(budget));
    }

    @Entonces("ninguna habitación supera {int} soles por noche")
    public void dentroDelPresupuesto(int budget) throws Exception {
        assertThat(results()).isNotEmpty().allSatisfy(r -> assertThat(number(r.get("pricePerNight")))
                .isLessThanOrEqualTo(budget));
    }

    @Cuando("pido el ranking sin motivo de estadía")
    public void rankingSinMotivo() throws Exception {
        Map<String, Object> body = search(500);
        body.remove("purpose");
        recommend(body);
    }

    @Cuando("pido el ranking con el motivo {string}")
    public void rankingConMotivo(String purpose) throws Exception {
        Map<String, Object> body = search(500);
        body.put("purpose", purpose);
        recommend(body);
    }

    @Entonces("el desglose incluye los criterios {string} y {string}")
    public void desgloseIncluye(String first, String second) throws Exception {
        assertThat(results()).isNotEmpty();
        List<String> criteria = json("$.results[0].breakdown[*].criterion");
        assertThat(criteria).contains(first, second);
    }

    @Cuando("pido el ranking con {int} prioridades")
    public void rankingConPrioridades(int count) throws Exception {
        Map<String, Object> body = search(500);
        body.put("priorities", List.of("VIEW", "QUIET", "SPACE", "PRICE").subList(0, count));
        recommend(body);
    }

    @Cuando("pido el ranking marcando dos servicios")
    public void rankingConServicios() throws Exception {
        recommend(search(500));
        List<Integer> services = JsonPath.read(getJson("/api/v1/services", guest).andReturn().getResponse()
                .getContentAsString(), "$[*].id");
        Map<String, Object> body = search(500);
        body.put("amenityIds", services.subList(0, 2));
        recommend(body);
    }

    @Entonces("cada resultado informa cuáles de los dos servicios tiene")
    public void serviciosInformados() throws Exception {
        assertThat(results()).isNotEmpty().allSatisfy(r -> assertThat(
                ((List<?>) r.get("matchedAmenities")).size() + ((List<?>) r.get("missingAmenities")).size())
                .isEqualTo(2));
    }

    @Entonces("recibo {int} habitaciones")
    public void cantidad(int count) throws Exception {
        assertThat(results()).hasSize(count);
    }

    @Cuando("describo mi viaje como {string}")
    public void textoLibre(String text) throws Exception {
        last = postJson("/api/v1/recommendations/interpret", guest, Map.of("text", text));
    }

    @Entonces("se detectan las prioridades {string} y {string}")
    public void prioridadesDetectadas(String first, String second) throws Exception {
        List<String> priorities = json("$.priorities");
        assertThat(priorities).contains(first, second);
    }

    @Entonces("no se detecta ninguna preferencia")
    public void sinPreferencias() throws Exception {
        assertThat(this.<String>json("$.mode")).isEqualTo("NONE");
    }

    @Entonces("cada habitación tiene un puntaje entre 0 y 100")
    public void puntajeEnRango() throws Exception {
        assertThat(results()).hasSize(3).allSatisfy(r -> assertThat(number(r.get("compatibility")))
                .isBetween(0.0, 100.0));
    }

    @Dado("dos habitaciones iguales con precios distintos")
    public void habitacionesGemelas() throws Exception {
        roomIds.add(createRoom(room("Gemela cara", "300.00")));
        roomIds.add(createRoom(room("Gemela barata", "299.00")));
    }

    @Entonces("ante igual puntaje aparece primero la de menor precio")
    public void desempatePorPrecio() throws Exception {
        List<Map<String, Object>> list = results();
        boolean hubo = false;
        for (int i = 1; i < list.size(); i++) {
            if (number(list.get(i - 1).get("compatibility")) == number(list.get(i).get("compatibility"))) {
                hubo = true;
                assertThat(number(list.get(i - 1).get("pricePerNight")))
                        .isLessThanOrEqualTo(number(list.get(i).get("pricePerNight")));
            }
        }
        assertThat(hubo).as("el escenario necesita al menos un empate").isTrue();
    }

    @Entonces("los resultados están ordenados de mayor a menor compatibilidad")
    public void ordenDescendente() throws Exception {
        assertThat(results()).isNotEmpty().extracting(r -> number(r.get("compatibility")))
                .isSortedAccordingTo((a, b) -> Double.compare(b, a));
    }

    @Entonces("no hay resultados y se informa el precio disponible más bajo")
    public void sinResultadosPorPresupuesto() throws Exception {
        assertThat(results()).isEmpty();
        assertThat(this.<String>json("$.hint")).isEqualTo("NONE_WITHIN_BUDGET");
        assertThat(number(json("$.cheapestAvailablePrice"))).isEqualTo(150.0);
    }

    @Entonces("cada resultado trae una explicación y su desglose")
    public void explicacion() throws Exception {
        assertThat(results()).isNotEmpty().allSatisfy(r -> {
            assertThat((String) r.get("explanation")).isNotBlank();
            assertThat((List<?>) r.get("breakdown")).isNotEmpty();
        });
    }

    @Cuando("consulto el clima de un hotel sin coordenadas")
    public void climaSinCoordenadas() throws Exception {
        last = getJson("/api/v1/hotels/" + hotelId + "/weather", guest);
    }

    @Entonces("se informa que el clima no está disponible")
    public void climaNoDisponible() throws Exception {
        assertThat(this.<Boolean>json("$.available")).isFalse();
    }

    // ---------- reservas (US15 y US17) ----------

    @Cuando("reservo la primera habitación dentro de {int} días")
    public void reservar(int days) throws Exception {
        book(today().plusDays(days).toString(), today().plusDays(days + 2).toString());
    }

    @Dado("que reservé la primera habitación dentro de {int} días")
    public void reservaPrevia(int days) throws Exception {
        reservar(days);
        assertThat(response().getStatus()).isEqualTo(201);
    }

    @Entonces("la reserva tiene código y aparece en mi cuenta")
    public void reservaEnMiCuenta() throws Exception {
        String code = json("$.code");
        assertThat(code).startsWith("HM-");
        last = getJson("/api/v1/bookings", guest);
        List<String> codes = json("$.content[*].code");
        assertThat(codes).contains(code);
    }

    @Cuando("cancelo la reserva")
    public void cancelar() throws Exception {
        last = deleteJson("/api/v1/bookings/" + bookingId, guest);
    }

    @Entonces("la reserva queda cancelada")
    public void cancelada() throws Exception {
        assertThat(response().getStatus()).isEqualTo(200);
        assertThat(this.<String>json("$.status")).isEqualTo("CANCELLED");
    }

    @Entonces("la habitación se puede volver a reservar en las mismas fechas")
    public void habitacionLiberada() throws Exception {
        book(bookingFrom, bookingTo);
        assertThat(response().getStatus()).isEqualTo(201);
    }

    // ---------- inventario y reportes (US18 a US22) ----------

    @Cuando("registro una habitación de {int} soles")
    public void registrarHabitacion(int price) throws Exception {
        roomIds.add(createRoom(room("Nueva", price + ".00")));
    }

    @Entonces("aparece en el ranking de mi ciudad")
    public void apareceEnRanking() throws Exception {
        assertThat(resultFor(roomIds.get(roomIds.size() - 1))).isNotNull();
    }

    @Cuando("registro una habitación sin nombre")
    public void registrarSinNombre() throws Exception {
        last = postJson("/api/v1/rooms", admin, room("", "180.00"));
    }

    @Cuando("cambio el precio de la primera habitación a {int} soles")
    public void cambiarPrecio(int price) throws Exception {
        last = putJson("/api/v1/rooms/" + roomIds.get(0), admin, room("Editada", price + ".00"));
        assertThat(response().getStatus()).isEqualTo(200);
    }

    @Entonces("el ranking muestra esa habitación a {int} soles")
    public void precioEnRanking(int price) throws Exception {
        assertThat(number(resultFor(roomIds.get(0)).get("pricePerNight"))).isEqualTo(price);
    }

    @Dado("que un huésped reservó la primera habitación")
    public void huespedReservo() throws Exception {
        huesped();
        reservaPrevia(30);
    }

    @Cuando("elimino la primera habitación")
    public void eliminar() throws Exception {
        last = deleteJson("/api/v1/rooms/" + roomIds.get(0) + "?confirm=true", admin);
        assertThat(response().getStatus()).isBetween(200, 204);
    }

    @Entonces("deja de aparecer en el ranking")
    public void yaNoAparece() throws Exception {
        assertThat(resultFor(roomIds.get(0))).isNull();
    }

    @Entonces("la reserva histórica se conserva")
    public void reservaConservada() throws Exception {
        last = getJson("/api/v1/bookings/" + bookingId, guest);
        assertThat(response().getStatus()).isEqualTo(200);
    }

    private void createRate(LocalDate from, LocalDate to, String price) throws Exception {
        last = send(post("/api/v1/rooms/" + roomIds.get(0) + "/rates"), admin,
                Map.of("name", "Temporada", "startDate", from.toString(), "endDate", to.toString(), "price", price));
    }

    @Cuando("registro una tarifa de {int} soles para mis fechas")
    public void tarifa(int price) throws Exception {
        createRate(checkIn, checkIn.plusDays(2), price + ".00");
        assertThat(response().getStatus()).isEqualTo(201);
    }

    @Entonces("el ranking usa {int} soles para la primera habitación")
    public void rankingUsaTarifa(int price) throws Exception {
        precioEnRanking(price);
    }

    @Cuando("registro una tarifa que termina antes de empezar")
    public void tarifaInvalida() throws Exception {
        createRate(checkIn.plusDays(3), checkIn, "200.00");
    }

    @Cuando("exporto el reporte del periodo")
    public void exportar() throws Exception {
        last = getJson("/api/v1/reports/pdf?from=" + today() + "&to=" + today().plusDays(30), admin);
    }

    @Entonces("descargo un archivo PDF")
    public void esPdf() throws Exception {
        assertThat(response().getStatus()).isEqualTo(200);
        assertThat(response().getContentType()).startsWith("application/pdf");
        assertThat(new String(response().getContentAsByteArray(), 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }
}
