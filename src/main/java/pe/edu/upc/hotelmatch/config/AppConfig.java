package pe.edu.upc.hotelmatch.config;

import java.time.Clock;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

@Configuration
public class AppConfig {

    public static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Lima");

    @Bean
    public Clock clock() {
        return Clock.system(BUSINESS_ZONE);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.forLanguageTag("es-419"));
        resolver.setSupportedLocales(List.of(Locale.forLanguageTag("es-419"), Locale.forLanguageTag("es"), Locale.ENGLISH, Locale.US));
        return resolver;
    }
}
