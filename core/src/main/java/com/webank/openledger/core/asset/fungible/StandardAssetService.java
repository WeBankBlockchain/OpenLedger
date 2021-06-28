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
import java.util.ArrayList;
import java.util.List;

import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.asset.fungible.entity.Condition;
import com.webank.openledger.core.asset.fungible.entity.RecordBuilder;
import com.webank.openledger.core.asset.fungible.entity.RecordEntity;
import com.webank.openledger.core.asset.fungible.entity.TransferResult;
import com.webank.openledger.core.common.BaseAsset;
import com.webank.openledger.core.constant.ErrorCode;
import com.webank.openledger.core.exception.OpenLedgerBaseException;
import com.webank.openledger.core.response.DataToolUtils;
import com.webank.openledger.core.response.ResponseData;
import com.webank.openledger.utils.OpenLedgerUtils;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.abi.datatypes.Address;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.contract.Contract;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.model.TransactionReceipt;

/**
 * standard fungible asset service
 *
 * @author pepperli
 */
@Slf4j
@Getter
public class StandardAssetService<T extends Contract> {
    /**
     * blockchain property object
     */
    protected Blockchain blockchain;
    /**
     * asset contract object
     */
    protected BaseAsset asset;
    /**
     * contractAddress
     */
    protected String contractAddress;

    /**
     * Initialize the contract object
     *
     * @param blockchain property object
     * @param contractAddress asset contractAddress
     */
    public StandardAssetService(Blockchain blockchain, String contractAddress, Class contractClass) {
        this.blockchain = blockchain;
        this.contractAddress = contractAddress;
        asset = blockchain.getLedger(Blockchain.DEFAULT_LEDGERID).getContract(contractAddress, contractClass);
    }

    /***
     * generate transaction detail list
     * @param desc description
     * @param subject accounting Subject can be null
     * @return
     */
    public static List<String> genDetail(String desc, String subject) {
        List<String> details = new ArrayList<>();
        details.add(desc);
        if (StringUtils.isNotBlank(subject)) {
            details.add(subject);
        }
        return details;
    }

    /**
     * generate transaction type list
     *
     * @param operationType operation type custom
     * @return
     */
    public static List<BigInteger> genType(int operationType) {
        List<BigInteger> types = new ArrayList<>();
        types.add(BigInteger.valueOf(operationType));
        return types;
    }

    /**
     * generate transaction address list
     *
     * @param fromAddress transfer out of account
     * @param toAddress transfer account
     * @param operatorAddress operator account address
     * @param assetAddress asset contract address
     * @param relateAsset related asset contract address (can be null)
     * @return transaction address list
     */
    public static List<String> genAddress(String fromAddress, String toAddress, String operatorAddress, String assetAddress, String relateAsset) {
        List<String> addressList = new ArrayList<>();
        addressList.add(operatorAddress);
        addressList.add(assetAddress);
        if (StringUtils.isBlank(fromAddress)) {
            fromAddress = Address.DEFAULT.getValue();
        }
        addressList.add(fromAddress);
        if (StringUtils.isBlank(toAddress)) {
            toAddress = Address.DEFAULT.getValue();
        }
        addressList.add(toAddress);
        if (StringUtils.isBlank(relateAsset)) {
            relateAsset = Address.DEFAULT.getValue();
        }
        addressList.add(relateAsset);
        return addressList;
    }


    /**
     * compute transaction args hash
     *
     * @param transactionAddress transaction address list generate by call 'genAddress'
     * @param amount transaction amount
     * @param typeList transaction type list generate by call 'genType'
     * @param detailList transaction detail list generate by call 'genDetail'
     * @param nonce account nonce value
     * @return transaction args hash
     * @throws OpenLedgerBaseException
     * @throws UnsupportedEncodingException
     */
    public static byte[] computeTxMsg(List<String> transactionAddress, BigInteger amount, List<BigInteger> typeList, List<String> detailList, BigInteger nonce) throws OpenLedgerBaseException, UnsupportedEncodingException {
        byte[] result = new byte[0];
        for (String item : transactionAddress) {
            result = OpenLedgerUtils.concatByte(result, OpenLedgerUtils.convertStringToAddressByte(item));
        }
        result = OpenLedgerUtils.concatByte(result, OpenLedgerUtils.getBytes32(amount.toByteArray()));
        for (BigInteger item : typeList) {
            result = OpenLedgerUtils.concatByte(result, OpenLedgerUtils.getBytes32(item.toByteArray()));
        }
        for (String item : detailList) {
            result = OpenLedgerUtils.concatByte(result, item.getBytes("utf-8"));
        }

        result = OpenLedgerUtils.concatByte(result, OpenLedgerUtils.getBytes32(nonce.toByteArray()));

        return OpenLedgerUtils.computeKeccak256Hash(result);
    }

