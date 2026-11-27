package pe.edu.upc.hotelmatch.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** Aplica las migraciones sobre un PostgreSQL real, valida el esquema contra las entidades y prueba la restricción. */
@SpringBootTest(properties = {"hotelmatch.seed-demo-data=true", "spring.jpa.hibernate.ddl-auto=validate"})
class PostgresMigrationTest {

    private static EmbeddedPostgres postgres;

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) throws IOException {
        postgres = EmbeddedPostgres.start();
        registry.add("spring.datasource.url", () -> postgres.getJdbcUrl("postgres", "postgres"));
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "postgres");
    }

    @AfterAll
    static void stop() throws IOException {
        postgres.close();
    }

    @Autowired
    private JdbcTemplate jdbc;

    private void book(String code, String checkIn, String checkOut, String status) {
        jdbc.update("insert into bookings (code, guest_id, room_id, check_in, check_out, guests, total_amount, status,"
                + " created_at) values (?, (select id from users where role = 'GUEST' limit 1),"
                + " (select min(id) from rooms), ?::date, ?::date, 1, 100, ?, now())", code, checkIn, checkOut, status);
    }

    @Test
    void databaseRejectsOverlappingActiveBookings() {
        assertThat(jdbc.queryForObject("select count(*) from flyway_schema_history where success", Integer.class))
                .isEqualTo(2);
        book("T-1", "2030-05-10", "2030-05-13", "CONFIRMED");
        book("T-2", "2030-05-13", "2030-05-15", "CONFIRMED");
        book("T-3", "2030-05-11", "2030-05-12", "CANCELLED");
        assertThatThrownBy(() -> book("T-4", "2030-05-12", "2030-05-14", "PENDING_PAYMENT"))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("bookings_sin_solape");
    }
}
