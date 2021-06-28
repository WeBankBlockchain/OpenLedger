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

package com.webank.openledger.core.account;


import com.webank.openledger.contracts.Account;
import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.constant.ErrorCode;
import com.webank.openledger.core.identity.IdentityService;
import com.webank.openledger.core.response.ResponseData;
import com.webank.openledger.utils.OpenLedgerUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple3;
import org.fisco.bcos.sdk.contract.Contract;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;


/**
 * account service
 * @param <T> object extend contract
 */
@Slf4j
@Getter
@Setter
public class AccountService<T extends Contract> {
    /**
     * blockchain property object
     */
    protected Blockchain blockchain;
    /**
     * account contract object
     */
    protected Account contractIns;

    /**
     * identity object
     * usage: AccountAservice.getIdentity()
     */
    protected IdentityService<Account> identity;

    /**
     * Initialize the contract object
     *
     * @param blockchain property object
     * @param contractAddress account contractAddress
     */
    public AccountService(Blockchain blockchain, String contractAddress) {
        this.blockchain = blockchain;
        contractIns = blockchain.getLedger(Blockchain.DEFAULT_LEDGERID).getContract(contractAddress, Account.class);
        identity = new IdentityService(contractIns);
    }

    /**
     *  get contract object
     *
     * @return
     */
    public T getContractIns() {
        return contractIns == null ? null : (T) contractIns;
    }


    /**
     * get account's asset
     * @param isFungible boolean is fungible
     * @param message args hash
     * @param rs sign by account or orgAdmin
     * @return
     */
    public ResponseData<HashMap> getAllAssets(Boolean isFungible,byte[] message, ECDSASignatureResult rs) {
        HashMap<String, String> assets = new HashMap<>();
        Tuple3<List<String>, List<String>, BigInteger> ret;
        try {
            ret = contractIns.getAllAssetsWithSign(isFungible, OpenLedgerUtils.convertSignToByte(message, rs));
        } catch (ContractException e) {
            log.error("getAllAssets failed:{}", e);
            return new ResponseData(assets, ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }

        if (ret.getValue1().size() != ret.getValue2().size()) {
            return new ResponseData(assets, ErrorCode.STORAGE_KV_SIZE_NOT_MATCH);
        }
        int num = ret.getValue3().intValue();
        for (int i = 0; i < ret.getValue1().size() & i < num; i++) {
            assets.put(ret.getValue1().get(i), ret.getValue2().get(i));
        }
        return new ResponseData<>(assets, ErrorCode.SUCCESS);
    }


}
