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

package com.webank.openledger.demo.service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.openledger.core.asset.fungible.StandardAssetService;
import com.webank.openledger.core.asset.nonfungible.NonFungibleAssetService;

import com.webank.openledger.core.asset.nonfungible.entity.IssueOption;
import com.webank.openledger.core.asset.nonfungible.entity.IssueOptionBuilder;
import com.webank.openledger.demo.entity.AssetEntity;
import com.webank.openledger.demo.holder.AssetHolder;
import com.webank.openledger.demo.holder.LoginHolder;
import com.webank.openledger.utils.OpenLedgerUtils;

import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;

/**
 * Client program: Generates parameter hashes and signatures
 * @author pepperli
 */
@Slf4j
public class ClientService extends BaseService {
    private static ClientService instance = null;

    static {
        instance = new ClientService();
    }

    private ClientService() {
        try {
            this.loadService();
        } catch (Exception e) {
            log.error("load service failed: ", e);
        }
    }

    public static ClientService getInstance() {
        return instance;
    }

    public Map<String, Object> createAccountSignAndMsg(String userAddr) throws Exception {
        Map<String, Object> result = new HashMap<>();
        CryptoKeyPair account = LoginService.getInstance().get();

        HashMap<String, String> kvMap = new HashMap<>();
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
        BigInteger nonce = authCenterService.getNonceFromAccount(account.getAddress()).getResult();
        byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.convertStringToAddressByte(userAddr),
                OpenLedgerUtils.concatByte(kvList),
                OpenLedgerUtils.getBytes32(nonce.toByteArray()));
        byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);
        result.put("message", message);
        result.put("sign", OpenLedgerUtils.sign(account, message).convertToString());
        return result;
    }

    public Map<String, Object> freezeSignAndMsg(String accountAddr) throws Exception {
        Map<String, Object> result = new HashMap<>();

        CryptoKeyPair account = LoginService.getInstance().get();
        BigInteger nonce = authCenterService.getNonceFromAccount(account.getAddress()).getResult();
        byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.convertStringToAddressByte(accountAddr),
                OpenLedgerUtils.getBytes32(nonce.toByteArray()));
        byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);
        result.put("message", message);
        result.put("sign", OpenLedgerUtils.sign(account, message).convertToString());
        return result;
    }

    public Map<String, Object> unfreezeSignAndMsg(String accountAddr) throws Exception {
        Map<String, Object> result = new HashMap<>();

        CryptoKeyPair account = LoginService.getInstance().get();
        BigInteger nonce = authCenterService.getNonceFromAccount(account.getAddress()).getResult();
        byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.convertStringToAddressByte(accountAddr),
                OpenLedgerUtils.getBytes32(nonce.toByteArray()));
        byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);
        result.put("message", message);
        result.put("sign", OpenLedgerUtils.sign(account, message).convertToString());
        return result;
    }

    public Map<String, Object> cancelSignAndMsg(String accountAddr) throws Exception {
        Map<String, Object> result = new HashMap<>();

        CryptoKeyPair account = LoginService.getInstance().get();
        BigInteger nonce = authCenterService.getNonceFromAccount(account.getAddress()).getResult();
        byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.convertStringToAddressByte(accountAddr),
                OpenLedgerUtils.getBytes32(nonce.toByteArray()));
        byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);

        result.put("message", message);
        result.put("sign", OpenLedgerUtils.sign(account, message).convertToString());
        return result;
    }

    public Map<String, Object> changeExternalAccountSignAndMsg(String accountAddr, String newAccountAddr) throws Exception {
        Map<String, Object> result = new HashMap<>();
        CryptoKeyPair account = LoginService.getInstance().get();
        BigInteger nonce = authCenterService.getNonceFromAccount(account.getAddress()).getResult();
        byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.convertStringToAddressByte(accountAddr),
                OpenLedgerUtils.convertStringToAddressByte(newAccountAddr),
                OpenLedgerUtils.getBytes32(nonce.toByteArray()));
        byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);
        result.put("message", message);
        result.put("sign", OpenLedgerUtils.sign(account, message).convertToString());
        return result;
    }

    public Map<String, Object> creatAssetSignAndMsg(Boolean isFungible, String assetName) throws Exception {
        Map<String, Object> result = new HashMap<>();
        CryptoKeyPair account = LoginService.getInstance().get();
        BigInteger nonce = authCenterService.getNonceFromAccount(account.getAddress()).getResult();
        byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.convertStringToAddressByte(account.getAddress()),
                OpenLedgerUtils.getBytes32(nonce.toByteArray()));
        byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);
        result.put("message", message);
        result.put("sign", OpenLedgerUtils.sign(account, message).convertToString());
        return result;
    }

    public Map<String, Object> openAccountSignAndMsg(String accountAddr) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        CryptoKeyPair account = LoginService.getInstance().get();
        BigInteger nonce = authCenterService.getNonceFromAccount(account.getAddress()).getResult();
        byte[] message = StandardAssetService.computeOpenAccountMsg(accountAddr, nonce);
        resultMap.put("message", message);
        resultMap.put("sign", OpenLedgerUtils.sign(account, message).convertToString());
        return resultMap;
    }


    public Map<String, Object> depositSignAndMsg(String accountAddr, BigInteger amount) throws Exception {
        CryptoKeyPair account = LoginService.getInstance().get();
        Map<String, Object> resultMap = new HashMap<>();

        BigInteger nonce = authCenterService.getNonceFromAccount(account.getAddress()).getResult();
        // 交易参数
        String operatorAddress = account.getAddress();
        String detail = "test";

        List<String> addressList = StandardAssetService.genAddress(null, accountAddr, operatorAddress, AssetHolder.getAsset().getAddress(), null);
        byte[] message = StandardAssetService.computeTxMsg(addressList, amount, StandardAssetService.genType(1), StandardAssetService.genDetail(detail, null), nonce);
        resultMap.put("message", message);
        resultMap.put("sign", OpenLedgerUtils.sign(account, message).convertToString());
        return resultMap;
    }

    public Map<String, Object> withdrawalSignAndMsg(String accountAddr, BigInteger amount) throws Exception {
        CryptoKeyPair account = LoginService.getInstance().get();
        Map<String, Object> resultMap = new HashMap<>();

        BigInteger nonce = authCenterService.getNonceFromAccount(account.getAddress()).getResult();
        // 交易参数
        String operatorAddress = account.getAddress();
        String detail = "test";

        List<String> addressList = StandardAssetService.genAddress(accountAddr, null, operatorAddress, AssetHolder.getAsset().getAddress(), null);
        byte[] message = StandardAssetService.computeTxMsg(addressList, amount, StandardAssetService.genType(1), StandardAssetService.genDetail(detail, null), nonce);
        resultMap.put("message", message);
        resultMap.put("sign", OpenLedgerUtils.sign(account, message).convertToString());
        return resultMap;
    }

    public String genFungibleTransferSign(String from, String to, BigInteger amonut) throws Exception {
        CryptoKeyPair account = LoginHolder.getAccount();
        AssetEntity asset = AssetHolder.getAsset();
        String operatorAddress = account.getAddress();
        String detail = "test";
        List<String> addressList = StandardAssetService.genAddress(from, to, operatorAddress, asset.getAddress(), null);
        BigInteger nonce = authCenterService.getNonceFromAccount(account.getAddress()).getResult();
        byte[] message = StandardAssetService.computeTxMsg(addressList, amonut, StandardAssetService.genType(1), StandardAssetService.genDetail(detail, null), nonce);
        ECDSASignatureResult sign = OpenLedgerUtils.sign(account, message);
        return sign.convertToString();
    }


    public String genNonFungibleTransferSign(String from, String to, List<BigInteger> noteNos) throws Exception {
        CryptoKeyPair account = LoginHolder.getAccount();
        AssetEntity asset = AssetHolder.getAsset();
        String detail = "test";
        BigInteger nonce = authCenterService.getNonceFromAccount(account.getAddress()).getResult();
        byte[] message = NonFungibleAssetService.computeTransferMsg(asset.getAddress(), account.getAddress(), from, to, noteNos, detail, nonce);
        ECDSASignatureResult sign = OpenLedgerUtils.sign(account, message);
        return sign.convertToString();
    }

    public String genGetSign() throws Exception {
        CryptoKeyPair account = LoginHolder.getAccount();
        BigInteger nonce = authCenterService.getNonceFromAccount(account.getAddress()).getResult();
        byte[] message = OpenLedgerUtils.computeKeccak256HashFromBigInteger(nonce);
        ECDSASignatureResult sign = OpenLedgerUtils.sign(account, message);
        return sign.convertToString();
    }

    public Map<String, Object> genIssueSignAndMsg(String issuer,BigInteger num,BigInteger notePrefix,BigInteger noteNoSize, Date effectiveDate,Date expireDate) throws Exception {
        CryptoKeyPair account = LoginHolder.getAccount();
        Map<String, Object> resultMap = new HashMap<>();

        //交易序列号 从authcenter获取
        BigInteger nonce = authCenterService.getNonceFromAccount(account.getAddress()).getResult();
        IssueOption issueOption = IssueOptionBuilder.builder()
                .withAmount(num)
                .withNoteNoPrefix(notePrefix)
                .withNoteNoSize(noteNoSize)
                .withIssuer(issuer)
                .withOperator(account.getAddress())
                .withDesc("desc")
                .withEffectiveDate(effectiveDate)
                .withExpirationDate(expireDate).build();
        byte[] message = NonFungibleAssetService.computeIssueMsg(AssetHolder.getAsset().getAddress(),issueOption, nonce);
        resultMap.put("message", message);
        resultMap.put("sign", OpenLedgerUtils.sign(account, message).convertToString());
        return resultMap;
    }

}

