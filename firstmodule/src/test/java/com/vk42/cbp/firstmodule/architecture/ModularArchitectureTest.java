package com.vk42.cbp.firstmodule.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.vk42.cbp.firstmodule.FirstmoduleApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

public class ModularArchitectureTest {
    // 1. Verificación automática con Spring Modulith
    @Test
    void verifyModularArchitecture() {
        // Leer la aplicación y detectar las carpetas y orden
        ApplicationModules modules = ApplicationModules.of(FirstmoduleApplication.class);

        // Verifica que no haya dependencias cíclicas
        modules.verify();
    }

    // 2. Generación de diagramas automáticos -> Diagramas UML de la arquitectura / se guardarán en
    // la carpeta target/spring-modulith-docs al correr los test
    @Test
    void writeDocumentationSnippets() {
        ApplicationModules modules = ApplicationModules.of(FirstmoduleApplication.class);

        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml();
    }

    @Test
    void paymentsModuleShouldNotDependOnOutboxModule() {
        JavaClasses importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.vk42.cbp.firstmodule");

        ArchRule rule = ArchRuleDefinition.noClasses()
                .that().resideInAPackage("..payments..")
                .should().dependOnClassesThat().resideInAPackage("..outbox..");

        rule.check(importedClasses);
    }
}
