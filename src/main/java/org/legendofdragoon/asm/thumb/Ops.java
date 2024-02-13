package org.legendofdragoon.asm.thumb;

public enum Ops {
  /** THUMB1, logical shift left */
  LSL,
  /** THUMB1, logical shift right */
  LSR,
  /** THUMB1, arithmetic shift right */
  ASR,
  /** THUMB2, add */
  ADD_REG,
  /** THUMB2, sub */
  SUB_REG,
  /** THUMB2, add */
  ADD_IMM,
  /** THUMB2, sub */
  SUB_IMM,
  /** THUMB3, move */
  MOV_IMM,
  /** THUMB3, compare */
  CMP_IMM,
  /** THUMB3, add */
  ADD_IMM_U,
  /** THUMB3, subtract */
  SUB_IMM_U,
  /** THUMB4, logical and */
  AND_ALU,
  /** THUMB4, logical xor */
  EOR_ALU,
  /** THUMB4, logical shift left */
  LSL_ALU,
  /** THUMB4, logical shift right */
  LSR_ALU,
  /** THUMB4, arithmetic shift right */
  ASR_ALU,
  /** THUMB4, add with carry */
  ADC_ALU,
  /** THUMB4, subtract with carry */
  SBC_ALU,
  /** THUMB4, rotate right */
  ROR_ALU,
  /** THUMB4, test */
  TST_ALU,
  /** THUMB4, negate */
  NEG_ALU,
  /** THUMB4, compare */
  CMP_ALU,
  /** THUMB4, negative compare */
  CMN_ALU,
  /** THUMB4, logical or */
  ORR_ALU,
  /** THUMB4, multiply */
  MUL_ALU,
  /** THUMB4, logical nand */
  BIC_ALU,
  /** THUMB4, logical not */
  MVN_ALU,
  /** THUMB5, high register add */
  ADD_HI,
  /** THUMB5, high register compare */
  CMP_HI,
  /** THUMB5, high register move */
  MOV_HI,
  /** THUMB5, branch, may switch THUMB/ARM */
  BX,
  /** THUMB6, load PR-relative */
  LDRPC,
  /** THUMB7, store with register offset */
  STR_REG,
  /** THUMB7, store byte with register offset */
  STRB_REG,
  /** THUMB7, load with register offset */
  LDR_REG,
  /** THUMB7, load byte with register offset */
  LDRB_REG,
  /** THUMB8, store signed halfword with register offset */
  STRH_REG,
  /** THUMB8, load signed byte with register offset */
  LDSB_REG,
  /** THUMB8, load signed halfword with register offset */
  LDRH_REG,
  /** THUMB8, load signed byte with register offset */
  LDSH_REG,
  /** THUMB9, store with immediate offset */
  STR_IMM,
  /** THUMB9, load with immediate offset */
  LDR_IMM,
  /** THUMB9, store byte with immediate offset */
  STRB_IMM,
  /** THUMB9, load byte with immediate offset */
  LDRB_IMM,
  /** THUMB10, store halfword with immediate offset */
  STRH_IMM,
  /** THUMB10, load halfword with immediate offset */
  LDRH_IMM,
  /** THUMB11, store SP-relative */
  STR_SP,
  /** THUMB11, load SP-relative */
  LDR_SP,
  /** THUMB12, get relative address */
  ADDRESS,
  /** THUMB13, add to stack pointer */
  ADD_SP,
  /** THUMB14 */
  PUSH,
  /** THUMB14 */
  POP,
  /** THUMB15, store multiple */
  STMIA,
  /** THUMB15, load multiple */
  LDMIA,
  /** THUMB16, branch if zero */
  BEQ,
  /** THUMB16, branch if not zero */
  BNE,
  /** THUMB16, branch if carry */
  BCS,
  /** THUMB16, branch if not carry */
  BCC,
  /** THUMB16, branch if negative */
  BMI,
  /** THUMB16, branch if not negative */
  BPL,
  /** THUMB16, branch if overflow */
  BVS,
  /** THUMB16, branch if no overflow */
  BVC,
  /** THUMB16, branch if unsigned > */
  BHI,
  /** THUMB16, branch if unsigned <= */
  BLS,
  /** THUMB16, branch if signed >= */
  BGE,
  /** THUMB16, branch if signed < */
  BLT,
  /** THUMB16, branch if signed > */
  BGT,
  /** THUMB16, branch if signed <= */
  BLE,

  /** THUMB18, branch */
  B,
  /** THUMB19, branch long with link (32-bit) */
  BL,
  /** THUMB19, branch long with link switch to ARM mode (32-bit) */
  BLX,
  ;

  public static Ops get(final int command) {
    // THUMB2 (must be first)
    if((command & 0xfc00) == 0x1c00) {
      return values()[ADD_REG.ordinal() + (command >>> 9 & 0x3)];
    }

    // THUMB1
    if((command & 0xe000) == 0x0) {
      return values()[LSL.ordinal() + (command >>> 11 & 0x3)];
    }

    // THUMB3
    if((command & 0xe000) == 0x2000) {
      return values()[MOV_IMM.ordinal() + (command >>> 11 & 0x3)];
    }

    // THUMB4
    if((command & 0xfc00) == 0x4000) {
      return values()[AND_ALU.ordinal() + (command >>> 6 & 0xf)];
    }

    // THUMB5
    if((command & 0xfc00) == 0x4400) {
      return values()[ADD_HI.ordinal() + (command >>> 8 & 0x3)];
    }

    // THUMB6
    if((command & 0xf800) == 0x4800) {
      return LDRPC;
    }

    // THUMB7/8
    if((command & 0xf000) == 0x5000) {
      if((command & 0x200) == 0) {
        return values()[STR_REG.ordinal() + (command >>> 10 & 0x3)];
      }

      return values()[STRH_REG.ordinal() + (command >>> 10 & 0x3)];
    }

    // THUMB9
    if((command & 0xe000) == 0x6000) {
      return values()[STR_IMM.ordinal() + (command >>> 11 & 0x3)];
    }

    // THUMB10
    if((command & 0xf000) == 0x8000) {
      return values()[STRH_IMM.ordinal() + (command >>> 11 & 0x1)];
    }

    // THUMB11
    if((command & 0xf000) == 0x9000) {
      return values()[STR_SP.ordinal() + (command >>> 11 & 0x1)];
    }

    // THUMB12
    if((command & 0xf000) == 0xa000) {
      return ADDRESS;
    }

    // THUMB13
    if((command & 0xff00) == 0xb000) {
      return ADD_SP;
    }

    // THUMB14
    if((command & 0xf600) == 0xb400) {
      return (command & 0x800) == 0 ? PUSH : POP;
    }

    // THUMB15
    if((command & 0xf000) == 0xc000) {
      return values()[STMIA.ordinal() + (command >>> 11 & 0x1)];
    }

    // THUMB16
    if((command & 0xf000) == 0xd000) {
      return values()[BEQ.ordinal() + (command >>> 8 & 0xf)];
    }

    // THUMB18
    if((command & 0xf800) == 0xe000) {
      return B;
    }

    // THUMB19
    if((command & 0xffff_0000) != 0) {
      return values()[BL.ordinal() + (command >>> 28 & 0x1)];
    }

    throw new RuntimeException("Unknown command %x".formatted(command));
  }
}
