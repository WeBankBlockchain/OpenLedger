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
import org.junit.Test;

/**
 * @author Rich Zhao richzhao@webank.com
 */
public class AccountImplTest {
    Blockchain blockchain;

    @Before
    public void setUp() {
        blockchain = new Blockchain("application.properties");
        String keyFile = blockchain.getTxKeyFile();
        //System.out.println(keyFile);
    }

    @Test
    public void testAccountFromPem() {
        String pemFile = AccountImplTest.class.getClassLoader().getResource(blockchain.getConfigPath() + "/" + blockchain.getTxKeyFile()).getPath();
        AccountImpl account = new AccountImpl(pemFile, "pem", "");
        System.out.println("address:" + account.getKeyPair().getAddress());
        System.out.println(account.getKeyPair().getKeyPair().getPrivate().toString());
    }
}