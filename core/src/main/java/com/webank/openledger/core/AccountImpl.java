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

import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;

/**
 * @author Rich Zhao richzhao@webank.com
 */
public class AccountImpl {
    private CryptoSuite cryptoSuite;

    public AccountImpl(String fileName, String format, String password) {
        this.cryptoSuite = new CryptoSuite(0);
        cryptoSuite.loadAccount(format, fileName, password);
    }

    public CryptoKeyPair getKeyPair() {
        return cryptoSuite.getCryptoKeyPair();
    }
}
