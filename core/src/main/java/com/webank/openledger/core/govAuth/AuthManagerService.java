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

package com.webank.openledger.core.govAuth;


import com.webank.openledger.contracts.gov_auth.AuthManager;
import com.webank.openledger.core.Blockchain;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.contract.Contract;

/**
 * auth manager service
 * @param <T> object which extend contract
 */
@Slf4j
@Getter
@Setter
public class AuthManagerService<T extends Contract> {
    protected Blockchain blockchain;
    protected AuthManager contractIns;

    /**
     * 初始化合约对象
     *
     * @param blockchain
     * @param contractAddress
     */
    public AuthManagerService(Blockchain blockchain, String contractAddress) {
        this.blockchain = blockchain;
        contractIns = blockchain.getLedger(Blockchain.DEFAULT_LEDGERID).getContract(contractAddress, AuthManager.class);
    }

    /**
     * 获取当前合约对象
     *
     * @return
     */
    public T getContractIns() {
        return contractIns == null ? null : (T) contractIns;
    }

}
