package org.legendofdragoon.asm.thumb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Asm {
  private static final Pattern regex = Pattern.compile("\\s+?(?:\\w*::)?([\\da-f]{8})\\s+((?:[\\da-f]{2}\\s+){1,4}).*", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
  private static final Pattern spaces = Pattern.compile("\\s+");

  public static Asm parse(final String asm) {
    final List<Command> commands = new ArrayList<>();

    final Matcher matcher = regex.matcher(asm);

    while(matcher.find()) {
      final String[] parts = spaces.split(matcher.group(2));
      final StringBuilder commandStr = new StringBuilder();
      for(int i = parts.length - 1; i >= 0; i--) {
        commandStr.append(parts[i]);
      }

      final int address = Integer.parseUnsignedInt(matcher.group(1), 16);
      final int command = Integer.parseUnsignedInt(commandStr.toString(), 16);

      commands.add(new Command(address, command));
    }

    return new Asm(commands);
  }

  public final List<Command> commands;

  private Asm(final List<Command> commands) {
    this.commands = Collections.unmodifiableList(commands);
  }
}
