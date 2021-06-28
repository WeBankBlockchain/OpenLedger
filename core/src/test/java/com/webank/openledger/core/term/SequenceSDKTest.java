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

package com.webank.openledger.core.term;


import java.math.BigInteger;

import com.webank.openledger.contracts.Sequence;
import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.ConnectionImpl;
import com.webank.openledger.core.constant.ErrorCode;
import com.webank.openledger.core.exception.OpenLedgerBaseException;
import com.webank.openledger.core.response.ResponseData;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@Slf4j
public class SequenceSDKTest {
    private String contractAddress = "0xdac934a09029641d298407ed66fe95d406e9b195";
    Blockchain blockchain;
    private SequenceService sequenceSDK;

    @Before
    public void init() {
        log.info("Begin Test-----------------");
        blockchain = new Blockchain("application.properties");

        if (StringUtils.isNotBlank(contractAddress)) {
            this.sequenceSDK = new SequenceService(blockchain, contractAddress);

        }
    }

    @Test
    public void deploy() throws ContractException {
        ConnectionImpl connection = blockchain.buildConnection();
        // 部署合约
        Client client = connection.getSdk().getClient(1);
        CryptoKeyPair cryptoKeyPair = client.getCryptoSuite().createKeyPair();
        client.getCryptoSuite().setCryptoKeyPair(cryptoKeyPair);
        Sequence sequence = null;

        sequence = Sequence.deploy(connection.getSdk().getClient(1), cryptoKeyPair);
        contractAddress = sequence.getContractAddress();
        log.info(contractAddress);

    }

    @Test
    public void getSequence() throws OpenLedgerBaseException {
        ResponseData<BigInteger> responseData = sequenceSDK.getSequence();
        assertEquals(ErrorCode.SUCCESS.getCode(), responseData.getErrorCode().intValue());
        log.info("seqNo:" + responseData.getResult());
    }


}