    /**
     * compute openAccount args hash
     *
     * @param account call account address
     * @param nonce account nonce value
     * @return openAccount function args hash
     * @throws OpenLedgerBaseException
     */
    public static byte[] computeOpenAccountMsg(String account, BigInteger nonce) throws OpenLedgerBaseException {
        byte[] result = new byte[0];
        result = OpenLedgerUtils.concatByte(result, OpenLedgerUtils.convertStringToAddressByte(account));
        result = OpenLedgerUtils.concatByte(result, OpenLedgerUtils.getBytes32(nonce.toByteArray()));

        return OpenLedgerUtils.computeKeccak256Hash(result);
    }

    /**
     * contract object
     *
     * @return
     */
    public T getAsset() {
        return asset == null ? null : (T) asset;
    }

    /**
     * deposit asset to account
     *
     * @param account transaction account
     * @param amount transaction amount
     * @param operationType operation type custom defined
     * @param desc description
     * @return transaction result
     * @throws OpenLedgerBaseException
     */
    public ResponseData<TransferResult> deposit(@NonNull String operatorAddress, @NonNull String account, @NonNull BigInteger amount, int operationType, String desc, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<String> addressList = genAddress(null, account, operatorAddress, contractAddress, null);
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
            TransactionReceipt transactionReceipt = asset.deposit(
                    addressList,
                    amount,
                    genType(operationType),
                    genDetail(desc, null),
                    resultSign);
            TransferResult tr = null;
            Tuple2<Boolean, List<BigInteger>> response = transactionReceipt.isStatusOK() ? asset.getDepositOutput(transactionReceipt) : null;
            if (response != null && response.getValue1()) {
                tr = new TransferResult(response.getValue1(), response.getValue2().get(0), response.getValue2().get(1));
            }

            return DataToolUtils.handleTransaction(transactionReceipt, tr);
        } catch (Exception e) {
            log.error("deposit failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.ASSET_DESIPOSIT_ERROR);
        }
    }

    /**
     * withdrawal asset from account
     *
     * @param account transaction account
     * @param amount transaction amount
     * @param operationType operation type custom defined
     * @param desc description
     * @return transaction result
     * @throws OpenLedgerBaseException
     */
    public ResponseData<TransferResult> withdrawal(@NonNull String operatorAddress, @NonNull String account, @NonNull BigInteger amount, int operationType, String desc, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<String> addressList = genAddress(account, null, operatorAddress, contractAddress, null);
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
            TransactionReceipt transactionReceipt = asset.withdrawal(
                    addressList,
                    amount,
                    genType(operationType),
                    genDetail(desc, null),
                    resultSign);
            TransferResult tr = null;
            Tuple2<Boolean, List<BigInteger>> response = transactionReceipt.isStatusOK() ? asset.getWithdrawalOutput(transactionReceipt) : null;
            if (response != null && response.getValue1()) {
                tr = new TransferResult(response.getValue1(), response.getValue2().get(0), response.getValue2().get(1));
            }
            return DataToolUtils.handleTransaction(transactionReceipt, tr);
        } catch (Exception e) {
            log.error("withdrawal failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.ASSET_WITHDRAWAL_ERROR);
        }
    }

    /**
     * transfer
     *
     * @param fromAddress account of payments
     * @param toAddress account of receipts
     * @param amount transaction amount
     * @param operationType operation type custom defined
     * @param desc description
     * @return
     * @throws OpenLedgerBaseException
     */
    public ResponseData<TransferResult> transfer(@NonNull String operatorAddress, @NonNull String fromAddress, @NonNull String toAddress, @NonNull BigInteger amount, int operationType, String desc, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<String> addressList = genAddress(fromAddress, toAddress, operatorAddress, contractAddress, null);
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);

