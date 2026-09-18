package pe.gob.onp.template.defuncion.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Pruebas de arquitectura (LIN-DEV-JAVA-001 §15.5, tipo AT) que hacen cumplir en tiempo de
 * compilación las reglas de aislamiento de LIN-DIS-001 §2.3. A diferencia de
 * `template-backend-java-modular` (donde este test vive en el módulo {@code -boot}, el único con
 * el reactor completo en su classpath), aquí vive junto al único módulo Maven del proyecto —
 * exactamente lo que describe LIN-DEV-JAVA-001 §14.1 para la variante de Microservicio en un
 * solo módulo: "domain/" -> "unitarias puras" y "cero imports de framework ni JPA".
 *
 * <p>Hexagonal PURO: {@code domain} y {@code application} son ambos POJOs sin ningún framework,
 * incluido Resilience4j — el Circuit Breaker obligatorio de este microservicio (LIN-DIS-001
 * §6.2, `DIS-R-009`) es un detalle exclusivo del Adapter de infraestructura
 * ({@code infrastructure.client.RegistroCivilHttpAdapter}), nunca del puerto de salida puro que
 * el dominio declara ({@code domain.port.out.RegistroCivilDefuncionClient}).</p>
 *
 * <p><b>Nomenclatura de los campos {@code @ArchTest} en MAYÚSCULAS_CON_GUION_BAJO</b> (en vez del
 * {@code lower_snake_case} habitual en la comunidad de ArchUnit, usado tal cual en el homónimo de
 * `template-backend-java-modular`): es una desviación deliberada y documentada, no un descuido de
 * estilo. Este campo es técnicamente un {@code static final} — Checkstyle exige mayúsculas por la
 * regla {@code ConstantName} de `checkstyle-onp.xml` (copia controlada, no editable desde esta
 * plantilla). `template-backend-java-modular` no choca con esa regla solo porque su
 * {@code maven-checkstyle-plugin} raíz no fija {@code includeTestSourceDirectory=true} (Checkstyle
 * no analiza tests ahí); esta plantilla sí lo fija (mismo criterio que `template-worker-java`), así
 * que el conflicto aparece aquí primero. Ver README, "Decisiones de diseño resueltas".</p>
 */
@AnalyzeClasses(packages = "pe.gob.onp.template.defuncion", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaHexagonalTest {

    // -----------------------------------------------------------------------------
    // §2.3 — Regla de Oro de domain/ y application/: cero dependencias técnicas.
    // -----------------------------------------------------------------------------

    @ArchTest
    static final ArchRule DOMAIN_Y_APPLICATION_NO_DEPENDEN_DE_JPA =
            noClasses().that().resideInAnyPackage("..domain..", "..application..")
                    .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..", "javax.persistence..")
                    .because("LIN-DIS-001 §2.3 (Regla de Oro): domain/ y application/ son Java puro, "
                            + "sin anotaciones JPA — la persistencia es responsabilidad exclusiva de infrastructure/");

    @ArchTest
    static final ArchRule DOMAIN_Y_APPLICATION_NO_DEPENDEN_DE_SPRING =
            noClasses().that().resideInAnyPackage("..domain..", "..application..")
                    .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                    .because("LIN-DEV-JAVA-001 §14.1 (Regla de pureza hexagonal): domain/ y application/ son "
                            + "POJOs sin @Service ni @Transactional — el cableado a Spring vive en infra/config");

    @ArchTest
    static final ArchRule DOMAIN_Y_APPLICATION_NO_DEPENDEN_DE_JACKSON =
            noClasses().that().resideInAnyPackage("..domain..", "..application..")
                    .should().dependOnClassesThat().resideInAPackage("com.fasterxml.jackson..")
                    .because("LIN-DIS-001 §2.3 (Regla de Oro): domain/ y application/ no serializan, "
                            + "no conocen Jackson — eso vive en los DTOs de infra/web e infra/client");

    /**
     * Regla propia de esta plantilla (no presente en `template-backend-java-modular`, donde
     * Resilience4j es excepcional): el Circuit Breaker obligatorio de Microservicio
     * (LIN-DIS-001 §6.2, `DIS-R-009`) es un detalle de infraestructura del adapter de salida —
     * el dominio y la aplicación no conocen Resilience4j, solo el puerto de salida puro.
     */
    @ArchTest
    static final ArchRule DOMAIN_Y_APPLICATION_NO_DEPENDEN_DE_RESILIENCE4J =
            noClasses().that().resideInAnyPackage("..domain..", "..application..")
                    .should().dependOnClassesThat().resideInAPackage("io.github.resilience4j..")
                    .because("LIN-DIS-001 §6.2 (DIS-R-009): el Circuit Breaker/Bulkhead es un detalle técnico "
                            + "del Adapter de salida (infrastructure.client) — domain/ y application/ solo conocen "
                            + "el contrato puro del puerto de salida (domain.port.out.RegistroCivilDefuncionClient)");

    // -----------------------------------------------------------------------------
    // §2.3 — La dependencia fluye siempre hacia adentro (infraestructura -> aplicación -> dominio).
    // -----------------------------------------------------------------------------

    @ArchTest
    static final ArchRule DOMAIN_Y_APPLICATION_NO_DEPENDEN_DE_INFRAESTRUCTURA =
            noClasses().that().resideInAnyPackage("..domain..", "..application..")
                    .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                    .because("LIN-DIS-001 §2.3 (Regla de Oro de infrastructure): la infraestructura implementa "
                            + "los puertos del dominio; ni domain/ ni application/ pueden conocer a sus adaptadores");

    @ArchTest
    static final ArchRule DOMAIN_NO_DEPENDE_DE_APPLICATION =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("..application..")
                    .because("LIN-DIS-001 §2.3: application/ orquesta el dominio, no al revés — "
                            + "domain/ no conoce sus propios casos de uso");

    // -----------------------------------------------------------------------------
    // Convención de nomenclatura de puertos y adapters — LIN-DEV-JAVA-001 §14.2.
    // -----------------------------------------------------------------------------

    @ArchTest
    static final ArchRule CAPAS_RESPETAN_EL_SENTIDO_UNICO_DE_DEPENDENCIA =
            Architectures.layeredArchitecture()
                    .consideringOnlyDependenciesInLayers()
                    .layer("Domain").definedBy("..domain..")
                    .layer("Application").definedBy("..application..")
                    .layer("Infrastructure").definedBy("..infrastructure..")
                    .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
                    .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure")
                    .because("LIN-DEV-JAVA-001 §14.1: infrastructure -> application -> domain, nunca al revés");
}
