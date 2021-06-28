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


import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.protocol.response.BlockNumber;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.junit.Test;

/**
 * @author Rich Zhao richzhao@webank.com
 */
public class ConnectionImplTest {

    @Test
    public void getSdk() {
        String toml = ConnectionImpl.class.getClassLoader().getResource("sdk.toml").getPath();
        ConnectionImpl connection = new ConnectionImpl(toml);
        BcosSDK sdk = connection.getSdk();

        Client client = sdk.getClient(Integer.valueOf(1));

        CryptoSuite cryptoSuite = client.getCryptoSuite();

        // 获取群组1的块高
        BlockNumber blockNumber = client.getBlockNumber();
        System.out.println(blockNumber.getBlockNumber());
    }
}