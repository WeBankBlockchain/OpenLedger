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

import java.io.Serializable;
import java.math.BigInteger;

import lombok.Getter;
import lombok.Setter;

/**
 * struct of transfer,deposit,withdrawl reuslt
 * @author pepperli
 */
@Getter
@Setter
public class TransferResult implements Serializable {
    Boolean isSuccees;
    BigInteger termNo;
    BigInteger seqNo;

    public TransferResult() {
    }

    public TransferResult(Boolean isSuccees, BigInteger termNo, BigInteger seqNo) {
        this.isSuccees = isSuccees;
        this.termNo = termNo;
        this.seqNo = seqNo;
    }

    @Override
    public String toString() {
        return "TransferResult{" +
                "isSuccees=" + isSuccees +
                ", termNo=" + termNo +
                ", seqNo=" + seqNo +
                '}';
    }
}
