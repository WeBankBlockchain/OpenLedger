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

package com.webank.openledger.core.assetpool;


import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.List;

import com.webank.openledger.core.AccountImplTest;
import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.assetpool.entity.PoolStatus;
import com.webank.openledger.core.auth.AuthCenterService;
import com.webank.openledger.core.constant.ErrorCode;
import com.webank.openledger.core.exception.OpenLedgerBaseException;
import com.webank.openledger.core.response.ResponseData;
import com.webank.openledger.utils.OpenLedgerUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.model.CryptoType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Slf4j
public class AssetPoolServiceTest {
    private CryptoSuite ecdsaCryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
    private static final String AUTH_ADDRESS = "0x22a001e0b4a60f135f88ce055540a14c5f8adae2";
    private static final String ORG_ADDRESS = "0x670f36852e6d8d0ce6fd97d5e2877e46c1008812";
    private String contractAddress = "0xdded91b67443f1f11c4032a2d4d14a20f206ff4b";
    Blockchain blockchain;
    String[] assets = {"0xa8343e11d8f46955e877566791c82a7a53c42848", "0xb8343e11d8f46955e877566791c82a7a53c42848", "0xb8343e11d8f46955e877566791c82a7a53c42846"};
    CryptoKeyPair admin;
    PoolStatus p1 = new PoolStatus(BigInteger.valueOf(1), "name1", "中文");
    PoolStatus p2 = new PoolStatus(BigInteger.valueOf(2), "name2", "desc2");
    private AssetPoolService assetPoolService;
    private AuthCenterService authCenterService;

    @Before
    public void init() {
        log.info("Begin Test-----------------");
        blockchain = new Blockchain("application.properties");

        String pemFile = AccountImplTest.class.getClassLoader().getResource("conf/test.pem").getPath();
        ecdsaCryptoSuite.loadAccount("pem", pemFile, "");
        admin = ecdsaCryptoSuite.getCryptoKeyPair();
        log.info(admin.getAddress());
        if (StringUtils.isNotBlank(contractAddress)) {
            this.assetPoolService = new AssetPoolService(blockchain, contractAddress);
            this.authCenterService = new AuthCenterService(blockchain, AUTH_ADDRESS);
        }
    }

    @Test
    public void deploy() throws OpenLedgerBaseException {
        contractAddress = assetPoolService.createAssetPool(blockchain, ORG_ADDRESS, AUTH_ADDRESS);
        log.info(contractAddress);
    }

