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

package com.webank.openledger.core.asset.fungible;


import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.List;

import com.webank.openledger.contractsbak.Account;
import com.webank.openledger.contractsbak.AuthCenter;
import com.webank.openledger.contracts.Organization;
import com.webank.openledger.contractsbak.FungibleAsset;
import com.webank.openledger.core.AccountImplTest;
import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.account.AccountService;
import com.webank.openledger.core.asset.AccountHolderService;
import com.webank.openledger.core.asset.BaseCustodyService;
import com.webank.openledger.core.asset.fungible.entity.Condition;
import com.webank.openledger.core.asset.fungible.entity.RecordEntity;
import com.webank.openledger.core.asset.fungible.entity.TransferResult;
import com.webank.openledger.core.auth.AuthCenterService;
import com.webank.openledger.core.constant.ErrorCode;
import com.webank.openledger.core.exception.OpenLedgerBaseException;
import com.webank.openledger.core.org.OrganizationService;
import com.webank.openledger.core.response.DataToolUtils;
import com.webank.openledger.core.response.ResponseData;
import com.webank.openledger.utils.OpenLedgerUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Slf4j
public class FungibleAssetServiceTest {
    private static final String AUTH_ADDRESS = "0xfb1f7fc9b23e8c86c3200610c56bb57576a1f6a3";
    private static final String ORG_ADDRESS = "0x81cc905d231db4dbb9b5ffe6dd9158f61d3c0f3e";
    Blockchain blockchain;
    CryptoSuite ecdsaCryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
    CryptoKeyPair admin;
    CryptoKeyPair operator;
    CryptoKeyPair owner;
    CryptoKeyPair user;
    private FungibleAssetService fungibleAssetService;
    private AuthCenterService<AuthCenter> authCenterSDK;
    private OrganizationService custody;
    private AccountService holder;
    private String custodyContractAddress = "0x098e612559058f70e2acbf95549eab264133ff2d";
    private String holderContractAddress = "0x098e612559058f70e2acbf95549eab264133ff2d";
    private String assetAddress = "0x64d6f547a787803e39b8bfa29656d8ac47633118";
    private String USER_ACCOUNT1 = "0x8445a5599fb313522d3355be6f1974c9b6b5bf6a";
    private String USER_ACCOUNT2 = "0x8445a5599fb313522d3355be6f1974c9b6b5bf6a";

    @Before
    public void init() {
        log.info("Begin Test-----------------");
        blockchain = new Blockchain("application.properties");
        String pemFile = AccountImplTest.class.getClassLoader().getResource("conf/test.pem").getPath();
        ecdsaCryptoSuite.loadAccount("pem", pemFile, "");
        admin = ecdsaCryptoSuite.getCryptoKeyPair();
        log.info(admin.getAddress());
        pemFile = AccountImplTest.class.getClassLoader().getResource("conf/test2.pem").getPath();
        ecdsaCryptoSuite.loadAccount("pem", pemFile, "");
        operator = ecdsaCryptoSuite.getCryptoKeyPair();
        log.info(operator.getAddress());
        pemFile = AccountImplTest.class.getClassLoader().getResource("conf/test3.pem").getPath();
        ecdsaCryptoSuite.loadAccount("pem", pemFile, "");
        owner = ecdsaCryptoSuite.getCryptoKeyPair();
        log.info(owner.getAddress());

        pemFile = AccountImplTest.class.getClassLoader().getResource("conf/test4.pem").getPath();
        ecdsaCryptoSuite.loadAccount("pem", pemFile, "");
        user = ecdsaCryptoSuite.getCryptoKeyPair();
        log.info(user.getAddress());

        if (StringUtils.isNotBlank(custodyContractAddress)) {
            this.custody = new OrganizationService(blockchain, custodyContractAddress, assetAddress);
        }
        if (StringUtils.isNotBlank(holderContractAddress)) {
            this.holder = new AccountService(blockchain, custodyContractAddress, assetAddress);
        }
    }


//
//    @Test
//    public void getAsset() {
//        assertNotNull(fungibleAssetService.getAsset());
//        assertNotNull(fungibleAssetService.getIdentity());
//    }
//
//    @Test
//    public void getAssetInfo() throws OpenLedgerBaseException {
//        BigInteger assetPrice = BigInteger.valueOf(100);
//        BigInteger assetRate = BigInteger.valueOf(200);
//        BigInteger nonce = authCenterSDK.getNonceFromAccount(admin.getAddress()).getResult();
//        byte[] messagePrice = OpenLedgerUtils.computeKeccak256Hash(OpenLedgerUtils.concatByte(OpenLedgerUtils.getBytes32(assetPrice.toByteArray()), OpenLedgerUtils.getBytes32(nonce.toByteArray())));
//
//        ResponseData<BigInteger> responseRPriceData = fungibleAssetService.setPrice(assetPrice, messagePrice, OpenLedgerUtils.sign(admin, messagePrice));
//        log.info(responseRPriceData.getErrMsg());
//        assertEquals(ErrorCode.SUCCESS.getCode(), responseRPriceData.getErrorCode().intValue());
//
//        nonce = authCenterSDK.getNonceFromAccount(admin.getAddress()).getResult();
//        byte[] messageRate = OpenLedgerUtils.computeKeccak256Hash(OpenLedgerUtils.concatByte(OpenLedgerUtils.getBytes32(assetRate.toByteArray()), OpenLedgerUtils.getBytes32(nonce.toByteArray())));
//
//        ResponseData<BigInteger> responseRateData = fungibleAssetService.setRate(assetRate, messageRate, OpenLedgerUtils.sign(admin, messageRate));
//        assertEquals(ErrorCode.SUCCESS.getCode(), responseRateData.getErrorCode().intValue());
//
//        AssetEntity assetEntity = fungibleAssetService.getAssetInfo();
//        assertNotNull(assetEntity);
//        assertEquals(contractAddress, assetEntity.getAddress());
//        assertEquals(assetPrice, assetEntity.getPrice());
//        assertEquals(assetRate, assetEntity.getRate());
//    }

