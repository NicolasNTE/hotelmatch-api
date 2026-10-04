package pe.edu.upc.hotelmatch.common;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApiErrorFactory {

    private final MessageSource messageSource;

    public ProblemDetail create(HttpStatus status, String code, Object... args) {
        String detail = messageSource.getMessage(code, args, code, LocaleContextHolder.getLocale());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        problem.setProperty("code", code);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
