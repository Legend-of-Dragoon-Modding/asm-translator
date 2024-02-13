package org.legendofdragoon.asm.arm;

public class Command {
  public final int address;
  public final int command;
  public final Ops op;

  public Command(final int address, final int command) {
    this.address = address;
    this.command = command;
    this.op = Ops.get(command);
  }
}
