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

package com.webank.openledger.core.asset.nonfungible.entity;

import java.math.BigInteger;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * NonFungible Asset Object
 *
 * @author pepperli@webank.com
 */
@Getter
@Setter
public class Note {
    /**
     * The initialized state is published in the initialized state
     */
    public static final int INIT_STATUS = 1;
    /**
     * effectiveState
     */
    public static final int EFFECTIVE_STATUS = 2;
    /**
     * Expiration status cannot be traded after expiration
     */
    public static final int EXPIRATE_STATUS = 3;
    /**
     * Unable to trade after freezing can be unfrozen by the unfreeze operation
     */
    public static final int FREEZE_STATUS = 4;
    /**
     * Unable to trade or modify information after destruction
     */
    public static final int TEAR_STATUS = 5;

    /**
     * unique Asset Number
     */
    BigInteger noteNo;
    /**
     * current Owner
     */
    String owner;
    /**
     * issuer address
     */
    String issuer;
    /**
     * the Batch number is issued once as a batch
     */
    BigInteger batchNo;
    /**
     * current State Of Asset
     */
    BigInteger status;
    /**
     * expirationDate
     */
    Date expirationDate;
    /**
     * effectiveDate
     */
    Date effectiveDate;

    @Override
    public String toString() {
        return "Note{" +
                "noteNo=" + noteNo +
                ", owner='" + owner + '\'' +
                ", issuer='" + issuer + '\'' +
                ", batchNo=" + batchNo +
                ", status=" + status +
                ", expirationDate=" + expirationDate +
                ", effectiveDate=" + effectiveDate +
                '}';
    }
}
