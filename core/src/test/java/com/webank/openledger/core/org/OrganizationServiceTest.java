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

package com.webank.openledger.core.org;


import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.openledger.contractsbak.AuthCenter;
import com.webank.openledger.core.AccountImplTest;
import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.auth.AuthCenterService;
import com.webank.openledger.core.constant.ErrorCode;
import com.webank.openledger.core.response.ResponseData;
import com.webank.openledger.utils.OpenLedgerUtils;

import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;


@Slf4j
public class OrganizationServiceTest {
    private static final String ORG_1_USER_PRI_KEY = "1c0362fb21ea3cc00dd2dbc0307232cc50782fb355bb507f5f9312d0e10db618";
    private static final String ORG_1_USER_2_PRI_KEY = "12c216b141605557d4772d0d73c4992100384e811db9cbc850c64ab1707f9dfe";
    private static final String ORG_1_USER_2_ADDR = "0xe894706e8dc3d03808fcc5863fc43c1ca24392f0";
    Blockchain blockchain;
    //private static final String org1AdminAddr  = "0x745f494b4646e8a64bddd20bc334a6a4e07c297c";
    CryptoSuite org1Admin = new CryptoSuite(CryptoType.ECDSA_TYPE, "4cadf8e1724eb6fd8f5b55b3371f6039a3769fd8b7c40fbce68ca1ccf61b0563");
    //addr: 0x5906d4a2d7c60f89732291d2ce629003beac72c5.pem
    CryptoSuite txAccount = new CryptoSuite(CryptoType.ECDSA_TYPE, "3adf8d4737369e4cf34f7be702057dbf3f44800b66cd18f454d49837de9bdb9e");
    //合约地址
    String projectAddr = "0x6df88fe42bf13bbd42b156fc8c83a438845336e1";
    String accountManagerAddr = "0xde5248911bca2468e71efc7a57e5002963917fd5";
    String authManagerAddr = "0x4ce6fc942eb79e89dbe597eac4dab78a8c29c2f1";
    String authCenterAddr = "0x1254e601ecde8bad5372ba188b26cb2052b56cde";
    String assetManagerAddr = "0xa41fac00c332b00bb36beb2772c831fb57fe2a80";
    String nonFungibleAssetManager="0x8f7b84195cfd596706fec3159cc5b083e173bada";


    //组织org
    String org1Addr = "0xc638ccb69e8f0c176c0b04c347d5b0d842ed34e6";
    String org2Addr = "0x0";
    private OrganizationService orgService;
    private AuthCenterService<AuthCenter> authCenterService;
    CryptoKeyPair admin;
    CryptoKeyPair user;
    CryptoKeyPair operator;
    private   String ORG_1_USER_1_ADDR = "0xa1ebac1c01725ef092f0a2d7192c6ab37106cc86";

    @Before
    public void init() {
        log.info("Begin Test-----------------");
        blockchain = new Blockchain("application.properties");
        blockchain.getDefaultClient().getCryptoSuite().setCryptoKeyPair(txAccount.getCryptoKeyPair());

        this.orgService = new OrganizationService(blockchain, org1Addr);
        this.authCenterService = new AuthCenterService<>(blockchain, authCenterAddr);

        log.info("org admin addr:{} {}",
                blockchain.getDefaultClient().getCryptoSuite().getCryptoKeyPair().getAddress(),
                org1Admin.getCryptoKeyPair().getAddress());
        CryptoSuite ecdsaCryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);

        String pemFile = AccountImplTest.class.getClassLoader().getResource("conf/test4.pem").getPath();
        ecdsaCryptoSuite.loadAccount("pem", pemFile, "");
        user = ecdsaCryptoSuite.getCryptoKeyPair();

        pemFile = AccountImplTest.class.getClassLoader().getResource("conf/test.pem").getPath();
        ecdsaCryptoSuite.loadAccount("pem", pemFile, "");
        admin = ecdsaCryptoSuite.getCryptoKeyPair();

