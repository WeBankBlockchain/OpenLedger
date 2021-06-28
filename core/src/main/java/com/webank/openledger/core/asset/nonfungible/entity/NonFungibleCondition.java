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
import java.util.ArrayList;
import java.util.List;

import com.webank.openledger.core.asset.fungible.entity.Condition;

import lombok.Getter;
import lombok.Setter;

/**
 * ledger Query Condition Object
 *
 * @author pepperli@webank.com
 */
@Getter
@Setter
public class NonFungibleCondition extends Condition {
    /**
     * 资产编号
     */
    private BigInteger noteNo;

    public NonFungibleCondition(BigInteger termNo, BigInteger seqNo, String fromAddress, String toAddress) {
        super(termNo, seqNo, fromAddress, toAddress);
    }

    public NonFungibleCondition(BigInteger termNo, BigInteger seqNo, String fromAddress, String toAddress, List<BigInteger> limits) {
        super(termNo, seqNo, fromAddress, toAddress, limits);
    }

    public NonFungibleCondition(BigInteger termNo, BigInteger seqNo, String fromAddress, String toAddress, BigInteger note, List<BigInteger> limits) {
        super(termNo, seqNo, fromAddress, toAddress, limits);
        this.noteNo = note;
    }


    /**
     * gen parameters
     *
     * @return
     */
    public List<BigInteger> getIntParams() {
        List<BigInteger> uintCondition = new ArrayList<>();
        uintCondition.add(this.getTermNo() == null ? BigInteger.valueOf(0) : this.getTermNo());
        uintCondition.add(this.getSeqNo() == null ? BigInteger.valueOf(0) : this.getSeqNo());
        uintCondition.add(this.getNoteNo() == null ? BigInteger.valueOf(0) : this.getNoteNo());
        return uintCondition;
    }

}
