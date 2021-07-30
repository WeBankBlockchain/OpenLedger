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

package com.webank.openledger.core.asset.fungible;


import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.exception.OpenLedgerBaseException;
import com.webank.openledger.utils.OpenLedgerUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.abi.datatypes.Address;
import org.fisco.bcos.sdk.contract.Contract;

/**
 * standard fungible asset service
 *
 * @author pepperli
 */
@Slf4j
@Getter
public class StandardAssetService<T extends Contract> {
    /**
     * blockchain property object
     */
    protected Blockchain blockchain;
     /**
     * asset contractAddress
     */
    protected String contractAddress;

    /**
     * asset contractAddress
     */
    protected String accountHolderContractAddress;

    /**
     * Initialize the contract object
     *
     * @param blockchain property object
     * @param contractAddress asset contractAddress
     */
    public StandardAssetService(Blockchain blockchain, String contractAddress, String holderContractAddress,Class holderClass) {
        this.blockchain = blockchain;
        this.contractAddress = contractAddress;
        this.accountHolderContractAddress=holderContractAddress;
    }

    /***
     * generate transaction detail list
     * @param desc description
     * @param subject accounting Subject can be null
     * @return
     */
    public static List<byte[]> genDetail(String desc, String subject) throws UnsupportedEncodingException {
        List<byte[]> details = new ArrayList<>();
        details.add(desc.getBytes("utf-8"));
        if (StringUtils.isNotBlank(subject)) {
            details.add(subject.getBytes("utf-8"));
        }
        return details;
    }

    /**
     * generate transaction type list
     *
     * @param operationType operation type custom
     * @return
     */
    public static List<BigInteger> genType(int operationType) {
        List<BigInteger> types = new ArrayList<>();
        types.add(BigInteger.valueOf(operationType));
        return types;
    }

    /**
     * generate transaction address list
     *
     * @param fromAddress transfer out of account
     * @param toAddress transfer account
     * @param operatorAddress operator account address
     * @param assetAddress asset contract address
     * @param relateAsset related asset contract address (can be null)
     * @return transaction address list
     */
    public static List<String> genAddress(String fromAddress, String toAddress, String operatorAddress, String assetAddress, String relateAsset) {
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
        if (StringUtils.isBlank(relateAsset)) {
            relateAsset = Address.DEFAULT.getValue();
        }
        addressList.add(relateAsset);
        return addressList;
    }


    /**
     * compute transaction args hash
     *
     * @param transactionAddress transaction address list generate by call 'genAddress'
     * @param amount transaction amount
     * @param typeList transaction type list generate by call 'genType'
     * @param detailList transaction detail list generate by call 'genDetail'
     * @param nonce account nonce value
     * @return transaction args hash
     * @throws OpenLedgerBaseException
     * @throws UnsupportedEncodingException
     */
    public static byte[] computeTxMsg(List<String> transactionAddress, BigInteger amount, List<BigInteger> typeList, List<byte[]> detailList, BigInteger nonce) throws OpenLedgerBaseException, UnsupportedEncodingException {
        byte[] result = new byte[0];
        for (String item : transactionAddress) {
            result = OpenLedgerUtils.concatByte(result, OpenLedgerUtils.convertStringToAddressByte(item));
        }
        result = OpenLedgerUtils.concatByte(result, OpenLedgerUtils.getBytes32(amount.toByteArray()));
        for (BigInteger item : typeList) {
            result = OpenLedgerUtils.concatByte(result, OpenLedgerUtils.getBytes32(item.toByteArray()));
        }
        for (byte[] item : detailList) {
            result = OpenLedgerUtils.concatByte(result, item);
        }

        result = OpenLedgerUtils.concatByte(result, OpenLedgerUtils.getBytes32(nonce.toByteArray()));

        return OpenLedgerUtils.computeKeccak256Hash(result);
    }

    /**
     * compute openAccount args hash
     *
     * @param account call account address
     * @param nonce account nonce value
     * @return openAccount function args hash
     * @throws OpenLedgerBaseException
     */
    public static byte[] computeOpenAccountMsg(String account, BigInteger nonce) throws OpenLedgerBaseException {
        byte[] result = new byte[0];
        result = OpenLedgerUtils.concatByte(result, OpenLedgerUtils.convertStringToAddressByte(account));
        result = OpenLedgerUtils.concatByte(result, OpenLedgerUtils.getBytes32(nonce.toByteArray()));

        return OpenLedgerUtils.computeKeccak256Hash(result);
    }




}
