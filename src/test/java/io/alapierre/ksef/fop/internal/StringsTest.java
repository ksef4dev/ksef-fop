package io.alapierre.ksef.fop.internal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringsTest {

    static Stream<Arguments> defaultIfBlank() {
        return Stream.of(
                Arguments.of(null, "default", "default"),
                Arguments.of("", "default", "default"),
                Arguments.of("   ", "default", "default"),
                Arguments.of("value", "default", "value"),
                Arguments.of("  value  ", "default", "  value  ")
        );
    }

    @ParameterizedTest
    @MethodSource
    void defaultIfBlank(String str, String defaultStr, String expected) {
        assertEquals(expected, Strings.defaultIfBlank(str, defaultStr));
    }
}
