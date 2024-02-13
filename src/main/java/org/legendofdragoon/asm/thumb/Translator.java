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
          yield "CPU.%1$s().value = CPU.lslT(CPU.%2$s().value, %3$d);".formatted(dest.name, src.name, offset);
        }

        // THUMB1
        case LSR -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register src = Register.values()[command.command >> 3 & 0x7];
          final int offset = command.command >> 6 & 0x1f;
          yield "CPU.%1$s().value = CPU.lsrT(CPU.%2$s().value, %3$d);".formatted(dest.name, src.name, offset);
        }

        // THUMB1
        case ASR -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register src = Register.values()[command.command >> 3 & 0x7];
          final int offset = command.command >> 6 & 0x1f;
          yield "CPU.%1$s().value = CPU.asrT(CPU.%2$s().value, %3$d);".formatted(dest.name, src.name, offset);
        }

        // THUMB2
        case ADD_REG -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register src = Register.values()[command.command >> 3 & 0x7];
          final Register operand = Register.values()[command.command >> 6 & 0x7];
          yield "CPU.%s().value = CPU.addT(CPU.%s().value, CPU.%s().value);".formatted(dest.name, src.name, operand.name);
        }

        // THUMB2
        case SUB_REG -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register src = Register.values()[command.command >> 3 & 0x7];
          final Register operand = Register.values()[command.command >> 6 & 0x7];
          yield "CPU.%s().value = CPU.subT(CPU.%s().value, CPU.%s().value);".formatted(dest.name, src.name, operand.name);
        }

        // THUMB2
        case ADD_IMM -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register src = Register.values()[command.command >> 3 & 0x7];
          final int immediate = command.command >> 6 & 0x7;
          yield "CPU.%s().value = CPU.addT(CPU.%s().value, 0x%x);".formatted(dest.name, src.name, immediate);
        }

        // THUMB2
        case SUB_IMM -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register src = Register.values()[command.command >> 3 & 0x7];
          final int immediate = command.command >> 6 & 0x7;
          yield "CPU.%s().value = CPU.subT(CPU.%s().value, 0x%x);".formatted(dest.name, src.name, immediate);
        }

        // THUMB3
        case MOV_IMM -> {
          final Register dest = Register.values()[command.command >> 8 & 0x7];
          final int immediate = command.command & 0xff;
          yield "CPU.%1$s().value = CPU.movT(0, 0x%2$x);".formatted(dest.name, immediate);
        }

        // THUMB3
        case CMP_IMM -> {
          final Register dest = Register.values()[command.command >> 8 & 0x7];
          final int immediate = command.command & 0xff;
          yield "CPU.cmpT(CPU.%1$s().value, 0x%2$x);".formatted(dest.name, immediate);
        }

        // THUMB3
        case ADD_IMM_U -> {
          final Register dest = Register.values()[command.command >> 8 & 0x7];
          final int immediate = command.command & 0xff;
          yield "CPU.%1$s().value = CPU.addT(CPU.%1$s().value, 0x%2$x);".formatted(dest.name, immediate);
        }

        // THUMB3
        case SUB_IMM_U -> {
          final Register dest = Register.values()[command.command >> 8 & 0x7];
          final int immediate = command.command & 0xff;
          yield "CPU.%1$s().value = CPU.subT(CPU.%1$s().value, 0x%2$x);".formatted(dest.name, immediate);
        }

        // THUMB4
        case AND_ALU, EOR_ALU, LSL_ALU, LSR_ALU, ASR_ALU, ADC_ALU, SBC_ALU, ROR_ALU, TST_ALU, NEG_ALU, CMP_ALU, CMN_ALU, ORR_ALU, MUL_ALU, BIC_ALU, MVN_ALU -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register src = Register.values()[command.command >> 3 & 0x7];
          yield "CPU.%1$s().value = CPU.%3$sT(CPU.%1$s().value, CPU.%2$s().value);".formatted(dest.name, src.name, command.op.name().substring(0, command.op.name().length() - 4).toLowerCase());
        }

        // THUMB5
        case ADD_HI -> {
          final Register dest = Register.values()[command.command & 0x7 | (command.command >> 7 & 0x1) << 3];
          final Register src = Register.values()[command.command >> 3 & 0x7 | (command.command >> 6 & 0x1) << 3];

          if(dest == Register.R15_PC || src == Register.R15_PC) {
            throw new RuntimeException("PC add not implemented");
          }

          yield "CPU.%s().value += CPU.%s().value;".formatted(dest.name, src.name);
        }

        // THUMB5
        case CMP_HI -> {
          final Register dest = Register.values()[command.command & 0x7 | (command.command >> 7 & 0x1) << 3];
          final Register src = Register.values()[command.command >> 3 & 0x7 | (command.command >> 6 & 0x1) << 3];

          if(dest == Register.R15_PC || src == Register.R15_PC) {
            throw new RuntimeException("PC add not implemented");
          }

          yield "CPU.cmpT(CPU.%s().value, CPU.%s().value);".formatted(dest.name, src.name);
        }

        // THUMB5
        case MOV_HI -> {
          final Register dest = Register.values()[command.command & 0x7 | (command.command >> 7 & 0x1) << 3];
          final Register src = Register.values()[command.command >> 3 & 0x7 | (command.command >> 6 & 0x1) << 3];

          final String srcValue;
          if(src == Register.R15_PC) {
            srcValue = "0x%07x".formatted(command.address + 0x4);
          } else {
            srcValue = "CPU.%s().value".formatted(src.name);
          }

          if(dest == Register.R15_PC) {
            throw new RuntimeException("PC add not implemented");
          }

          yield "CPU.%s().value = %s;".formatted(dest.name, srcValue);
        }

        // THUMB5
        case BX -> {
          final Register src = Register.values()[command.command >> 3 & 0x7 | (command.command >> 6 & 0x1) << 3];

          if(src == Register.R14_LR) {
            yield "return;";
          }

          if(src == Register.R15_PC) {
            yield "MEMORY.call(0x%07x);".formatted(command.address + 0x4);
          }

          yield "MEMORY.call(CPU.%s().value);".formatted(src.name);
        }

        // THUMB6
        case LDRPC -> {
          final Register dest = Register.values()[command.command >> 8 & 0x7];
          final int offset = command.command & 0xff;
          final int address = (command.address + 0x4 + offset * 0x4) & ~0x2;
          yield "CPU.%s().value = MEMORY.ref(4, 0x%07x).get();".formatted(dest.name, address);
        }

        // THUMB7
        case STR_REG -> {
          final Register src = Register.values()[command.command & 0x7];
          final Register base = Register.values()[command.command >> 3 & 0x7];
          final Register offset = Register.values()[command.command >> 6 & 0x7];
          yield "MEMORY.ref(4, CPU.%s().value + CPU.%s().value).setu(CPU.%s().value);".formatted(base.name, offset.name, src.name);
        }

        // THUMB7
        case STRB_REG -> {
          final Register src = Register.values()[command.command & 0x7];
          final Register base = Register.values()[command.command >> 3 & 0x7];
          final Register offset = Register.values()[command.command >> 6 & 0x7];
          yield "MEMORY.ref(1, CPU.%s().value + CPU.%s().value).setu(CPU.%s().value);".formatted(base.name, offset.name, src.name);
        }

        // THUMB7
        case LDR_REG -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register base = Register.values()[command.command >> 3 & 0x7];
          final Register offset = Register.values()[command.command >> 6 & 0x7];
          yield "CPU.%s().value = MEMORY.ref(4, CPU.%s().value + CPU.%s().value).get();".formatted(dest.name, base.name, offset.name);
        }

        // THUMB7
        case LDRB_REG -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register base = Register.values()[command.command >> 3 & 0x7];
          final Register offset = Register.values()[command.command >> 6 & 0x7];
          yield "CPU.%s().value = MEMORY.ref(1, CPU.%s().value + CPU.%s().value).getUnsigned();".formatted(dest.name, base.name, offset.name);
        }

        // THUMB8
        case STRH_REG -> {
          final Register offset = Register.values()[command.command >> 6 & 0x7];
          final Register base = Register.values()[command.command >> 3 & 0x7];
          final Register dest = Register.values()[command.command & 0x7];
          yield "MEMORY.ref(2, CPU.%s().value + CPU.%s().value).setu(CPU.%s().value);".formatted(base.name, offset.name, dest.name);
        }

        // THUMB8
        case LDSB_REG -> {
          final Register offset = Register.values()[command.command >> 6 & 0x7];
          final Register base = Register.values()[command.command >> 3 & 0x7];
          final Register dest = Register.values()[command.command & 0x7];
          yield "CPU.%s().value = MEMORY.ref(1, CPU.%s().value + CPU.%s().value).get();".formatted(dest.name, base.name, offset.name);
        }

        // THUMB8
        case LDRH_REG -> {
          final Register offset = Register.values()[command.command >> 6 & 0x7];
          final Register base = Register.values()[command.command >> 3 & 0x7];
          final Register dest = Register.values()[command.command & 0x7];
          yield "CPU.%s().value = MEMORY.ref(2, CPU.%s().value + CPU.%s().value).getUnsigned();".formatted(dest.name, base.name, offset.name);
        }

        // THUMB8
        case LDSH_REG -> {
          final Register offset = Register.values()[command.command >> 6 & 0x7];
          final Register base = Register.values()[command.command >> 3 & 0x7];
          final Register dest = Register.values()[command.command & 0x7];
          yield "CPU.%s().value = MEMORY.ref(2, CPU.%s().value + CPU.%s().value).get();".formatted(dest.name, base.name, offset.name);
        }

        // THUMB9
        case STR_IMM -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register base = Register.values()[command.command >> 3 & 0x7];
          final int offset = (command.command >> 6 & 0x1f) * 0x4;
          yield "MEMORY.ref(4, CPU.%s().value + 0x%x).setu(CPU.%s().value);".formatted(base.name, offset, dest.name);
        }

        // THUMB9
        case LDR_IMM -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register base = Register.values()[command.command >> 3 & 0x7];
          final int offset = (command.command >> 6 & 0x1f) * 0x4;
          yield "CPU.%s().value = MEMORY.ref(4, CPU.%s().value + 0x%x).get();".formatted(dest.name, base.name, offset);
        }

        // THUMB9
        case STRB_IMM -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register base = Register.values()[command.command >> 3 & 0x7];
          final int offset = command.command >> 6 & 0x1f;
          yield "MEMORY.ref(1, CPU.%s().value + 0x%x).setu(CPU.%s().value);".formatted(base.name, offset, dest.name);
        }

        // THUMB9
        case LDRB_IMM -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register base = Register.values()[command.command >> 3 & 0x7];
          final int offset = command.command >> 6 & 0x1f;
          yield "CPU.%s().value = MEMORY.ref(1, CPU.%s().value + 0x%x).getUnsigned();".formatted(dest.name, base.name, offset);
        }

        // THUMB10
        case STRH_IMM -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register base = Register.values()[command.command >> 3 & 0x7];
          final int offset = (command.command >> 6 & 0x1f) * 0x2;
          yield "MEMORY.ref(2, CPU.%s().value + 0x%x).setu(CPU.%s().value);".formatted(base.name, offset, dest.name);
        }

        // THUMB10
        case LDRH_IMM -> {
          final Register dest = Register.values()[command.command & 0x7];
          final Register base = Register.values()[command.command >> 3 & 0x7];
          final int offset = (command.command >> 6 & 0x1f) * 0x2;
          yield "CPU.%s().value = MEMORY.ref(2, CPU.%s().value + 0x%x).getUnsigned();".formatted(dest.name, base.name, offset);
        }

        // THUMB11
        case STR_SP -> {
          final int offset = (command.command & 0xff) * 0x4;
          final Register dest = Register.values()[command.command >>> 8 & 0x7];
          yield "MEMORY.ref(4, CPU.sp().value + 0x%x).setu(CPU.%s().value);".formatted(offset, dest.name);
        }

        // THUMB11
        case LDR_SP -> {
          final int offset = (command.command & 0xff) * 0x4;
          final Register dest = Register.values()[command.command >>> 8 & 0x7];
          yield "CPU.%s().value = MEMORY.ref(4, CPU.sp().value + 0x%x).get();".formatted(dest.name, offset);
        }

        // THUMB12
        case ADDRESS -> {
          final boolean isSp = (command.command >>> 11 & 0x1) != 0;
          final Register dest = Register.values()[command.command >>> 8 & 0x7];
          final int offset = (command.command & 0xff) * 0x4;

          if(isSp) {
            yield "CPU.%s().value = CPU.sp().value + 0x%x;".formatted(dest.name, offset);
          }

          yield "CPU.%s().value = 0x%07x;".formatted(dest.name, (command.address + 0x4 & ~0x2) + offset);
        }

        // THUMB13
        case ADD_SP -> {
          final boolean negative = (command.command >> 7 & 0x1) != 0;
          final int offset = (command.command & 0x7f) * 0x4;

          if(negative) {
            yield "CPU.sp().value -= 0x%x;".formatted(offset);
          }

          yield "CPU.sp().value += 0x%x;".formatted(offset);
        }

        // THUMB14
        case PUSH -> {
          final List<Register> rlist = Register.unpack(command.command & 0xff);
          final boolean lrpc = (command.command >> 8 & 0x1) != 0;

          final StringBuilder builder = new StringBuilder();
          if(lrpc) {
            builder.append("CPU.push(CPU.").append(Register.R14_LR.name).append("().value);\n");
          }

          for(int i = rlist.size() - 1; i >= 0; i--) {
            builder.append("CPU.push(CPU.").append(rlist.get(i).name).append("().value);\n");
          }

          yield builder.toString();
        }

        // THUMB14
        case POP -> {
          final List<Register> rlist = Register.unpack(command.command & 0xff);
          final boolean lrpc = (command.command >> 8 & 0x1) != 0;

          final StringBuilder builder = new StringBuilder();
          for(final Register r : rlist) {
            builder.append("CPU.").append(r.name).append("().value = CPU.pop();\n");
          }

          if(lrpc) {
            builder.append("CPU.").append(Register.R15_PC.name).append("().value = CPU.pop();");
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
              .append("MEMORY.ref(4, CPU.").append(base.name).append("().value).setu(CPU.").append(r.name).append("().value);\n")
              .append("CPU.").append(base.name).append("().value += 0x4;\n");
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
              .append("CPU.").append(r.name).append("().value = ").append("MEMORY.ref(4, CPU.").append(base.name).append("().value).get();\n")
              .append("CPU.").append(base.name).append("().value += 0x4;\n");
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
            "  FUN_%07x();\n".formatted(address) +
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
            "  FUN_%07x();\n".formatted(address) +
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

        // THUMB18
        case B -> {
          final int offset = sign(command.command & 0x7ff, 11) * 0x2;
          final int address = command.address + 0x4 + offset;

          if(address >= firstAddress && address <= lastAddress) {
            labels.add(address);
            yield "LAB_%07x;".formatted(address);
          }

          yield "FUN_%07x();".formatted(address);
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

          yield "FUN_%07x();".formatted(address);
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
