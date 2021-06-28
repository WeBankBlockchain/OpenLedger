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

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.webank.openledger.contracts.Organization;
import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.response.DataToolUtils;
import com.webank.openledger.core.response.ResponseData;
import com.webank.openledger.utils.OpenLedgerUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.contract.Contract;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.model.TransactionReceipt;

/**
 * organzation service
 *
 * @param <T> object extend contract
 */
@Slf4j
@Getter
@Setter
public class OrganizationService<T extends Contract> {
    /**
     * blockchain property
     */
    protected Blockchain blockchain;
    /**
     * contract object
     */
    protected Organization contractIns;

    /**
     * initializes the contract object
     *
     * @param blockchain blockchain property
     * @param contractAddress organzation contract address
     */
    public OrganizationService(Blockchain blockchain, String contractAddress) {
        this.blockchain = blockchain;
        contractIns = blockchain.getLedger(Blockchain.DEFAULT_LEDGERID).getContract(contractAddress, Organization.class);
    }

    /**
     * get contract object
     *
     * @return
     */
    public T getContractIns() {
        return contractIns == null ? null : (T) contractIns;
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
        TransactionReceipt transactionReceipt = contractIns.createAccountWithSign(externalAccount, keyList, valueList, OpenLedgerUtils.convertSignToByte(message, rs));
        if (!transactionReceipt.isStatusOK()) {
            return DataToolUtils.handleTransaction(transactionReceipt, "");
        }

        Tuple2<Boolean, String> ret = contractIns.getCreateAccountWithSignOutput(transactionReceipt);
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
        TransactionReceipt transactionReceipt = contractIns.cancelWithSign(externalAccount, OpenLedgerUtils.convertSignToByte(message, rs));
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
        TransactionReceipt transactionReceipt = contractIns.freezeWithSign(externalAccount, OpenLedgerUtils.convertSignToByte(message, rs));
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
        TransactionReceipt transactionReceipt = contractIns.unfreezeWithSign(externalAccount, OpenLedgerUtils.convertSignToByte(message, rs));
        Boolean result = transactionReceipt.isStatusOK() ? contractIns.getUnfreezeOutput(transactionReceipt).getValue1() : false;
        ResponseData<Boolean> responseData = DataToolUtils.handleTransaction(transactionReceipt, result);
        return responseData;
    }

    /**
     * change account external address
     * @param oldAccount current account address
     * @param newAccount new account address
     * @param message args hash
     * @param rs sign by orgAdmin
     * @return
     */
    public ResponseData<Boolean> changeExternalAccount(String oldAccount, String newAccount, byte[] message, ECDSASignatureResult rs) {
        TransactionReceipt transactionReceipt = contractIns.changeExternalAccountWithSign(oldAccount, newAccount, OpenLedgerUtils.convertSignToByte(message, rs));
        Boolean result = transactionReceipt.isStatusOK() ? contractIns.getChangeExternalAccountWithSignOutput(transactionReceipt).getValue1() : false;
        ResponseData<Boolean> responseData = DataToolUtils.handleTransaction(transactionReceipt, result);
        return responseData;
    }

    /**
     * add admin to organzation
     * reuqire admin's account has been create
     * @param externalAccount admin external address
     * @param role account role value
     * @param message args hash
     * @param rs sign by orgadmin
     * @return
     */
    public ResponseData<Boolean> addAdmin(String externalAccount, String role, byte[] message, ECDSASignatureResult rs) {
        TransactionReceipt transactionReceipt = contractIns.addAdminWithSign(externalAccount, role.getBytes(StandardCharsets.UTF_8), OpenLedgerUtils.convertSignToByte(message, rs));
        Boolean result = transactionReceipt.isStatusOK() ? contractIns.getAddAdminWithSignOutput(transactionReceipt).getValue1() : false;
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
        TransactionReceipt transactionReceipt = contractIns.removeAdminWithSign(externalAccount, OpenLedgerUtils.convertSignToByte(message, rs));
        Boolean result = transactionReceipt.isStatusOK() ? contractIns.getRemoveAdminWithSignOutput(transactionReceipt).getValue1() : false;
        ResponseData<Boolean> responseData = DataToolUtils.handleTransaction(transactionReceipt, result);
        return responseData;
    }

    /**
     * create asset of organzation
     * @param externalAccount operator account address
     * @param assetName asset's name custom defined
     * @param isFungible boolean is fungible
     * @param message args hash
     * @param rs sign by orgAdmin
     * @return asset address
     */
    public ResponseData<String> createAsset(String externalAccount, String assetName, Boolean isFungible, byte[] message, ECDSASignatureResult rs) {
        TransactionReceipt transactionReceipt = contractIns.createAssetWithSign(externalAccount, assetName, isFungible, OpenLedgerUtils.convertSignToByte(message, rs));
        String result = transactionReceipt.isStatusOK() ? contractIns.getCreateAssetWithSignOutput(transactionReceipt).getValue1() : null;
        ResponseData<String> responseData = DataToolUtils.handleTransaction(transactionReceipt, result);
        return responseData;
    }

    /**
     * create currency of project
     *
     * @param name currency's name custom defined
     * @param symbol currency's symbol custom defined
     * @param decimals currency's decimals custom defined
     * @return currency asset address
     */
    public ResponseData<String> createCurrency(String name, String symbol, BigInteger decimals) {
        TransactionReceipt transactionReceipt = contractIns.createCurrency(name, symbol, decimals);
        String result = transactionReceipt.isStatusOK() ? contractIns.getCreateCurrencyOutput(transactionReceipt).getValue1() : null;
        ResponseData<String> responseData = DataToolUtils.handleTransaction(transactionReceipt, result);
        return responseData;
    }

}
