package br.com.cyberbank.arquitetura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import jakarta.persistence.Entity;

/**
 * A fronteira entre assuntos e imposta por teste, e falha no build igual o compilador
 * falharia (ADR-0010).
 *
 * <p>Sao tres regras, e elas nao tem excecao. Excecao aqui nao e sinal de que a regra e
 * rigida demais — e sinal de que dois assuntos viraram um, e a resposta e mexer no modelo.
 *
 * <p>Hoje o projeto e um esqueleto e as tres passam sem ter o que reprovar. E isso mesmo:
 * a trava nasce antes da primeira linha que ela vai reprovar.
 */
@AnalyzeClasses(packages = FronteiraDeArquiteturaTest.RAIZ, importOptions = ImportOption.DoNotIncludeTests.class)
class FronteiraDeArquiteturaTest {

    static final String RAIZ = "br.com.cyberbank";

    /** 1. Dominio nao importa dominio: o grafo entre assuntos e vazio por construcao. */
    @ArchTest
    static final ArchRule dominio_nao_conhece_outro_dominio = classes()
            .that(estaoNoDominioDeUmAssunto())
            .should(dependerApenasDoProprioDominio())
            .because("dominio referencia dominio por id, nunca pelo objeto;"
                    + " quem junta dois assuntos e a camada de aplicacao (ADR-0010)")
            .allowEmptyShould(true);

    /** 2. A seta aponta para dentro: camada de dentro nunca conhece camada de fora. */
    @ArchTest
    static final ArchRule a_seta_aponta_para_dentro = CompositeArchRule
            .of(noClasses()
                    .that().resideInAPackage("..dominio..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..aplicacao..", "..api..", "..persistencia..")
                    .allowEmptyShould(true))
            .and(noClasses()
                    .that().resideInAPackage("..aplicacao..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..api..", "..persistencia..")
                    .allowEmptyShould(true))
            .because("a dependencia aponta para dentro (docs/01-arquitetura/visao-geral.md)");

    /** 3. Entidade JPA nao sai da persistencia: entidade de dominio nao e entidade JPA. */
    @ArchTest
    static final ArchRule entidade_jpa_nao_sai_da_persistencia = CompositeArchRule
            .of(classes()
                    .that().areAnnotatedWith(Entity.class)
                    .should().resideInAPackage("..persistencia..")
                    .allowEmptyShould(true))
            .and(noClasses()
                    .that().resideOutsideOfPackage("..persistencia..")
                    .should().dependOnClassesThat().areAnnotatedWith(Entity.class)
                    .allowEmptyShould(true))
            .because("a conversao entre entidade JPA, entidade de dominio e DTO e explicita");

    private static DescribedPredicate<JavaClass> estaoNoDominioDeUmAssunto() {
        return new DescribedPredicate<>("estao no dominio de um assunto") {
            @Override
            public boolean test(JavaClass classe) {
                return ehDominio(classe);
            }
        };
    }

    private static ArchCondition<JavaClass> dependerApenasDoProprioDominio() {
        return new ArchCondition<>("depender apenas do proprio dominio") {
            @Override
            public void check(JavaClass origem, ConditionEvents eventos) {
                for (Dependency dependencia : origem.getDirectDependenciesFromSelf()) {
                    JavaClass alvo = dependencia.getTargetClass();
                    if (ehDominio(alvo) && !assuntoDe(alvo).equals(assuntoDe(origem))) {
                        eventos.add(SimpleConditionEvent.violated(
                                dependencia, dependencia.getDescription()));
                    }
                }
            }
        };
    }

    /** {@code br.com.cyberbank.<assunto>.dominio[.<sub>]} — e nada mais. */
    private static boolean ehDominio(JavaClass classe) {
        String pacote = classe.getPackageName();
        return pacote.matches(RAIZ.replace(".", "\\.") + "\\.[^.]+\\.dominio(\\..*)?");
    }

    /** O primeiro segmento depois da raiz: o assunto, que e o nome do doc dono (ADR-0008). */
    private static String assuntoDe(JavaClass classe) {
        String resto = classe.getPackageName().substring(RAIZ.length() + 1);
        int ponto = resto.indexOf('.');
        return ponto < 0 ? resto : resto.substring(0, ponto);
    }
}