    @Test
    public void openAccount() throws OpenLedgerBaseException, ContractException, UnsupportedEncodingException {
        String account = custodyContractAddress;
//        BigInteger nonce = authCenterSDK.getNonceFromAccount(operator.getAddress()).getResult();
        BigInteger nonce = new BigInteger("1");
        byte[] result = new byte[0];
        result = OpenLedgerUtils.concatByte(result, OpenLedgerUtils.convertStringToAddressByte(account));
        byte[] messageOpenAccount = StandardAssetService.computeOpenAccountMsg(account, nonce);

        ResponseData<Boolean> responseData = custody.openAccount(account, messageOpenAccount, OpenLedgerUtils.sign(user, messageOpenAccount));
        log.info(responseData.getErrMsg());
        assertTrue(responseData.getResult());

    }

    @Test
    public void deposit() throws OpenLedgerBaseException, UnsupportedEncodingException {
        // 交易参数
        String account = custodyContractAddress;
        String operatorAddress = USER_ACCOUNT1;
        BigInteger amount = BigInteger.valueOf(100);
        String detail = "test";
        //交易序列号 从authcenter获取
//        BigInteger nonce = authCenterSDK.getNonceFromAccount(admin.getAddress()).getResult();
        BigInteger nonce = new BigInteger("1");

        List<String> addressList = StandardAssetService.genAddress(null, account, operatorAddress, assetAddress, null);
        byte[] message = StandardAssetService.computeTxMsg(addressList, amount, StandardAssetService.genType(1), StandardAssetService.genDetail(detail, null), nonce);
        ECDSASignatureResult sign = OpenLedgerUtils.sign(user, message);

        ResponseData<TransferResult> responseData = custody.deposit(operatorAddress, account, amount, 1, detail, message, sign);
        log.info(responseData.getErrMsg());
        assertTrue(responseData.getResult().getIsSuccees());
        log.info(responseData.getResult().toString());
    }

    @Test
    public void withdrawal() throws OpenLedgerBaseException, UnsupportedEncodingException {
        // 交易参数
        String account = USER_ACCOUNT1;
        String operatorAddress = USER_ACCOUNT1;
        BigInteger amount = BigInteger.valueOf(1);
        String detail = "test";
        //交易序列号 从authcenter获取

//        BigInteger nonce = authCenterSDK.getNonceFromAccount(operatorAddress).getResult();
        BigInteger nonce = new BigInteger("1");

        List<String> addressList = StandardAssetService.genAddress(account, null, operatorAddress, assetAddress, null);

        byte[] message = StandardAssetService.computeTxMsg(addressList, amount, StandardAssetService.genType(2), StandardAssetService.genDetail(detail, null), nonce);
        ECDSASignatureResult sign = OpenLedgerUtils.sign(user, message);

        ResponseData<TransferResult> responseData = custody.withdrawal(operatorAddress, account, amount, 2, detail, message, sign);

        log.info(responseData.getErrMsg());
        assertTrue(responseData.getResult() != null && responseData.getResult().getIsSuccees());
        log.info(responseData.getResult().toString());
    }

