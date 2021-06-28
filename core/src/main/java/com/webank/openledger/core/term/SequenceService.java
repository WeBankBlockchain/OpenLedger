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

package com.webank.openledger.core.term;


import java.math.BigInteger;

import com.webank.openledger.contracts.Sequence;
import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.constant.ErrorCode;
import com.webank.openledger.core.exception.OpenLedgerBaseException;
import com.webank.openledger.core.response.DataToolUtils;
import com.webank.openledger.core.response.ResponseData;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.model.TransactionReceipt;

/**
 * SerialNumberGenerator
 * @author pepperli
 */
@Getter
@Slf4j
public class SequenceService {

    private Blockchain blockchain;
    private Sequence seq;

    public SequenceService(Blockchain blockchain, String contractAddress) {
        this.blockchain = blockchain;
        seq = blockchain.getLedger(Blockchain.DEFAULT_LEDGERID).getContract(contractAddress, Sequence.class);
    }


    /**
     * 获取新的序列号
     *
     * @return
     */
    public ResponseData<BigInteger> getSequence() throws OpenLedgerBaseException {
        try {
            TransactionReceipt transactionReceipt = seq.get();
            BigInteger seqNo = transactionReceipt.isStatusOK() ? seq.getGetOutput(transactionReceipt).getValue1() : BigInteger.valueOf(-1);
            return DataToolUtils.handleTransaction(transactionReceipt, seqNo);
        } catch (Exception e) {
            log.error("getSequence failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.SEQUENCE_UNKNOW_ERROR);
        }
    }

}