            TransactionReceipt transactionReceipt = asset.transfer(
                    addressList,
                    amount,
                    genType(operationType),
                    genDetail(desc, null),
                    resultSign);
            TransferResult tr = null;
            Tuple2<Boolean, List<BigInteger>> response = transactionReceipt.isStatusOK() ? asset.getTransferOutput(transactionReceipt) : null;
            if (response != null && response.getValue1()) {
                tr = new TransferResult(response.getValue1(), response.getValue2().get(0), response.getValue2().get(1));
            }
            return DataToolUtils.handleTransaction(transactionReceipt, tr);
        } catch (Exception e) {
            log.error("transfer failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.ASSET_TRANSFER_ERROR);
        }
    }

    /**
     * balance of account
     *
     * @param accountAddress account
     * @return balance of account
     * @throws OpenLedgerBaseException
     */
    public BigInteger getBalance(@NonNull String accountAddress, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);

            return asset.getBalance(accountAddress, resultSign);
        } catch (Exception e) {
            log.error("getBalance failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.ASSET_GETBALEANCE_ERROR);
        }
    }


    /**
     * Getting a list of accounts only supports the OrgAdmin operate     * Getting a list of accounts only supports the OrgAdmin operate
     *
     * @param message args hash
     * @param rs sign object
     * @return
     * @throws OpenLedgerBaseException
     */
    public List<String> getHolders(@NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);

            List resultList = asset.getHolders(resultSign);
            List<String> assetList = new ArrayList<>();
            resultList.stream().forEach(item -> {
                assetList.add(item.toString());
            });
            return assetList;
        } catch (Exception e) {
            log.error("getAccountList failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.ASSET_GETACCOUNTLIST_ERROR);
        }
    }


    /**
     * Getting the total asset is supported only by the OrgAdmin operation
     *
     * @param message args hash
     * @param rs sign by org admin
     * @return
     * @throws OpenLedgerBaseException
     */
    public BigInteger getTotalBalance(@NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
            return asset.getTotalBalance(resultSign);
        } catch (Exception e) {
            log.error("getTotalBalance failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.ASSET_GETTOTALBALEANCE_ERROR);
        }
    }


    /**
     * Adding ledgers only supports OrgAdmin for operation
     *
     * @param message args hash
     * @param rs sign by orgadmin
     * @return
     * @throws OpenLedgerBaseException
     */
    public ResponseData<BigInteger> addBook(@NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);

            TransactionReceipt transactionReceipt = asset.addBook(resultSign);
            BigInteger result = transactionReceipt.isStatusOK() ? asset.getAddBookOutput(transactionReceipt).getValue1() : BigInteger.valueOf(-1);
            return DataToolUtils.handleTransaction(transactionReceipt, result);
        } catch (Exception e) {
            log.error("addBook failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.ASSET_ADDBOOK_EROOR);
        }
    }

    /**
     * The account query only has the authority to query their own account records
     *
     * @param condition 查询参数封装对象
     * @return
     * @throws Exception 账本查询
     * todo recommoned 数据mysql查询
     */
    public List<RecordEntity> query(Condition condition, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws Exception {
        List<RecordEntity> recordEntities = new ArrayList<>();
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);

            List records = asset.queryBook(condition.getIntParams(), condition.getAddressParams(), condition.getLimits(), resultSign);
            for (int i = 0; i < records.size(); i++) {
                RecordEntity recordEntity = RecordBuilder.buildRecordEntity()
                        .withTermNo(new BigInteger(records.get(i).toString()))
                        .withSeq(new BigInteger(records.get(++i).toString()))
                        .withFrom(new Address(new BigInteger(records.get(++i).toString())).toString())
                        .withTo(new Address(new BigInteger(records.get(++i).toString())).toString())
                        .withAmount(new BigInteger(records.get(++i).toString()))
                        .withDesc(records.get(++i).toString())
                        .withAsset(new Address(new BigInteger(records.get(++i).toString())).toString())
                        .withOperator(new Address(new BigInteger(records.get(++i).toString())).toString())
                        .withTransactionType(new BigInteger(records.get(++i).toString()))
                        .withOperationType(new BigInteger(records.get(++i).toString()))
                        .withRelateAsset(new Address(new BigInteger(records.get(++i).toString())).toString())
                        .withSubject(records.get(++i).toString())
                        .build();
                recordEntities.add(recordEntity);
            }

        } catch (Exception e) {
            log.error("query failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.ASSET_BOOKQUERY_ERROR);
        }
        return recordEntities;
    }

    /**
     * open account
     *
     * @param account account address
     * @param message args hash
     * @param rs sign y orgAdmin
     * @return
     * @throws OpenLedgerBaseException
     */
    public ResponseData<Boolean> openAccount(@NonNull String account, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
            TransactionReceipt transactionReceipt = asset.openAccount(account, resultSign);
            Boolean result = transactionReceipt.isStatusOK() ? asset.getOpenAccountOutput(transactionReceipt).getValue1() : false;
            return DataToolUtils.handleTransaction(transactionReceipt, result);
        } catch (Exception e) {
            log.error("openAccount failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.ASSET_UNKNOW_ERROR);
        }

    }


}
