package pe.gob.onp.template.backend.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(2) // Envuelve a SaaTokenValidationFilter (@Order 3): registra también los rechazos
public class CanonicalRequestLogFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(CanonicalRequestLogFilter.class);

    /** Identidad publicada por SaaTokenValidationFilter; sobrevive a la limpieza del MDC. */
    public static final String ATTR_USER_ID = "onp.user.id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - start;
            // El MDC ya fue limpiado por SaaTokenValidationFilter al desapilarse:
            // la identidad se recupera del atributo de la request (LIN-OBS-001 §4.9).
            Object attr = request.getAttribute(ATTR_USER_ID);
            String userId = (attr != null) ? attr.toString() : null;
            LOG.info(
                    "REQUEST method={} path={} status={} duration_ms={} user.id={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs,
                    userId != null ? userId : "anonymous");
        }
    }
}
