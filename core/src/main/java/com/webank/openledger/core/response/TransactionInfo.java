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

package com.webank.openledger.core.response;

import lombok.Data;
import org.fisco.bcos.sdk.model.TransactionReceipt;

@Data
public class TransactionInfo {

    /**
     * The block number.
     */
    private String blockNumber;

    /**
     * The transaction hash value.
     */
    private String transactionHash;

    /**
     * The transaction index.
     */
    private String transactionIndex;

    private TransactionReceipt transactionReceipt;

    /**
     * Constructor from a transactionReceipt.
     *
     * @param receipt the transaction receipt
     */
    public TransactionInfo(TransactionReceipt receipt) {
        if (receipt != null) {
            this.blockNumber = receipt.getBlockNumber();
            this.transactionHash = receipt.getTransactionHash();
            this.transactionIndex = receipt.getTransactionIndex();
            this.transactionReceipt = receipt;
        }
    }


    /**
     * Constructor from a transactionReceipt.
     *
     * @param receipt the transaction receipt
     */
    /*
    public TransactionInfo(
            org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt receipt) {
        if (receipt != null) {
            this.blockNumber = receipt.getBlockNumber();
            this.transactionHash = receipt.getTransactionHash();
            this.transactionIndex = receipt.getTransactionIndex();
        }
    }
    */

    /**
     * Constructor.
     *
     * @param blockNumber blockNumber
     * @param transactionHash transactionHash
     * @param transactionIndex transactionIndex
     */
    public TransactionInfo(String blockNumber,
                           String transactionHash,
                           String transactionIndex) {
        this.blockNumber = blockNumber;
        this.transactionHash = transactionHash;
        this.transactionIndex = transactionIndex;
    }
}
