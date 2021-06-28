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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.webank.openledger.core.Blockchain;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Config {
    String projectAddr;
    String accountManagerAddr;
    String authManagerAddr;
    String authCenterAddr;
    String orgAddr;


    private Boolean isJar;
    private static Properties propConfig = new Properties();

    public Config(String configFile) {
        isJar = false;
        InputStream inputStream = Blockchain.class.getClassLoader().getResourceAsStream(configFile);
        try {
            propConfig.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        fillProperties();
    }

    public Config(String configFile, Boolean isJar) {
        this.isJar = isJar;
        if (isJar) {
            File file = new File(configFile);
            try (FileInputStream fis = new FileInputStream(file);) {
                propConfig.load(fis);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            new Config(configFile);
        }

        fillProperties();
    }


    private void fillProperties() {
        projectAddr = propConfig.getProperty("project.addr");
        accountManagerAddr = propConfig.getProperty("accountmanager.addr");
        authManagerAddr = propConfig.getProperty("authmanager.addr");
        authCenterAddr = propConfig.getProperty("authcenter.addr");
        orgAddr = propConfig.getProperty("org.addr");
    }


}
