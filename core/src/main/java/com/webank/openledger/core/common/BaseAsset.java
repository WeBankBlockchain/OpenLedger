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

package com.webank.openledger.core.common;

import java.math.BigInteger;
import java.util.List;

import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple1;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple5;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;

/**
 * asset interface
 * @author pepperli
 */
public interface BaseAsset {
    List getHolders(List<byte[]> sign) throws ContractException;

    TransactionReceipt addBook(List<byte[]> sign);

    void addBook(List<byte[]> sign, TransactionCallback callback);

    Tuple1<List<byte[]>> getAddBookInput(TransactionReceipt transactionReceipt);

    Tuple1<BigInteger> getAddBookOutput(TransactionReceipt transactionReceipt);

    TransactionReceipt deposit(List<String> transactionAddress, BigInteger amount, List<BigInteger> typeList, List<String> detailList, List<byte[]> sign);

    void deposit(List<String> transactionAddress, BigInteger amount, List<BigInteger> typeList, List<String> detailList, List<byte[]> sign, TransactionCallback callback);

    Tuple5<List<String>, BigInteger, List<BigInteger>, List<String>, List<byte[]>> getDepositInput(TransactionReceipt transactionReceipt);

    Tuple2<Boolean, List<BigInteger>> getDepositOutput(TransactionReceipt transactionReceipt);

    TransactionReceipt withdrawal(List<String> transactionAddress, BigInteger amount, List<BigInteger> typeList, List<String> detailList, List<byte[]> sign);

    void withdrawal(List<String> transactionAddress, BigInteger amount, List<BigInteger> typeList, List<String> detailList, List<byte[]> sign, TransactionCallback callback);

    Tuple5<List<String>, BigInteger, List<BigInteger>, List<String>, List<byte[]>> getWithdrawalInput(TransactionReceipt transactionReceipt);

    Tuple2<Boolean, List<BigInteger>> getWithdrawalOutput(TransactionReceipt transactionReceipt);

    TransactionReceipt transfer(List<String> transactionAddress, BigInteger amount, List<BigInteger> typeList, List<String> detailList, List<byte[]> sign);

    void transfer(List<String> transactionAddress, BigInteger amount, List<BigInteger> typeList, List<String> detailList, List<byte[]> sign, TransactionCallback callback);

    Tuple5<List<String>, BigInteger, List<BigInteger>, List<String>, List<byte[]>> getTransferInput(TransactionReceipt transactionReceipt);

    Tuple2<Boolean, List<BigInteger>> getTransferOutput(TransactionReceipt transactionReceipt);

    BigInteger getBalance(String account, List<byte[]> sign) throws ContractException;

    List queryBook(List<BigInteger> uint_condition, List<String> address_condtion,List<BigInteger> limit, List<byte[]> sign) throws ContractException;

    TransactionReceipt openAccount(String account, List<byte[]> sign);

    void openAccount(String account, List<byte[]> sign, TransactionCallback callback);

    Tuple2<String, List<byte[]>> getOpenAccountInput(TransactionReceipt transactionReceipt);

    Tuple1<Boolean> getOpenAccountOutput(TransactionReceipt transactionReceipt);

    public BigInteger getTotalBalance(List<byte[]> sign) throws ContractException;
}
