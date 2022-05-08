package org.legendofdragoon.asm;

public class Command {
  public final long address;
  public final long command;
  public final Ops op;

  public Command(final long address, final long command) {
    this.address = address;
    this.command = command;

    if(command != 0) {
      final int primaryOp = (int)(command >>> 26);
      final int secondaryOp = (int)(command & 0x3f);
      this.op = Ops.get(primaryOp, secondaryOp);
    } else {
      this.op = Ops.NOOP;
    }
  }

  public Register dest() {
    return Register.values()[(int)((this.command >>> 11) & 0x1f)];
  }

  public Register target() {
    return Register.values()[(int)((this.command >>> 16) & 0x1f)];
  }

  public Register source() {
    return Register.values()[(int)((this.command >>> 21) & 0x1f)];
  }

  public long immediate5() {
    return (this.command >> 6) & 0x1f;
  }

  public long immediate16() {
    return this.command & 0xffffL;
  }

  public long immediate26() {
    return this.command & 0x3ff_ffffL;
  }
}
