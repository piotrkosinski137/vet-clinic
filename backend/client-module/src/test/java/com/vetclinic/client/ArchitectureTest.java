package com.vetclinic.client;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
        packages = "com.vetclinic.client",
        importOptions = ImportOption.DoNotIncludeTests.class)
@SuppressWarnings({
    "checkstyle:HideUtilityClassConstructor",
    "checkstyle:ConstantName"
}) // ArchUnit test naming convention uses snake_case for readability
class ArchitectureTest {

    @ArchTest
    static final ArchRule layered_architecture_is_respected =
            layeredArchitecture()
                    .consideringAllDependencies()
                    .layer("API")
                    .definedBy("..api..")
                    .layer("Domain")
                    .definedBy("..domain..")
                    .layer("Infrastructure")
                    .definedBy("..infrastructure..")
                    .whereLayer("API")
                    .mayNotBeAccessedByAnyLayer()
                    .whereLayer("Infrastructure")
                    .mayNotBeAccessedByAnyLayer()
                    .whereLayer("Domain")
                    .mayOnlyBeAccessedByLayers("API", "Infrastructure");

    @ArchTest
    static final ArchRule domain_should_not_depend_on_infrastructure =
            noClasses()
                    .that()
                    .resideInAPackage("..domain..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule domain_should_not_depend_on_api =
            noClasses()
                    .that()
                    .resideInAPackage("..domain..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..api..");

    @ArchTest
    static final ArchRule controllers_should_be_in_api_package =
            classes()
                    .that()
                    .haveSimpleNameEndingWith("Controller")
                    .should()
                    .resideInAPackage("..api..");

    @ArchTest
    static final ArchRule services_should_be_in_domain_package =
            classes()
                    .that()
                    .haveSimpleNameEndingWith("Service")
                    .and()
                    .areNotInterfaces()
                    .should()
                    .resideInAPackage("..domain..");

    @ArchTest
    static final ArchRule client_module_should_not_depend_on_patient_module =
            noClasses()
                    .that()
                    .resideInAPackage("com.vetclinic.client..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("com.vetclinic.patient..")
                    .because("Modules should not have direct dependencies on each other");
}
