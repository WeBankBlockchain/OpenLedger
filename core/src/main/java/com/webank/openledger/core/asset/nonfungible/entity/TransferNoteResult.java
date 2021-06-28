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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * NonFungible asset transaction result
 *
 * @author pepperli@webank.com
 */
@Getter
@Setter
@ToString
public class TransferNoteResult {
    /**
     * transaction results
     * true：successful  false: fail
     */
    private Boolean result;
    /**
     * transaction asset noteNo
     */
    private BigInteger noteNo;
    /**
     * ledger's termNo used for query book conditon
     */
    private BigInteger termNo;
    /**
     * transaction seqNo used for query book conditon
     */
    private BigInteger seqNo;
}
