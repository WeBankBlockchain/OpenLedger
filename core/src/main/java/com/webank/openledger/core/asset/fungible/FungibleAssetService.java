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

import com.webank.openledger.contracts.FungibleAsset;
import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.asset.fungible.entity.AssetEntity;
import com.webank.openledger.core.constant.ErrorCode;
import com.webank.openledger.core.exception.OpenLedgerBaseException;
import com.webank.openledger.core.identity.IdentityService;
import com.webank.openledger.core.response.DataToolUtils;
import com.webank.openledger.core.response.ResponseData;
import com.webank.openledger.utils.OpenLedgerUtils;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.model.TransactionReceipt;


/**
 * Fungible Asset Service
 * @author pepperli
 *
 */
@Slf4j
@Getter
public class FungibleAssetService extends StandardAssetService<FungibleAsset> {

    /**
     * identity object
     * usage: FungibleAssetService.getIdentity()
     */
    private IdentityService<FungibleAsset> identity;

    /**
     * Initialize the contract object
     *
     * @param blockchain property object
     * @param contractAddress asset contractAddress
     */
    public FungibleAssetService(Blockchain blockchain, String contractAddress) {
        super(blockchain, contractAddress, FungibleAsset.class);
        identity = new IdentityService(this.getAsset());
    }


    /**
     * get the asset object information
     * including rate and price
     *
     * @return
     */
    public AssetEntity getAssetInfo() throws OpenLedgerBaseException {
        AssetEntity assetEntity = genAssetInfo();
        return assetEntity;
    }

    /**
     * assembly asset information
     *
     * @return
     * @throws OpenLedgerBaseException
     */
    private AssetEntity genAssetInfo() throws OpenLedgerBaseException {
        AssetEntity assetEntity = new AssetEntity();

        try {
            assetEntity.setAddress(this.getAsset().getAddress());
            assetEntity.setPrice(this.getAsset().price());
            assetEntity.setRate(this.getAsset().rate());
        } catch (Exception e) {
            log.error("getAsset failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.ASSET_GETASSETINFO_ERROR);
        }
        return assetEntity;
    }

    /**
     * Set the price
     * permission to support only the admin of org
     *
     * @param price
     * @param message hash result
     * @param rs Signature object
     * @return current price
     * @throws OpenLedgerBaseException
     */
    public ResponseData<BigInteger> setPrice(@NonNull BigInteger price, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
            TransactionReceipt transactionReceipt = this.getAsset().setPrice(price, resultSign);
            BigInteger curPrice = transactionReceipt.isStatusOK()? this.getAsset().getSetPriceOutput(transactionReceipt).getValue1() : BigInteger.valueOf(-1);
            ResponseData<BigInteger> responseData = DataToolUtils.handleTransaction(transactionReceipt, curPrice);
            return responseData;
        } catch (Exception e) {
            log.error("setPrice failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.ASSET_UNKNOW_ERROR);
        }
    }

    /**
     * Set the rate
     * permission to support only the admin of org
     *
     * @param rate
     * @param message hash result
     * @param rs Signature object
     * @return current price
     * @throws OpenLedgerBaseException
     */
    public ResponseData<BigInteger> setRate(@NonNull BigInteger rate, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
            TransactionReceipt transactionReceipt = this.getAsset().setRate(rate, resultSign);
            BigInteger curRate = transactionReceipt.isStatusOK() ? this.getAsset().getSetRateOutput(transactionReceipt).getValue1() : BigInteger.valueOf(-1);
            ResponseData<BigInteger> responseData = DataToolUtils.handleTransaction(transactionReceipt, curRate);
            return responseData;
        } catch (Exception e) {
            log.error("setRate failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.ASSET_UNKNOW_ERROR);
        }
    }


}