    @Test
    public void getBalance() throws OpenLedgerBaseException {
        // 交易参数
        String fromAddress = custodyContractAddress;
//        BigInteger nonce = authCenterSDK.getNonceFromAccount(user.getAddress()).getResult();
        BigInteger nonce = new BigInteger("1");
        System.out.println(custody.getBalance(fromAddress, OpenLedgerUtils.computeKeccak256HashFromBigInteger(nonce), OpenLedgerUtils.sign(user, OpenLedgerUtils.computeKeccak256HashFromBigInteger(nonce))));
    }


    @Test
    public void transfer() throws OpenLedgerBaseException, UnsupportedEncodingException {
        log.info(admin.getAddress());
        // 交易参数
        String fromAddress = custodyContractAddress;
        String toAddress = USER_ACCOUNT2;
        String operatorAddress = USER_ACCOUNT1;
        BigInteger amount = BigInteger.valueOf(10);
        String detail = "test";
        //交易序列号 从authcenter获取
//        BigInteger nonce = authCenterSDK.getNonceFromAccount(operatorAddress).getResult();
        BigInteger nonce = new BigInteger("1");
        byte[] message = StandardAssetService.computeTxMsg(StandardAssetService.genAddress(fromAddress, toAddress, operatorAddress, assetAddress, null), amount, StandardAssetService.genType(3), StandardAssetService.genDetail(detail, null), nonce);
        ResponseData<TransferResult> responseData = holder.transferFungibleAsset(operatorAddress, fromAddress, toAddress, amount, 3, detail, message, OpenLedgerUtils.sign(admin, message));
        log.info(responseData.getErrMsg());
        assertTrue(responseData.getResult() != null && responseData.getResult().getIsSuccees());
        log.info(responseData.getResult().toString());
    }

