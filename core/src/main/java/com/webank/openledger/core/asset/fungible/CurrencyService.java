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


import java.math.BigInteger;
import java.util.List;

import com.webank.openledger.contracts.Currency;
import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.asset.fungible.entity.CurrencyEntity;
import com.webank.openledger.core.asset.fungible.entity.TransferResult;
import com.webank.openledger.core.constant.ErrorCode;
import com.webank.openledger.core.exception.OpenLedgerBaseException;
import com.webank.openledger.core.response.DataToolUtils;
import com.webank.openledger.core.response.ResponseData;
import com.webank.openledger.utils.OpenLedgerUtils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.model.TransactionReceipt;


/**
 * currency service
 *
 * @author pepperli
 */
@Slf4j
public class CurrencyService extends StandardAssetService<Currency> {
    private Currency currency;

    public CurrencyService(Blockchain blockchain, String contractAddress) {
        super(blockchain, contractAddress, Currency.class);
        currency = getAsset();
    }

    /**
     * get asset information
     *
     * @return
     */
    public CurrencyEntity getCurrencyInfo() throws OpenLedgerBaseException {
        CurrencyEntity assetEntity = genCurrencyInfo();
        return assetEntity;
    }

    /**
     * assembly_returns_asset_information
     *
     * @return
     * @throws OpenLedgerBaseException
     */
    private CurrencyEntity genCurrencyInfo() throws OpenLedgerBaseException {
        CurrencyEntity currencyEntity = new CurrencyEntity();

        try {
            currencyEntity.setAddress(currency.getAddress());
            currencyEntity.setDecimals(currency.decimals().intValue());
            currencyEntity.setName(currency.name());
            currencyEntity.setSymbol(currency.symbol());
        } catch (Exception e) {
            log.error("getAsset failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.CURRENCY_GETINFO_ERROR);
        }
        return currencyEntity;
    }


    /**
     * deposit currency to account
     *
     * @param account transaction account
     * @param amount transaction amount
     * @param operationType operation type custom defined
     * @param desc description
     * @param subject accounting Subject can be null
     * @param relateAsset related asset contract address (can be null)
     * @param message args hash
     * @param rs sign by orgadmin
     * @return
     * @throws OpenLedgerBaseException
     */
    public ResponseData<TransferResult> deposit(@NonNull String operatorAddress, @NonNull String account, @NonNull BigInteger amount, int operationType, String desc, String subject, String relateAsset, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<String> addressList = genAddress(null, account, operatorAddress, contractAddress, relateAsset);
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);

            TransactionReceipt transactionReceipt = asset.deposit(
                    addressList,
                    amount,
                    genType(operationType),
                    genDetail(desc, subject),
                    resultSign);
            TransferResult tr = null;
            Tuple2<Boolean, List<BigInteger>> response = transactionReceipt.isStatusOK() ? asset.getDepositOutput(transactionReceipt) : null;
            if (response != null && response.getValue1()) {
                tr = new TransferResult(response.getValue1(), response.getValue2().get(0), response.getValue2().get(1));
            }
            return DataToolUtils.handleTransaction(transactionReceipt, tr);
        } catch (Exception e) {
            log.error("desposit failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.ASSET_DESIPOSIT_ERROR);
        }
    }

    /**
     * withddrawl currency from account
     *
     * @param account transaction account
     * @param amount transaction amount
     * @param operationType operation type custom defined
     * @param desc description
     * @param subject accounting Subject can be null
     * @param relateAsset related asset contract address (can be null)
     * @param message args hash
     * @param rs sign by orgadmin
     * @return
     * @throws OpenLedgerBaseException
     */
    public ResponseData<TransferResult> withdrawal(@NonNull String operatorAddress, @NonNull String account, @NonNull BigInteger amount, int operationType, String desc, String subject, String relateAsset, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<String> addressList = genAddress(account, null, operatorAddress, contractAddress, relateAsset);
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);

            TransactionReceipt transactionReceipt = asset.withdrawal(
                    addressList,
                    amount,
                    genType(operationType),
                    genDetail(desc, subject),
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
     * @param subject accounting Subject can be null
     * @param relateAsset related asset contract address (can be null)
     * @param message args hash
     * @param rs sign by orgadmin
     * @return
     * @throws OpenLedgerBaseException
     */
    public ResponseData<TransferResult> transfer(@NonNull String operatorAddress, @NonNull String fromAddress, @NonNull String toAddress, @NonNull BigInteger amount, int operationType, String desc, String subject, String relateAsset, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<String> addressList = genAddress(fromAddress, toAddress, operatorAddress, contractAddress, relateAsset);
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);

            TransactionReceipt transactionReceipt = asset.transfer(
                    addressList,
                    amount,
                    genType(operationType),
                    genDetail(desc, subject),
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


}
