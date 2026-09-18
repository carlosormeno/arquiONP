package pe.gob.onp.template.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada único para las tres variantes de workload de {@code LIN-K8S-001 §4.1}
 * (Worker/{@code Deployment}, Job puntual/{@code Job}, Job recurrente/{@code CronJob}).
 *
 * <p>La variante activa NO se decide en código ni con clases {@code main} separadas, sino con
 * el perfil Spring indicado por {@code SPRING_PROFILES_ACTIVE} en el manifiesto K8s:
 *
 * <ul>
 *   <li>{@code worker} — activa {@link pe.gob.onp.template.worker.messaging.TareaSolicitadaConsumer}
 *       (listener Kafka continuo). Manifiesto: {@code k8s/base/deployment.yaml}.</li>
 *   <li>{@code job} — activa {@link pe.gob.onp.template.worker.batch.ActualizacionMasivaJobRunner}
 *       (tarea batch puntual que termina el proceso). Manifiestos: {@code k8s/base/job.yaml} y
 *       {@code k8s/base/cronjob.yaml} — es la MISMA clase Java para ambos; lo único que cambia
 *       entre "puntual" y "recurrente" es qué recurso K8s la invoca (uno vs. programado por
 *       {@code schedule}), no el código (ver README, sección "Worker vs. Job puntual vs. Job
 *       recurrente").</li>
 * </ul>
 *
 * <p>El perfil de ambiente (dev/qa/prod) se combina con el de variante, ej.
 * {@code SPRING_PROFILES_ACTIVE=prod,worker} o {@code SPRING_PROFILES_ACTIVE=prod,job}.
 */
@SpringBootApplication
public class TemplateWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TemplateWorkerApplication.class, args);
    }
}
