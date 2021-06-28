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

package com.webank.openledger.core.auth;


import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;

import com.webank.openledger.contracts.AuthCenter;
import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.constant.ErrorCode;
import com.webank.openledger.core.response.DataToolUtils;
import com.webank.openledger.core.response.ResponseData;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple3;
import org.fisco.bcos.sdk.contract.Contract;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;


/**
 *  authCenter Service
 * @param <T> object extends contract
 */
@Slf4j
@Getter
@Setter
public class AuthCenterService<T extends Contract> {
    /**
     * blockchain property object
     */
    protected Blockchain blockchain;
    /**
     * authCenter contract object
     */
    protected AuthCenter contractIns;

    /**
     * Initialize the contract object
     *
     * @param blockchain property object
     * @param contractAddress account contractAddress
     */
    public AuthCenterService(Blockchain blockchain, String contractAddress) {
        this.blockchain = blockchain;
        contractIns = blockchain.getLedger(Blockchain.DEFAULT_LEDGERID).getContract(contractAddress, AuthCenter.class);
    }

    /**
     * get contract object
     *
     * @return
     */
    public T getContractIns() {
        return contractIns == null ? null : (T) contractIns;
    }

    /**
     * check permission
     * @param callee callee account address
     * @param key auth key
     * @param args function's args
     * @param sign sign by account
     * @return boolean isPermission
     */
    public ResponseData<Boolean> check(String callee, byte[] key, byte[] args, List<byte[]> sign) {
        Tuple2<String, Boolean> ret = null;
        try {
            ret = contractIns.checkWithSign(callee, key, args, sign);
        } catch (ContractException e) {
            log.error("check contract execute error:{}", e.toString());
            return new ResponseData<>(false, ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }
        return new ResponseData<>(ret.getValue2(), ErrorCode.TRANSACTION_EXECUTE_ERROR);
    }

    /**
     * check permission
     * @param callee callee account address
     * @param key auth key
     * @param args function's args
     * @param sign sign by account
     * @return boolean isPermission
     */
    public ResponseData<Boolean> check2(String org, String callee, byte[] key, byte[] args, List<byte[]> sign) {
        Tuple2<String, Boolean> ret = null;
        try {
            ret = contractIns.check2WithSign(org, callee, key, args, sign);
        } catch (ContractException e) {
            log.error("check2 contract execute error:{}", e.toString());
            return new ResponseData<>(false, ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }
        return new ResponseData<>(ret.getValue2(), ErrorCode.TRANSACTION_EXECUTE_ERROR);
    }


    /**
     * add permission key
     * @param key key value can be function name or a key
     * @param value key value
     * @return boolen is add successfully
     */
    public ResponseData<Boolean> addKeyType(byte[] key, byte[] value) {
        TransactionReceipt transactionReceipt = contractIns.addKeyType(key, value);
        Boolean result = transactionReceipt.isStatusOK()? contractIns.getAddKeyTypeOutput(transactionReceipt).getValue1() : false;
        ResponseData<Boolean> responseData = DataToolUtils.handleTransaction(transactionReceipt, result);
        return responseData;
    }

    /**
     * remove key type from permission list
     * @param key key value
     * @return boolean is remove successfully
     */
    public ResponseData<Boolean> removeKeyType(byte[] key) {
        TransactionReceipt transactionReceipt = contractIns.removeKeyType(key);
        Boolean result = transactionReceipt.isStatusOK()? contractIns.getRemoveKeyTypeOutput(transactionReceipt).getValue1() : false;
        ResponseData<Boolean> responseData = DataToolUtils.handleTransaction(transactionReceipt, result);
        return responseData;
    }

    /**
     * get all permission values
     * @return
     */
    public ResponseData<HashMap> getAllKeyType() {
        HashMap<byte[], byte[]> keyTypes = new HashMap<>();

        Tuple3<List<byte[]>, List<byte[]>, BigInteger> ret;
        try {
            ret = contractIns.getAllKeyType();
        } catch (ContractException e) {
            log.error("getAllKeyType contract execute error:{}", e.toString());
            return new ResponseData<>(keyTypes, ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }
        if (ret.getValue1().size() != ret.getValue2().size()) {
            return new ResponseData<>(keyTypes, ErrorCode.STORAGE_KV_SIZE_NOT_MATCH);
        }

        int num = ret.getValue3().intValue();
        for (int i = 0; i < ret.getValue1().size() && i < num; i++) {
            keyTypes.put(ret.getValue1().get(i), ret.getValue2().get(i));
        }
        return new ResponseData<>(keyTypes, ErrorCode.SUCCESS);
    }


    /**
     * get nonce of account
     * @param account account address
     * @return nonce value
     */
    public ResponseData<BigInteger> getNonceFromAccount(String account) {
        BigInteger ret = null;
        try {
            ret = contractIns.getNonceFromAccount(account);
        } catch (ContractException e) {
            log.error("getNonceFromAccount contract execute error:{}", e.toString());
            return new ResponseData<>(null, ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }
        return new ResponseData<>(ret, ErrorCode.SUCCESS);
    }

    /**
     * check if nonce verify
     * before account transfer will check account nonce
     * @param args args value
     * @param message args hash value
     * @param account account address
     * @return boolean is nonce verify
     */
    public ResponseData<Boolean> checkNonce(byte[] args, byte[] message, String account) {
        TransactionReceipt transactionReceipt = contractIns.checkNonce(args, message, account);
        Boolean result = transactionReceipt.isStatusOK()? contractIns.getCheckNonceOutput(transactionReceipt).getValue1() : false;
        ResponseData<Boolean> responseData = DataToolUtils.handleTransaction(transactionReceipt, result);
        return responseData;
    }
}
