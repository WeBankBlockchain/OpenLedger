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

package com.webank.openledger.core.asset.fungible.entity;

import java.math.BigInteger;

import lombok.Data;

/**
 * ledger record entity
 *
 * @author pepperli
 */
@Data
public class RecordEntity {
    /**
     * number of term
     */
    private BigInteger termNo;
    /**
     * serialNumber
     */
    private BigInteger seq;
    /**
     * account of payment
     */
    private String from;
    /**
     * account of recepit
     */
    private String to;
    /**
     * transaction amount
     */
    private BigInteger amount;
    /**
     * transaction asset contract address
     */
    private String asset;
    /**
     * operator account address
     */
    private String operator;
    /**
     * description
     */
    private String desc;
    /**
     * transaction type
     * 0-deposit 1-withdrawl  2-transfer
     */
    private BigInteger transactionType;
    /**
     * operation type custom defined
     */
    private BigInteger operationType;

    /**
     * accounting Ssubject
     */
    private String subject;

    /**
     * related transaction asset contract address
     */
    private String relateAsset;


}
