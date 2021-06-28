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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rich Zhao richzhao@webank.com
 */
public class LedgerImpl {
    private static final Logger logger = LoggerFactory.getLogger(Blockchain.class);
    private String ledgerId;
    private Client client;
    private ConnectionImpl connection;

    public LedgerImpl(ConnectionImpl connection, String ledgerID, CryptoKeyPair keyPair) {
        this.connection = connection;
        this.client = connection.getSdk().getClient(Integer.parseInt(ledgerID));
        this.client.getCryptoSuite().setCryptoKeyPair(keyPair);
    }

    public String getLedgerId() {
        return ledgerId;
    }

    public void setLedgerId(String ledgerId) {
        this.ledgerId = ledgerId;
    }

    public CryptoKeyPair getCryptoKeyPair() {
        return client.getCryptoSuite().getCryptoKeyPair();
    }

    public void setCryptoKeyPair(CryptoKeyPair keyPair) {
        this.client.getCryptoSuite().setCryptoKeyPair(keyPair);
    }

    public <T> T getContract(String id, Object... params) {
        Class type = (Class) params[0];
        T contract = null;
        try {
            Method loader = type.getMethod("load",
                    String.class,
                    Client.class,
                    CryptoKeyPair.class);
            contract = (T) loader.invoke(
                    null,
                    id, client, client.getCryptoSuite().getCryptoKeyPair());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return contract;
    }
}
