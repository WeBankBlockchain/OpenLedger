/*
 *   Copyright (C) @2021 Webank Group Holding Limited
 *   <p>
 *   Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *   <p>
 *   Unless required by applicable law or agreed to in writing, software distributed under the License
 *   is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing permissions and limitations under
 *  he License.
 *
 */

package com.webank.openledger.demo;

import java.io.IOException;

import com.webank.openledger.demo.holder.AssetHolder;
import com.webank.openledger.demo.holder.LoginHolder;
import com.webank.openledger.demo.holder.ProjectHolder;
import com.webank.openledger.demo.service.CommandService;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jline.reader.Completer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

/**
 *  demo
 * @author pepperli
 */
@Slf4j
public class DemoApplication {
    public static void main(String[] args) throws IOException {
        Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .build();

        Completer commandCompleter = new StringsCompleter(CommandService.COMMANDS);

        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(commandCompleter)
                .build();
        String welcome = "Welcome to OpenLedger! please input 'init' to run a new project or 'help' to learn more,input 'quit' to exist.";
        System.out.println(welcome);
        String prompt = "OpenLedger-demo>";
        String line = "init";
        while (true && StringUtils.isNotBlank(line)) {
            try {
                line = reader.readLine(prompt);
                log.info("input:{}", line);
                if ("quit".equals(line)) {
                    System.exit(0);
                }
                System.out.println(CommandService.getInstance().exectue(line));
            } catch (UserInterruptException e) {
                // Ignore
            } catch (EndOfFileException e) {
                System.out.println("\nBye.");
                return;
            }
        }
        AssetHolder.clear();
        LoginHolder.clear();
        ProjectHolder.clear();
        System.exit(0);
        return;
    }
}
