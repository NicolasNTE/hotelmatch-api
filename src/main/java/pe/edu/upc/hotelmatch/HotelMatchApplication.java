package pe.edu.upc.hotelmatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HotelMatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelMatchApplication.class, args);
    }
}
