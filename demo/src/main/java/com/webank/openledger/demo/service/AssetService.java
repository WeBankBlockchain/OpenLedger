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
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.webank.openledger.core.asset.fungible.FungibleAssetService;
import com.webank.openledger.core.asset.fungible.StandardAssetService;
import com.webank.openledger.core.asset.fungible.entity.Condition;
import com.webank.openledger.core.asset.fungible.entity.RecordEntity;
import com.webank.openledger.core.asset.fungible.entity.TransferResult;
import com.webank.openledger.core.asset.nonfungible.NonFungibleAssetService;
import com.webank.openledger.core.asset.nonfungible.entity.IssueNoteResult;
import com.webank.openledger.core.asset.nonfungible.entity.IssueOption;
import com.webank.openledger.core.asset.nonfungible.entity.IssueOptionBuilder;
import com.webank.openledger.core.asset.nonfungible.entity.NonFungibleAssetRecord;
import com.webank.openledger.core.asset.nonfungible.entity.NonFungibleCondition;
import com.webank.openledger.core.asset.nonfungible.entity.Note;
import com.webank.openledger.core.asset.nonfungible.entity.TransferNoteResult;
import com.webank.openledger.core.constant.ErrorCode;
import com.webank.openledger.core.response.ResponseData;

import com.webank.openledger.demo.entity.AssetEntity;
import com.webank.openledger.demo.holder.AssetHolder;
import com.webank.openledger.demo.holder.LoginHolder;
import com.webank.openledger.utils.OpenLedgerUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.model.CryptoType;
import picocli.CommandLine;

/**
 * Expense creation homogeneous and non-homogeneous assets at the same time provide deposit, withdrawal, audit and other interfaces
 * @author pepperli
 */
