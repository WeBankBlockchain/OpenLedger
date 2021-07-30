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

import org.apache.commons.lang3.StringUtils;

/**
 * Issue parameter o  builder
 *
 * @author pepperli@webank.com
 */
public final class IssueOptionBuilder {
    private String issuer;
    private String operator;
    private BigInteger amount;
    private BigInteger noteNoPrefix;
    private BigInteger noteNoSize;
    private Date effectiveDate;
    private Date expirationDate;
    private String desc;
    private String assetAddress;
    private IssueOptionBuilder() {
    }

    public static IssueOptionBuilder builder() {
        return new IssueOptionBuilder();
    }

    public IssueOptionBuilder withAssetAddress(String assetAddress) {
        this.assetAddress = assetAddress;
        return this;
    }
    public IssueOptionBuilder withIssuer(String issuer) {
        this.issuer = issuer;
        return this;
    }

    public IssueOptionBuilder withOperator(String operator) {
        this.operator = operator;
        return this;
    }

    public IssueOptionBuilder withAmount(BigInteger amount) {
        this.amount = amount;
        return this;
    }

    public IssueOptionBuilder withNoteNoPrefix(BigInteger noteNoPrefix) {
        this.noteNoPrefix = noteNoPrefix;
        return this;
    }

    public IssueOptionBuilder withNoteNoSize(BigInteger noteNoSize) {
        this.noteNoSize = noteNoSize;
        return this;
    }

    public IssueOptionBuilder withEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
        return this;
    }

    public IssueOptionBuilder withExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
        return this;
    }

    public IssueOptionBuilder withDesc(String desc) {
        this.desc = desc;
        return this;
    }

    public IssueOption build() throws Exception {
        IssueOption issueOption = new IssueOption();
        if (StringUtils.isBlank(issuer) || StringUtils.isBlank(operator) || amount == null || noteNoPrefix == null || noteNoSize == null || expirationDate == null || StringUtils.isBlank(desc)) {
            throw new Exception("issuer,operator,amount,noteNoPrefix,noteNoSize,expirationDate and desc can not be null");
        }
        issueOption.setIssuer(issuer);
        issueOption.setOperator(operator);
        issueOption.setAmount(amount);
        issueOption.setNoteNoPrefix(noteNoPrefix);
        issueOption.setNoteNoSize(noteNoSize);
        issueOption.setEffectiveDate(effectiveDate);
        issueOption.setExpirationDate(expirationDate);
        issueOption.setDesc(desc);
        issueOption.setAssetAddress(assetAddress);
        return issueOption;
    }
}
