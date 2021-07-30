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
import com.webank.openledger.core.asset.AccountHolderService;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


/**
 * account service
 */
@Slf4j
@Getter
@Setter
public class AccountService extends AccountHolderService<Account> {
//    /**
//     * blockchain property object
//     */
//    protected Blockchain blockchain;
//    /**
//     * account contract object
//     */
//    protected Account contractIns;
//
//    /**
//     * identity object
//     * usage: AccountAservice.getIdentity()
//     */
//    protected IdentityService<Account> identity;

    /**
     * Initialize the contract object
     *
     * @param blockchain property object
     * @param contractAddress account contractAddress
     */
    public AccountService(Blockchain blockchain, String contractAddress,String assetAddress) {
        super(blockchain,contractAddress,assetAddress,Account.class);
    }



}
