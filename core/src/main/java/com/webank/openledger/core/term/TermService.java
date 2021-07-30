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

import com.webank.openledger.contractsbak.Term;
import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.constant.ErrorCode;
import com.webank.openledger.core.exception.OpenLedgerBaseException;
import com.webank.openledger.core.response.DataToolUtils;
import com.webank.openledger.core.response.ResponseData;
import com.webank.openledger.core.term.entity.TermEntity;
import com.webank.openledger.utils.OpenLedgerUtils;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.model.TransactionReceipt;

/**
 * term service
 * @author pepperli
 */
@Slf4j
@Getter
public class TermService {
    /**
     * blockchain propertgy
     */
    private Blockchain blockchain;
    /**
     * term's contract object
     */
    private Term term;

    /**
     *
     * @param blockchain
     * @param contractAddress
     */
    public TermService(Blockchain blockchain, String contractAddress) {
        this.blockchain = blockchain;
        term = blockchain.getLedger(Blockchain.DEFAULT_LEDGERID).getContract(contractAddress, Term.class);
    }


    public String getAddress() throws OpenLedgerBaseException {
        try {
            return term.getAddress();
        } catch (Exception e) {
            log.error("getAsset failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.TERM_UNKNOW_ERROR);
        }

    }

    public TermEntity getTermInfo() throws OpenLedgerBaseException {
        try {
            Tuple2<BigInteger, String> result = term.getTerm();
            TermEntity termEntity = new TermEntity(result.getValue1().intValue(), result.getValue2());
            return termEntity;
        } catch (Exception e) {
            throw new OpenLedgerBaseException(ErrorCode.TERM_UNKNOW_ERROR);
        }

    }

    public ResponseData<BigInteger> newTerm(@NonNull String termName, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            TransactionReceipt transactionReceipt = term.newTerm(termName, OpenLedgerUtils.convertSignToByte(message, rs));
            BigInteger curTermNo = transactionReceipt.isStatusOK()? term.getNewTermOutput(transactionReceipt).getValue1() : BigInteger.valueOf(-1);
            return DataToolUtils.handleTransaction(transactionReceipt, curTermNo);
        } catch (Exception e) {
            log.error("newTerm failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.TERM_UNKNOW_ERROR);
        }
    }


}
