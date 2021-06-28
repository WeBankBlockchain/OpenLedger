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

package com.webank.openledger.demo.holder;

import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;

/**
 * stores the currently logon user
 * @author pepperli
 */
@Slf4j
public class LoginHolder {
    private static  CryptoKeyPair accountKeyPair;


    public static void setAccount(CryptoKeyPair account){
        accountKeyPair=account;
    }

    public static CryptoKeyPair getAccount() throws Exception {
        if(accountKeyPair==null){
            throw new Exception("account not logon,please login");
        }
        return accountKeyPair;
    }

    public static void clear(){
        accountKeyPair=null;
    }


}
