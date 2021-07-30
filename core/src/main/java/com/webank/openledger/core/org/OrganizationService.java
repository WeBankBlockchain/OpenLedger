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

package com.webank.openledger.core.org;

import java.util.List;

import com.webank.openledger.contracts.Organization;
import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.asset.BaseCustodyService;
import com.webank.openledger.core.common.BaseHolder;
import com.webank.openledger.core.response.DataToolUtils;
import com.webank.openledger.core.response.ResponseData;
import com.webank.openledger.utils.OpenLedgerUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;

/**
 * organzation service
 *
 *
 */
@Slf4j
@Getter
@Setter
public class OrganizationService extends BaseCustodyService<Organization> {
    /**
     * blockchain property
     */
    protected Blockchain blockchain;
    /**
     * contract object
     */
    protected Organization contractIns;

    private BaseHolder accountHolder;

    /**
     * initializes the contract object
     *
     * @param blockchain blockchain property
     * @param contractAddress organzation contract address
     */
    public OrganizationService(Blockchain blockchain, String contractAddress) {
        super(blockchain, contractAddress,Organization.class);
        contractIns = blockchain.getLedger(Blockchain.DEFAULT_LEDGERID).getContract(contractAddress, Organization.class);


    }


    /**
     * initializes the contract object
     *
     * @param blockchain blockchain property
     * @param contractAddress organzation contract address
     */
    public OrganizationService(Blockchain blockchain, String contractAddress,String assetAddress) {
        super(blockchain, contractAddress,Organization.class);
        accountHolder = blockchain.getLedger(Blockchain.DEFAULT_LEDGERID).getContract(contractAddress, assetAddress,Organization.class);

    }
    /**
     * get contract object
     *
     * @return
     */
    public Organization getContractIns() {
        return contractIns == null ? null :  contractIns;
    }


    /**
     * create account of organzation
     * @param externalAccount account external address
     * @param keyList account property keys
     * @param valueList account property values
     * @param message args hash
     * @param rs sign by org hash
     * @return account innner address
     */
    public ResponseData<String> createAccount(String externalAccount, List<byte[]> keyList, List<byte[]> valueList, byte[] message, ECDSASignatureResult rs) {
        TransactionReceipt transactionReceipt = contractIns.createAccount(externalAccount, keyList, valueList, OpenLedgerUtils.convertSignToByte(message, rs));
        if (!transactionReceipt.isStatusOK()) {
            return DataToolUtils.handleTransaction(transactionReceipt, "");
        }

        Tuple2<Boolean, String> ret = contractIns.getCreateAccountOutput(transactionReceipt);
        if (!ret.getValue1()) {
            return DataToolUtils.handleTransaction(transactionReceipt, "");
        }

        ResponseData<String> responseData = DataToolUtils.handleTransaction(transactionReceipt, ret.getValue2());
        return responseData;
    }

    /**
     * cancel account from organzation
     * @param externalAccount account external address
     * @param message args hash
     * @param rs sign by orgAdmin
     * @return boolean is cancel successfully
     */
    public ResponseData<Boolean> cancel(String externalAccount, byte[] message, ECDSASignatureResult rs) {
        TransactionReceipt transactionReceipt = contractIns.cancel(externalAccount, OpenLedgerUtils.convertSignToByte(message, rs));
        Boolean result = transactionReceipt.isStatusOK() ? contractIns.getCancelOutput(transactionReceipt).getValue1() : false;
        ResponseData<Boolean> responseData = DataToolUtils.handleTransaction(transactionReceipt, result);
        return responseData;
    }

