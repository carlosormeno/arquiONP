package pe.gob.onp.template.boot.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Pruebas de arquitectura (LIN-DEV-JAVA-001 §15.5, tipo AT) que hacen cumplir en tiempo
 * de compilación las reglas de aislamiento de LIN-DIS-001 §2.3 y §3.4. Viven en
 * onp-template-boot porque es el único módulo con el reactor completo (domain,
 * application, infrastructure, api, messaging y common) en su classpath de test.
 *
 * <p>Hexagonal PURO: domain/ y application/ son ambos POJOs sin ningún framework.
 * Solo infrastructure/ (incluyendo api/ y messaging/, que son sus adaptadores de
 * entrada) puede depender de Spring, JPA o Jackson. El cableado a Spring —incluida
 * la demarcación transaccional— ocurre exclusivamente en infrastructure/, nunca en
 * application/ (ver AfiliacionUseCaseConfig).</p>
 */
@AnalyzeClasses(packages = "pe.gob.onp", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaHexagonalTest {

    // -----------------------------------------------------------------------------
    // §2.3 — Regla de Oro de domain/ y application/: cero dependencias técnicas.
    // Hexagonal puro: la prohibición no es solo de domain/, también de application/.
    // -----------------------------------------------------------------------------

    @ArchTest
    static final ArchRule domain_y_application_no_dependen_de_jpa =
            noClasses().that().resideInAnyPackage("..domain..", "..application..")
                    .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..", "javax.persistence..")
                    .because("LIN-DIS-001 §2.3 (Regla de Oro): domain/ y application/ son Java puro, "
                            + "sin anotaciones JPA — la persistencia es responsabilidad exclusiva de infrastructure/");

    @ArchTest
    static final ArchRule domain_y_application_no_dependen_de_spring =
            noClasses().that().resideInAnyPackage("..domain..", "..application..")
                    .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                    .because("LIN-DIS-001 §2.3 (Regla de Oro, hexagonal puro): domain/ y application/ son POJOs "
                            + "sin @Service ni @Transactional — el cableado a Spring vive en infrastructure/");

    @ArchTest
    static final ArchRule domain_y_application_no_dependen_de_jackson =
            noClasses().that().resideInAnyPackage("..domain..", "..application..")
                    .should().dependOnClassesThat().resideInAPackage("com.fasterxml.jackson..")
                    .because("LIN-DIS-001 §2.3 (Regla de Oro): domain/ y application/ no serializan, "
                            + "no conocen Jackson — eso vive en los DTOs de infrastructure/api");

    // -----------------------------------------------------------------------------
    // §2.3 — Regla de Oro de infrastructure/: la dependencia fluye siempre hacia
    // adentro (infraestructura -> aplicación -> dominio), nunca al revés.
    // -----------------------------------------------------------------------------

    @ArchTest
    static final ArchRule domain_y_application_no_dependen_de_infraestructura =
            noClasses().that().resideInAnyPackage("..domain..", "..application..")
                    .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..api..", "..messaging..")
                    .because("LIN-DIS-001 §2.3 (Regla de Oro de infrastructure): la infraestructura implementa "
                            + "los puertos del dominio; ni domain/ ni application/ pueden conocer a sus adaptadores");

    @ArchTest
    static final ArchRule domain_no_depende_de_application =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("..application..")
                    .because("LIN-DIS-001 §2.3: application/ orquesta el dominio, no al revés — "
                            + "domain/ no conoce sus propios casos de uso");

    // -----------------------------------------------------------------------------
    // §3.4 — Gobierno del Shared Kernel (onp-common-domain / pe.gob.onp.common.domain).
    // -----------------------------------------------------------------------------

    @ArchTest
    static final ArchRule shared_kernel_sin_entidades_jpa =
            noClasses().that().resideInAPackage("pe.gob.onp.common.domain..")
                    .should().beAnnotatedWith(Entity.class)
                    .because("LIN-DIS-001 §3.4: ninguna tabla de Oracle puede mapearse en el Shared Kernel — "
                            + "cada módulo es dueño exclusivo de sus entidades JPA");

    @ArchTest
    static final ArchRule shared_kernel_sin_anotaciones_transaccionales =
            noClasses().that().resideInAPackage("pe.gob.onp.common.domain..")
                    .should().beAnnotatedWith(Service.class)
                    .orShould().beAnnotatedWith(Transactional.class)
                    .because("LIN-DIS-001 §3.4: el Shared Kernel es Java puro, igual que domain/ — "
                            + "ningún servicio previsional ni frontera transaccional puede vivir aquí");

    @ArchTest
    static final ArchRule shared_kernel_sin_servicios_de_negocio =
            noClasses().that().resideInAPackage("pe.gob.onp.common.domain..")
                    .should().haveNameMatching(".*Service(Impl)?")
                    .because("LIN-DIS-001 §3.4: la lógica y los servicios previsionales (ej. CalculoPensionService) "
                            + "pertenecen exclusivamente a su Bounded Context, nunca al Shared Kernel");

    @ArchTest
    static final ArchRule shared_kernel_sin_puertos_de_persistencia =
            noClasses().that().resideInAPackage("pe.gob.onp.common.domain..")
                    .should().haveNameMatching(".*(Repository|Port)")
                    .because("LIN-DIS-001 §3.4: prohibido poner interfaces de repositorios o clientes HTTP "
                            + "en el Shared Kernel");
}