@Slf4j
public class AssetService extends BaseService {
    private NonFungibleAssetService nonFungibleAssetService;
    private FungibleAssetService fungibleAssetService;
    private static AssetService instance = null;
    private static final CryptoSuite ecdsaCryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = " cmd cli help msg")
    public boolean help = false;

    static {
        instance = new AssetService();
    }

    private AssetService() {
        try {
            this.loadService();
        } catch (Exception e) {
            log.error("load service failed: ", e);
        }
    }

    public static AssetService getInstance() {
        return instance;
    }


    public String createAsset(Boolean isFungible, String assetName, String signVal) throws Exception {
        CryptoKeyPair account = LoginService.getInstance().get();
        Map<String, Object> signAndMsg = ClientService.getInstance().creatAssetSignAndMsg(isFungible, assetName);
        ResponseData<String> ret = organizationService.createAsset(account.getAddress(), assetName, isFungible, (byte[]) signAndMsg.get("message"), StringUtils.isBlank(signVal) ?
                new ECDSASignatureResult((String) signAndMsg.get("sign")) : new ECDSASignatureResult(signVal));
        if (ErrorCode.SUCCESS.getCode() != ret.getErrorCode()) {
            throw new Exception("create Asset failed :" + ret.getErrMsg());
        }
        loadAsset(ret.getResult(), isFungible);
        return ret.getResult();
    }

    public String loadAsset(String assetAddr, Boolean isFungible) throws Exception {
        AssetHolder.setAsset(new AssetEntity(assetAddr, isFungible));
        setAssetService();
        log.info("load asset:{}", assetAddr);

        return assetAddr;

    }

    private Boolean setAssetService() throws Exception {
        AssetEntity assetEntity = AssetHolder.getAsset();
        if (assetEntity.getIsFungible()) {
            fungibleAssetService = new FungibleAssetService(blockchain, assetEntity.getAddress());
        } else {
            nonFungibleAssetService = new NonFungibleAssetService(blockchain, assetEntity.getAddress());
        }
        return assetEntity.getIsFungible();
    }

    private Boolean isloadService() {
        if (nonFungibleAssetService == null && fungibleAssetService == null) {
            return false;
        }
        return true;
    }


    public String openAccount(String accountAddr, String signVal) throws Exception {
        if (!isloadService()) {
            throw new Exception("please run loadAsset");
        }
        Map<String, Object> signAndMsg = ClientService.getInstance().openAccountSignAndMsg(accountAddr);

        ResponseData<Boolean> responseData = null;
        if (AssetHolder.isFungible()) {
            responseData = fungibleAssetService.openAccount(accountAddr, (byte[]) signAndMsg.get("message"), StringUtils.isBlank(signVal) ?
                    new ECDSASignatureResult((String) signAndMsg.get("sign")) : new ECDSASignatureResult(signVal));
        } else {
            responseData = nonFungibleAssetService.openAccount(accountAddr, (byte[]) signAndMsg.get("message"), StringUtils.isBlank(signVal) ?
                    new ECDSASignatureResult((String) signAndMsg.get("sign")) : new ECDSASignatureResult(signVal));
        }
        if (responseData == null || ErrorCode.SUCCESS.getCode() != responseData.getErrorCode()) {
            throw new Exception("create Asset failed :" + (responseData == null ? "" : responseData.getErrMsg()));
        }
        return accountAddr;
    }


    public TransferResult deposit(String accountAddr, BigInteger amount, String signVal) throws Exception {
        if (!isloadService()) {
            throw new Exception("please run loadAsset");
        }
        if (!AssetHolder.isFungible()) {
            throw new Exception("nonFungible is not support,please load fungbleAsset");
        }
        Map<String, Object> signAndMsg = ClientService.getInstance().depositSignAndMsg(accountAddr, amount);

        CryptoKeyPair account = LoginService.getInstance().get();
        String detail = "test";
        ResponseData<TransferResult> responseData = null;
        responseData = fungibleAssetService.deposit(account.getAddress(), accountAddr, amount, 1, detail, (byte[]) signAndMsg.get("message"), StringUtils.isBlank(signVal) ?
                new ECDSASignatureResult((String) signAndMsg.get("sign")) : new ECDSASignatureResult(signVal));
        if (responseData == null || ErrorCode.SUCCESS.getCode() != responseData.getErrorCode()) {
            throw new Exception("deposit asset failed :" + (responseData == null ? "" : responseData.getErrMsg()));
        }
        return responseData.getResult();
    }

    public TransferResult withdrawal(String accountAddr, BigInteger amount, String signVal) throws Exception {
        if (!isloadService()) {
            throw new Exception("please run loadAsset");
        }
        if (!AssetHolder.isFungible()) {
            throw new Exception("nonFungible is not support,please load fungbleAsset");
        }
        CryptoKeyPair account = LoginService.getInstance().get();
        Map<String, Object> signAndMsg = ClientService.getInstance().withdrawalSignAndMsg(accountAddr, amount);

        String detail = "test";
        ResponseData<TransferResult> responseData = null;
        responseData = fungibleAssetService.withdrawal(account.getAddress(), accountAddr, amount, 1, detail, (byte[]) signAndMsg.get("message"), StringUtils.isBlank(signVal) ?
                new ECDSASignatureResult((String) signAndMsg.get("sign")) : new ECDSASignatureResult(signVal));
        if (responseData == null || ErrorCode.SUCCESS.getCode() != responseData.getErrorCode()) {
            throw new Exception("withdrawal asset failed :" + (responseData == null ? "" : responseData.getErrMsg()));
        }
        return responseData.getResult();
    }

    public TransferResult transfer(String from, String to, BigInteger amonut, String signVal) throws Exception {
        if (!isloadService()) {
            throw new Exception("please run loadAsset");
        }
        if (!AssetHolder.isFungible()) {
            throw new Exception("nonFungible is not support,please load fungbleAsset");
        }
        CryptoKeyPair account = LoginService.getInstance().get();

        // 交易参数
        String operatorAddress = account.getAddress().equals(from) ? account.getAddress() : from;
        String detail = "test";
        BigInteger nonce = authCenterService.getNonceFromAccount(account.getAddress().equals(from) ? account.getAddress() : from).getResult();
        ECDSASignatureResult sign = new ECDSASignatureResult(signVal);
        ResponseData<TransferResult> responseData = null;

        byte[] message = null;
        List<String> addressList = StandardAssetService.genAddress(from, to, operatorAddress, fungibleAssetService.getContractAddress(), null);
        message = StandardAssetService.computeTxMsg(addressList, amonut, StandardAssetService.genType(1), StandardAssetService.genDetail(detail, null), nonce);
        responseData = fungibleAssetService.transfer(operatorAddress, from, to, amonut, 1, detail, message, sign);
        if (responseData == null || ErrorCode.SUCCESS.getCode() != responseData.getErrorCode()) {
            throw new Exception("transfer asset failed :" + (responseData == null ? "" : responseData.getErrMsg()));
        }
        return responseData.getResult();
    }

    public List<TransferNoteResult> transfer(String from, String to, List<BigInteger> noteNos, String signVal) throws Exception {
        if (!isloadService()) {
            throw new Exception("please run loadAsset");
        }
        if (AssetHolder.isFungible()) {
            throw new Exception("fungible is not support,please load nonfungbleAsset");
        }
        CryptoKeyPair account = LoginService.getInstance().get();
        String operatorAddress = account.getAddress().equals(from) ? account.getAddress() : from;

        // 交易参数
        String detail = "test";
        BigInteger nonce = authCenterService.getNonceFromAccount(account.getAddress().equals(from) ? account.getAddress() : from).getResult();
        ECDSASignatureResult sign = new ECDSASignatureResult(signVal);
        ResponseData<List<TransferNoteResult>> responseData = null;

        byte[] message = NonFungibleAssetService.computeTransferMsg(nonFungibleAssetService.getContractAddress(), operatorAddress, from, to, noteNos, detail, nonce);
        responseData = nonFungibleAssetService.transfer(operatorAddress, from, to, noteNos, detail, message, sign);
        if (responseData == null || ErrorCode.SUCCESS.getCode() != responseData.getErrorCode()) {
            throw new Exception("transfer asset failed :" + (responseData == null ? "" : responseData.getErrMsg()));
        }
        return responseData.getResult();
    }

    public BigInteger getBalance(String accountAddr, String signVal) throws Exception {
        if (!AssetHolder.isFungible()) {
            throw new Exception("nonFungible is not support,please load fungbleAsset");
        }
        if (!isloadService()) {
            throw new Exception("please run loadAsset");
        }
        CryptoKeyPair account = LoginService.getInstance().get();
        BigInteger nonce = authCenterService.getNonceFromAccount(account.getAddress().equals(accountAddr) ? account.getAddress() : accountAddr).getResult();
        byte[] message = OpenLedgerUtils.computeKeccak256HashFromBigInteger(nonce);
        ECDSASignatureResult sign = StringUtils.isBlank(signVal) ? new ECDSASignatureResult(ClientService.getInstance().genGetSign()) : new ECDSASignatureResult(signVal);

        return fungibleAssetService.getBalance(accountAddr, message, sign);
    }


    public String issue(String issuer, BigInteger num, BigInteger notePrefix, BigInteger noteNoSize, Date expireDate, String signVal) throws Exception {
        if (!isloadService()) {
            throw new Exception("please run loadAsset");
        }
        if (AssetHolder.isFungible()) {
            throw new Exception("fungible is not support,please load nonfungbleAsset");
        }
        CryptoKeyPair account = LoginHolder.getAccount();
        Map<String, Object> signAndMsg = ClientService.getInstance().genIssueSignAndMsg(issuer, num, notePrefix, noteNoSize, null, expireDate);

        IssueOption issueOption = IssueOptionBuilder.builder()
                .withAmount(num)
                .withNoteNoPrefix(notePrefix)
                .withNoteNoSize(noteNoSize)
                .withIssuer(issuer)
                .withOperator(account.getAddress())
                .withDesc("desc")
                .withEffectiveDate(null)
                .withExpirationDate(expireDate).build();
        ResponseData<List<IssueNoteResult>> response = nonFungibleAssetService.issue(issueOption, (byte[]) signAndMsg.get("message"), StringUtils.isBlank(signVal) ?
                new ECDSASignatureResult((String) signAndMsg.get("sign")) : new ECDSASignatureResult(signVal));

        if (response == null || ErrorCode.SUCCESS.getCode() != response.getErrorCode()) {
            throw new Exception("issue asset failed :" + (response == null ? "" : response.getErrMsg()));
        }
        return response.getResult().toString();
    }

    public Boolean isAccountHoldNote(String accountAddr, BigInteger noteNo, String signVal) throws Exception {
        if (!isloadService()) {
            throw new Exception("please run loadAsset");
        }
        if (AssetHolder.isFungible()) {
            throw new Exception("fungible is not support,please load nonfungbleAsset");
        }

        CryptoKeyPair account = LoginService.getInstance().get();
        BigInteger nonce = authCenterService.getNonceFromAccount(account.getAddress()).getResult();
        byte[] message = OpenLedgerUtils.computeKeccak256HashFromBigInteger(nonce);
        ECDSASignatureResult sign = StringUtils.isBlank(signVal) ? new ECDSASignatureResult(ClientService.getInstance().genGetSign()) : new ECDSASignatureResult(signVal);
        return nonFungibleAssetService.getAsset().accountHoldNote(accountAddr, noteNo, OpenLedgerUtils.convertSignToByte(message, sign));
    }

    public List<BigInteger> getAccountNotes(String accountAddr, BigInteger start, BigInteger end, String signVal) throws Exception {
        if (!isloadService()) {
            throw new Exception("please run loadAsset");
        }
        if (AssetHolder.isFungible()) {
            throw new Exception("fungible is not support,please load nonfungbleAsset");
        }

        CryptoKeyPair account = LoginService.getInstance().get();
        BigInteger nonce = authCenterService.getNonceFromAccount(account.getAddress()).getResult();
        byte[] message = OpenLedgerUtils.computeKeccak256HashFromBigInteger(nonce);
        ECDSASignatureResult sign = StringUtils.isBlank(signVal) ? new ECDSASignatureResult(ClientService.getInstance().genGetSign()) : new ECDSASignatureResult(signVal);
//        System.out.println(this.getNoteDetail());
        return nonFungibleAssetService.getAsset().getAccountNotes(accountAddr, start, end, OpenLedgerUtils.convertSignToByte(message, sign));
    }

    public List<RecordEntity> queryFungibleBook(Condition condition, String signVal) throws Exception {
        if (!isloadService()) {
            throw new Exception("please run loadAsset");
        }
        if (!AssetHolder.isFungible()) {
            throw new Exception("nonFungible is not support,please load fungbleAsset");
        }
        CryptoKeyPair account = LoginService.getInstance().get();
        BigInteger nonce = authCenterService.getNonceFromAccount(account.getAddress()).getResult();
        byte[] message = OpenLedgerUtils.computeKeccak256HashFromBigInteger(nonce);
        ECDSASignatureResult sign = StringUtils.isBlank(signVal) ? new ECDSASignatureResult(ClientService.getInstance().genGetSign()) : new ECDSASignatureResult(signVal);
        List<RecordEntity> recordEntities = fungibleAssetService.query(condition, message, sign);
        return recordEntities;
    }

    public List<NonFungibleAssetRecord> queryNonFungibleBook(NonFungibleCondition condition, String signVal) throws Exception {
        if (!isloadService()) {
            throw new Exception("please run loadAsset");
        }
        if (AssetHolder.isFungible()) {
            throw new Exception("fungible is not support,please load nonfungbleAsset");
        }

        CryptoKeyPair account = LoginService.getInstance().get();
        BigInteger nonce = authCenterService.getNonceFromAccount(account.getAddress()).getResult();
        byte[] message = OpenLedgerUtils.computeKeccak256HashFromBigInteger(nonce);
        ECDSASignatureResult sign = StringUtils.isBlank(signVal) ? new ECDSASignatureResult(ClientService.getInstance().genGetSign()) : new ECDSASignatureResult(signVal);
        List<NonFungibleAssetRecord> recordEntities = nonFungibleAssetService.query(condition, message, sign);
        return recordEntities;
    }

    public String activeBatch(BigInteger batchNo, String signVal) throws Exception {
        if (!isloadService()) {
            throw new Exception("please run loadAsset");
        }
        if (AssetHolder.isFungible()) {
            throw new Exception("fungible is not support,please load nonfungbleAsset");
        }
        CryptoKeyPair account = LoginService.getInstance().get();
        BigInteger nonce = authCenterService.getNonceFromAccount(account.getAddress()).getResult();

        byte[] message = OpenLedgerUtils.computeKeccak256Hash(OpenLedgerUtils.concatByte(OpenLedgerUtils.getBytes32(batchNo.toByteArray()),
                OpenLedgerUtils.getBytes32(nonce.toByteArray())
        ));
        ECDSASignatureResult sign = StringUtils.isBlank(signVal) ? OpenLedgerUtils.sign(account, message) : new ECDSASignatureResult(signVal);


        ResponseData<Boolean> responseData = nonFungibleAssetService.effectBatch(batchNo, message, sign);
        if (responseData == null || ErrorCode.SUCCESS.getCode() != responseData.getErrorCode()) {
            throw new Exception("activeBatch  failed :" + (responseData == null ? "" : responseData.getErrMsg()));
        }

        return responseData.getResult().toString();
    }

    public String getNoteDetail() throws Exception {
        CryptoKeyPair account = LoginService.getInstance().get();

        BigInteger nonce = authCenterService.getNonceFromAccount(account.getAddress()).getResult();
        byte[] message = OpenLedgerUtils.computeKeccak256Hash(OpenLedgerUtils.getBytes32(nonce.toByteArray()));
        ECDSASignatureResult sign = OpenLedgerUtils.sign(account, message);

        Note response = nonFungibleAssetService.getNoteDetail(new BigInteger("20210003"), account.getAddress(), message, sign);
        log.info(response.toString());
        return response.toString();
    }

}