    /**
     * freeze account of organzation
     * account status will be forzen,can not do anyting
     * @param externalAccount  account external address
     * @param message args has
     * @param rs sign by orgAdmin
     * @return boolean is freeze successfully
     */
    public ResponseData<Boolean> freeze(String externalAccount, byte[] message, ECDSASignatureResult rs) {
        TransactionReceipt transactionReceipt = contractIns.freeze(externalAccount, OpenLedgerUtils.convertSignToByte(message, rs));
        Boolean result = transactionReceipt.isStatusOK() ? contractIns.getFreezeOutput(transactionReceipt).getValue1() : false;
        ResponseData<Boolean> responseData = DataToolUtils.handleTransaction(transactionReceipt, result);
        return responseData;
    }
    /**
     * unfreeze account of organzation
     * account status change to normal
     * @param externalAccount  account external address
     * @param message args has
     * @param rs sign by orgAdmin
     * @return boolean is unfreeze successfully
     */
    public ResponseData<Boolean> unfreeze(String externalAccount, byte[] message, ECDSASignatureResult rs) {
        TransactionReceipt transactionReceipt = contractIns.unfreeze(externalAccount, OpenLedgerUtils.convertSignToByte(message, rs));
        Boolean result = transactionReceipt.isStatusOK() ? contractIns.getUnfreezeOutput(transactionReceipt).getValue1() : false;
        ResponseData<Boolean> responseData = DataToolUtils.handleTransaction(transactionReceipt, result);
        return responseData;
    }

//    /**
//     * change account external address
//     * @param oldAccount current account address
//     * @param newAccount new account address
//     * @param message args hash
//     * @param rs sign by orgAdmin
//     * @return
//     */
//    public ResponseData<Boolean> changeExternalAccount(String oldAccount, String newAccount, byte[] message, ECDSASignatureResult rs) {
//        TransactionReceipt transactionReceipt = contractIns.chan(oldAccount, newAccount, OpenLedgerUtils.convertSignToByte(message, rs));
//        Boolean result = transactionReceipt.isStatusOK() ? contractIns.getChangeExternalAccountWithSignOutput(transactionReceipt).getValue1() : false;
//        ResponseData<Boolean> responseData = DataToolUtils.handleTransaction(transactionReceipt, result);
//        return responseData;
//    }

    /**
     * add admin to organzation
     * reuqire admin's account has been create
     * @param externalAccount admin external address
     * @param message args hash
     * @param rs sign by orgadmin
     * @return
     */
    public ResponseData<Boolean> addAdmin(String externalAccount,  byte[] message, ECDSASignatureResult rs) throws ContractException {
        TransactionReceipt transactionReceipt = contractIns.registerAdmin(externalAccount, OpenLedgerUtils.convertSignToByte(message, rs));
        Boolean result = transactionReceipt.isStatusOK() ? contractIns.getRegisterAdminOutput(transactionReceipt).getValue1() : false;
        ResponseData<Boolean> responseData = DataToolUtils.handleTransaction(transactionReceipt, result);
        return responseData;
    }


    /**
     * remove admin from organzation
     * reuqire admin's account has been add as a admin
     * TODO Whether the  account exists is determined by the administrator
     * @param externalAccount admin external address
     * @param message args hash
     * @param rs sign by orgadmin
     * @return
     */
    public ResponseData<Boolean> removeAdmin(String externalAccount, byte[] message, ECDSASignatureResult rs) {
        TransactionReceipt transactionReceipt = contractIns.unregisterAdmin(externalAccount, OpenLedgerUtils.convertSignToByte(message, rs));
        Boolean result = transactionReceipt.isStatusOK() ? contractIns.getUnregisterAdminOutput(transactionReceipt).getValue1() : false;
        ResponseData<Boolean> responseData = DataToolUtils.handleTransaction(transactionReceipt, result);
        return responseData;
    }


    /**
     * add admin to organzation
     * reuqire admin's account has been create
     * @param externalAccount admin external address
     * @param message args hash
     * @param rs sign by orgadmin
     * @return
     */
    public ResponseData<Boolean> addMember(String externalAccount,  byte[] message, ECDSASignatureResult rs) {
        TransactionReceipt transactionReceipt = contractIns.registerMember(externalAccount, OpenLedgerUtils.convertSignToByte(message, rs));
        Boolean result = transactionReceipt.isStatusOK() ? contractIns.getRegisterMemberOutput(transactionReceipt).getValue1() : false;
        ResponseData<Boolean> responseData = DataToolUtils.handleTransaction(transactionReceipt, result);
        return responseData;
    }


    /**
     * remove admin from organzation
     * reuqire admin's account has been add as a admin
     * TODO Whether the  account exists is determined by the administrator
     * @param externalAccount admin external address
     * @param message args hash
     * @param rs sign by orgadmin
     * @return
     */
    public ResponseData<Boolean> removeMember(String externalAccount, byte[] message, ECDSASignatureResult rs) {
        TransactionReceipt transactionReceipt = contractIns.unregisterMember(externalAccount, OpenLedgerUtils.convertSignToByte(message, rs));
        Boolean result = transactionReceipt.isStatusOK() ? contractIns.getUnregisterMemberOutput(transactionReceipt).getValue1() : false;
        ResponseData<Boolean> responseData = DataToolUtils.handleTransaction(transactionReceipt, result);
        return responseData;
    }





}
