package pe.gob.onp.template.backend.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(3) // Dentro de CanonicalRequestLogFilter (@Order 2) para que sus rechazos queden registrados
@ConditionalOnProperty(name = "onp.security.saa.enabled", havingValue = "true")
public class SaaTokenValidationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token requerido");
            return;
        }

        // Plantilla base: reemplazar por cliente real SAA conforme a LIN-SEC-APP-001 sección 8.3.
        MDC.put("user.id", "usuario-demo");                                  // logs de negocio
        request.setAttribute(CanonicalRequestLogFilter.ATTR_USER_ID, "usuario-demo"); // log canónico
        try {
            filterChain.doFilter(request, response);
        } finally {
            // El atributo de la request NO se limpia: lo lee CanonicalRequestLogFilter,
            // que envuelve a este filtro (LIN-OBS-001 §4.11).
            MDC.remove("user.id");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health")
                || path.startsWith("/actuator/info")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/api/v1/template/healthcheck");
    }
}
