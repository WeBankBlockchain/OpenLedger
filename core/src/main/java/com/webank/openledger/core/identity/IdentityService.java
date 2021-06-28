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

package com.webank.openledger.core.identity;

import java.math.BigInteger;

import com.webank.openledger.contracts.Identity;
import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.common.BaseIdentity;
import com.webank.openledger.core.common.ValueModel;
import com.webank.openledger.core.constant.ErrorCode;
import com.webank.openledger.core.exception.OpenLedgerBaseException;
import com.webank.openledger.core.response.DataToolUtils;
import com.webank.openledger.core.response.ResponseData;
import com.webank.openledger.utils.JsonHelper;
import com.webank.openledger.utils.OpenLedgerUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.contract.Contract;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;

/**
 * Base Identity Serivce
 * @author pepperli
 * @param <T> object extend contract
 */
@Slf4j
public class IdentityService<T extends Contract> {
    /**
     *  contract object
     */
    BaseIdentity identity;
    /**
     * blockchain property
     */
    private Blockchain blockchain;


    /**
     * construct
     * @param blockchain
     * @param contractAddress
     */
    public IdentityService(Blockchain blockchain, String contractAddress) {
        this.blockchain = blockchain;
        identity = blockchain.getLedger(Blockchain.DEFAULT_LEDGERID).getContract(contractAddress, Identity.class);
    }

    /**
      *  initialize the identity contract object
     */
    public IdentityService(T contract) {
        identity = (BaseIdentity) contract;
    }


