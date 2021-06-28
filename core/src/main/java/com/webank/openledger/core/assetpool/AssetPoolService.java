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


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.webank.openledger.contracts.AssetPool;
import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.assetpool.entity.PoolStatus;
import com.webank.openledger.core.constant.ErrorCode;
import com.webank.openledger.core.exception.OpenLedgerBaseException;
import com.webank.openledger.core.response.DataToolUtils;
import com.webank.openledger.core.response.ResponseData;
import com.webank.openledger.utils.OpenLedgerUtils;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;

/**
 * assetpool service
 *
 * @author pepperli
 */
@Slf4j
@Getter
@Setter
public class AssetPoolService {
    /**
     * the normal state of the assetpool
     */
    public static final int NORMAL_STATUS = 1;
    /**
     * the forzen stat of the assetpool
     */
    public static final int FROZEN_STATUS = 2;

    /**
     * blockchain property object
     */
    private Blockchain blockchain;

    /**
     * assetpool contract object
     */
    private AssetPool assetPool;
    /**
     * Initialize the contract object
     *
     * @param blockchain property object
     * @param contractAddress account contractAddress
     */
    public AssetPoolService(Blockchain blockchain, String contractAddress) {
        this.blockchain = blockchain;
        assetPool = blockchain.getLedger(Blockchain.DEFAULT_LEDGERID).getContract(contractAddress, AssetPool.class);
    }


    /**
     *  create assetpool
     * @param blockchain blockchain property object
     * @param orgAddress org contract address
     * @param authAddress authCenter contract address
     * @return assetpool contract address
     * @throws OpenLedgerBaseException
     */
    public static String createAssetPool(@NonNull Blockchain blockchain, @NonNull String orgAddress, @NonNull String authAddress) throws OpenLedgerBaseException {
        // 部署合约
        try {
            AssetPool assetPool = AssetPool.deploy(blockchain.getClient(Blockchain.DEFAULT_LEDGERID), blockchain.getProjectAccount().getKeyPair(), authAddress, orgAddress);
            log.info(assetPool.getContractAddress());
            return assetPool.getContractAddress();
        } catch (ContractException e) {
            log.error("createAssetPool failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.ASSET_DEPLOY_ERROR);
        }

    }


    /**
     * add asset to assetpool
     * @param assetAddress asset address
     * @param message args hash
     * @param rs sign by orgAdmin
     * @return assetpool asset list
     * @throws OpenLedgerBaseException
     */
    public ResponseData<List<String>> addAsset(@NonNull String assetAddress, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
            TransactionReceipt transactionReceipt = assetPool.addAsset(assetAddress, resultSign);
            List<String> result = transactionReceipt.isStatusOK() ? assetPool.getAddAssetOutput(transactionReceipt).getValue1() : null;
            ResponseData<List<String>> responseData = DataToolUtils.handleTransaction(transactionReceipt, result);
            return responseData;
        } catch (Exception e) {
            log.error("addAsset failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }
    }

    /**
     * add status value to assetpool
     * @param status stauts entity
     * @param message args hash
     * @param rs sign by orgAdmin
     * @return
     * @throws OpenLedgerBaseException
     */
    public ResponseData<BigInteger> addStatus(@NonNull PoolStatus status, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
            TransactionReceipt transactionReceipt = assetPool.addStatus(status.getStatus(), status.getName(), status.getDesc(), resultSign);
            BigInteger curStatus = transactionReceipt.isStatusOK() ? assetPool.getAddStatusOutput(transactionReceipt).getValue1() : BigInteger.valueOf(-1);
            return DataToolUtils.handleTransaction(transactionReceipt, curStatus);
        } catch (Exception e) {
            log.error("addStatus failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }
    }

    /**
     * move asset status to new status
     * @param assetAddress asset contract address
     * @param status new status value
     * @param message args hash
     * @param rs sign by orgAdmin
     * @return asset current status
     * @throws OpenLedgerBaseException
     */
    public ResponseData<BigInteger> moveAsset(@NonNull String assetAddress, int status, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
            TransactionReceipt transactionReceipt = assetPool.moveAsset(assetAddress, BigInteger.valueOf(status), resultSign);
            BigInteger curAssetStatus = transactionReceipt.isStatusOK() ? assetPool.getMoveAssetOutput(transactionReceipt).getValue1() : BigInteger.valueOf(-1);
            return DataToolUtils.handleTransaction(transactionReceipt, curAssetStatus);
        } catch (Exception e) {
            log.error("moveAsset failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }
    }

    /**
     * remove asset from assetpool
     * @param assetAddress asset contract address
     * @param message args hash
     * @param rs sign by orgAmdmin
     * @return currenct assetpool asset list
     * @throws OpenLedgerBaseException
     */
    public ResponseData<List<String>> removeAsset(@NonNull String assetAddress, @NonNull byte[] message, ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
            TransactionReceipt transactionReceipt = assetPool.removeAsset(assetAddress, resultSign);
            List<String> assetList = transactionReceipt.isStatusOK() ? assetPool.getRemoveAssetOutput(transactionReceipt).getValue1() : null;
            return DataToolUtils.handleTransaction(transactionReceipt, assetList);
        } catch (Exception e) {
            log.error("removeAsset failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }
    }

    /**
     * freeze asset pool
     * after freeze assetpool,can not addAsset、moveAsset、addStatus、removeAsset and so on.     *
     * @param message args hash
     * @param rs sign by orgAdmin
     * @return current assetpool stauts
     * @throws OpenLedgerBaseException
     */
    public ResponseData<BigInteger> freezePool(@NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
            TransactionReceipt transactionReceipt = assetPool.freezePool(resultSign);
            BigInteger poolStatus = transactionReceipt.isStatusOK() ? assetPool.getFreezePoolOutput(transactionReceipt).getValue1() : null;
            return DataToolUtils.handleTransaction(transactionReceipt, poolStatus);
        } catch (Exception e) {
            log.error("forzenPool failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }
    }

    /**
     * unfreeze assetpool change assetpool stauts to normal stauts
     * @param message args hash
     * @param rs sign by orgAdmin
     * @return current assetpool status
     * @throws OpenLedgerBaseException
     */
    public ResponseData<BigInteger> unFreezePool(@NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
            TransactionReceipt transactionReceipt = assetPool.unfreezePool(resultSign);
            BigInteger poolStatus = transactionReceipt.isStatusOK() ? assetPool.getUnfreezePoolOutput(transactionReceipt).getValue1() : BigInteger.valueOf(-1);
            return DataToolUtils.handleTransaction(transactionReceipt, poolStatus);
        } catch (Exception e) {
            log.error("unFreezePool failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }
    }

    /**
     * get assetpool current status
     * @return
     * @throws OpenLedgerBaseException
     */
    public int getPoolStatus() throws OpenLedgerBaseException {
        try {
            return assetPool.status().intValue();
        } catch (Exception e) {
            log.error("getPoolStatus failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }
    }

    /**
     * get assetpool's asset address list
     * @param message args hash
     * @param rs sign by orgAdmin
     * @return asset address list
     * @throws OpenLedgerBaseException
     */
    public List<String> getAssetList(@NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
            List resultList = assetPool.getAssetList(resultSign);
            List<String> assetList = new ArrayList<>();
            resultList.stream().forEach(item -> {
                assetList.add(item.toString());
            });
            return assetList;
        } catch (Exception e) {
            log.error("getAssetList failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }
    }

}
