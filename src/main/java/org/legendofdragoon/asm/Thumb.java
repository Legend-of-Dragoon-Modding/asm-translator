package org.legendofdragoon.asm;

import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import org.legendofdragoon.asm.thumb.Asm;
import org.legendofdragoon.asm.thumb.Translator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class Thumb {
  static {
    System.setProperty("log4j.skipJansi", "false");
    PluginManager.addPackage("org.legendofdragoon.asm");
  }

  private Thumb() { }

  public static void main(final String[] args) throws IOException {
    final String input = Files.readString(Paths.get("input.txt"));

    final Asm asm = Asm.parse(input);

    if(asm.commands.isEmpty()) {
      System.err.println("No code found");
      return;
    }

    System.out.println("Disassembly for code starting at " + Long.toHexString(asm.commands.get(0).address));

    final Translator translator = new Translator();
    for(final String line : translator.translate(asm)) {
      System.out.println(line);
    }
  }
}
