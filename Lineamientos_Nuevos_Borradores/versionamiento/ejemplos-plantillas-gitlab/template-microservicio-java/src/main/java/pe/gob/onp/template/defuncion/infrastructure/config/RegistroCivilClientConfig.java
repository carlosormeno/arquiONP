package pe.gob.onp.template.defuncion.infrastructure.config;

import java.util.concurrent.TimeUnit;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Configura el cliente HTTP saliente hacia el Registro Civil (RENIEC) con Apache HttpClient 5
 * (LIN-DIS-001 §6.1) — el mismo mecanismo que usa `template-backend-java-modular`, más
 * Resilience4j encima (ver {@code RegistroCivilHttpAdapter}).
 *
 * <p>Categoría de servicio externo según la matriz de `LIN-DIS-001 §6.1`: <b>Alta demanda / Ruta
 * crítica interactiva</b> — esta consulta se invoca en la ruta de autorización de pago de
 * pensión, en volumen alto y de cara al ciudadano, igual que RENIEC/SAA en la tabla oficial.
 * Por eso el timeout es agresivo (fail-fast) y NO hay reintento (`LIN-DIS-001 §6.4`: "Alta
 * demanda / ruta crítica interactiva -> No, fail-fast").</p>
 */
@Configuration
public class RegistroCivilClientConfig {

    @Bean
    public RestClient registroCivilRestClient(
            @Value("${onp.cliente.registro-civil.base-url}") String baseUrl,
            @Value("${onp.cliente.registro-civil.connect-timeout-ms:1500}") int connectTimeoutMs,
            @Value("${onp.cliente.registro-civil.response-timeout-ms:3000}") int responseTimeoutMs,
            @Value("${onp.cliente.registro-civil.max-conexiones:50}") int maxConexiones) {

        // Bulkhead de primera capa: pool de conexiones TCP acotado por proveedor externo
        // (LIN-DIS-001 §6.3) — independiente del @Bulkhead de Resilience4j del adapter, que
        // rechaza en JVM sin ni siquiera pedir una conexión de este pool una vez lleno.
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(maxConexiones);
        connectionManager.setDefaultMaxPerRoute(maxConexiones);
        connectionManager.setDefaultConnectionConfig(ConnectionConfig.custom()
                .setConnectTimeout(Timeout.of(connectTimeoutMs, TimeUnit.MILLISECONDS))
                .build());

        RequestConfig requestConfig = RequestConfig.custom()
                .setResponseTimeout(Timeout.of(responseTimeoutMs, TimeUnit.MILLISECONDS))
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
                .build();
    }
}
