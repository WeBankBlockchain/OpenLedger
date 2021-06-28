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

import com.webank.openledger.contracts.Term;
import com.webank.openledger.core.AccountImplTest;
import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.ConnectionImpl;
import com.webank.openledger.core.constant.ErrorCode;
import com.webank.openledger.core.exception.OpenLedgerBaseException;
import com.webank.openledger.core.response.ResponseData;
import com.webank.openledger.core.term.entity.TermEntity;
import com.webank.openledger.utils.OpenLedgerUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Slf4j
public class TermSDKTest {
    private String contractAddress = "0x6f6226990a1a071f13e131a45bcf99c83fc75ba5";
    Blockchain blockchain;
    CryptoSuite ecdsaCryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
    CryptoKeyPair cryptoKeyPair = ecdsaCryptoSuite.createKeyPair();
    CryptoKeyPair admin;
    private TermService termSDK;

    @Before
    public void init() {
        log.info("Begin Test-----------------");
        blockchain = new Blockchain("application.properties");
        log.info("Begin Test-----------------");
        blockchain = new Blockchain("application.properties");
        String pemFile = AccountImplTest.class.getClassLoader().getResource("conf/test.pem").getPath();
        ecdsaCryptoSuite.loadAccount("pem", pemFile, "");
        admin = ecdsaCryptoSuite.getCryptoKeyPair();
        log.info(admin.getAddress());
        if (StringUtils.isNotBlank(contractAddress)) {
            this.termSDK = new TermService(blockchain, contractAddress);
        }
    }

    @Test
    public void deploy() {
        ConnectionImpl connection = blockchain.buildConnection();
        // 部署合约
        Client client = connection.getSdk().getClient(1);
        CryptoKeyPair cryptoKeyPair = client.getCryptoSuite().createKeyPair();
        client.getCryptoSuite().setCryptoKeyPair(cryptoKeyPair);
        Term term = null;
        try {
            term = Term.deploy(blockchain.getClient(Blockchain.DEFAULT_LEDGERID), admin, admin.getAddress());

            contractAddress = term.getContractAddress();
            log.info(contractAddress);
            System.out.println(contractAddress);
        } catch (ContractException e) {
            log.error("test failed:",e);
        }
    }

    @Test
    public void getAddress() throws OpenLedgerBaseException {
        assertEquals(contractAddress, termSDK.getAddress());
    }

    @Test
    public void getTermInfo() throws OpenLedgerBaseException {
        String rightTermName = "test";
        TermEntity termEntity = termSDK.getTermInfo();
        assertNotNull(termEntity);
        assertEquals(rightTermName, termEntity.getName());

    }

    @Test
    public void newTerm() throws OpenLedgerBaseException {
        int curTermNo = termSDK.getTermInfo().getTermNo();
        String termName = "test78";
        byte[] message = OpenLedgerUtils.computeKeccak256Hash(termName.getBytes());
        ECDSASignatureResult rs = OpenLedgerUtils.sign(blockchain.getProjectAccount().getKeyPair(), message);
        ResponseData<BigInteger> newTermNo = termSDK.newTerm(termName, message, rs);
        log.info(newTermNo.getErrMsg());
        assertEquals(ErrorCode.SUCCESS.getCode(), newTermNo.getErrorCode().intValue());
        assertEquals(curTermNo + 1, newTermNo.getResult().intValue());

        log.info("newTermNo:" + newTermNo.getResult());
    }
}