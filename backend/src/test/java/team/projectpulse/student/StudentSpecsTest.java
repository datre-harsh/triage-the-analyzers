package team.projectpulse.student;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StudentSpecsTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    Root<Student> root;
    @Mock
    CriteriaBuilder criteriaBuilder;
    @Mock
    Expression<String> lowercaseColumn;

    @ParameterizedTest(name = "{0}")
    @MethodSource("searchFilters")
    void testSearchOnTurkishServer(String field, Specification<Student> specification,
                                   String expectedValue, boolean partialMatch) {
        given(this.criteriaBuilder.lower(any())).willReturn(this.lowercaseColumn);
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));

            specification.toPredicate(this.root, null, this.criteriaBuilder);

            if (partialMatch) {
                verify(this.criteriaBuilder).like(this.lowercaseColumn, expectedValue);
            } else {
                verify(this.criteriaBuilder).equal(this.lowercaseColumn, expectedValue);
            }
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    static Stream<Arguments> searchFilters() {
        return Stream.of(
                Arguments.of("first name", StudentSpecs.containsFirstName("IVAN"), "%ivan%", true),
                Arguments.of("last name", StudentSpecs.containsLastName("IRVINE"), "%irvine%", true),
                Arguments.of("email", StudentSpecs.hasEmail("IVAN@EXAMPLE.EDU"), "ivan@example.edu", false),
                Arguments.of("team name", StudentSpecs.hasTeamName("INFINITY"), "infinity", false),
                Arguments.of("section name", StudentSpecs.hasSectionName("INTRO"), "intro", false)
        );
    }
}