    /**
     * get identity's nonce
     * @return
     * @throws OpenLedgerBaseException
     */
    public BigInteger getNonce() throws OpenLedgerBaseException {
        try {
            return identity.getNonce();
        } catch (ContractException e) {
            log.error("getNonce failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.IDENTITY_UNKNOW_ERROR);
        }
    }


    /**
     * insert property
     * if the key already exists overwrite enforced
     * @param key property key
     * @param value property value
     * @param hashByte args hash
     * @param rs sign by orgadmin
     * @return boolean is insert successfully
     * @throws OpenLedgerBaseException
     */
    public ResponseData<Boolean> insertWithSignatureResult(@NonNull String key,@NonNull Object value,@NonNull byte[] hashByte,@NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        ValueModel vm = new ValueModel(value);
        byte[] valueByte = ValueModel.getByteVal(vm);
        TransactionReceipt transactionReceipt = identity.insertWithSign(key.getBytes(),valueByte, OpenLedgerUtils.convertSignToByte(hashByte, rs));
        Boolean result = transactionReceipt.isStatusOK()? identity.getInsertWithSignOutput(transactionReceipt).getValue1() : false;

        return DataToolUtils.handleTransaction(transactionReceipt, result);

    }


    /**
     * add property
     * requried key isn't exist
     * @param key property key
     * @param value property value
     * @param hashByte args hash
     * @param rs sign by orgadmin
     * @return boolean is add successfully
     * @throws OpenLedgerBaseException
     */
    public ResponseData<Boolean> addWithSignatureResult(@NonNull String key, @NonNull Object value, @NonNull byte[] hashByte, @NonNull ECDSASignatureResult rs)  {
        ValueModel vm = new ValueModel(value);
        byte[] valueByte = ValueModel.getByteVal(vm);
        TransactionReceipt transactionReceipt = identity.addWithSign(key.getBytes(), valueByte, OpenLedgerUtils.convertSignToByte(hashByte, rs));
        Boolean result = transactionReceipt.isStatusOK()? identity.getAddWithSignOutput(transactionReceipt).getValue1() : false;
        return DataToolUtils.handleTransaction(transactionReceipt, result);
    }


    /**
     * set property
     * requried key is exist
     * @param key property key
     * @param value property value
     * @param hashByte args hash
     * @param rs sign by orgadmin
     * @return boolean is set successfully
     * @throws OpenLedgerBaseException
     */
    public ResponseData<Boolean> setWithSignatureResult(@NonNull String key,@NonNull Object value,@NonNull byte[] hashByte, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        ValueModel vm = new ValueModel(value);
        byte[] valueByte = ValueModel.getByteVal(vm);
        TransactionReceipt transactionReceipt = identity.setWithSign(key.getBytes(), valueByte, OpenLedgerUtils.convertSignToByte(hashByte, rs));
        Boolean result = transactionReceipt.isStatusOK()? identity.getSetWithSignOutput(transactionReceipt).getValue1() : false;

        return DataToolUtils.handleTransaction(transactionReceipt, result);
    }

    /**
     * remove property
     * @param key property key
     * @param hashByte args hash
     * @param rs sign by orgadmin
     * @return boolean is remove successfully
     * @throws OpenLedgerBaseException
     */
    public ResponseData<Boolean> removeWithSignatureResult(@NonNull String key,@NonNull byte[] hashByte,@NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        TransactionReceipt transactionReceipt = identity.removeWithSign(key.getBytes(), OpenLedgerUtils.convertSignToByte(hashByte, rs));
        Boolean result = transactionReceipt.isStatusOK()? identity.getRemoveWithSignOutput(transactionReceipt).getValue1() : false;
        return DataToolUtils.handleTransaction(transactionReceipt, result);
    }

    /**
     * get property size
     * @return
     * @throws OpenLedgerBaseException
     */
    public BigInteger getSize() throws OpenLedgerBaseException {
        BigInteger size = null;
        try {
            size = identity.size();
        } catch (ContractException e) {
            log.error("getSize failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.ASSET_UNKNOW_ERROR);
        }
        return size;

    }


    /**
     * get property
     * @param key property key
     * @param hashByte args hash
     * @param rs sign object
     * @return property object
     * @throws OpenLedgerBaseException
     */
    public Object getWithSignatureResult(String key, byte[] hashByte,ECDSASignatureResult rs) throws OpenLedgerBaseException {
        return this.getWithSignatureResult(key, hashByte,rs, null);
    }

    /**
     * get property
     * @param key property key
     * @param hashByte args hash
     * @param rs sign object
     * @param typeReference object type
     * @return property object
     * @throws OpenLedgerBaseException
     */
    public Object getWithSignatureResult(String key, byte[] hashByte,ECDSASignatureResult rs, TypeReference typeReference) throws OpenLedgerBaseException {
        try {
            byte[] value = identity.getWithSign(key.getBytes(), OpenLedgerUtils.convertSignToByte(hashByte, rs));
            ValueModel vm = JsonHelper.getObjectMapper().readValue(value, ValueModel.class);
            return vm.getValue(typeReference);
        } catch (Exception e) {
            log.error("get failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.IDENTITY_UNKNOW_ERROR);
        }
    }

    /**
     * get property
     * @param key property key
     * @param hashByte args hash
     * @param rs sign object
     * @return property bytes
     * @throws OpenLedgerBaseException
     */
    public  byte[]  getByteWithSignatureResultWith(String key, byte[] hashByte,ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            byte[] value = identity.getWithSign(key.getBytes(), OpenLedgerUtils.convertSignToByte(hashByte, rs));
            return value;
        } catch (Exception e) {
            log.error("get failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.IDENTITY_UNKNOW_ERROR);
        }
    }

    public static byte[] genHashByte(String key,BigInteger nonce){
        return genHashByte(key,null,nonce);
    }

    /**
     * generate hash byte
     * @param key property key value
     * @param value property value
     * @param nonce account nonce
     * @return hash bytes
     */
    public static byte[] genHashByte(String key,Object value,BigInteger nonce){
        byte[] messageOrigin ;
        if(value!=null){
            ValueModel vm = new ValueModel(value);
            byte[] valueByte = ValueModel.getByteVal(vm);
            messageOrigin = OpenLedgerUtils.concatByte(key.getBytes(), valueByte, OpenLedgerUtils.getBytes32(nonce.toByteArray()));
        }else{
            messageOrigin = OpenLedgerUtils.concatByte(key.getBytes(), OpenLedgerUtils.getBytes32(nonce.toByteArray()));
        }
        byte[] hashByte = OpenLedgerUtils.computeKeccak256Hash(messageOrigin);
        return hashByte;
    }



}
