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

import com.webank.openledger.contractsbak.AuthCenter;
import com.webank.openledger.contractsbak.Currency;
import com.webank.openledger.core.AccountImplTest;
import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.ConnectionImpl;
import com.webank.openledger.core.asset.fungible.entity.CurrencyEntity;
import com.webank.openledger.core.asset.fungible.entity.TransferResult;
import com.webank.openledger.core.auth.AuthCenterService;
import com.webank.openledger.core.exception.OpenLedgerBaseException;
import com.webank.openledger.core.response.ResponseData;
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
import static org.junit.Assert.assertTrue;

@Slf4j
public class CurrencyServiceTest extends FungibleAssetServiceTest {
//    private static final int DECIMALS = 2;
//    private static final String NAME = "ABC-NAME";
//    private static final String SYMBOL = "ABC";
//    private static final String AUTH_ADDRESS = "0x1254e601ecde8bad5372ba188b26cb2052b56cde";
//    private static final String ORG_ADDRESS = "0x5106c2658d88a4e21137e259cb684b4d3741b65e";
//    private String contractAddress = "0x9c8850ec1138350d119cc9c5b235a0e28a0ae18f";
//    private static String assetAddress = "0xbacd866e041c837579496766d9c110a1566e49db";
//    Blockchain blockchain;
//    CryptoSuite ecdsaCryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
//    CryptoKeyPair admin;
//    CryptoKeyPair operator;
//    CryptoKeyPair owner;
//    CryptoKeyPair user;
//    private CurrencyService currencySDK;
//    private AuthCenterService<AuthCenter> authCenterSDK;
//
//    @Before
//    public void init() {
//        log.info("Begin Test-----------------");
//        blockchain = new Blockchain("application.properties");
//        String pemFile = AccountImplTest.class.getClassLoader().getResource("conf/test.pem").getPath();
//        ecdsaCryptoSuite.loadAccount("pem", pemFile, "");
//        admin = ecdsaCryptoSuite.getCryptoKeyPair();
//        log.info(admin.getAddress());
//        pemFile = AccountImplTest.class.getClassLoader().getResource("conf/test2.pem").getPath();
//        ecdsaCryptoSuite.loadAccount("pem", pemFile, "");
//        operator = ecdsaCryptoSuite.getCryptoKeyPair();
//        log.info(operator.getAddress());
//        pemFile = AccountImplTest.class.getClassLoader().getResource("conf/test3.pem").getPath();
//        ecdsaCryptoSuite.loadAccount("pem", pemFile, "");
//        owner = ecdsaCryptoSuite.getCryptoKeyPair();
//        log.info(owner.getAddress());
//
//        pemFile = AccountImplTest.class.getClassLoader().getResource("conf/test4.pem").getPath();
//        ecdsaCryptoSuite.loadAccount("pem", pemFile, "");
//        user = ecdsaCryptoSuite.getCryptoKeyPair();
//        log.info(user.getAddress());
//
//
//        if (StringUtils.isNotBlank(contractAddress)) {
//            this.currencySDK = new CurrencyService(blockchain, contractAddress);
//            this.authCenterSDK = new AuthCenterService<>(blockchain, AUTH_ADDRESS);
//        }
//    }
//
//
//    @Test
//    public void openAccount() throws OpenLedgerBaseException {
//        // 交易参数
//        String fromAddress = operator.getAddress();
//
//        BigInteger nonce = authCenterSDK.getNonceFromAccount(admin.getAddress()).getResult();
//        byte[] result = new byte[0];
//        result = OpenLedgerUtils.concatByte(result, OpenLedgerUtils.convertStringToAddressByte(fromAddress));
//        byte[] messageOpenAccount = StandardAssetService.computeOpenAccountMsg(fromAddress, nonce);
//
//        ResponseData<Boolean> responseData = currencySDK.openAccount(fromAddress, messageOpenAccount, OpenLedgerUtils.sign(admin, messageOpenAccount));
//        log.info(responseData.getErrMsg());
//        assertTrue(responseData.getResult());
//    }
//
//
//    @Test
//    public void deploy() {
//        ConnectionImpl connection = blockchain.buildConnection();
//        // 部署合约
//        Client client = connection.getSdk().getClient(1);
//        CryptoKeyPair cryptoKeyPair = client.getCryptoSuite().createKeyPair();
//        client.getCryptoSuite().setCryptoKeyPair(cryptoKeyPair);
//        Currency currency = null;
//        String tableName = "currenc11y";
//        try {
//            currency = Currency.deploy(client, cryptoKeyPair, NAME, SYMBOL, BigInteger.valueOf(DECIMALS), AUTH_ADDRESS, ORG_ADDRESS);
//            contractAddress = currency.getContractAddress();
//            log.info(contractAddress);
//        } catch (ContractException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void getCurrencyInfo() throws OpenLedgerBaseException {
//        CurrencyEntity currencyEntity = currencySDK.getCurrencyInfo();
//        assertNotNull(currencyEntity);
//        assertEquals(DECIMALS, currencyEntity.getDecimals());
//        assertEquals(NAME, currencyEntity.getName());
//        assertEquals(SYMBOL, currencyEntity.getSymbol());
//    }
//
//    @Test
//    public void deposit() throws OpenLedgerBaseException, UnsupportedEncodingException {
//        // 交易参数
//        String account = admin.getAddress();
//        String operatorAddress = operator.getAddress();
//        BigInteger amount = BigInteger.valueOf(100);
//        String detail = "TEST";
//        String subject = "subject";
//        //交易序列号 从authcenter获取
//        BigInteger nonce = authCenterSDK.getNonceFromAccount(operator.getAddress()).getResult();
//        List<String> addressList = StandardAssetService.genAddress(null, account, operatorAddress, contractAddress, assetAddress);
//        byte[] message = StandardAssetService.computeTxMsg(addressList, amount.multiply(BigInteger.valueOf(10).pow(DECIMALS)), StandardAssetService.genType(1), StandardAssetService.genDetail(detail, subject), nonce);
//        ECDSASignatureResult sign = OpenLedgerUtils.sign(operator, message);
//
//        ResponseData<TransferResult> responseData = currencySDK.deposit(operatorAddress, account, amount, 1, detail, subject, assetAddress, message, sign);
//        log.info(responseData.getErrMsg());
//
//        log.info(responseData.getErrMsg());
//        assertTrue(responseData.getResult() != null && responseData.getResult().getIsSuccees());
//        log.info(responseData.getResult().toString());
//    }


}