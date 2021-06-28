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

package com.webank.openledger.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import lombok.Getter;
import org.fisco.bcos.sdk.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rich Zhao richzhao@webank.com
 */
@Getter
public class Blockchain {
    /**
     * default ledgerId
     */
    public static final String DEFAULT_LEDGERID = "1";
    private static final Logger logger = LoggerFactory.getLogger(Blockchain.class);
    private static Properties propConfig = new Properties();
    private String sdkFile="sdk.toml";
    private String txKeyFile;
    private String ledger;
    private String configPath="conf";
    private ConnectionImpl connection;
    private Boolean isJar;

    public Blockchain(String configFile) {
        isJar=false;
        //todo determineFileFormat
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

    public Blockchain(String configFile,Boolean isJar) {
        this.isJar=isJar;
        if(isJar){
            File file=new File(configFile);
            try( FileInputStream fis = new FileInputStream(file);) {
                propConfig.load(fis);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            new Blockchain(configFile);
        }

        fillProperties();
    }

    public String getLedger() {
        return ledger;
    }

    public String getConfigPath() {
        return configPath;
    }

    public String getTxKeyFile() {
        return txKeyFile;
    }

    public ConnectionImpl getConnection() {
        if (this.connection == null) {
            this.connection = buildConnection();
        }
        return connection;
    }

    //显示设置必须的属性
    private void fillProperties() {
//        sdkFile = propConfig.getProperty("blockchain.sdkFile");
        txKeyFile = propConfig.getProperty("blockchain.txKey");
//        configPath = propConfig.getProperty("blockchain.ConfigPath");
        ledger = propConfig.getProperty("ledger.id=1");
    }

    public ConnectionImpl buildConnection() {
        String toml = null;
        if(isJar){
            File file=new File(this.sdkFile);
            toml =file.getAbsolutePath();
        }else{
            toml = ConnectionImpl.class.getClassLoader().getResource(this.sdkFile).getPath();
        }
        this.connection = new ConnectionImpl(toml);
        return connection;
    }

    public Client getClient(String ledgerId) {
        ConnectionImpl connection = this.getConnection();
        Client client = connection.getSdk().getClient(Integer.parseInt(ledgerId));
        return client;
    }

    public Client getDefaultClient() {
        return getClient(DEFAULT_LEDGERID);
    }

    public AccountImpl getProjectAccount() {
        String pemFile=null;
        if(isJar){
            File file=new File(this.getConfigPath() + "/" + this.getTxKeyFile());
            pemFile=file.getPath();
        }else{
            pemFile = AccountImpl.class.getClassLoader().getResource(this.getConfigPath() + "/" + this.getTxKeyFile()).getPath();
        }
        AccountImpl account = new AccountImpl(pemFile, "pem", "");
        return account;
    }

    public LedgerImpl getLedger(String ledgerId) {
        LedgerImpl ledger = new LedgerImpl(getConnection(), ledgerId, this.getProjectAccount().getKeyPair());
        return ledger;
    }
}
