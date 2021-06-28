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


import org.junit.Test;

/**
 * @author Rich Zhao richzhao@webank.com
 */
public class BlockchainTest {
    Blockchain blockchain;

    @Test
    public void getTxKeyFile() {
        blockchain = new Blockchain("application.properties");
        String keyFile = blockchain.getTxKeyFile();
        System.out.println(keyFile);
    }
}