    @Test
    public void addAsset() throws OpenLedgerBaseException {
        BigInteger nonce = (BigInteger) authCenterService.getNonceFromAccount(admin.getAddress()).getResult();
        byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.convertStringToAddressByte(assets[0]), OpenLedgerUtils.getBytes32(nonce.toByteArray()));
        byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);
        ECDSASignatureResult rs = OpenLedgerUtils.sign(admin, message);
        ResponseData<List<String>> assetListRes = assetPoolService.addAsset(assets[0], message, rs);
        log.info(assetListRes.getErrMsg());
        assertTrue(ErrorCode.SUCCESS.getCode() == assetListRes.getErrorCode().intValue());

    }

    @Test
    public void addStatus() throws OpenLedgerBaseException, UnsupportedEncodingException {
        BigInteger nonce = (BigInteger) authCenterService.getNonceFromAccount(admin.getAddress()).getResult();
        byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.getBytes32(p1.getStatus().toByteArray()), p1.getName().getBytes("utf-8"), p1.getDesc().getBytes("utf-8"), OpenLedgerUtils.getBytes32(nonce.toByteArray()));

        byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);
        ECDSASignatureResult rs = OpenLedgerUtils.sign(admin, message);
        ResponseData<BigInteger> responseData = assetPoolService.addStatus(p1, message, rs);
        log.info(responseData.getErrMsg());

        assertTrue(ErrorCode.SUCCESS.getCode() == responseData.getErrorCode().intValue());

    }

    @Test
    public void moveAsset() throws OpenLedgerBaseException {
        BigInteger nonce = (BigInteger) authCenterService.getNonceFromAccount(admin.getAddress()).getResult();
        byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.convertStringToAddressByte(assets[0]), OpenLedgerUtils.getBytes32(p1.getStatus().toByteArray()), OpenLedgerUtils.getBytes32(nonce.toByteArray()));
        byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);
        ECDSASignatureResult rs = OpenLedgerUtils.sign(admin, message);
        ResponseData<BigInteger> responseData = assetPoolService.moveAsset(assets[0], p1.getStatus().intValue(), message, rs);
        log.info(responseData.getErrMsg());
        assertEquals(p1.getStatus().intValue(), responseData.getResult().intValue());
    }

    @Test
    public void removeAsset() throws OpenLedgerBaseException {
        BigInteger nonce = (BigInteger) authCenterService.getNonceFromAccount(admin.getAddress()).getResult();
        byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.convertStringToAddressByte(assets[0]), OpenLedgerUtils.getBytes32(nonce.toByteArray()));
        byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);
        ECDSASignatureResult rs = OpenLedgerUtils.sign(admin, message);
        ResponseData<List<String>> resultList = assetPoolService.removeAsset(assets[0], message, rs);
        log.info(resultList.getErrMsg());
        assertEquals(ErrorCode.SUCCESS.getCode(), resultList.getErrorCode().intValue());
        assertNotEquals(assets, resultList.getResult().size());
    }

    @Test
    public void getPoolStatus() throws OpenLedgerBaseException {
        assertEquals(assetPoolService.NORMAL_STATUS, assetPoolService.getPoolStatus());
    }

    @Test
    public void forzenPool() throws OpenLedgerBaseException {
        BigInteger nonce = (BigInteger) authCenterService.getNonceFromAccount(admin.getAddress()).getResult();
        byte[] message = OpenLedgerUtils.computeKeccak256HashFromBigInteger(nonce);
        ECDSASignatureResult rs = OpenLedgerUtils.sign(admin, message);
        ResponseData<BigInteger> poolStatus = assetPoolService.freezePool(message, rs);
        log.info(poolStatus.getErrMsg());
        assertEquals(ErrorCode.SUCCESS.getCode(), poolStatus.getErrorCode().intValue());
        assertEquals(assetPoolService.FROZEN_STATUS, poolStatus.getResult().intValue());

        nonce = (BigInteger) authCenterService.getNonceFromAccount(admin.getAddress()).getResult();
        message = OpenLedgerUtils.computeKeccak256HashFromBigInteger(nonce);
        ResponseData<List<String>> resultList = assetPoolService.removeAsset(assets[0], message, rs);
        assertNotEquals(ErrorCode.SUCCESS.getCode(), resultList.getErrorCode().intValue());
        assertNotNull(resultList.getErrMsg());
        System.out.println(resultList.getErrMsg());
    }

    @Test
    public void unForzenPool() throws OpenLedgerBaseException {
        BigInteger nonce = (BigInteger) authCenterService.getNonceFromAccount(admin.getAddress()).getResult();
        byte[] message = OpenLedgerUtils.computeKeccak256HashFromBigInteger(nonce);
        ECDSASignatureResult rs = OpenLedgerUtils.sign(admin, message);
        ResponseData<BigInteger> poolStatus = assetPoolService.unFreezePool(message, rs);
        log.info(poolStatus.getErrMsg());
        assertEquals(ErrorCode.SUCCESS.getCode(), poolStatus.getErrorCode().intValue());
        assertEquals(assetPoolService.NORMAL_STATUS, poolStatus.getResult().intValue());
    }

    @Test
    public void getAssetList() throws OpenLedgerBaseException {
        BigInteger nonce = (BigInteger) authCenterService.getNonceFromAccount(admin.getAddress()).getResult();
        byte[] message = OpenLedgerUtils.computeKeccak256HashFromBigInteger(nonce);
        ECDSASignatureResult rs = OpenLedgerUtils.sign(admin, message);
        List<String> result = assetPoolService.getAssetList(message, rs);

        System.out.println(result.toString());
    }

}