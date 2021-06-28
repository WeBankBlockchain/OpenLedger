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
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple3;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;

/**
 * identity interface
 * @author pepperli
 */
public interface BaseIdentity {
    TransactionReceipt insert(byte[] key, byte[] value);

    void insert(byte[] key, byte[] value, TransactionCallback callback);

    Tuple2<byte[], byte[]> getInsertInput(TransactionReceipt transactionReceipt);

    Tuple1<Boolean> getInsertOutput(TransactionReceipt transactionReceipt);

    TransactionReceipt add(byte[] key, byte[] value);

    void add(byte[] key, byte[] value, TransactionCallback callback);

    Tuple2<byte[], byte[]> getAddInput(TransactionReceipt transactionReceipt);

    Tuple1<Boolean> getAddOutput(TransactionReceipt transactionReceipt);

    TransactionReceipt remove(byte[] key);

    void remove(byte[] key, TransactionCallback callback);

    Tuple1<byte[]> getRemoveInput(TransactionReceipt transactionReceipt);

    Tuple1<Boolean> getRemoveOutput(TransactionReceipt transactionReceipt);

    TransactionReceipt removeWithSign(byte[] key, List<byte[]> sign);

    void removeWithSign(byte[] key, List<byte[]> sign, TransactionCallback callback);

    Tuple2<byte[], List<byte[]>> getRemoveWithSignInput(TransactionReceipt transactionReceipt);

    Tuple1<Boolean> getRemoveWithSignOutput(TransactionReceipt transactionReceipt);

    byte[] getWithSign(byte[] key, List<byte[]> sign) throws ContractException;

    TransactionReceipt set(byte[] key, byte[] value);

    void set(byte[] key, byte[] value, TransactionCallback callback);

    Tuple2<byte[], byte[]> getSetInput(TransactionReceipt transactionReceipt);

    Tuple1<Boolean> getSetOutput(TransactionReceipt transactionReceipt);

    TransactionReceipt addWithSign(byte[] key, byte[] value, List<byte[]> sign);

    void addWithSign(byte[] key, byte[] value, List<byte[]> sign, TransactionCallback callback);

    Tuple3<byte[], byte[], List<byte[]>> getAddWithSignInput(TransactionReceipt transactionReceipt);

    Tuple1<Boolean> getAddWithSignOutput(TransactionReceipt transactionReceipt);

    TransactionReceipt insertWithSign(byte[] key, byte[] value, List<byte[]> sign);

    void insertWithSign(byte[] key, byte[] value, List<byte[]> sign, TransactionCallback callback);

    Tuple3<byte[], byte[], List<byte[]>> getInsertWithSignInput(TransactionReceipt transactionReceipt);

    Tuple1<Boolean> getInsertWithSignOutput(TransactionReceipt transactionReceipt);

    BigInteger getNonce() throws ContractException;

    byte[] get(byte[] key) throws ContractException;

    TransactionReceipt setWithSign(byte[] key, byte[] value, List<byte[]> sign);

    void setWithSign(byte[] key, byte[] value, List<byte[]> sign, TransactionCallback callback);

    Tuple3<byte[], byte[], List<byte[]>> getSetWithSignInput(TransactionReceipt transactionReceipt);

    Tuple1<Boolean> getSetWithSignOutput(TransactionReceipt transactionReceipt);

    public BigInteger size() throws ContractException;


}
