package com.oc.codingtest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PasswordStrength {

  private static final Logger log = LoggerFactory.getLogger(PasswordStrength.class);

  public boolean isPasswordPermissible(String password, int maxAllowedRepetitionCount, int maxAllowedSequenceLength) {
    // This method accepts a password (String) and calculates two password strength parameters:
    // Repetition count and Max Sequence length
    return getMaxRepetitionCount(password) <= maxAllowedRepetitionCount
             && getMaxSequenceLen(password) <= maxAllowedSequenceLength;
  }

  /**
   * Repetition count - the number of occurrences of the *most repeated* character within the password
   *   eg1: "Melbourne" has a repetition count of 2 - for the 2 non-consecutive "e" characters.
   *   eg2: "passwords" has a repetition count of 3 - for the 3 "s" characters
   *   eg3: "lucky" has a repetition count of 1 - each character appears only once.
   *   eg4: "Elephant" has a repetition count of 1 - as the two "e" characters have different cases (ie one "E", one "e")
   * The repetition count should be case-sensitive.
   * @param password
   * @return
   */
  public int getMaxRepetitionCount(String password) {
    Map<Integer, Long> charToRepetitionCount = password.chars()
            .boxed()
            .collect(Collectors.groupingBy(
                    Function.identity(),
                    Collectors.counting()
            ));

    return charToRepetitionCount.values()
            .stream()
            .mapToInt(Long::intValue)
            .max()
            .orElse(0);
  }

  /**
   * Max Sequence length - The length of the longest ascending/descending sequence of alphabetical or numeric characters
   * eg: "4678" and "4321" would both have sequence length of 4
   * eg2: "cdefgh" would have a sequence length of 6
   * eg3: "password123" would have a max. sequence length of 3 - for the sequence of "123".
   * eg3a: "1pass2word3" would have a max. sequence length of 0 - as there is no sequence.
   * eg3b: "passwordABC" would have a max. sequence length of 3 - for the sequence of "ABC".
   * eg4: "AbCdEf" would have a sequence length of 6, even though it is mixed case.
   * eg5: "ABC_DEF" would have a sequence length of 3, because the special character breaks the progression
   * Check the supplied password.  Return true if the repetition count and sequence length are below or equal to the
   * specified maximum.  Otherwise, return false.
   * @param password
   * @return
   */
  public int getMaxSequenceLen(String password) {

    String normalisedPassword = password.toUpperCase();
    int maxSequenceLen = 1;

    NormalisedChar previousChar = NormalisedChar.createFromChar(normalisedPassword.charAt(0));
    CharSequence sequence = new CharSequence();

    for (int i = 1; i < normalisedPassword.length(); i++) {
      NormalisedChar currentChar = NormalisedChar.createFromChar(normalisedPassword.charAt(i));
      boolean charTypeHasChanged = currentChar.type() != previousChar.type();

      if(charTypeHasChanged || currentChar.isNonAlphanumeric()) {
        // sequence has broken due to either:
        //     - change of type
        //     - char being non-alphanumeric, we don't track those sequences
        // restart sequence
        if(sequence.getLength() > maxSequenceLen ) {
          maxSequenceLen = sequence.getLength();
        }
        sequence.restart();
      } else {
        // previous and current chars share type, we need to check the sequence direction
        SequenceDirection currentDirection = currentChar.getDirectionFrom(previousChar);

        if(sequence.getDirection() == SequenceDirection.NO_DIRECTION) {
          if(currentDirection == SequenceDirection.ASCENDING || currentDirection == SequenceDirection.DESCENDING) {
            // we are starting a sequence in a new direction
            sequence.increment();
            sequence.setDirection(currentDirection);
          } else {
            // we don't track sequences with NO_DIRECTION
            sequence.restart();
          }
        } else {
          boolean sequenceContinuingInSameDirection = currentDirection == sequence.getDirection();
          boolean sequenceFlippingDirection = currentDirection != SequenceDirection.NO_DIRECTION;

          if(sequenceContinuingInSameDirection) {
            sequence.increment();
          } else if (sequenceFlippingDirection) {
            if(sequence.getLength() > maxSequenceLen) {
              maxSequenceLen = sequence.getLength();
            }
            sequence.setLength(2);
            sequence.setDirection(currentDirection);
          } else {
            // current char is not in sequence, sequence is broken
            if(sequence.getLength() > maxSequenceLen) {
              maxSequenceLen = sequence.getLength();
            }
            sequence.restart();
          }
        }
      }
      previousChar = currentChar;
    }

    if(sequence.getLength() > maxSequenceLen) {
      maxSequenceLen = sequence.getLength();
    }

    return maxSequenceLen;
  }

  private enum CharType {
    ALPHABETICAL,
    NUMERIC,
    NON_ALPHANUMERIC
  }

  private enum SequenceDirection{
    ASCENDING,
    DESCENDING,
    NO_DIRECTION
  }

  /**
   * Convenience class for normalising the ascii value of a char based on its CharType
   * @param type
   * @param normalisedValue
   */
  private record NormalisedChar(CharType type, int normalisedValue) {

    public static NormalisedChar createFromChar(char character){
      CharType type = getCharType(character);
      int normalisedValue = switch (type) {
          case ALPHABETICAL -> character - 'A';
          case NUMERIC -> character - '0';
          case NON_ALPHANUMERIC -> character;
      };
      return new NormalisedChar(type, normalisedValue);
    }

    private static CharType getCharType(char character) {
      CharType charType = CharType.NON_ALPHANUMERIC;

      if (Character.isDigit(character)) {
        charType = CharType.NUMERIC;
      } else if (Character.isLetter(character)) {
        charType = CharType.ALPHABETICAL;
      }

      return charType;
    }

    public boolean isNonAlphanumeric() {
      return type == CharType.NON_ALPHANUMERIC;
    }

    /**
     * Given a NormalisedChar, if it were directly before this NormalisedChar in a sequence, determines the direction
     * of the sequence.
     * @param previousChar
     * @return ASCENDING if this is one positive increment in sequence from previousChar, DESCENDING if this is one
     * negative increment in sequence from previousChar, else NO_DIRECTION
     */
    public SequenceDirection getDirectionFrom(NormalisedChar previousChar) {
        final int sequenceAscendingDiff = 1;
        final int sequenceDescendingDiff = -1;

      int charDifference = this.normalisedValue() - previousChar.normalisedValue();

      return switch(charDifference) {
        case sequenceAscendingDiff -> SequenceDirection.ASCENDING;
        case sequenceDescendingDiff -> SequenceDirection.DESCENDING;
        default -> SequenceDirection.NO_DIRECTION;
      };
    }
  }

  /**
   * Convenience class for tracking the length and direction of a sequence.
   */
  private static class CharSequence {

    private SequenceDirection direction;
    private int length;

    public CharSequence(SequenceDirection direction, int length) {
      this.direction = direction;
      this.length = length;
    }

    public CharSequence(){
      this(SequenceDirection.NO_DIRECTION, 1);
    }

    public void restart() {
      this.direction = SequenceDirection.NO_DIRECTION;
      this.length = 1;
    }

    public void increment() {
      this.length++;
    }

    public SequenceDirection getDirection() {
      return direction;
    }

    public void setDirection(SequenceDirection direction){
      this.direction = direction;
    }

    public int getLength() {
      return length;
    }

    public void setLength(int length){
      this.length = length;
    }
  }
}
