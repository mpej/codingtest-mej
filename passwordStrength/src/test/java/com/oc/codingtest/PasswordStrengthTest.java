package com.oc.codingtest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PasswordStrengthTest {

  private static final Logger log = LoggerFactory.getLogger(PasswordStrengthTest.class);

  PasswordStrength ps = new PasswordStrength();

  private static Stream<Arguments> maxRepetitionsArgProvider() {
    return Stream.of(
      Arguments.of("abcdefg", 1),
      Arguments.of("password", 2),
      Arguments.of("touchwood", 3),
      // The check should be case-sensitive
      Arguments.of("TheQuickBrownFoxJumpsOverTheLazyDog", 3)
    );
  }

  @ParameterizedTest
  @DisplayName("Max character repetition tests")
  @MethodSource("com.oc.codingtest.PasswordStrengthTest#maxRepetitionsArgProvider")
  void checkingMaxRepetitionsOnly(String password, int expectedResult) {
    assertEquals(expectedResult, ps.getMaxRepetitionCount(password));
  }

  private static Stream<Arguments> maxSequenceLenArgProvider() {
    return Stream.of(
      //simple ascending
      Arguments.of("abcdef", 6),
      //simple descending
      Arguments.of("fedcba", 6),
      //numeric ascending
      Arguments.of("0123456789", 10),
      //numeric descending
      Arguments.of("9876543210", 10),
      //ascending - mixed case
      Arguments.of("ABCdef", 6),
      //descending - mixed case
      Arguments.of("fedCBA", 6),
      //ascending - mixed alpha and numeric
      Arguments.of("password123", 3),
      //descending - mixed alpha and numeric
      Arguments.of("passwordcba123", 4),

      //Exclude/handle non-alphanumeric chars
      Arguments.of("/012345678", 9),
      Arguments.of("0123456789:", 10),
      Arguments.of("01234*567:", 5),
      Arguments.of("012#$%&345", 3),

      //Ascending and descending mixed
      Arguments.of("23454321", 5)
    );
  }

  @ParameterizedTest
  @DisplayName("Max sequence length tests")
  @MethodSource("com.oc.codingtest.PasswordStrengthTest#maxSequenceLenArgProvider")
  void checkingMaxSequenceLenOnly(String password, int expectedResult) {
    assertEquals(expectedResult, ps.getMaxSequenceLen(password));
  }

  private static Stream<Arguments> isPasswordPermissibleArgProvider() {
    return Stream.of(
            // repetitions > max, sequence < max
            Arguments.of("SmartyGrantsAAAA", 5, 2, false),
            // repetitions == max, sequence < max
            Arguments.of("MichaelIsAGr8Engineer!123", 4, 4, true),
            // repetitions < max, sequence < max
            Arguments.of("Grassr00tsAction", 4, 3, true),
            // repetitions < max, sequence > max
            Arguments.of("IW4nt2workwithU4321", 4, 3, false),
            // repetitions < max, sequence == max
            Arguments.of("123OCHouseABCDEF", 3, 6, true),
            // repetitions < max, sequence < max
            Arguments.of("ASMANYASIWANT987654321ZZZZZZZZ", 45, 65, true),
            // repetitions == max, sequence == max
            Arguments.of("n0_rep3atz!", 1, 1, true),
            // repetitions > max, sequence > max
            Arguments.of("Toooooomanyrepeats123456", 5, 5, false)
    );
  }

  @ParameterizedTest
  @DisplayName("Permissible password tests")
  @MethodSource("com.oc.codingtest.PasswordStrengthTest#isPasswordPermissibleArgProvider")
  void checkingIsPasswordPermissible(String password, int maxAllowedRepetitionCount, int maxAllowedSequenceLength, boolean expectedResult) {
    assertEquals(expectedResult, ps.isPasswordPermissible(password, maxAllowedRepetitionCount, maxAllowedSequenceLength));
  }
}
