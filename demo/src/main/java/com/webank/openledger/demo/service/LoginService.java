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

package com.webank.openledger.demo.service;


import com.webank.openledger.demo.holder.LoginHolder;

import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.CryptoType;

/**
 * login service
 * @author pepperli
 */
@Slf4j
public class LoginService {
    private static LoginService instance = null;
    private static final CryptoSuite ecdsaCryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);

    static {
        instance = new LoginService();
    }

    private LoginService() {
    }

    public static LoginService getInstance() {
        return instance;
    }

    public static void main(String[] args) throws Exception {
        LoginService.getInstance().login("9293ec45b72146ded90b5972a56a231e368ded522f84059bf328267e6584ce2d");
        log.info( LoginService.getInstance().get().getAddress());
        LoginService.getInstance().logout();
        log.info( LoginService.getInstance().get().getAddress());

    }

    public String login(String prikey) throws Exception {
        CryptoSuite account = null;
        try {
            account = new CryptoSuite(CryptoType.ECDSA_TYPE, prikey);
        } catch (Exception e) {
          throw new Exception("load account by private key fail!");
        }
        LoginHolder.setAccount(account.getCryptoKeyPair());
        log.info("login account:{}", account.getCryptoKeyPair().getAddress());
        return account.getCryptoKeyPair().getAddress();
    }

    public  CryptoKeyPair get() throws Exception {
        CryptoKeyPair account = LoginHolder.getAccount();
        return account;
    }

    public String  getAccount() throws Exception {
        CryptoKeyPair account = LoginHolder.getAccount();
        return account.getAddress();
    }
    public Boolean logout() throws Exception {
        log.info("logout account:{}", LoginHolder.getAccount().getAddress());
        LoginHolder.clear();
        return true;
    }

    public CryptoKeyPair createKeyPair() {
        CryptoKeyPair cryptoKeyPair = ecdsaCryptoSuite.createKeyPair();
        return cryptoKeyPair;
    }

}
