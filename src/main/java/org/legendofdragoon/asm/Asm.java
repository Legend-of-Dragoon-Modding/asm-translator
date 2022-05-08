package org.legendofdragoon.asm;

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
      final long address = Long.parseLong(matcher.group(1), 16);
      final long command = Long.parseLong(matcher.group(5) + matcher.group(4) + matcher.group(3) + matcher.group(2), 16);

      commands.add(new Command(address, command));
    }

    return new Asm(commands);
  }

  public final List<Command> commands;

  private Asm(final List<Command> commands) {
    this.commands = Collections.unmodifiableList(commands);
  }
}
