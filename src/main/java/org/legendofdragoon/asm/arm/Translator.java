package org.legendofdragoon.asm.arm;

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
        case AND, EOR, SUB, RSB, ADD, ADC, SBC, RSC, TST, TEQ, CMP, CMN, ORR, MOV, BIC, MVN -> {
          final boolean isRightImmediate = (command.command >>> 25 & 0x1) != 0;
          final boolean setFlags = (command.command >>> 20 & 0x1) != 0;
          final Register left = Register.values()[command.command >>> 16 & 0xf];
          final Register dest = Register.values()[command.command >>> 12 & 0xf];

          String out = "";
          final boolean hasAssignment = command.op != Ops.TST && command.op != Ops.TEQ && command.op != Ops.CMP && command.op != Ops.CMN;
          if(isRightImmediate) {
            if(hasAssignment) {
              out += "CPU.%s().value = ".formatted(dest.name);
            }

            final int shift = (command.command >>> 8 & 0xf) * 2;
            final int right = Integer.rotateRight(command.command & 0xff, shift);

            if(left == Register.R15_PC) { // ADR pseudo-op
              if(setFlags) {
                out += "CPU.%sA(0x%x, 0x%x);".formatted(command.op.name().toLowerCase(), command.address + 0x8, right);

                if(shift != 0) {
                  out += "%nCPU.setCFlag(%b);".formatted((right & 0x8000_0000) != 0);
                }
              } else {
                out += "0x%x %s 0x%x;".formatted(command.address + 0x8, command.op.getOperator(), right);
              }

              yield conditional(command.command, out);
            }

            if(setFlags) {
              out += "CPU.%sA(CPU.%s().value, 0x%x);".formatted(command.op.name().toLowerCase(), left.name, right);

              if(shift != 0) {
                out += "%nCPU.setCFlag(%b);".formatted((right & 0x8000_0000) != 0);
              }
            } else {
              out += switch(command.op) {
                case MOV -> "0x%x;".formatted(right);
                case BIC -> "CPU.%s().value & ~0x%x;".formatted(left.name, right);
                case MVN -> "~0x%x;".formatted(right);
                case RSB -> "0x%x %s CPU.%s().value;".formatted(right, command.op.getOperator(), left.name);
                case ADC -> "CPU.%s().value + 0x%x + (CPU.cpsr().getCarry() ? 1 : 0);".formatted(left.name, right);
                case SBC -> "CPU.%s().value - 0x%x - (CPU.cpsr().getCarry() ? 0 : 1);".formatted(left.name, right);
                case RSC -> "0x%x - CPU.%s().value - (CPU.cpsr().getCarry() ? 0 : 1);".formatted(right, left.name);
                default -> "CPU.%s().value %s 0x%x;".formatted(left.name, command.op.getOperator(), right);
              };
            }

            yield conditional(command.command, out);
          }

          final boolean isShiftImmediate = (command.command >>> 4 & 0x1) == 0;
          final Register right = Register.values()[command.command & 0xf];
          final int shiftType = command.command >>> 5 & 0x3;

          if(left == Register.R15_PC) { // ADR pseudo-op
            throw new RuntimeException("Not implemented, see Using R15 (PC)");
          }

          if(isShiftImmediate) {
            final int shift = command.command >>> 7 & 0x1f;
            final String shifted = shift(shiftType, "CPU.%s().value".formatted(right.name), shift);

            if(setFlags) {
              if(command.op.isLogical()) {
                switch(shiftType) {
                  case 0x0 -> {
                    if(shift != 0) {
                      out += "CPU.setCFlag((CPU.%s().value & 0x1 << %d) != 0);".formatted(right.name, 32 - shift);
                    }
                  }

                  case 0x1, 0x2 -> {
                    if(shift != 0) {
                      out += "CPU.setCFlag((CPU.%s().value & 0x1 << %d) != 0);".formatted(right.name, shift - 1);
                    } else {
                      out += "CPU.setCFlag((CPU.%s().value & 0x8000_0000) != 0);".formatted(right.name);
                    }
                  }

                  case 0x3 -> {
                    if(shift != 0) {
                      out += "CPU.setCFlag((CPU.%s().value & 0x1 << %d) != 0);".formatted(right.name, shift - 1);
                    } else {
                      out += "CPU.setCFlag((CPU.%s().value & 0x1) != 0);".formatted(right.name);
                    }
                  }
                }

                out += "\n";
              }

              if(hasAssignment) {
                out += "CPU.%s().value = ".formatted(dest.name);
              }

              out += "CPU.%sA(CPU.%s().value, %s);".formatted(command.op.name().toLowerCase(), left.name, shifted);
            } else {
              if(hasAssignment) {
                out += "CPU.%s().value = ".formatted(dest.name);
              }

              out += switch(command.op) {
                case MOV -> "%s;".formatted(shifted);
                case BIC -> "CPU.%s().value & (~%s);".formatted(left.name, shifted);
                case MVN -> "(~%s);".formatted(shifted);
                case RSB -> "(%s) %s CPU.%s().value;".formatted(shifted, command.op.getOperator(), left.name);
                case ADC -> "CPU.%s().value + (%s) + (CPU.cpsr().getCarry() ? 1 : 0);".formatted(left.name, shifted);
                case SBC -> "CPU.%s().value - (%s) - (CPU.cpsr().getCarry() ? 0 : 1);".formatted(left.name, shifted);
                case RSC -> "(%s) - CPU.%s().value - (CPU.cpsr().getCarry() ? 0 : 1);".formatted(shifted, left.name);
                default -> "CPU.%s().value %s (%s);".formatted(left.name, command.op.getOperator(), shifted);
              };
            }

            yield conditional(command.command, out);
          }

          final Register shift = Register.values()[command.command >>> 8 & 0xf];
          final String shifted = shift(shiftType, "CPU.%s().value".formatted(right.name), "CPU.%s().value".formatted(shift.name));

          if(setFlags) {
            if(command.op.isLogical()) {
              out += "if(CPU.%s().value != 0) {%n".formatted(shift.name);
              switch(shiftType) {
                case 0x0 -> out += "  CPU.setCFlag((CPU.%s().value & 0x1 << (32 - CPU.%s().value)) != 0);".formatted(right.name, shift.name);
                case 0x1, 0x2 -> out += "  CPU.setCFlag((CPU.%s().value & 0x1 << (CPU.%s().value - 1)) != 0);".formatted(right.name, shift.name);
                case 0x3 -> out += "  CPU.setCFlag((CPU.%s().value & 0x1 << ((CPU.%s().value & 0x1f) - 1)) != 0);".formatted(right.name, shift.name);
              }
              out += "}\n";
            }

            if(hasAssignment) {
              out += "CPU.%s().value = ".formatted(dest.name);
            }

            out += "CPU.%sA(CPU.%s().value, %s);".formatted(command.op.name().toLowerCase(), left.name, shifted);
          } else {
            if(hasAssignment) {
              out += "CPU.%s().value = ".formatted(dest.name);
            }

            out += switch(command.op) {
              case MOV -> "%s;".formatted(shifted);
              case BIC -> "CPU.%s().value & (~%s);".formatted(left.name, shifted);
              case MVN -> "(~%s);".formatted(shifted);
              case RSB -> "(%s) %s CPU.%s().value;".formatted(shifted, command.op.getOperator(), left.name);
              case ADC -> "CPU.%s().value + (%s) + (CPU.cpsr().getCarry() ? 1 : 0);".formatted(left.name, shifted);
              case SBC -> "CPU.%s().value - (%s) - (CPU.cpsr().getCarry() ? 0 : 1);".formatted(left.name, shifted);
              case RSC -> "(%s) - CPU.%s().value - (CPU.cpsr().getCarry() ? 0 : 1);".formatted(shifted, left.name);
              default -> "CPU.%s().value %s (%s);".formatted(left.name, command.op.getOperator(), shifted);
            };
          }

          yield conditional(command.command, out);
        }

        case PSR_IMM, PSR_REG -> {
          final boolean isImmediate = (command.command >>> 25 & 0x1) != 0;
          final boolean isSpsr = (command.command >>> 22 & 0x1) != 0;
          final boolean isMsr = (command.command >>> 21 & 0x1) != 0;

          final String psr = isSpsr ? "spsr" : "cpsr";

          if(!isMsr) {
            final Register dest = Register.values()[command.command >>> 12 & 0xf];
            yield conditional(command.command, "CPU.%s().value = CPU.%s().get();".formatted(dest.name, psr));
          }

          final boolean maskFlags = (command.command >>> 19 & 0x1) != 0;
          final boolean maskStatus = (command.command >>> 18 & 0x1) != 0;
          final boolean maskExtension = (command.command >>> 17 & 0x1) != 0;
          final boolean maskControl = (command.command >>> 16 & 0x1) != 0;

          if(isImmediate) {
            final int shift = (command.command >>> 8 & 0xf) * 2;
            final int immediate = Integer.rotateRight(command.command & 0xff, shift);
            yield conditional(command.command, "CPU.%s().msr(%x, %b, %b, %b, %b);".formatted(psr, immediate, maskFlags, maskStatus, maskExtension, maskControl));
          }

          final Register src = Register.values()[command.command & 0xf];
          yield conditional(command.command, "CPU.%s().msr(CPU.%s().value, %b, %b, %b, %b);".formatted(psr, src.name, maskFlags, maskStatus, maskExtension, maskControl));
        }

        case MUL -> {
          final int op = command.command >>> 21 & 0xf;
          final boolean setFlags = (command.command >>> 20 & 0x1) != 0;
          final Register destOrHi = Register.values()[command.command >>> 16 & 0xf];
          final Register accOrLo = Register.values()[command.command >>> 12 & 0xf];
          final Register right = Register.values()[command.command >>> 8 & 0xf];
          final Register left = Register.values()[command.command & 0xf];

          yield switch(op) {
            case 0x0 -> {
              String out = "CPU.%s().value = ".formatted(destOrHi.name);

              if(setFlags) {
                out += "CPU.mulA(CPU.%s().value, CPU.%s().value);".formatted(left.name, right.name);
              } else {
                out += "CPU.%s().value * CPU.%s().value;".formatted(left.name, right.name);
              }

              yield conditional(command.command, out);
            }

            default -> throw new RuntimeException("MUL op %d not supported".formatted(op));
          };
        }

        case TRANS_IMM_9, TRANS_REG_9 -> {
          final boolean isShiftedRegister = (command.command >>> 25 & 0x1) != 0;
          final boolean isPre = (command.command >>> 24 & 0x1) != 0;
          final boolean isPositive = (command.command >>> 23 & 0x1) != 0;
          final boolean isByte = (command.command >>> 22 & 0x1) != 0;
          final boolean writeBack = !isPre || (command.command >>> 21 & 0x1) != 0;
          final boolean isLoad = (command.command >>> 20 & 0x1) != 0;
          final Register base = Register.values()[command.command >>> 16 & 0xf];
          final Register value = Register.values()[command.command >>> 12 & 0xf];

          final String baseValue;
          if(base == Register.R15_PC) {
            baseValue = "0x%x".formatted(command.address + 0x8);
          } else {
            baseValue = "CPU.%s().value".formatted(base.name);
          }

          final String offset;
          if(!isShiftedRegister) { // immediate
            offset = "0x%x".formatted(command.command & 0xfff);
          } else {
            final int shift = command.command >>> 7 & 0x1f;
            final int shiftType = command.command >>> 5 & 0x3;
            offset = shift(shiftType, "CPU.%s().value".formatted(Register.values()[command.command & 0xf].name), shift);
          }

          final String var = "address%x".formatted(command.address);

          String out = "";
          if(isPre) {
            out += "final int %s = %s %s %s;".formatted(var, baseValue, isPositive ? '+' : '-', offset);
          } else {
            out += "final int %s = %s;".formatted(var, baseValue);
          }

          if(isLoad) {
            out += "\nCPU.%s().value = MEMORY.ref(%d, %s).getUnsigned();".formatted(value.name, isByte ? 1 : 4, var);
          } else {
            final String valueValue;
            if(value == Register.R15_PC) {
              valueValue = "0x%x".formatted(command.address + 0xc);
            } else {
              valueValue = "CPU.%s().value".formatted(value.name);
            }

            out += "\nMEMORY.ref(%d, %s).setu(%s);".formatted(isByte ? 1 : 4, var, valueValue);
          }

          if(!isPre) {
            out += "\nCPU.%s().value = %s %s %s;".formatted(base.name, baseValue, isPositive ? '+' : '-', offset);
          } else if(writeBack) {
            out += "\nCPU.%s().value = %s;".formatted(base.name, var);
          }

          yield conditional(command.command, out);
        }

        case TRANS_IMM_10, TRANS_REG_10 -> {
          final boolean isPre = (command.command >>> 24 & 0x1) != 0;
          final boolean isPositive = (command.command >>> 23 & 0x1) != 0;
          final boolean isImmediate = (command.command >>> 22 & 0x1) != 0;
          final boolean writeBack = !isPre || (command.command >>> 21 & 0x1) != 0;
          final boolean isLoad = (command.command >>> 20 & 0x1) != 0;
          final Register base = Register.values()[command.command >>> 16 & 0xf];
          final Register value = Register.values()[command.command >>> 12 & 0xf];
          final int op = command.command >>> 5 & 0x3;

          if(!isLoad && op != 1) {
            throw new RuntimeException("Double not supported");
          }

          final String baseValue;
          if(base == Register.R15_PC) {
            baseValue = "0x%x".formatted(command.address + 0x8);
          } else {
            baseValue = "CPU.%s().value".formatted(base.name);
          }

          final String address;
          if(isImmediate) {
            final int immediateUpper = command.command >>> 8 & 0xf;
            final int immediateLower = command.command & 0xf;
            final int offset = immediateUpper << 4 | immediateLower;
            address = "%s %s 0x%x".formatted(baseValue, isPositive ? '+' : '-', offset);
          } else {
            final Register offset = Register.values()[command.command & 0xf];
            address = "%s %s %s".formatted(baseValue, isPositive ? '+' : '-', "CPU.%s().value".formatted(offset.name));
          }

          String out = "";

          final String var = "address%x".formatted(command.address);
          out += "final int %s = %s;".formatted(var, address);

          if(isPre && writeBack) {
            out += "\nCPU.%s().value = %s;".formatted(base.name, var);
          }

          if(isLoad) {
            switch(op) {
              case 1 -> out += "\nCPU.%s().value = MEMORY.ref(2, %s).getUnsigned();".formatted(value.name, var);
              case 2 -> out += "\nCPU.%s().value = MEMORY.ref(1, %s).get();".formatted(value.name, var);
              case 3 -> out += "\nCPU.%s().value = MEMORY.ref(2, %s).get();".formatted(value.name, var);
            }
          } else {
            final String valueValue;
            if(value == Register.R15_PC) {
              valueValue = "0x%x".formatted(command.address + 0xc);
            } else {
              valueValue = "CPU.%s().value".formatted(value.name);
            }

            out += "\nMEMORY.ref(2, %s).setu(%s);".formatted(var, valueValue);
          }

          if(!isPre) {
            out += "\nCPU.%s().value = %s;".formatted(base.name, var);
          }

          yield conditional(command.command, out);
        }

        case BLOCK_TRANS -> {
          final boolean isPre = (command.command >>> 24 & 0x1) != 0;
          final boolean isPositive = (command.command >>> 23 & 0x1) != 0;
          final boolean isPsr = (command.command >>> 22 & 0x1) != 0;
          final boolean isWriteBack = (command.command >>> 21 & 0x1) != 0;
          final boolean isLoad = (command.command >>> 20 & 0x1) != 0;
          final Register base = Register.values()[command.command >> 16 & 0xf];
          final Set<Register> rlist = Register.unpack(command.command & 0xffff);

          if(isPsr) {
            throw new RuntimeException("S bit is not supported @%07x".formatted(command.address));
          }

          String out = "";

          final String var = "address%x".formatted(command.address);
          if(isPositive) {
            out += "int %s = CPU.%s().value;".formatted(var, base.name);
          } else {
            out += "int %s = CPU.%s().value - 0x%x;".formatted(var, base.name, rlist.size() * 0x4);
          }

          if(isPre && isWriteBack) {
            out += "\nCPU.%s().value = %s;".formatted(base.name, var);
          }

          for(final Register r : rlist) {
            if(isLoad) {
              out += "\nCPU.%s().value = MEMORY.ref(4, %s).getUnsigned();".formatted(r.name, var);
            } else {
              out += "\nMEMORY.ref(4, %s).setu(CPU.%s().value);".formatted(var, r.name);
            }

            out += "\n%s += 0x4;".formatted(var);
          }

          if(!isPre) {
            out += "\nCPU.%s().value = %s;".formatted(base.name, var);
          }

          yield conditional(command.command, out);
        }

        case B -> {
          final int offset = sign(command.command & 0xff_ffff, 24) * 0x4;
          final int address = command.address + 0x8 + offset;

          if(address >= firstAddress && address <= lastAddress) {
            labels.add(address);
            yield conditional(command.command, "LAB_%07x;".formatted(address));
          }

          yield conditional(command.command, "FUN_%07x();".formatted(address));
        }

        case BL -> {
          final int offset = sign(command.command & 0xff_ffff, 24) * 0x4;
          yield conditional(command.command, "FUN_%07x();".formatted(command.address + 0x8 + offset));
        }

        case BX -> {
          final Register dest = Register.values()[command.command & 0xf];

          if(dest == Register.R14_LR) {
            yield "return;";
          }

          yield conditional(command.command, "MEMORY.call(CPU.%s().value);".formatted(dest.name));
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

  private static String conditional(final int command, final String output) {
    return switch(command >>> 28) {
      case 0x0 -> wrapCondition("CPU.cpsr().getZero()", output, "==");
      case 0x1 -> wrapCondition("!CPU.cpsr().getZero()", output, "!=");
      case 0x2 -> wrapCondition("CPU.cpsr().getCarry()", output, "unsigned >=");
      case 0x3 -> wrapCondition("!CPU.cpsr().getCarry()", output, "unsigned <");
      case 0x4 -> wrapCondition("CPU.cpsr().getNegative()", output, "negative");
      case 0x5 -> wrapCondition("!CPU.cpsr().getNegative()", output, "positive or 0");
      case 0x6 -> wrapCondition("CPU.cpsr().getOverflow()", output, "signed overflow");
      case 0x7 -> wrapCondition("!CPU.cpsr().getOverflow()", output, "signed no overflow");
      case 0x8 -> wrapCondition("CPU.cpsr().getCarry() && !CPU.cpsr().getZero()", output, "unsigned >");
      case 0x9 -> wrapCondition("!CPU.cpsr().getCarry() || CPU.cpsr().getZero()", output, "unsigned <=");
      case 0xa -> wrapCondition("CPU.cpsr().getNegative() == CPU.cpsr().getOverflow()", output, ">=");
      case 0xb -> wrapCondition("CPU.cpsr().getNegative() != CPU.cpsr().getOverflow()", output, "<");
      case 0xc -> wrapCondition("!CPU.cpsr().getZero() && CPU.cpsr().getOverflow()", output, ">");
      case 0xd -> wrapCondition("CPU.cpsr().getZero() || !CPU.cpsr().getOverflow()", output, "<=");
      case 0xe -> output;
      default -> throw new RuntimeException("Illegal condition %x".formatted(command >>> 28));
    };
  }

  private static String wrapCondition(final String condition, final String output, final String comment) {
    final String[] split = output.split("\n");

    for(int i = 0; i < split.length; i++) {
      split[i] = "  " + split[i];
    }

    return
      "if(%s) { // %s\n".formatted(condition, comment) +
      String.join("\n", split) + '\n' +
      '}';
  }

  private static String shift(final int shiftType, final String value, final int amount) {
    if(amount == 0) {
      return switch(shiftType) {
        case 0x0 -> value;
        case 0x1 -> "0";
        case 0x2 -> "%s >> 31".formatted(value);
        case 0x3 -> "(CPU.cpsr().getCarry() ? 0x8000_0000 : 0) | %s >>> 1".formatted(value);
        default -> throw new IllegalArgumentException("Invalid shiftType " + shiftType);
      };
    }

    return shift(shiftType, value, Integer.toString(amount));
  }

  private static String shift(final int shiftType, final String value, final String amount) {
    return switch(shiftType) {
      case 0x0 -> "%s << %s".formatted(value, amount);
      case 0x1 -> "%s >>> %s".formatted(value, amount);
      case 0x2 -> "%s >> %s".formatted(value, amount);
      case 0x3 -> "Integer.rotateRight(%s, %s)".formatted(value, amount);
      default -> throw new IllegalArgumentException("Invalid shiftType " + shiftType);
    };
  }
}
