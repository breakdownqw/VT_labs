package io;

public interface FieldFlags {
  int NO_RESTRICTIONS = 0b000;
  int MUST_BE_POSITIVE = 0b001;
  int MUST_BE_NON_EMPTY = 0b010;
  int MUST_BE_POSITIVE_AND_NON_EMPTY = 0b011;
  int MUST_BE_NEGATIVE = 0b100;
  int MUST_BE_NEGATIVE_AND_NON_EMPTY = 0b110;
}
