package org.legendofdragoon.asm.mips;

public enum Ops {
  SLL(0x0, 0x0),
  SRL(0x0, 0x2),
  SRA(0x0, 0x3),
  SLLV(0x0, 0x4),
  SRLV(0x0, 0x6),
  SRAV(0x0, 0x7),
  JR(0x0, 0x8),
  JALR(0x0, 0x9),
  SYSCALL(0x0, 0xc),
  BREAK(0x0, 0xd),
  MFHI(0x0, 0x10),
  MTHI(0x0, 0x11),
  MFLO(0x0, 0x12),
  MTLO(0x0, 0x13),
  MULT(0x0, 0x18),
  MULTU(0x0, 0x19),
  DIV(0x0, 0x1a),
  DIVU(0x0, 0x1b),
  ADD(0x0, 0x20),
  ADDU(0x0, 0x21),
  SUB(0x0, 0x22),
  SUBU(0x0, 0x23),
  AND(0x0, 0x24),
  OR(0x0, 0x25),
  XOR(0x0, 0x26),
  NOR(0x0, 0x27),
  SLT(0x0, 0x2a),
  SLTU(0x0, 0x2b),
  BCONDZ(0x1),
  J(0x2),
  JAL(0x3),
  BEQ(0x4),
  BNE(0x5),
  BLEZ(0x6),
  BGTZ(0x7),
  ADDI(0x8),
  ADDIU(0x9),
  SLTI(0xa),
  SLTIU(0xb),
  ANDI(0xc),
  ORI(0xd),
  XORI(0xe),
  LUI(0xf),
  COP0(0x10),
  COP2(0x12),
  LB(0x20),
  LH(0x21),
  LWL(0x22),
  LW(0x23),
  LBU(0x24),
  LHU(0x25),
  LWR(0x26),
  SB(0x28),
  SH(0x29),
  SWL(0x2a),
  SW(0x2b),
  SWR(0x2e),
  LWC0(0x30),
  LWC2(0x32),
  SWC0(0x38),
  SWC2(0x3a),
  NOOP(-1),
  ;

  public static Ops get(final int primary, final int secondary) {
    for(final Ops op : Ops.values()) {
      if(op.primary == primary && (primary != 0 || op.secondary == secondary)) {
        return op;
      }
    }

    throw new IllegalArgumentException("Unknown instruction primary " + Integer.toHexString(primary) + " secondary " + Integer.toHexString(secondary));
  }

  private final int primary;
  private final int secondary;

  Ops(final int primary) {
    this(primary, 0);
  }

  Ops(final int primary, final int secondary) {
    this.primary = primary;
    this.secondary = secondary;
  }
}
