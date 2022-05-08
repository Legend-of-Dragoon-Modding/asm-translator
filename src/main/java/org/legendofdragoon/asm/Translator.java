package org.legendofdragoon.asm;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Translator {
  public List<String> translate(final Asm asm) {
    final Map<Long, String> lines = new LinkedHashMap<>();
    final Set<Long> labels = new HashSet<>();
    final Set<Register> registers = EnumSet.noneOf(Register.class);
    final Set<String> extraVars = new HashSet<>();

    BranchType lastBranchType = BranchType.NONE;

    for(final Command command : asm.commands) {
      BranchType branchType = BranchType.NONE;

      final String line = switch(command.op) {
        case SLL -> {
          registers.add(command.dest());
          registers.add(command.target());
          yield command.dest().name + " = " + command.target().name + " << " + command.immediate5() + ';';
        }

        case SRL -> {
          registers.add(command.dest());
          registers.add(command.target());
          yield command.dest().name + " = " + command.target().name + " >>> " + command.immediate5() + ';';
        }

        case SRA -> {
          registers.add(command.dest());
          registers.add(command.target());
          yield command.dest().name + " = (int)" + command.target().name + " >> " + command.immediate5() + ';';
        }

        case SLLV -> {
          registers.add(command.dest());
          registers.add(command.target());
          registers.add(command.source());
          yield command.dest().name + " = " + command.target().name + " << " + command.source().name + ';';
        }

        case SRLV -> {
          registers.add(command.dest());
          registers.add(command.target());
          registers.add(command.source());
          yield command.dest().name + " = " + command.target().name + " >>> " + command.source().name + ';';
        }

        case SRAV -> {
          registers.add(command.dest());
          registers.add(command.target());
          registers.add(command.source());
          yield command.dest().name + " = (int)" + command.target().name + " >> " + command.source().name + ';';
        }

        case AND -> {
          registers.add(command.dest());
          registers.add(command.target());
          registers.add(command.source());
          yield command.dest().name + " = " + command.source().name + " & " + command.target().name + ';';
        }

        case OR -> {
          registers.add(command.dest());
          registers.add(command.target());
          registers.add(command.source());
          yield command.dest().name + " = " + command.source().name + " | " + command.target().name + ';';
        }

        case XOR -> {
          registers.add(command.dest());
          registers.add(command.target());
          registers.add(command.source());
          yield command.dest().name + " = " + command.source().name + " ^ " + command.target().name + ';';
        }

        case NOR -> {
          registers.add(command.dest());
          registers.add(command.target());
          registers.add(command.source());
          yield command.dest().name + " = ~(" + command.source().name + " | " + command.target().name + ");";
        }

        case SLT -> {
          registers.add(command.dest());
          registers.add(command.target());
          registers.add(command.source());
          yield command.dest().name + " = (int)" + command.source().name + " < (int)" + command.target().name + ';';
        }

        case SLTU -> {
          registers.add(command.dest());
          registers.add(command.target());
          registers.add(command.source());
          yield command.dest().name + " = " + command.source().name + " < " + command.target().name + ';';
        }

        case JR -> {
          registers.add(command.source());

          branchType = BranchType.ALWAYS;

          if(command.source() != Register.RA) {
            yield command.source().name + "();";
          }

          yield "return;";
        }

        case JALR -> {
          registers.add(command.dest());
          registers.add(command.source());

          branchType = BranchType.ALWAYS;

          if(command.source() != Register.RA) {
            yield command.source().name + "();";
          }

          yield "return;";
        }

        case BREAK -> "throw new RuntimeException(\"break\");";

        case MFHI -> {
          registers.add(Register.HI);
          registers.add(command.dest());

          yield command.dest().name + " = " + Register.HI.name + ';';
        }

        case MFLO -> {
          registers.add(Register.LO);
          registers.add(command.dest());

          yield command.dest().name + " = " + Register.LO.name + ';';
        }

        case MULT -> {
          registers.add(Register.HI);
          registers.add(Register.LO);
          registers.add(command.target());
          registers.add(command.source());

          yield
            Register.HI.name + " = ((long)(int)" + command.source().name + " * (int)" + command.target().name + ") >>> 32;\n" +
            Register.LO.name + " = ((long)(int)" + command.source().name + " * (int)" + command.target().name + ") & 0xffff_ffffL;";
        }

        case MULTU -> {
          registers.add(Register.HI);
          registers.add(Register.LO);
          registers.add(command.target());
          registers.add(command.source());

          yield
            Register.HI.name + " = ((" + command.source().name + " & 0xffff_ffffL) * (" + command.target().name + " & 0xffff_ffffL)) >>> 32;\n" +
            Register.LO.name + " = ((" + command.source().name + " & 0xffff_ffffL) * (" + command.target().name + " & 0xffff_ffffL)) & 0xffff_ffffL;";
        }

        case DIV -> {
          registers.add(Register.HI);
          registers.add(Register.LO);
          registers.add(command.target());
          registers.add(command.source());

          yield
            Register.HI.name + " = (int)" + command.source().name + " % (int)" + command.target().name + ";\n" +
            Register.LO.name + " = (int)" + command.source().name + " / (int)" + command.target().name + ';';
        }

        case DIVU -> {
          registers.add(Register.HI);
          registers.add(Register.LO);
          registers.add(command.target());
          registers.add(command.source());

          yield
            Register.HI.name + " = (" + command.source().name + " & 0xffff_ffffL) % (" + command.target().name + " & 0xffff_ffffL);\n" +
            Register.LO.name + " = (" + command.source().name + " & 0xffff_ffffL) / (" + command.target().name + " & 0xffff_ffffL);";
        }

        case ADD, ADDU -> {
          registers.add(command.dest());
          registers.add(command.target());
          registers.add(command.source());
          yield command.dest().name + " = " + command.source().name + " + " + command.target().name + ';';
        }

        case SUB, SUBU -> {
          registers.add(command.dest());
          registers.add(command.target());
          registers.add(command.source());
          yield command.dest().name + " = " + command.source().name + " - " + command.target().name + ';';
        }

        // Both BLTZ and BGEZ
        case BCONDZ -> {
          branchType = BranchType.CONDITIONAL;
          registers.add(command.source());

          final long jump = command.address + 4 + signed(command.immediate16(), 16) * 4;
          labels.add(jump);

          final String comp = (command.command & (1 << 16)) == 0 ? " < " : " >= ";

          yield
            "if((int)" + command.source().name + comp + Register.ZERO.name + ") {\n" +
            "  LAB_" + Long.toHexString(jump) + ";\n" +
            "}";
        }

        case J -> {
          branchType = BranchType.ALWAYS;

          final long jump = (command.address & 0xf000_0000L) + command.immediate26() * 4;
          labels.add(jump);

          yield "LAB_" + Long.toHexString(jump) + ';';
        }

        case JAL -> {
          branchType = BranchType.ALWAYS;

          final long jump = (command.address & 0xf000_0000L) + command.immediate26() * 4;

          yield Register.V0.name + " = " + "FUN_" + Long.toHexString(jump) + "();";
        }

        case BEQ -> {
          branchType = BranchType.CONDITIONAL;
          registers.add(command.target());
          registers.add(command.source());

          final long jump = command.address + 4 + signed(command.immediate16(), 16) * 4;
          labels.add(jump);

          yield
            "if(" + command.source().name + " == " + command.target().name + ") {\n" +
            "  LAB_" + Long.toHexString(jump) + ";\n" +
            "}";
        }

        case BNE -> {
          branchType = BranchType.CONDITIONAL;
          registers.add(command.target());
          registers.add(command.source());

          final long jump = command.address + 4 + signed(command.immediate16(), 16) * 4;
          labels.add(jump);

          yield
            "if(" + command.source().name + " != " + command.target().name + ") {\n" +
            "  LAB_" + Long.toHexString(jump) + ";\n" +
            "}";
        }

        case BLEZ -> {
          branchType = BranchType.CONDITIONAL;
          registers.add(command.source());

          final long jump = command.address + 4 + signed(command.immediate16(), 16) * 4;
          labels.add(jump);

          yield
            "if((int)" + command.source().name + " <= " + Register.ZERO.name + ") {\n" +
            "  LAB_" + Long.toHexString(jump) + ";\n" +
            "}";
        }

        case BGTZ -> {
          branchType = BranchType.CONDITIONAL;
          registers.add(command.source());

          final long jump = command.address + 4 + signed(command.immediate16(), 16) * 4;
          labels.add(jump);

          yield
            "if((int)" + command.source().name + " > " + Register.ZERO.name + ") {\n" +
            "  LAB_" + Long.toHexString(jump) + ";\n" +
            "}";
        }

        case ADDIU -> {
          registers.add(command.target());
          registers.add(command.source());
          yield command.target().name + " = " + command.source().name + " + " + signedHex(command.immediate16(), 16) + "L;";
        }

        case SLTI -> {
          registers.add(command.target());
          registers.add(command.source());
          yield command.target().name + " = (int)" + command.source().name + " < " + signedHex(command.immediate16(), 16) + "L;";
        }

        case SLTIU -> {
          registers.add(command.target());
          registers.add(command.source());
          yield command.target().name + " = " + command.source().name + " < 0x" + Long.toHexString(command.immediate16()) + "L;";
        }

        case ANDI -> {
          registers.add(command.target());
          registers.add(command.source());
          yield command.target().name + " = " + command.source().name + " & 0x" + Long.toHexString(command.immediate16()) + "L;";
        }

        case ORI -> {
          registers.add(command.target());
          registers.add(command.source());
          yield command.target().name + " = " + command.source().name + " | 0x" + Long.toHexString(command.immediate16()) + "L;";
        }

        case XORI -> {
          registers.add(command.target());
          registers.add(command.source());
          yield command.target().name + " = " + command.source().name + " ^ 0x" + Long.toHexString(command.immediate16()) + "L;";
        }

        case LUI -> {
          registers.add(command.target());
          yield command.target().name + " = 0x" + Long.toHexString(command.immediate16()) + "_0000L;";
        }

        case LB -> {
          if(command.source() == Register.SP) {
            registers.add(command.target());
            final String var = "sp" + Long.toHexString(command.immediate16());
            extraVars.add(var);
            yield command.target().name + " = (byte)" + var + ';';
          }

          registers.add(command.target());
          registers.add(command.source());
          yield command.target().name + " = MEMORY.ref(1, " + command.source().name + ").offset(" + signedHex(command.immediate16(), 16) + "L).getSigned();";
        }

        case LH -> {
          if(command.source() == Register.SP) {
            registers.add(command.target());
            final String var = "sp" + Long.toHexString(command.immediate16());
            extraVars.add(var);
            yield command.target().name + " = (short)" + var + ';';
          }

          registers.add(command.target());
          registers.add(command.source());
          yield command.target().name + " = MEMORY.ref(2, " + command.source().name + ").offset(" + signedHex(command.immediate16(), 16) + "L).getSigned();";
        }

        case LW -> {
          if(command.source() == Register.SP) {
            registers.add(command.target());
            final String var = "sp" + Long.toHexString(command.immediate16());
            extraVars.add(var);
            yield command.target().name + " = " + var + ';';
          }

          registers.add(command.target());
          registers.add(command.source());
          yield command.target().name + " = MEMORY.ref(4, " + command.source().name + ").offset(" + signedHex(command.immediate16(), 16) + "L).get();";
        }

        case LBU -> {
          if(command.source() == Register.SP) {
            registers.add(command.target());
            final String var = "sp" + Long.toHexString(command.immediate16());
            extraVars.add(var);
            yield command.target().name + " = " + var + ';';
          }

          registers.add(command.target());
          registers.add(command.source());
          yield command.target().name + " = MEMORY.ref(1, " + command.source().name + ").offset(" + signedHex(command.immediate16(), 16) + "L).get();";
        }

        case LHU -> {
          if(command.source() == Register.SP) {
            registers.add(command.target());
            final String var = "sp" + Long.toHexString(command.immediate16());
            extraVars.add(var);
            yield command.target().name + " = " + var + ';';
          }

          registers.add(command.target());
          registers.add(command.source());
          yield command.target().name + " = MEMORY.ref(2, " + command.source().name + ").offset(" + signedHex(command.immediate16(), 16) + "L).get();";
        }

        case SB -> {
          if(command.source() == Register.SP) {
            registers.add(command.target());
            final String var = "sp" + Long.toHexString(command.immediate16());
            extraVars.add(var);
            yield var + " = " + command.target().name + ';';
          }

          registers.add(command.target());
          registers.add(command.source());
          yield "MEMORY.ref(1, " + command.source().name + ").offset(" + signedHex(command.immediate16(), 16) + "L).setu(" + command.target().name + ");";
        }

        case SH -> {
          if(command.source() == Register.SP) {
            registers.add(command.target());
            final String var = "sp" + Long.toHexString(command.immediate16());
            extraVars.add(var);
            yield var + " = " + command.target().name + ';';
          }

          registers.add(command.target());
          registers.add(command.source());
          yield "MEMORY.ref(2, " + command.source().name + ").offset(" + signedHex(command.immediate16(), 16) + "L).setu(" + command.target().name + ");";
        }

        case SW -> {
          if(command.source() == Register.SP) {
            registers.add(command.target());
            final String var = "sp" + Long.toHexString(command.immediate16());
            extraVars.add(var);
            yield var + " = " + command.target().name + ';';
          }

          registers.add(command.target());
          registers.add(command.source());
          yield "MEMORY.ref(4, " + command.source().name + ").offset(" + signedHex(command.immediate16(), 16) + "L).setu(" + command.target().name + ");";
        }

        case LWC2 -> {
          registers.add(command.target());
          registers.add(command.source());
          yield "CPU.MTC2(MEMORY.ref(4, " + command.source().name + ").offset(" + signedHex(command.immediate16(), 16) + "L).get(), " + command.target().ordinal() + ");";
        }

        case SWC2 -> {
          registers.add(command.target());
          registers.add(command.source());
          yield "MEMORY.ref(4, " + command.source().name + ").offset(" + signedHex(command.immediate16(), 16) + "L).setu(CPU.MFC2(" + command.target().ordinal() + "));";
        }

        case COP2 -> {
          registers.add(command.target());

          yield switch((int)(command.immediate26() >>> 21) & 0x1f) {
            // MFC
            case 0b0000 -> command.target().name + " = CPU.MFC2(" + command.dest().ordinal() + ");";
            // CFC
            case 0b0010 -> command.target().name + " = CPU.CFC2(" + command.dest().ordinal() + ");";
            // MTC
            case 0b0100 -> "CPU.MTC2(" + command.target().name + ", " + command.dest().ordinal() + ");";
            // CTC
            case 0b0110 -> "CPU.CTC2(" + command.target().name + ", " + command.dest().ordinal() + ");";
            // COP2
            default -> "CPU.COP2(0x" + Long.toHexString(command.immediate26() & 0x1ff_ffffL) + "L);";
          };
        }

        case NOOP -> "";

        default -> "//TODO Unsupported operation " + command.op + " at address " + Long.toHexString(command.address);
      };

      // Add output line (deals with branch delay by reordering and/or duplicating lines)
      if(lastBranchType == BranchType.NONE) {
        lines.put(command.address, line);
      } else if(lastBranchType == BranchType.ALWAYS) {
        lines.merge(command.address - 4, line, (current, added) -> added + "\n" + current);
      } else if(lastBranchType == BranchType.CONDITIONAL) {
        lines.merge(command.address - 4, line, (current, added) -> {
          final String[] split = current.split("\n");
          return split[0] + "\n  " + added + "\n" + split[1] + "\n" + split[2] + "\n" + added;
        });
      }

      lastBranchType = branchType;
    }

    // Prepend jump destinations
    for(final long address : labels) {
      lines.merge(address, "\n//LAB_" + Long.toHexString(address), (current, added) -> added + "\n" + current);
    }

    final List<String> output = new ArrayList<>();

    // Add register variable definitions to output
    for(final Register register : registers) {
      if(register != Register.ZERO) {
        output.add("long " + register.name + ';');
      }
    }

    // Add extra variable definitions to output
    for(final String var : extraVars) {
      output.add("long " + var + ';');
    }

    // Add code to output
    output.addAll(lines.values());

    return output;
  }

  private static long signed(final long val, final int bits) {
    if((val & 1L << bits - 1) != 0) {
      return val | -(1L << bits);
    }

    return val;
  }

  private static String signedHex(final long val, final int bits) {
    final long value = signed(val, bits);

    if(value < 0) {
      return "-0x" + Long.toHexString(-value);
    }

    return "0x" + Long.toHexString(value);
  }
}
