package org.legendofdragoon.asm.thumb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Translator {
  public List<String> translate(final Asm asm) {
    final Map<Integer, String> lines = new LinkedHashMap<>();
    final Set<Integer> labels = new HashSet<>();

    final int firstAddress = asm.commands.get(0).address;
    final int lastAddress = asm.commands.get(asm.commands.size() - 1).address;

    for(final Command command : asm.commands) {
      final String line = switch(command.op) {
        // THUMB1
        case LSL -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register src = Register.values()[command.command >> 3 & 0x7];
          final int offset = command.command >> 6 & 0x1f;
          yield "%1$s = CPU.lslT(%2$s, %3$d);".formatted(dest.fullName(), src.fullName(), offset);
        }

        // THUMB1
        case LSR -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register src = Register.values()[command.command >> 3 & 0x7];
          final int offset = command.command >> 6 & 0x1f;
          yield "%1$s = CPU.lsrT(%2$s, %3$d);".formatted(dest.fullName(), src.fullName(), offset);
        }

        // THUMB1
        case ASR -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register src = Register.values()[command.command >> 3 & 0x7];
          final int offset = command.command >> 6 & 0x1f;
          yield "%1$s = CPU.asrT(%2$s, %3$d);".formatted(dest.fullName(), src.fullName(), offset);
        }

        // THUMB2
        case ADD_REG -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register src = Register.values()[command.command >> 3 & 0x7];
          final Register operand = Register.values()[command.command >> 6 & 0x7];
          yield "%s = CPU.addT(%s, %s);".formatted(dest.fullName(), src.fullName(), operand.fullName());
        }

        // THUMB2
        case SUB_REG -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register src = Register.values()[command.command >> 3 & 0x7];
          final Register operand = Register.values()[command.command >> 6 & 0x7];
          yield "%s = CPU.subT(%s, %s);".formatted(dest.fullName(), src.fullName(), operand.fullName());
        }

        // THUMB2
        case ADD_IMM -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register src = Register.values()[command.command >> 3 & 0x7];
          final int immediate = command.command >> 6 & 0x7;
          yield "%s = CPU.addT(%s, 0x%x);".formatted(dest.fullName(), src.fullName(), immediate);
        }

        // THUMB2
        case SUB_IMM -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register src = Register.values()[command.command >> 3 & 0x7];
          final int immediate = command.command >> 6 & 0x7;
          yield "%s = CPU.subT(%s, 0x%x);".formatted(dest.fullName(), src.fullName(), immediate);
        }

        // THUMB3
        case MOV_IMM -> {
          final Register dest = Register.values()[command.command >> 8 & 0x7];
          final int immediate = command.command & 0xff;
          yield "%1$s = CPU.movT(0, 0x%2$x);".formatted(dest.fullName(), immediate);
        }

        // THUMB3
        case CMP_IMM -> {
          final Register dest = Register.values()[command.command >> 8 & 0x7];
          final int immediate = command.command & 0xff;
          yield "CPU.cmpT(%1$s, 0x%2$x);".formatted(dest.fullName(), immediate);
        }

        // THUMB3
        case ADD_IMM_U -> {
          final Register dest = Register.values()[command.command >> 8 & 0x7];
          final int immediate = command.command & 0xff;
          yield "%1$s = CPU.addT(%1$s, 0x%2$x);".formatted(dest.fullName(), immediate);
        }

        // THUMB3
        case SUB_IMM_U -> {
          final Register dest = Register.values()[command.command >> 8 & 0x7];
          final int immediate = command.command & 0xff;
          yield "%1$s = CPU.subT(%1$s, 0x%2$x);".formatted(dest.fullName(), immediate);
        }

        // THUMB4
        case AND_ALU, EOR_ALU, LSL_ALU, LSR_ALU, ASR_ALU, ADC_ALU, SBC_ALU, ROR_ALU, TST_ALU, NEG_ALU, CMP_ALU, CMN_ALU, ORR_ALU, MUL_ALU, BIC_ALU, MVN_ALU -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register src = Register.values()[command.command >> 3 & 0x7];
          yield "%1$s = CPU.%3$sT(%1$s, %2$s);".formatted(dest.fullName(), src.fullName(), command.op.name().substring(0, command.op.name().length() - 4).toLowerCase());
        }

        // THUMB5
        case ADD_HI -> {
          final Register dest = Register.values()[command.command & 0x7 | (command.command >> 7 & 0x1) << 3];
          final Register src = Register.values()[command.command >> 3 & 0x7 | (command.command >> 6 & 0x1) << 3];

          if(src == Register.R15_PC) {
            yield "%s += %s + 0x4;".formatted(dest.fullName(), src.fullName());
          }

          if(dest == Register.R15_PC) {
            throw new RuntimeException("PC add not implemented @ 0x%x".formatted(command.address));
          }

          yield "%s += %s;".formatted(dest.fullName(), src.fullName());
        }

        // THUMB5
        case CMP_HI -> {
          final Register dest = Register.values()[command.command & 0x7 | (command.command >> 7 & 0x1) << 3];
          final Register src = Register.values()[command.command >> 3 & 0x7 | (command.command >> 6 & 0x1) << 3];

          if(dest == Register.R15_PC || src == Register.R15_PC) {
            throw new RuntimeException("PC add not implemented");
          }

          yield "CPU.cmpT(%s, %s);".formatted(dest.fullName(), src.fullName());
        }

        // THUMB5
        case MOV_HI -> {
          final Register dest = Register.values()[command.command & 0x7 | (command.command >> 7 & 0x1) << 3];
          final Register src = Register.values()[command.command >> 3 & 0x7 | (command.command >> 6 & 0x1) << 3];

          final String srcValue;
          if(src == Register.R15_PC) {
            srcValue = "0x%07x".formatted(command.address + 0x4);
          } else {
            srcValue = "%s".formatted(src.fullName());
          }

          if(dest == Register.R15_PC) {
            if(src == Register.R14_LR) {
              yield "return %s;".formatted(Register.R0.fullName());
            }

            yield "//TODO PC SET 0x%x\n%s = %s;".formatted(command.address, dest.fullName(), srcValue);
          }

          yield "%s = %s;".formatted(dest.fullName(), srcValue);
        }

        // THUMB5
        case BX -> {
          final Register src = Register.values()[command.command >> 3 & 0x7 | (command.command >> 6 & 0x1) << 3];

          if(src == Register.R14_LR) {
            yield "return %s;".formatted(Register.R0.fullName());
          }

          if(src == Register.R15_PC) {
            yield "%s = MEMORY.call(0x%07x);".formatted(Register.R0.fullName(), command.address + 0x4);
          }

          yield "%s = MEMORY.call(%s);".formatted(Register.R0.fullName(), src.fullName());
        }

        // THUMB6
        case LDRPC -> {
          final Register dest = Register.values()[command.command >> 8 & 0x7];
          final int offset = command.command & 0xff;
          final int address = (command.address + 0x4 + offset * 0x4) & ~0x2;
          yield "%s = MEMORY.ref(4, 0x%07x).get();".formatted(dest.fullName(), address);
        }

        // THUMB7
        case STR_REG -> {
          final Register src = Register.values()[command.command & 0x7];
          final Register base = Register.values()[command.command >> 3 & 0x7];
          final Register offset = Register.values()[command.command >> 6 & 0x7];
          yield "MEMORY.ref(4, %s + %s).setu(%s);".formatted(base.fullName(), offset.fullName(), src.fullName());
        }

        // THUMB7
        case STRB_REG -> {
          final Register src = Register.values()[command.command & 0x7];
          final Register base = Register.values()[command.command >> 3 & 0x7];
          final Register offset = Register.values()[command.command >> 6 & 0x7];
          yield "MEMORY.ref(1, %s + %s).setu(%s);".formatted(base.fullName(), offset.fullName(), src.fullName());
        }

        // THUMB7
        case LDR_REG -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register base = Register.values()[command.command >> 3 & 0x7];
          final Register offset = Register.values()[command.command >> 6 & 0x7];
          yield "%s = MEMORY.ref(4, %s + %s).get();".formatted(dest.fullName(), base.fullName(), offset.fullName());
        }

        // THUMB7
        case LDRB_REG -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register base = Register.values()[command.command >> 3 & 0x7];
          final Register offset = Register.values()[command.command >> 6 & 0x7];
          yield "%s = MEMORY.ref(1, %s + %s).getUnsigned();".formatted(dest.fullName(), base.fullName(), offset.fullName());
        }

        // THUMB8
        case STRH_REG -> {
          final Register offset = Register.values()[command.command >> 6 & 0x7];
          final Register base = Register.values()[command.command >> 3 & 0x7];
          final Register dest = Register.values()[command.command & 0x7];
          yield "MEMORY.ref(2, %s + %s).setu(%s);".formatted(base.fullName(), offset.fullName(), dest.fullName());
        }

        // THUMB8
        case LDSB_REG -> {
          final Register offset = Register.values()[command.command >> 6 & 0x7];
          final Register base = Register.values()[command.command >> 3 & 0x7];
          final Register dest = Register.values()[command.command & 0x7];
          yield "%s = MEMORY.ref(1, %s + %s).get();".formatted(dest.fullName(), base.fullName(), offset.fullName());
        }

        // THUMB8
        case LDRH_REG -> {
          final Register offset = Register.values()[command.command >> 6 & 0x7];
          final Register base = Register.values()[command.command >> 3 & 0x7];
          final Register dest = Register.values()[command.command & 0x7];
          yield "%s = MEMORY.ref(2, %s + %s).getUnsigned();".formatted(dest.fullName(), base.fullName(), offset.fullName());
        }

        // THUMB8
        case LDSH_REG -> {
          final Register offset = Register.values()[command.command >> 6 & 0x7];
          final Register base = Register.values()[command.command >> 3 & 0x7];
          final Register dest = Register.values()[command.command & 0x7];
          yield "%s = MEMORY.ref(2, %s + %s).get();".formatted(dest.fullName(), base.fullName(), offset.fullName());
        }

        // THUMB9
        case STR_IMM -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register base = Register.values()[command.command >> 3 & 0x7];
          final int offset = (command.command >> 6 & 0x1f) * 0x4;
          yield "MEMORY.ref(4, %s + 0x%x).setu(%s);".formatted(base.fullName(), offset, dest.fullName());
        }

        // THUMB9
        case LDR_IMM -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register base = Register.values()[command.command >> 3 & 0x7];
          final int offset = (command.command >> 6 & 0x1f) * 0x4;
          yield "%s = MEMORY.ref(4, %s + 0x%x).get();".formatted(dest.fullName(), base.fullName(), offset);
        }

        // THUMB9
        case STRB_IMM -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register base = Register.values()[command.command >> 3 & 0x7];
          final int offset = command.command >> 6 & 0x1f;
          yield "MEMORY.ref(1, %s + 0x%x).setu(%s);".formatted(base.fullName(), offset, dest.fullName());
        }

        // THUMB9
        case LDRB_IMM -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register base = Register.values()[command.command >> 3 & 0x7];
          final int offset = command.command >> 6 & 0x1f;
          yield "%s = MEMORY.ref(1, %s + 0x%x).getUnsigned();".formatted(dest.fullName(), base.fullName(), offset);
        }

        // THUMB10
        case STRH_IMM -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register base = Register.values()[command.command >> 3 & 0x7];
          final int offset = (command.command >> 6 & 0x1f) * 0x2;
          yield "MEMORY.ref(2, %s + 0x%x).setu(%s);".formatted(base.fullName(), offset, dest.fullName());
        }

        // THUMB10
        case LDRH_IMM -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register base = Register.values()[command.command >> 3 & 0x7];
          final int offset = (command.command >> 6 & 0x1f) * 0x2;
          yield "%s = MEMORY.ref(2, %s + 0x%x).getUnsigned();".formatted(dest.fullName(), base.fullName(), offset);
        }

        // THUMB11
        case STR_SP -> {
          final int offset = (command.command & 0xff) * 0x4;
          final Register dest = Register.values()[command.command >>> 8 & 0x7];
          yield "MEMORY.ref(4, %s + 0x%x).setu(%s);".formatted(Register.R13_SP.fullName(), offset, dest.fullName());
        }

        // THUMB11
        case LDR_SP -> {
          final int offset = (command.command & 0xff) * 0x4;
          final Register dest = Register.values()[command.command >>> 8 & 0x7];
          yield "%s = MEMORY.ref(4, %s + 0x%x).get();".formatted(dest.fullName(), Register.R13_SP.fullName(), offset);
        }

        // THUMB12
        case ADDRESS -> {
          final boolean isSp = (command.command >>> 11 & 0x1) != 0;
          final Register dest = Register.values()[command.command >>> 8 & 0x7];
          final int offset = (command.command & 0xff) * 0x4;

          if(isSp) {
            yield "%s = %s + 0x%x;".formatted(dest.fullName(), Register.R13_SP.fullName(), offset);
          }

          yield "%s = 0x%07x;".formatted(dest.fullName(), (command.address + 0x4 & ~0x2) + offset);
        }

        // THUMB13
        case ADD_SP -> {
          final boolean negative = (command.command >> 7 & 0x1) != 0;
          final int offset = (command.command & 0x7f) * 0x4;

          if(negative) {
            yield "%s -= 0x%x;".formatted(Register.R13_SP.fullName(), offset);
          }

          yield "%s += 0x%x;".formatted(Register.R13_SP.fullName(), offset);
        }

        // THUMB14
        case PUSH -> {
          final List<Register> rlist = Register.unpack(command.command & 0xff);
          final boolean lrpc = (command.command >> 8 & 0x1) != 0;

          final StringBuilder builder = new StringBuilder();
          if(lrpc) {
            builder.append("CPU.push(%s);\n".formatted(Register.R14_LR.fullName()));
          }

          for(int i = rlist.size() - 1; i >= 0; i--) {
            builder.append("CPU.push(%s);\n".formatted(rlist.get(i).fullName()));
          }

          yield builder.toString();
        }

        // THUMB14
        case POP -> {
          final List<Register> rlist = Register.unpack(command.command & 0xff);
          final boolean lrpc = (command.command >> 8 & 0x1) != 0;

          final StringBuilder builder = new StringBuilder();
          for(final Register r : rlist) {
            builder.append("%s = CPU.pop();\n".formatted(r.fullName()));
          }

          if(lrpc) {
            builder.append("%s = CPU.pop();".formatted(Register.R15_PC.fullName()));
            builder.append("\n//TODO PC changed");
          }

          yield builder.toString();
        }

        // THUMB15
        case STMIA -> {
          final List<Register> rlist = Register.unpack(command.command & 0xff);
          final Register base = Register.values()[command.command >> 8 & 0x7];

          final StringBuilder builder = new StringBuilder();
          for(final Register r : rlist) {
            builder
              .append("MEMORY.ref(4, %s).setu(%s);\n".formatted(base.fullName(), r.fullName()))
              .append("%s += 0x4;\n".formatted(base.fullName()));
          }

          yield builder.toString();
        }

        // THUMB15
        case LDMIA -> {
          final List<Register> rlist = Register.unpack(command.command & 0xff);
          final Register base = Register.values()[command.command >> 8 & 0x7];

          final StringBuilder builder = new StringBuilder();
          for(final Register r : rlist) {
            builder
              .append("%s = MEMORY.ref(4, %s).get();\n".formatted(r.fullName(), base.fullName()))
              .append("%s += 0x4;\n".formatted(base.fullName()));
          }

          yield builder.toString();
        }

        // THUMB16
        case BEQ -> {
          final int offset = sign(command.command & 0xff, 8) * 0x2;
          final int address = command.address + 0x4 + offset;

          if(address >= firstAddress && address <= lastAddress) {
            labels.add(address);

            yield
              "if(CPU.cpsr().getZero()) { // ==\n" +
              "  LAB_%07x;\n".formatted(address) +
              '}';
          }

          yield
            "if(CPU.cpsr().getZero()) { // ==\n" +
            "  %s = FUN_%07x(); //TODO branch\n".formatted(Register.R0.fullName(), address) +
            '}';
        }

        // THUMB16
        case BNE -> {
          final int offset = sign(command.command & 0xff, 8) * 0x2;
          final int address = command.address + 0x4 + offset;

          if(address >= firstAddress && address <= lastAddress) {
            labels.add(address);

            yield
              "if(!CPU.cpsr().getZero()) { // !=\n" +
              "  LAB_%07x;\n".formatted(address) +
              '}';
          }

          yield
            "if(!CPU.cpsr().getZero()) { // !=\n" +
            "  %s = FUN_%07x(); //TODO branch\n".formatted(Register.R0.fullName(), address) +
            '}';
        }

        // THUMB16
        case BCS -> {
          final int offset = sign(command.command & 0xff, 8) * 0x2;
          final int address = command.address + 0x4 + offset;
          labels.add(address);

          yield
            "if(CPU.cpsr().getCarry()) { // unsigned >=\n" +
            "  LAB_%07x;\n".formatted(address) +
            '}';
        }

        // THUMB16
        case BCC -> {
          final int offset = sign(command.command & 0xff, 8) * 0x2;
          final int address = command.address + 0x4 + offset;
          labels.add(address);

          yield
            "if(!CPU.cpsr().getCarry()) { // unsigned <\n" +
            "  LAB_%07x;\n".formatted(address) +
            '}';
        }

        // THUMB16
        case BMI -> {
          final int offset = sign(command.command & 0xff, 8) * 0x2;
          final int address = command.address + 0x4 + offset;
          labels.add(address);

          yield
            "if(CPU.cpsr().getNegative()) { // negative\n" +
            "  LAB_%07x;\n".formatted(address) +
            '}';
        }

        // THUMB16
        case BPL -> {
          final int offset = sign(command.command & 0xff, 8) * 0x2;
          final int address = command.address + 0x4 + offset;
          labels.add(address);

          yield
            "if(!CPU.cpsr().getNegative()) { // positive or 0\n" +
            "  LAB_%07x;\n".formatted(address) +
            '}';
        }

        // THUMB16
        case BVS -> {
          final int offset = sign(command.command & 0xff, 8) * 0x2;
          final int address = command.address + 0x4 + offset;
          labels.add(address);

          yield
            "if(CPU.cpsr().getOverflow()) { // signed overflow\n" +
            "  LAB_%07x;\n".formatted(address) +
            '}';
        }

        // THUMB16
        case BVC -> {
          final int offset = sign(command.command & 0xff, 8) * 0x2;
          final int address = command.address + 0x4 + offset;
          labels.add(address);

          yield
            "if(!CPU.cpsr().getOverflow()) { // signed no overflow\n" +
            "  LAB_%07x;\n".formatted(address) +
            '}';
        }

        // THUMB16
        case BHI -> {
          final int offset = sign(command.command & 0xff, 8) * 0x2;
          final int address = command.address + 0x4 + offset;
          labels.add(address);

          yield
            "if(CPU.cpsr().getCarry() && !CPU.cpsr().getZero()) { // unsigned >\n" +
            "  LAB_%07x;\n".formatted(address) +
            '}';
        }

        // THUMB16
        case BLS -> {
          final int offset = sign(command.command & 0xff, 8) * 0x2;
          final int address = command.address + 0x4 + offset;
          labels.add(address);

          yield
            "if(!CPU.cpsr().getCarry() || CPU.cpsr().getZero()) { // unsigned <=\n" +
            "  LAB_%07x;\n".formatted(address) +
            '}';
        }

        // THUMB16
        case BGE -> {
          final int offset = sign(command.command & 0xff, 8) * 0x2;
          final int address = command.address + 0x4 + offset;
          labels.add(address);

          yield
            "if(CPU.cpsr().getNegative() == CPU.cpsr().getOverflow()) { // >=\n" +
            "  LAB_%07x;\n".formatted(address) +
            '}';
        }

        // THUMB16
        case BLT -> {
          final int offset = sign(command.command & 0xff, 8) * 0x2;
          final int address = command.address + 0x4 + offset;
          labels.add(address);

          yield
            "if(CPU.cpsr().getNegative() != CPU.cpsr().getOverflow()) { // <\n" +
            "  LAB_%07x;\n".formatted(address) +
            '}';
        }

        // THUMB16
        case BGT -> {
          final int offset = sign(command.command & 0xff, 8) * 0x2;
          final int address = command.address + 0x4 + offset;
          labels.add(address);

          yield
            "if(!CPU.cpsr().getZero() && CPU.cpsr().getNegative() == CPU.cpsr().getOverflow()) { // >\n" +
            "  LAB_%07x;\n".formatted(address) +
            '}';
        }

        // THUMB16
        case BLE -> {
          final int offset = sign(command.command & 0xff, 8) * 0x2;
          final int address = command.address + 0x4 + offset;
          labels.add(address);

          yield
            "if(CPU.cpsr().getZero() || CPU.cpsr().getNegative() != CPU.cpsr().getOverflow()) { // <=\n" +
            "  LAB_%07x;\n".formatted(address) +
            '}';
        }

        // THUMB17
        case SWI -> "%s = 0x%x;\n%s = CPU.SWI(InstructionSet.THUMB); // 0x%x".formatted(Register.R15_PC.fullName(), command.address + 0x2, Register.R0.fullName(), command.command & 0xff);

        // THUMB18
        case B -> {
          final int offset = sign(command.command & 0x7ff, 11) * 0x2;
          final int address = command.address + 0x4 + offset;

          if(address >= firstAddress && address <= lastAddress) {
            labels.add(address);
            yield "LAB_%07x;".formatted(address);
          }

          yield "%s = FUN_%07x(); //TODO branch".formatted(Register.R0.fullName(), address);
        }

        // THUMB19
        case BL, BLX -> {
          final int lower = command.command & 0x7ff;
          final int upper = command.command >>> 16 & 0x7ff;
          final int offset = upper << 1 | lower << 12;
          final int address = command.address + 0x4 + sign(offset, 23);

          if(address >= firstAddress && address <= lastAddress) {
            labels.add(address);
            yield "LAB_%07x;".formatted(address);
          }

          yield "%s = FUN_%07x();".formatted(Register.R0.fullName(), address);
        }

        default -> "//TODO Unsupported operation " + command.op + " at address " + Integer.toHexString(command.address);
      };

      lines.put(command.address, line);
    }

    // Prepend jump destinations
    for(final int address : labels) {
      lines.merge(address, "\n//LAB_%07x".formatted(address), (current, added) -> added + '\n' + current);
    }

    // Add code to output
    return new ArrayList<>(lines.values());
  }

  private static int sign(final int value, final int numberOfBits) {
    if((value & 1 << numberOfBits - 1) != 0) {
      return value | -(1 << numberOfBits);
    }

    return value;
  }
}
