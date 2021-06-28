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


import org.junit.Before;


/**
 * @author Rich Zhao richzhao@webank.com
 */
public class LedgerImplTest {
    Blockchain blockchain;
    private LedgerImpl ledger;
    private AccountImpl account;

    @Before
    public void setUp() {
        blockchain = new Blockchain("application.properties");
        ConnectionImpl connection = blockchain.buildConnection();
        String keyFile = blockchain.getTxKeyFile();
        String pemFile = AccountImplTest.class.getClassLoader().getResource(blockchain.getConfigPath() + "/" + blockchain.getTxKeyFile()).getPath();
        account = new AccountImpl(pemFile, "pem", "");
        ledger = new LedgerImpl(connection, "1", account.getKeyPair());
        //System.out.println(keyFile);
    }

}