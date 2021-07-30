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

package com.webank.openledger.core.asset.nonfungible;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.webank.openledger.contractsbak.NonFungibleAsset;
import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.asset.nonfungible.entity.IssueOption;
import com.webank.openledger.core.exception.OpenLedgerBaseException;
import com.webank.openledger.core.identity.IdentityService;
import com.webank.openledger.utils.OpenLedgerUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.abi.datatypes.Address;

/**
 * NonFungible asset services
 *
 * @author pepperli@webank.com
 */
@Slf4j
@Getter
public class NonFungibleAssetService {
    /**
     * blockchain property object
     */
    protected Blockchain blockchain;
    /**
     * asset contract object
     */
    private NonFungibleAsset asset;
    /**
     * contractAddress
     */
    private String contractAddress;
    /**
     * identity object
     * eg: NonFungibleAssetService.getIdentity()
     */
    private IdentityService<NonFungibleAsset> identity;

    /**
     * Initialize the contract object
     *
     * @param blockchain property object
     * @param contractAddress asset contractAddress
     */
    public NonFungibleAssetService(Blockchain blockchain, String contractAddress) {
        this.blockchain = blockchain;
        this.contractAddress = contractAddress;
        asset = blockchain.getLedger(Blockchain.DEFAULT_LEDGERID).getContract(contractAddress, NonFungibleAsset.class);
        identity = new IdentityService(this.getAsset());
    }



    public static byte[] computeIssueMsg(IssueOption issueOption, BigInteger nonce) throws Exception {
        byte[] result = new byte[0];
        List<String> transactionAddress = issueOption.genAddressList();
        List<BigInteger> uint256Args = issueOption.genBigIntegerList();
        List<byte[]> stringValList = issueOption.genStringValueList();
        for (String item : transactionAddress) {
            result = OpenLedgerUtils.concatByte(result, OpenLedgerUtils.convertStringToAddressByte(item));
        }
        for (BigInteger item : uint256Args) {
            result = OpenLedgerUtils.concatByte(result, OpenLedgerUtils.getBytes32(item.toByteArray()));
        }

        for (byte[] item : stringValList) {
            result = OpenLedgerUtils.concatByte(result, item);
        }

        result = OpenLedgerUtils.concatByte(result, OpenLedgerUtils.getBytes32(nonce.toByteArray()));

        return OpenLedgerUtils.computeKeccak256Hash(result);
    }

    public static byte[] computeTransferMsg(String contractAddress, String operator, String from, String to, List<BigInteger> notes, String desc, BigInteger nonce) throws OpenLedgerBaseException, UnsupportedEncodingException {
        byte[] result = new byte[0];
        List<String> transactionAddress = genAddress(from, to, operator, contractAddress);
        List<String> stringValList = new ArrayList<>();
        stringValList.add(desc);
        for (String item : transactionAddress) {
            result = OpenLedgerUtils.concatByte(result, OpenLedgerUtils.convertStringToAddressByte(item));
        }
        for (BigInteger item : notes) {
            result = OpenLedgerUtils.concatByte(result, OpenLedgerUtils.getBytes32(item.toByteArray()));
        }

        for (String item : stringValList) {
            result = OpenLedgerUtils.concatByte(result, item.getBytes("utf-8"));
        }

        result = OpenLedgerUtils.concatByte(result, OpenLedgerUtils.getBytes32(nonce.toByteArray()));

        return OpenLedgerUtils.computeKeccak256Hash(result);
    }

    public static List<String> genAddress(String fromAddress, String toAddress, String operatorAddress, String assetAddress) {
        List<String> addressList = new ArrayList<>();
        addressList.add(operatorAddress);
        addressList.add(assetAddress);
        if (StringUtils.isBlank(fromAddress)) {
            fromAddress = Address.DEFAULT.getValue();
        }
        addressList.add(fromAddress);
        if (StringUtils.isBlank(toAddress)) {
            toAddress = Address.DEFAULT.getValue();
        }
        addressList.add(toAddress);
        return addressList;
    }


}