        pemFile = AccountImplTest.class.getClassLoader().getResource("conf/test2.pem").getPath();
        ecdsaCryptoSuite.loadAccount("pem", pemFile, "");
        operator = ecdsaCryptoSuite.getCryptoKeyPair();
    }

    @Test
    public void testAddAdmin() throws ContractException {
        //账户外部地址
        String externalAccount = "0x9c4269b93bab5a83565279f897df031c88a86f79";
        String role = "admin";
        log.info("admin" + admin.getAddress());
        // 组织管理员公私钥对
//        BigInteger nonce = authCenterService.getNonceFromAccount(org1Admin.getCryptoKeyPair().getAddress()).getResult();
        BigInteger nonce = new BigInteger("1");
        byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.convertStringToAddressByte(externalAccount), role.getBytes(),
                OpenLedgerUtils.getBytes32(nonce.toByteArray()));
        byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);

        ECDSASignatureResult sign = OpenLedgerUtils.sign(admin, message);
        ResponseData<Boolean> ret = orgService.addAdmin(operator.getAddress(), message, sign);
        log.info(ret.getErrMsg());
        log.info("addAdmin result:{}", ret.getResult());
        testAddMember();
        testCreateAsset();
    }

    @Test
    public void testRemoveAdmin() {
        //账户外部地址
        String externalAccount = "0x9c4269b93bab5a83565279f897df031c88a86f79";
        // 组织管理员公私钥对
//        BigInteger nonce = authCenterService.getNonceFromAccount(org1Admin.getCryptoKeyPair().getAddress()).getResult();
        BigInteger nonce=new BigInteger("1");

        byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.convertStringToAddressByte(externalAccount),
                OpenLedgerUtils.getBytes32(nonce.toByteArray()));
        byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);

        ECDSASignatureResult sign = OpenLedgerUtils.sign(admin, message);
        ResponseData<Boolean> ret = orgService.removeAdmin(operator.getAddress(), message, sign);
        log.info(ret.getErrMsg());

        log.info("addAdmin result:{}", ret.getResult());
    }


    @Test
    public void testAddMember() {
        //账户外部地址
        String externalAccount = "0x9c4269b93bab5a83565279f897df031c88a86f79";
        String role = "admin";
        // 组织管理员公私钥对
//        BigInteger nonce = authCenterService.getNonceFromAccount(org1Admin.getCryptoKeyPair().getAddress()).getResult();
        BigInteger nonce=new BigInteger("1");

        byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.convertStringToAddressByte(externalAccount), role.getBytes(),
                OpenLedgerUtils.getBytes32(nonce.toByteArray()));
        byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);

        ECDSASignatureResult sign = OpenLedgerUtils.sign(operator, message);
        ResponseData<Boolean> ret = orgService.addMember(user.getAddress(),message, sign);
        log.info(ret.getErrMsg());

        log.info("addMember result:{}", ret.getResult());
    }

    @Test
    public void testRemoveMember() {
        //账户外部地址
        String externalAccount = "0x9c4269b93bab5a83565279f897df031c88a86f79";
        // 组织管理员公私钥对
//        BigInteger nonce = authCenterService.getNonceFromAccount(org1Admin.getCryptoKeyPair().getAddress()).getResult();
        BigInteger nonce=new BigInteger("1");

        byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.convertStringToAddressByte(externalAccount),
                OpenLedgerUtils.getBytes32(nonce.toByteArray()));
        byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);

        ECDSASignatureResult sign = OpenLedgerUtils.sign(operator, message);
        ResponseData<Boolean> ret = orgService.removeAdmin(externalAccount, message, sign);
        log.info("addAdmin result:{}", ret.getResult());
        log.info(ret.getErrMsg());

    }


    @Test
    public void testCreateAccount() throws ContractException {
        HashMap<String, String> kvMap = new HashMap<>();
//        kvMap.put("name", "tom"); //need add name to KeyType
        String externalAccount = "0x9c4269b93bab5a83565379f897df031c88a16f76";

        List<byte[]> keyList = new ArrayList<>();
        List<byte[]> valueList = new ArrayList<>();
        List<byte[]> kvList = new ArrayList<>();
        for (Map.Entry<String, String> entry : kvMap.entrySet()) {
            byte[] k = entry.getKey().getBytes(StandardCharsets.UTF_8);
            byte[] v = entry.getValue().getBytes(StandardCharsets.UTF_8);
            keyList.add(k);
            valueList.add(v);
            kvList.add(k);
            kvList.add(v);
        }

//        BigInteger nonce = authCenterService.getNonceFromAccount(admin.getAddress()).getResult();
//        log.info("createAccount get nonce:{}", nonce.intValue());
        BigInteger nonce = new BigInteger("1");

        byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.convertStringToAddressByte(user.getAddress()),
                OpenLedgerUtils.concatByte(kvList),
                OpenLedgerUtils.getBytes32(nonce.toByteArray()));
        byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);

        ECDSASignatureResult sign = OpenLedgerUtils.sign(user, message);

        ResponseData<String> ret = this.orgService.createAccount(externalAccount, keyList, valueList, message, sign);
        log.info("createAccount ret:{}, {}", ret.getResult(), ret);
        assertTrue(!ret.getResult().isEmpty());
    }

    @Test
    public void testFreeze() {
//        BigInteger nonce = authCenterService.getNonceFromAccount(org1Admin.getCryptoKeyPair().getAddress()).getResult();
//        log.info("freeze, get nonce:{}", nonce.intValue());
        BigInteger nonce = new BigInteger("1");
        String account="0xd06f23387b3938f718abe4473a2a6673b52d0b0c";
        byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.convertStringToAddressByte(ORG_1_USER_1_ADDR),
                OpenLedgerUtils.getBytes32(nonce.toByteArray()));
        byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);

        ECDSASignatureResult sign = OpenLedgerUtils.sign(user, message);

        ResponseData<Boolean> ret = this.orgService.freeze(account, message, sign);
        log.info("freeze ret:{}, {}", ret.getResult(), ret);
        assertTrue(ret.getResult());
    }

    @Test
    public void testUnfreeze() {
//        BigInteger nonce = authCenterService.getNonceFromAccount(org1Admin.getCryptoKeyPair().getAddress()).getResult();
//        log.info("unfreeze, get nonce:{}", nonce.intValue());
        BigInteger nonce = new BigInteger("1");
        String account="0xd06f23387b3938f718abe4473a2a6673b52d0b0c";

        byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.convertStringToAddressByte(ORG_1_USER_1_ADDR),
                OpenLedgerUtils.getBytes32(nonce.toByteArray()));
        byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);

        ECDSASignatureResult sign = OpenLedgerUtils.sign(user, message);

        ResponseData<Boolean> ret = this.orgService.unfreeze(account, message, sign);
        log.info("unfreeze ret:{}, {}", ret.getResult(), ret);
        assertTrue(ret.getResult());
    }

    @Test
    public void testCancel() {
//        BigInteger nonce = authCenterService.getNonceFromAccount(org1Admin.getCryptoKeyPair().getAddress()).getResult();
//        log.info("cancel, get nonce:{}", nonce.intValue());
        BigInteger nonce = new BigInteger("1");

        byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.convertStringToAddressByte(ORG_1_USER_2_ADDR),
                OpenLedgerUtils.getBytes32(nonce.toByteArray()));
        byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);

        ECDSASignatureResult sign = OpenLedgerUtils.sign(operator, message);

        ResponseData<Boolean> ret = this.orgService.cancel(ORG_1_USER_1_ADDR, message, sign);
        log.info("cancel ret:{}, {}", ret.getResult(), ret);
        assertTrue(ret.getResult());
    }