    @Test
    public void getHolders() {
        try {
//            BigInteger nonce = authCenterSDK.getNonceFromAccount(admin.getAddress()).getResult();
            BigInteger nonce = new BigInteger("1");
            byte[] message = OpenLedgerUtils.computeKeccak256HashFromBigInteger(nonce);
            System.out.println(custody.getHolders(message, OpenLedgerUtils.sign(operator, message)));
        } catch (OpenLedgerBaseException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getTotalBalance() throws OpenLedgerBaseException {
//        BigInteger nonce = authCenterSDK.getNonceFromAccount(admin.getAddress()).getResult();
        BigInteger nonce = new BigInteger("1");
        byte[] message = OpenLedgerUtils.computeKeccak256HashFromBigInteger(nonce);
        System.out.println(custody.getTotalBalance(message, OpenLedgerUtils.sign(admin, message)));
    }

    @Test
    public void addBook() throws OpenLedgerBaseException {
//        BigInteger nonce = authCenterSDK.getNonceFromAccount(admin.getAddress()).getResult();
        BigInteger nonce = new BigInteger("1");
        byte[] message = OpenLedgerUtils.computeKeccak256HashFromBigInteger(nonce);
        // 交易参数
        ResponseData<BigInteger> responseData = custody.addBook(message, OpenLedgerUtils.sign(admin, message));
        assertTrue(ErrorCode.SUCCESS.getCode() == responseData.getErrorCode().intValue());
        log.info("booknum:" + responseData.getResult());
    }


    @Test
    public void query() throws Exception {
        BigInteger rightTermNo = BigInteger.valueOf(0);
        BigInteger rightSeq = BigInteger.valueOf(28);

        // 交易参数
        String operatorAddress = user.getAddress();
        BigInteger nonce = authCenterSDK.getNonceFromAccount(admin.getAddress()).getResult();
        byte[] message = OpenLedgerUtils.computeKeccak256HashFromBigInteger(nonce);
        ECDSASignatureResult rs = OpenLedgerUtils.sign(admin, message);
        String account1 = admin.getAddress();
        String account2 = user.getAddress();

        Condition condition1 = new Condition(rightTermNo, rightSeq, null, account1);
        List<RecordEntity> recordEntities = custody.queryFungibleByCustody(condition1, message, rs);
        assertNotNull(recordEntities);
        assertTrue(recordEntities.size() == 1);

        Condition condition2 = new Condition(BigInteger.valueOf(0), rightSeq, account1, account2);
        recordEntities = custody.queryFungibleByCustody(condition2, message, rs);
        assertNotNull(recordEntities);
        recordEntities.stream().forEach(item -> System.out.println(item));
        System.out.println("=====================================");
        assertTrue(recordEntities.size() == 1);

        Condition condition3 = new Condition(rightTermNo, BigInteger.valueOf(0), account1, account2);
        recordEntities = custody.queryFungibleByCustody(condition3, message, rs);
        assertNotNull(recordEntities);
        recordEntities.stream().forEach(item -> System.out.println(item));
        System.out.println("=====================================");
        assertTrue(recordEntities.size() >= 1);

        Condition condition4 = new Condition(BigInteger.valueOf(0), BigInteger.valueOf(0), account1, account2);
        recordEntities = custody.queryFungibleByCustody(condition4, message, rs);
        assertNotNull(recordEntities);
        recordEntities.stream().forEach(item -> System.out.println(item));
        System.out.println("=====================================");
        assertTrue(recordEntities.size() >= 1);

        Condition condition5 = new Condition(BigInteger.valueOf(0), BigInteger.valueOf(0), account1, null);
        recordEntities = custody.queryFungibleByCustody(condition5, message, rs);
        assertNotNull(recordEntities);
        recordEntities.stream().forEach(item -> System.out.println(item));
        System.out.println("=====================================");
        assertTrue(recordEntities.size() >= 1);

        Condition condition7 = new Condition(BigInteger.valueOf(3), rightSeq, account1, account2);
        recordEntities = custody.queryFungibleByCustody(condition7, message, rs);
        assertTrue(recordEntities == null || recordEntities.size() == 0);

        Condition condition8 = new Condition(rightTermNo, BigInteger.valueOf(363), account1, account2);
        recordEntities = custody.queryFungibleByCustody(condition8, message, rs);
        assertTrue(recordEntities == null || recordEntities.size() == 0);


        // 只能查詢自己的賬本 即from/to 是查詢者
        nonce = authCenterSDK.getNonceFromAccount(user.getAddress()).getResult();
        message = OpenLedgerUtils.computeKeccak256HashFromBigInteger(nonce);
        rs = OpenLedgerUtils.sign(user, message);
        Condition condition6 = new Condition(BigInteger.valueOf(0), BigInteger.valueOf(0), null, account2);
        recordEntities = custody.queryFungibleByCustody(condition6, message, rs);
        assertNotNull(recordEntities);
        recordEntities.stream().forEach(item -> System.out.println(item));
        System.out.println("=====================================");
        assertTrue(recordEntities.size() >= 1);

        Condition condition9 = new Condition(rightTermNo, rightSeq, account2, account2);
        recordEntities = custody.queryFungibleByCustody(condition9, message, rs);
        assertTrue(recordEntities == null || recordEntities.size() == 0);


    }

    @Test
    public void queryBookByAdmin() throws Exception {
        BigInteger rightTermNo = BigInteger.valueOf(0);
        BigInteger rightSeq = BigInteger.valueOf(27);
        // 交易参数
        String operatorAddress = user.getAddress();
        BigInteger nonce = authCenterSDK.getNonceFromAccount(admin.getAddress()).getResult();
        byte[] message = OpenLedgerUtils.computeKeccak256HashFromBigInteger(nonce);
        ECDSASignatureResult rs = OpenLedgerUtils.sign(admin, message);
        //组织管理者运行查询全部
        Condition condition10 = new Condition(rightTermNo, rightSeq, null, null);
        List<RecordEntity> recordEntities = custody.queryFungibleByCustody(condition10, message, rs);
        assertNotNull(recordEntities);
        recordEntities.stream().forEach(item -> System.out.println(item));
        System.out.println("=====================================");
        assertTrue(recordEntities.size() >= 1);

    }

    @Test
    public void testCreateKeyPair() {
        admin = ecdsaCryptoSuite.createKeyPair();
        System.out.println(admin.getHexPrivateKey());
        System.out.println(admin.getAddress());

        admin.storeKeyPairWithPem("src/test/resources/conf/test4.pem");
    }


}