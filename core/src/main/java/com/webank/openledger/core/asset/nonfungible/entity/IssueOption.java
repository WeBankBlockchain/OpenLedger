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
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.abi.datatypes.Address;

/**
 * Issue parameter object
 *
 * @author pepperli@webank.com
 */
@Getter
@Setter
@ToString
public class IssueOption {

    /**
     * Issuer's address
     */
    private String issuer;
    /**
     * Operator's address
     */
    private String operator;
    /**
     * Issue amount
     */
    private BigInteger amount;
    /**
     * Asset noteNo prefix
     */
    private BigInteger noteNoPrefix;
    /**
     * Number of asset noteNo occupied
     */
    private BigInteger noteNoSize;
    /**
     * The effective date of the asset
     * Can be null, when the default assets are all inactive state need to call the EnableBatch interface to enable
     * If it is not null, the EnableBatch interface needs to be called to enable periodically, and it will not take effect until the effective time
     */
    private Date effectiveDate;
    /**
     * Asset Expiration Time Assets are not allowed to be traded after the expiration date
     */
    private Date expirationDate;
    /**
     * description
     */
    private String desc;

    private String assetAddress;

    public BigInteger getEffectiveDate() {
        if (effectiveDate != null) {
            return BigInteger.valueOf(effectiveDate.getTime());
        } else {
            return BigInteger.valueOf(0);
        }
    }

    public BigInteger getExpirationDate() {
        return BigInteger.valueOf(expirationDate.getTime());
    }

    /**
     * Build a list of Address types for issue and trade parameter construction
     *
     * @return
     * @throws Exception
     */
    public List<String> genAddressList() throws Exception {
        if (StringUtils.isBlank(issuer) || StringUtils.isBlank(operator) || amount == null || noteNoPrefix == null || noteNoSize == null || expirationDate == null || StringUtils.isBlank(desc)) {
            throw new Exception("issuer,operator,amount,noteNoPrefix,noteNoSize,expirationDate and desc can not be null");
        }
        List<String> transactionAddress = new ArrayList<>();
        transactionAddress.add(this.getOperator());
        transactionAddress.add(this.getAssetAddress());
        transactionAddress.add(Address.DEFAULT.getValue());
        transactionAddress.add(this.getIssuer());
        return transactionAddress;
    }

    /**
     * Build a list of UINT256 types for publication and trade parameter construction
     *
     * @param
     * @return
     * @throws Exception
     */
    public List<BigInteger> genBigIntegerList() throws Exception {
        if (StringUtils.isBlank(issuer) || StringUtils.isBlank(operator) || amount == null || noteNoPrefix == null || noteNoSize == null || expirationDate == null || StringUtils.isBlank(desc)) {
            throw new Exception("issuer,operator,amount,noteNoPrefix,noteNoSize,expirationDate and desc can not be null");
        }
        List<BigInteger> uint256Args = new ArrayList<>();
        uint256Args.add(this.getAmount());
        uint256Args.add(this.getNoteNoPrefix());
        uint256Args.add(this.getNoteNoSize());
        uint256Args.add(this.getEffectiveDate());
        uint256Args.add(this.getExpirationDate());
        return uint256Args;
    }

    /**
     * The build ustring type list is used to build the publish and trade parameters
     *
     * @param
     * @return
     * @throws Exception
     */
    public List<byte[]> genStringValueList() throws Exception {
        if (StringUtils.isBlank(issuer) || StringUtils.isBlank(operator) || amount == null || noteNoPrefix == null || noteNoSize == null || expirationDate == null || StringUtils.isBlank(desc)) {
            throw new Exception("issuer,operator,amount,noteNoPrefix,noteNoSize,expirationDate and desc can not be null");
        }
        List<byte[]> stringValList = new ArrayList<>();
        stringValList.add(this.getDesc().getBytes("utf-8"));
        return stringValList;
    }
}
