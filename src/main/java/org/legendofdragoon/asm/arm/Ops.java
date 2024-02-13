package org.legendofdragoon.asm.arm;

public enum Ops {
  AND,
  EOR,
  SUB,
  RSB,
  ADD,
  ADC,
  SBC,
  RSC,
  TST,
  TEQ,
  CMP,
  CMN,
  ORR,
  MOV,
  BIC,
  MVN,

  PSR_IMM,
  PSR_REG,

  BX,
  BLX,

  MUL,
  MUL_LONG,

  TRANS_SWAP_12,

  TRANS_REG_10,
  TRANS_IMM_10,
  TRANS_IMM_9,
  TRANS_REG_9,

  BLOCK_TRANS,

  /** Branch */
  B,
  /** Branch and link */
  BL,
  ;

  public String getOperator() {
    return switch(this) {
      case AND -> "&";
      case EOR -> "^";
      case SUB, RSB, SBC, RSC -> "-";
      case ADD, ADC -> "+";
      case ORR -> "|";
      default -> throw new IllegalArgumentException("No operator for " + this);
    };
  }

  public boolean isLogical() {
    return this == AND || this == EOR || this == TST || this == TEQ || this == ORR || this == MOV || this == BIC || this == MVN;
  }

  public boolean isArithmetic() {
    return this == SUB || this == RSB || this == ADD || this == ADC || this == SBC || this == RSC;
  }

  public static Ops get(final int command) {
    if((command & 0xfffffd0) == 0x12fff10) {
      return values()[BX.ordinal() + (command >>> 5 & 0x1)];
    }

    if((command & 0xfc000f0) == 0x90) {
      return MUL;
    }

    if((command & 0xf8000f0) == 0x800090) {
      return MUL_LONG;
    }

    if((command & 0xfb00ff0) == 0x1000090) {
      return TRANS_SWAP_12;
    }

    if((command & 0xe400f90) == 0x90) {
      return TRANS_REG_10;
    }

    if((command & 0xe400090) == 0x400090) {
      return TRANS_IMM_10;
    }

    if((command & 0xe000010) == 0x6000000) {
      return TRANS_REG_9;
    }

    if((command & 0xe000000) == 0x4000000) {
      return TRANS_IMM_9;
    }

    if((command & 0xfb00000) == 0x3200000) {
      return PSR_IMM;
    }

    if((command & 0xf900000) == 0x1000000) {
      return PSR_REG;
    }

    if((command & 0xe000000) == 0x8000000) {
      return BLOCK_TRANS;
    }

    if((command & 0xf000000) == 0xa000000) {
      return B;
    }

    if((command & 0xf000000) == 0xb000000) {
      return BL;
    }

    if((command & 0xe000090) == 0x10) {
      return values()[AND.ordinal() + (command >>> 21 & 0xf)];
    }

    if((command & 0xe000010) == 0x0) {
      return values()[AND.ordinal() + (command >>> 21 & 0xf)];
    }

    if((command & 0xe000000) == 0x2000000) {
      return values()[AND.ordinal() + (command >>> 21 & 0xf)];
    }

    throw new IllegalArgumentException("Unknown command %x".formatted(command));
  }
}
