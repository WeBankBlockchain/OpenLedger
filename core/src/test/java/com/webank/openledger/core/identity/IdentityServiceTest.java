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

package com.webank.openledger.core.identity;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.webank.openledger.contracts.Identity;
import com.webank.openledger.core.AccountImplTest;
import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.auth.AuthCenterService;
import com.webank.openledger.core.common.User;
import com.webank.openledger.core.exception.OpenLedgerBaseException;
import com.webank.openledger.core.response.ResponseData;
import com.webank.openledger.utils.OpenLedgerUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Slf4j
public class IdentityServiceTest {
    //    CryptoKeyPair cryptoKeyPair = ecdsaCryptoSuite.createKeyPair();
    private static final String AUTH_ADDRESS = "0xd18f9082236a5a7c2fad7d9bddf991ce6d3cad17";
    private static final String ORG_ADDRESS = "0x1b9f93ebd889c96d51736626ba3b1ad209adb5df";
    private String contractAddress = "0x91c07a9b92750ea2e23aa5f6561485052c9159f1";
    Blockchain blockchain;
    CryptoSuite ecdsaCryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
    CryptoKeyPair admin;
    private IdentityService<Identity> identityService;
    private AuthCenterService authCenterService;

    @Before
    public void init() throws ContractException {
        log.info("Begin Test-----------------");
        blockchain = new Blockchain("application.properties");
        String pemFile = AccountImplTest.class.getClassLoader().getResource("conf/test.pem").getPath();
        ecdsaCryptoSuite.loadAccount("pem", pemFile, "");
        admin = ecdsaCryptoSuite.getCryptoKeyPair();
        log.info(admin.getAddress());
        if (StringUtils.isNotBlank(contractAddress)) {
            this.identityService = new IdentityService(blockchain, contractAddress);
            this.authCenterService = new AuthCenterService(blockchain, AUTH_ADDRESS);
        }
    }

    @Test
    public void testDeploy() throws ContractException {
        Identity identity = Identity.deploy(blockchain.getClient(Blockchain.DEFAULT_LEDGERID), admin, AUTH_ADDRESS, ORG_ADDRESS);
        contractAddress = identity.getContractAddress();
        log.info(contractAddress);
    }

    @Test
    public void testInsertBaseType() throws OpenLedgerBaseException {
        String key = "test";
        String value = "testValue";
        BigInteger nonce = (BigInteger) authCenterService.getNonceFromAccount(admin.getAddress()).getResult();

        byte[] message = IdentityService.genHashByte(key, value, nonce);
        ResponseData<Boolean> responseData = identityService.insertWithSignatureResult(key, value, message, OpenLedgerUtils.sign(admin, message));
        log.info(responseData.getErrMsg());
        assertTrue(responseData.getResult());

    }


    @Test
    public void testInsertBeanType() throws OpenLedgerBaseException {
        String key = "test2";
        User value = new User();
        value.setSex(1);
        value.setName("a");
        BigInteger nonce = (BigInteger) authCenterService.getNonceFromAccount(admin.getAddress()).getResult();
        byte[] message = IdentityService.genHashByte(key, value, nonce);
        ResponseData<Boolean> responseData = identityService.insertWithSignatureResult(key, value, message, OpenLedgerUtils.sign(admin, message));
        log.info(responseData.getErrMsg());
        assertTrue(responseData.getResult());
    }

    @Test
    public void testInsertListType() throws OpenLedgerBaseException {
        String key = "test3";
        User value1 = new User();
        value1.setSex(1);
        value1.setName("a");
        User value2 = new User();
        value2.setSex(2);
        value2.setName("b");
        List<User> userList = new ArrayList<>();
        userList.add(value1);
        userList.add(value2);
        BigInteger nonce = (BigInteger) authCenterService.getNonceFromAccount(admin.getAddress()).getResult();
        byte[] message = IdentityService.genHashByte(key, userList, nonce);
        ResponseData<Boolean> responseData = identityService.insertWithSignatureResult(key, userList, message, OpenLedgerUtils.sign(admin, message));
        log.info(responseData.getErrMsg());

        assertTrue(responseData.getResult());


    }

    @Test
    public void testGetBaseType() throws OpenLedgerBaseException {
        String key = "test3";
        User value1 = new User();
        value1.setSex(1);
        value1.setName("a");
        User value2 = new User();
        value2.setSex(2);
        value2.setName("b");
        List<User> userList = new ArrayList<>();
        userList.add(value1);
        userList.add(value2);

        BigInteger nonce = (BigInteger) authCenterService.getNonceFromAccount(admin.getAddress()).getResult();
        byte[] message = IdentityService.genHashByte(key, nonce);
        List<User> valueObj = (List<User>) identityService.getWithSignatureResult(key, message, OpenLedgerUtils.sign(admin, message), new TypeReference<List<User>>() {
        });

        assertEquals(userList.get(0).getName(), valueObj.get(0).getName());

        key = "test";
        String value = "testValue";
        nonce = (BigInteger) authCenterService.getNonceFromAccount(admin.getAddress()).getResult();
        message = IdentityService.genHashByte(key, nonce);
        String valueString = (String) identityService.getWithSignatureResult(key, message, OpenLedgerUtils.sign(admin, message));
        assertEquals(value, valueString);

    }

    @Test
    public void testAdd() throws OpenLedgerBaseException {
        String key = "test6";
        String value = "testValue2";
        BigInteger nonce = (BigInteger) authCenterService.getNonceFromAccount(admin.getAddress()).getResult();

        byte[] message = IdentityService.genHashByte(key, value, nonce);
        ResponseData<Boolean> responseData = identityService.addWithSignatureResult(key, value, message, OpenLedgerUtils.sign(admin, message));
        log.info(responseData.getErrMsg());
        assertTrue(responseData.getResult());

    }

    @Test
    public void testSet() throws OpenLedgerBaseException {
        String key = "test5";
        String value = "testValue";
        BigInteger nonce = (BigInteger) authCenterService.getNonceFromAccount(admin.getAddress()).getResult();
        byte[] message = IdentityService.genHashByte(key, value, nonce);
        ResponseData<Boolean> responseData = identityService.setWithSignatureResult(key, value, message, OpenLedgerUtils.sign(admin, message));
        assertFalse(responseData.getResult());

        key = "test";
        nonce = (BigInteger) authCenterService.getNonceFromAccount(admin.getAddress()).getResult();
        message = IdentityService.genHashByte(key, value, nonce);
        responseData = identityService.setWithSignatureResult(key, value, message, OpenLedgerUtils.sign(admin, message));
        assertTrue(responseData.getResult());


    }

    @Test
    public void testRemove() throws OpenLedgerBaseException {
        String key = "test";
        String value = "testValue";
        BigInteger nonce = (BigInteger) authCenterService.getNonceFromAccount(admin.getAddress()).getResult();
        byte[] message = IdentityService.genHashByte(key, nonce);
        ResponseData<Boolean> responseData = identityService.removeWithSignatureResult(key, message, OpenLedgerUtils.sign(admin, message));
        assertTrue(responseData.getResult());
    }

}