//    @Test
//    public void testChangeExternalAccount() {
//        BigInteger nonce = authCenterService.getNonceFromAccount(org1Admin.getCryptoKeyPair().getAddress()).getResult();
//        log.info("changeExternalAccount, get nonce:{}", nonce.intValue());
//        assertTrue(nonce.intValue() > 0);
//
//        byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.convertStringToAddressByte(ORG_1_USER_1_ADDR),
//                OpenLedgerUtils.convertStringToAddressByte(ORG_1_USER_2_ADDR),
//                OpenLedgerUtils.getBytes32(nonce.toByteArray()));
//        byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);
//
//        ECDSASignatureResult sign = OpenLedgerUtils.sign(org1Admin.getCryptoKeyPair(), message);
//
//        ResponseData<Boolean> ret = this.orgService.changeExternalAccount(ORG_1_USER_1_ADDR, ORG_1_USER_2_ADDR, message, sign);
//        log.info("changeExternalAccount ret:{}, {}", ret.getResult(), ret);
//        assertTrue(ret.getResult());
//    }

    @Test
    public void testCreateAsset() {
        String assetName = "aaa120q9a";
        Boolean isFungible = false;
//        BigInteger nonce = authCenterService.getNonceFromAccount(admin.getAddress()).getResult();
        BigInteger nonce = new BigInteger("1");
        log.info("testCreateAsset, get nonce:{}", nonce.intValue());

        byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.convertStringToAddressByte(admin.getAddress()),
                OpenLedgerUtils.getBytes32(nonce.toByteArray()));
        byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);

        ECDSASignatureResult sign = OpenLedgerUtils.sign(admin, message);

        ResponseData<String> ret = this.orgService.createAsset(assetName, isFungible, message, sign);
        log.info("testCreateAsset ret:{}, {}", ret.getResult(), ret);
    }



    @Test
    public void testGetllAsset() throws ContractException {
        List<String> assets = orgService.getContractIns().getAllAssets(false);
        log.info("fungible assets: {}",assets);
        List<String> nonFunAssets = orgService.getContractIns().getAllAssets(true);
        log.info("nonfungible assets: {}",nonFunAssets);
    }

    @Test
    public void testCreateCurrency(){
        String name ="mymoney";
        String symbol="abc";
        BigInteger decimal = BigInteger.valueOf(2);
        ResponseData<String> responseData = orgService.createCurrency(name,symbol,decimal);
        log.info(responseData.getErrMsg());
        assertTrue(ErrorCode.SUCCESS.getCode()==responseData.getErrorCode());
        log.info(responseData.getResult());

    }

    @Test
    public void testUpgradeAsset() {
        String assetName = "test20210531112";
        Boolean isFungible = false;
        BigInteger nonce = authCenterService.getNonceFromAccount(admin.getAddress()).getResult();
        log.info("testUpgradeAsset, get nonce:{}", nonce.intValue());

        byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.convertStringToAddressByte(admin.getAddress()),
                OpenLedgerUtils.getBytes32(nonce.toByteArray()));
        byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);

        ECDSASignatureResult sign = OpenLedgerUtils.sign(admin, message);

        ResponseData<String> ret = this.orgService.upgradeAsset( assetName, isFungible, message, sign);
        log.info("testCreateAsset ret:{}, {}", ret.getResult(), ret);
    }
}