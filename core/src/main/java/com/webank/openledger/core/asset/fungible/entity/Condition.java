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
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * ledger query conditon entity
 * @author pepperli
 */
@Getter
@Setter
public class Condition {
    /**
     * DEFAULT address
     */
    protected static final String DEFALUT_ADDRESS = "0x0";
    /**
     * termNo of ledger
     */
    private BigInteger termNo;
    /**
     * serialNumber
     */
    private BigInteger seqNo;
    /**
     * account of payment
     */
    private String fromAddress;
    /**
     * account of recepit
     */
    private String toAddress;

    /**
     *  conditon limit start from zero
     */
    private List<BigInteger> limits;

    public Condition(BigInteger termNo, BigInteger seqNo, String fromAddress, String toAddress) {
        this.termNo = termNo;
        this.seqNo = seqNo;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        List<BigInteger> limits = new ArrayList<>();
        limits.add(BigInteger.valueOf(0));
        limits.add(BigInteger.valueOf(10));
        this.limits=limits;
    }

    public Condition(BigInteger termNo, BigInteger seqNo, String fromAddress, String toAddress,List<BigInteger> limits) {
        this.termNo = termNo;
        this.seqNo = seqNo;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        if(limits!=null){
            this.limits = limits;
        }else{
            limits = new ArrayList<>();
            limits.add(BigInteger.valueOf(0));
            limits.add(BigInteger.valueOf(10));
            this.limits=limits;
        }
    }

    /**
     * gen parameters
     *
     * @return
     */
    public List<BigInteger> getIntParams() {
        List<BigInteger> uintCondition = new ArrayList<>();
        uintCondition.add(this.getTermNo()==null?BigInteger.valueOf(0):this.getTermNo());
        uintCondition.add(this.getSeqNo()==null?BigInteger.valueOf(0):this.getSeqNo());
        return uintCondition;
    }


    /**
     * gen parameters
     *
     * @return
     */
    public List<String> getAddressParams() {
        List<String> addressCondition = new ArrayList<>();
        if (StringUtils.isNotBlank(this.getFromAddress())) {
            addressCondition.add(this.getFromAddress());
        }
        if (StringUtils.isNotBlank(this.getToAddress())) {
            if (StringUtils.isBlank(this.getFromAddress())) {
                addressCondition.add(DEFALUT_ADDRESS);
            }
            addressCondition.add(this.getToAddress());
        }
        return addressCondition;
    }
}
