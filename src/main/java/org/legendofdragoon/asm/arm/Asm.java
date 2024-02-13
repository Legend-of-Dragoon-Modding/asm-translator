package org.legendofdragoon.asm.arm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Asm {
  private static final Pattern regex = Pattern.compile("\\s+?(?:\\w*::)?([\\da-f]{8})\\s+([\\da-f]{2})\\s+?([\\da-f]{2})\\s+?([\\da-f]{2})\\s+?([\\da-f]{2}).*", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

  public static Asm parse(final String asm) {
    final List<Command> commands = new ArrayList<>();

    final Matcher matcher = regex.matcher(asm);

    while(matcher.find()) {
      final int address = Integer.parseUnsignedInt(matcher.group(1), 16);
      final int command = Integer.parseUnsignedInt(matcher.group(5) + matcher.group(4) + matcher.group(3) + matcher.group(2), 16);

      try {
        commands.add(new Command(address, command));
      } catch(final IllegalArgumentException e) {
        throw new RuntimeException(e.getMessage() + " at " + Integer.toHexString(address), e);
      }
    }

    return new Asm(commands);
  }

  public final List<Command> commands;

  private Asm(final List<Command> commands) {
    this.commands = Collections.unmodifiableList(commands);
  }
}
