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

/**
 * NonFungibleAsset Record builder
 *
 * @author pepperli@webank.com
 */
public final class NonFungibleAssetRecordBuilder {
    private BigInteger termNo;
    private BigInteger seq;
    private String from;
    private String to;
    private BigInteger noteNo;
    private String asset;
    private String operator;
    private String desc;

    private NonFungibleAssetRecordBuilder() {
    }

    public static NonFungibleAssetRecordBuilder aNonHomoAssetRecord() {
        return new NonFungibleAssetRecordBuilder();
    }

    public NonFungibleAssetRecordBuilder withTermNo(BigInteger termNo) {
        this.termNo = termNo;
        return this;
    }

    public NonFungibleAssetRecordBuilder withSeq(BigInteger seq) {
        this.seq = seq;
        return this;
    }

    public NonFungibleAssetRecordBuilder withFrom(String from) {
        this.from = from;
        return this;
    }

    public NonFungibleAssetRecordBuilder withTo(String to) {
        this.to = to;
        return this;
    }

    public NonFungibleAssetRecordBuilder withNoteId(BigInteger noteId) {
        this.noteNo = noteId;
        return this;
    }

    public NonFungibleAssetRecordBuilder withAsset(String asset) {
        this.asset = asset;
        return this;
    }

    public NonFungibleAssetRecordBuilder withOperator(String operator) {
        this.operator = operator;
        return this;
    }

    public NonFungibleAssetRecordBuilder withDesc(String desc) {
        this.desc = desc;
        return this;
    }

    public NonFungibleAssetRecord build() {
        NonFungibleAssetRecord nonFungibleAssetRecord = new NonFungibleAssetRecord();
        nonFungibleAssetRecord.setTermNo(termNo);
        nonFungibleAssetRecord.setSeq(seq);
        nonFungibleAssetRecord.setFrom(from);
        nonFungibleAssetRecord.setTo(to);
        nonFungibleAssetRecord.setNoteNo(noteNo);
        nonFungibleAssetRecord.setAsset(asset);
        nonFungibleAssetRecord.setOperator(operator);
        nonFungibleAssetRecord.setDesc(desc);
        return nonFungibleAssetRecord;
    }

    public static NonFungibleAssetRecordBuilder buildRecordEntity() {
        return new NonFungibleAssetRecordBuilder();
    }

}
