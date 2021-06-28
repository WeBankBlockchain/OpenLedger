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

/**
 * RecordEntity builder
 * @author pepperli
 */
public final class RecordBuilder {
//    private BigInteger bookNum;
    private BigInteger termNo;
    private BigInteger seq;
    private String from;
    private String to;
    private BigInteger amount;
    private String asset;
    private String operator;
    private String desc;
    private BigInteger transactionType;
    private BigInteger operationType;
    private String subject;
    private String relateAsset;

    private RecordBuilder() {
    }

    public static RecordBuilder buildRecordEntity() {
        return new RecordBuilder();
    }



    public RecordBuilder withTermNo(BigInteger termNo) {
        this.termNo = termNo;
        return this;
    }

    public RecordBuilder withSeq(BigInteger seq) {
        this.seq = seq;
        return this;
    }

    public RecordBuilder withFrom(String from) {
        this.from = from;
        return this;
    }

    public RecordBuilder withTo(String to) {
        this.to = to;
        return this;
    }

    public RecordBuilder withAmount(BigInteger amount) {
        this.amount = amount;
        return this;
    }

    public RecordBuilder withAsset(String asset) {
        this.asset = asset;
        return this;
    }

    public RecordBuilder withOperator(String operator) {
        this.operator = operator;
        return this;
    }

    public RecordBuilder withDesc(String desc) {
        this.desc = desc;
        return this;
    }

    public RecordBuilder withTransactionType(BigInteger transactionType) {
        this.transactionType = transactionType;
        return this;
    }

    public RecordBuilder withOperationType(BigInteger operationType) {
        this.operationType = operationType;
        return this;
    }

    public RecordBuilder withSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public RecordBuilder withRelateAsset(String relateAsset) {
        this.relateAsset = relateAsset;
        return this;
    }

    public RecordEntity build() {
        RecordEntity recordEntity = new RecordEntity();
//        recordEntity.setBookNum(bookNum);
        recordEntity.setTermNo(termNo);
        recordEntity.setSeq(seq);
        recordEntity.setFrom(from);
        recordEntity.setTo(to);
        recordEntity.setAmount(amount);
        recordEntity.setAsset(asset);
        recordEntity.setOperator(operator);
        recordEntity.setDesc(desc);
        recordEntity.setTransactionType(transactionType);
        recordEntity.setOperationType(operationType);
        recordEntity.setRelateAsset(relateAsset);
        recordEntity.setSubject(subject);
        return recordEntity;
    }
}
