package net.taskwolf.core.intro;

import lombok.RequiredArgsConstructor;
import net.taskwolf.core.log.Log;

import java.util.logging.Level;

@RequiredArgsConstructor(staticName = "create")
public final class Intro {
  private final Log log;
  private final String version;

  public void print() {
    printWolf();
    printVersion();
  }

  private void printWolf() {
    log.consoleLog(Level.INFO, "                                                                                                    ");
    log.consoleLog(Level.INFO, "                              █                                     ██                              ");
    log.consoleLog(Level.INFO, "                             ██████                              ██████                             ");
    log.consoleLog(Level.INFO, "                            █▓   ██████                      ██████░  ▒█                            ");
    log.consoleLog(Level.INFO, "                           ██       ▒█████                █████▒       ██                           ");
    log.consoleLog(Level.INFO, "                          ██▒           ████████████████████           ░██                          ");
    log.consoleLog(Level.INFO, "                          ██   ██░        ░░            ░░        ░██   ██                          ");
    log.consoleLog(Level.INFO, "                          ██   ████▒                            ▒████   ███                         ");
    log.consoleLog(Level.INFO, "                          ██   ██████                          ██████   ███                         ");
    log.consoleLog(Level.INFO, "                          ███  ████░                            ░████  ▓██                          ");
    log.consoleLog(Level.INFO, "                          ███   █▓                                ▒█   ███                          ");
    log.consoleLog(Level.INFO, "                           ██▓                                        ▒███                          ");
    log.consoleLog(Level.INFO, "                           ███░ ░     ▒█  ░█▓          ▒█░  █▓     ░  ████                          ");
    log.consoleLog(Level.INFO, "                      ██████░ ░      ░████████▓      ▒████████▒      ░ ░██████                      ");
    log.consoleLog(Level.INFO, "                       ███          ░██▒░░░▒████░  ░████▒  ░▒██▒          ▓██                       ");
    log.consoleLog(Level.INFO, "                       ████          ▓█░▒█▒▒  ▓███████  ▒▓▒█ ██          ████                       ");
    log.consoleLog(Level.INFO, "                         ████         ███▒▓░  ████████  ░▓▒███         ▓███                         ");
    log.consoleLog(Level.INFO, "                         ███ ░    ▓██▒ ░█████ ▓███████ █████░ ░██▓    ░ ███                         ");
    log.consoleLog(Level.INFO, "                         ██▒      ████████████████████████████████      ░██                         ");
    log.consoleLog(Level.INFO, "                         ██ ░    ██████████████████████████████████    ░ ██                         ");
    log.consoleLog(Level.INFO, "                        ███ ░    ████████ ▒████▒▒▒▓▒▒████▒ ████████      ███                        ");
    log.consoleLog(Level.INFO, "                        ███      █░ █████ ▓███  ░▓▓░  ████ █████ ░█░    ░███                        ");
    log.consoleLog(Level.INFO, "                         ██         █████▒█████      █████▒█████░        ██                         ");
    log.consoleLog(Level.INFO, "                         ██░▓█      ██████████████▓█████████████░     █▓░██                         ");
    log.consoleLog(Level.INFO, "                         █████ ░    ██████▒██████▓▒██████▒██████    ░ █████                         ");
    log.consoleLog(Level.INFO, "                          ████▓░    ▓██░███              ███ ███    ░▒████                          ");
    log.consoleLog(Level.INFO, "                          █████ ░    █   ▓█▓  ████████  ▒█▓   █    ░ █████                          ");
    log.consoleLog(Level.INFO, "                           █████ ░        ▒█   ██████   ▓▓        ░ █████                           ");
    log.consoleLog(Level.INFO, "                           ██████ ░        ░            ░        ░ ██████                           ");
    log.consoleLog(Level.INFO, "                            ██ ███░                              ░███ ██                            ");
    log.consoleLog(Level.INFO, "                                 ███  ▒█ ░                ░ █▒  ███                                 ");
    log.consoleLog(Level.INFO, "                                  ████░██▓ ░            ░ ▒██▒▓███                                  ");
    log.consoleLog(Level.INFO, "                                    ████████  ░      ░  ▓███████                                    ");
    log.consoleLog(Level.INFO, "                                      ████████▓   ░  ▒████████                                      ");
    log.consoleLog(Level.INFO, "                                        ████ ██████████ ████                                        ");
    log.consoleLog(Level.INFO, "                                          ██     ██      █                                          ");
  }

  private void printVersion() {
    log.consoleLog(Level.INFO, "\033[0;1m");
    log.consoleLog(Level.INFO, "                                           Version: " + version);
    log.consoleLog(Level.INFO, "\u001B[0m");
  }
}
