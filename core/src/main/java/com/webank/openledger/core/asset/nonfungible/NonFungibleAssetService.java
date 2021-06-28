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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.webank.openledger.contracts.NonFungibleAsset;
import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.asset.nonfungible.entity.IssueNoteResult;
import com.webank.openledger.core.asset.nonfungible.entity.IssueOption;
import com.webank.openledger.core.asset.nonfungible.entity.NonFungibleCondition;
import com.webank.openledger.core.asset.nonfungible.entity.NonFungibleAssetRecord;
import com.webank.openledger.core.asset.nonfungible.entity.NonFungibleAssetRecordBuilder;
import com.webank.openledger.core.asset.nonfungible.entity.Note;
import com.webank.openledger.core.asset.nonfungible.entity.TransferNoteResult;
import com.webank.openledger.core.common.ValueModel;
import com.webank.openledger.core.constant.ErrorCode;
import com.webank.openledger.core.exception.OpenLedgerBaseException;
import com.webank.openledger.core.identity.IdentityService;
import com.webank.openledger.core.response.DataToolUtils;
import com.webank.openledger.core.response.ResponseData;
import com.webank.openledger.utils.JsonHelper;
import com.webank.openledger.utils.OpenLedgerUtils;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.abi.datatypes.Address;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple3;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple4;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;

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

    /**
     * Set the price permission to support only the admin of org
     *
     * @param price
     * @param message hash result
     * @param rs Signature object
     * @return current price
     * @throws OpenLedgerBaseException
     */
    public ResponseData<BigInteger> setPrice(@NonNull BigInteger price, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
            TransactionReceipt transactionReceipt = this.getAsset().setPrice(price, resultSign);
            BigInteger curPrice = transactionReceipt.isStatusOK() ? this.getAsset().getSetPriceOutput(transactionReceipt).getValue1() : BigInteger.valueOf(-1);
            ResponseData<BigInteger> responseData = DataToolUtils.handleTransaction(transactionReceipt, curPrice);
            return responseData;
        } catch (Exception e) {
            log.error("setPrice failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.ASSET_UNKNOW_ERROR);
        }
    }

    /**
     * to open an account
     * The account to issue or transfer must first call the account opening interface
     *
     * @param account address
     * @param message hash result
     * @param rs Signature object
     * @return Whether the  operation was successful
     * @throws OpenLedgerBaseException
     */
    public ResponseData<Boolean> openAccount(@NonNull String account, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
            TransactionReceipt transactionReceipt = asset.openAccount(account, resultSign);
            Boolean result = transactionReceipt.isStatusOK() ? asset.getOpenAccountOutput(transactionReceipt).getValue1() : false;
            return DataToolUtils.handleTransaction(transactionReceipt, result);
        } catch (Exception e) {
            log.error("openAccount failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.UNKNOW_ERROR);
        }

    }


    /**
     * Issued assets
     *
     * @param issueOption issue parameter object
     * @param message hash is generated by calling ComputeIssuemsg
     * @param rs Signature object
     * @return Issuing Assets List
     * @throws OpenLedgerBaseException
     */
    public ResponseData<List<IssueNoteResult>> issue(IssueOption issueOption, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {

        List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
        TransactionReceipt transactionReceipt;
        try {
            if (StrictMath.pow(issueOption.getAmount().doubleValue(), 1d / 10) + 1 > issueOption.getNoteNoSize().intValue()) {
                throw new Exception("noteNoSize  is less than " + StrictMath
                        .round(StrictMath.pow(issueOption.getAmount().doubleValue(), 1d / 10) + 1));
            }
            transactionReceipt = asset.issue(issueOption.genAddressList(contractAddress), issueOption.genBigIntegerList(), issueOption.genStringValueList(), resultSign);
            Tuple3<BigInteger, List<BigInteger>, List<BigInteger>> response = transactionReceipt.isStatusOK() ? asset.getIssueOutput(transactionReceipt) : null;
            BigInteger batchNo = response == null ? null : response.getValue1();
            List<BigInteger> noteNoList = response == null ? null : response.getValue2();
            List<BigInteger> termNoAndSeqNo = response == null ? null : response.getValue3();
            List<IssueNoteResult> notes = new ArrayList<>(noteNoList == null ? 0 : noteNoList.size());
            AtomicInteger index = new AtomicInteger(0);
            if (noteNoList != null) {
                noteNoList.stream().forEach(item -> {
                    IssueNoteResult nr = new IssueNoteResult();
                    nr.setNoteNo(item);
                    nr.setBatchNo(batchNo);
                    nr.setTermNo(termNoAndSeqNo.get(2 * index.get()));
                    nr.setSeqNo(termNoAndSeqNo.get(2 * index.getAndIncrement() + 1));
                    notes.add(nr);
                });
            }
            return DataToolUtils.handleTransaction(transactionReceipt, notes);
        } catch (Exception e) {
            log.error("issue failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.UNKNOW_ERROR);
        }


    }

    /**
     * Get asset details based on the asset noteNo
     *
     * @param noteNo Asset Number
     * @param message hash
     * @param rs Signature object
     * @return asset object
     * @throws OpenLedgerBaseException
     */
    public Note getNoteDetail(BigInteger noteNo, String account, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
        try {
            Tuple4<List<String>, List<BigInteger>, List<BigInteger>, BigInteger> response = asset.getNoteDetail(noteNo, account, resultSign);
            List<String> addressList = response.getValue1();
            List<BigInteger> uint256List = response.getValue2();
            List<BigInteger> uintList = response.getValue3();
            BigInteger status = response.getValue4();

            Note note = new Note();
            note.setOwner(addressList.get(0));
            note.setIssuer(addressList.get(1));
            note.setNoteNo(noteNo);
            note.setBatchNo(uint256List.get(1));
            note.setEffectiveDate(uintList.get(0).intValue() == 0 ? null : new Date(uintList.get(0).longValue()));
            note.setExpirationDate(new Date(uintList.get(1).longValue()));
            note.setStatus(status);
            return note;
        } catch (ContractException e) {
            log.error("getNoteDetail failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.UNKNOW_ERROR);
        }
    }

    /**
     * Asset transactions
     *
     * @param operator account address
     * @param from spool out address
     * @param to to address
     * @param notes list of asset noteNo
     * @param desc description
     * @param message hash result can be generated by calling ComputeTransferMsg
     * @param rs Signature object
     * @return transaction results
     * @throws OpenLedgerBaseException
     */
    public ResponseData<List<TransferNoteResult>> transfer(String operator, String from, String to, List<BigInteger> notes, String desc, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
        List<String> transactionAddress = new ArrayList<>();
        transactionAddress.add(operator);
        transactionAddress.add(contractAddress);
        transactionAddress.add(from);
        transactionAddress.add(to);

        List<String> stringValList = new ArrayList<>();
        stringValList.add(desc);

        TransactionReceipt transactionReceipt;
        try {
            transactionReceipt = asset.transfer(transactionAddress, notes, stringValList, resultSign);
            Tuple2<Boolean, List<BigInteger>> response = transactionReceipt.isStatusOK() ? asset.getTransferOutput(transactionReceipt) : null;

            List<TransferNoteResult> transferNoteResults = new ArrayList<>();

            if (response != null) {
                TransferNoteResult transferNoteResult = null;
                // 组装交易结果返回
                List<BigInteger> results = response.getValue2();
                for (int i = 0; i < notes.size(); i++) {
                    transferNoteResult = new TransferNoteResult();
                    transferNoteResult.setResult(response.getValue1());
                    transferNoteResult.setTermNo(results.get(3 * i));
                    transferNoteResult.setSeqNo(results.get(3 * i + 1));
                    transferNoteResult.setNoteNo(results.get(3 * i + 2));
                    transferNoteResults.add(transferNoteResult);
                }
            }
            return DataToolUtils.handleTransaction(transactionReceipt, transferNoteResults);

        } catch (Exception e) {
            log.error("transfer failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.UNKNOW_ERROR);
        }

    }


    /**
     * Query the list of assets for the account
     *
     * @param account account address
     * @param start list queries the starting value,start from 0.
     * @param end lists the end value of the query
     * @param message hash result
     * @param rs Signature object
     * @return asset list
     * @throws OpenLedgerBaseException
     */
    public List<BigInteger> getAccountNotes(String account, BigInteger start, BigInteger end, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);

        try {
            List<BigInteger> notes = asset.getAccountNotes(account, start, end, resultSign);
            return notes;
        } catch (ContractException e) {
            log.error("getAccountNotes failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.UNKNOW_ERROR);
        }

    }

    /**
     * Update the asset noteNo
     * The asset noteNo is globally unique
     *
     * @param oldNoteNo Current asset noteNo
     * @param newNoteNo updates the asset noteNo,Only the issuing account has permissions
     * @param message hash result
     * @param rs Signature object
     * @return Whether the operation was successful
     * @throws OpenLedgerBaseException
     */
    public ResponseData<Boolean> updateNoteNo(BigInteger oldNoteNo, BigInteger newNoteNo, String account, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);

        TransactionReceipt transactionReceipt = null;
        Boolean isUpdate;
        try {
            transactionReceipt = asset.updateNoteNo(oldNoteNo, newNoteNo, account, resultSign);
            isUpdate = transactionReceipt.isStatusOK() ? asset.getUpdateNoteNoOutput(transactionReceipt).getValue1() : false;
            return DataToolUtils.handleTransaction(transactionReceipt, isUpdate);
        } catch (Exception e) {
            log.error("updateNoteNo failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.UNKNOW_ERROR);
        }
    }

    /**
     * Update the asset properties
     *
     * @param account operation account address
     * @param noteNo Asset noteNo
     * @param items property list
     * @param message hash result
     * @param rs Signature object
     * @return A list of current asset properties
     * @throws OpenLedgerBaseException
     */
    public ResponseData<Map<String, Object>> updateNoteProperties(String account, BigInteger noteNo, HashMap<String, Object> items, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
        TransactionReceipt transactionReceipt = null;
        List<byte[]> keys = new ArrayList<>();
        List<byte[]> values = new ArrayList<>();
        HashMap<String, Object> noteItems = new HashMap<>();
        for (Map.Entry<String, Object> entry : items.entrySet()) {
            String k = entry.getKey();
            Object v = entry.getValue();
            if (v instanceof List) {
                throw new OpenLedgerBaseException(ErrorCode.NONHOMOASSET_VALUE_ERROR);
            }
            keys.add(k.getBytes(Charset.defaultCharset()));
            ValueModel vm = new ValueModel(v);
            values.add(ValueModel.getByteVal(vm));
        }
        try {
            transactionReceipt = asset.updateNoteProperties(noteNo, keys, values, account, resultSign);
            Tuple2<List<byte[]>, List<byte[]>> itemKeyValues = transactionReceipt.isStatusOK() ? asset.getUpdateNotePropertiesOutput(transactionReceipt) : null;
            if (itemKeyValues != null && itemKeyValues.getValue1().size() > 0) {
                ValueModel vm = null;
                for (int i = 0; i < itemKeyValues.getValue1().size(); i++) {
                    if (itemKeyValues.getValue2().get(i).length == 0) {
                        continue;
                    }
                    vm = JsonHelper.getObjectMapper().readValue(itemKeyValues.getValue2().get(i), ValueModel.class);
                    noteItems.put(new String(itemKeyValues.getValue1().get(i)), vm.getValue());
                }
            }

            return DataToolUtils.handleTransaction(transactionReceipt, noteItems);
        } catch (Exception e) {
            log.error("updateNoteNo failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.UNKNOW_ERROR);
        }
    }


    /**
     * Obtaining asset attributes
     *
     * @param noteNo asset noteNo
     * @param account operation account address
     * @param message args hash result
     * @param rs signature object
     * @return asset attribute list
     * @throws OpenLedgerBaseException
     */
    public Map<String, Object> getNoteProperties(BigInteger noteNo, String account, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
        HashMap<String, Object> noteItems = new HashMap<>();

        try {
            Tuple2<List<byte[]>, List<byte[]>> itemKeyValues = asset.getNoteProperties(noteNo, account, resultSign);
            if (itemKeyValues != null && itemKeyValues.getValue1().size() > 0) {
                ValueModel vm = null;
                for (int i = 0; i < itemKeyValues.getValue1().size(); i++) {
                    if (itemKeyValues.getValue2().get(i).length == 0) {
                        continue;
                    }
                    vm = JsonHelper.getObjectMapper().readValue(itemKeyValues.getValue2().get(i), ValueModel.class);
                    noteItems.put(new String(itemKeyValues.getValue1().get(i)), vm.getValue());
                }
            }

            return noteItems;
        } catch (Exception e) {
            log.error("getNoteItems failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.UNKNOW_ERROR);
        }
    }

    /**
     * Update the batch expiration time
     *
     * @param batchNo batchNo
     * @param expirationDate The expiration time must be greater than the current time and the effective time
     * @param account operation account address
     * @param message args hash result
     * @param rs signature object
     * @return Whether the operation was successful
     * @throws OpenLedgerBaseException
     */
    public ResponseData<Boolean> updateExpirationDate(BigInteger batchNo, Date expirationDate, String account, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
        TransactionReceipt transactionReceipt = null;
        try {
            transactionReceipt = asset.updateNoteBatch(batchNo, BigInteger.valueOf(expirationDate.getTime()), false, account, resultSign);
            Boolean ipUpdate = transactionReceipt.isStatusOK() ? asset.getUpdateNoteBatchOutput(transactionReceipt).getValue1() : false;
            return DataToolUtils.handleTransaction(transactionReceipt, ipUpdate);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateNoteBatch failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.UNKNOW_ERROR);
        }

    }

    /**
     * 更新批次生效时间
     *
     * @param batchNo 批次号
     * @param effectiveDate The effective time must be greater than the current time
     * @param account operation account address
     * @param message args hash result
     * @param rs signature object
     * @return Whether the operation was successful
     * @throws OpenLedgerBaseException
     */
    public ResponseData<Boolean> updateEffectiveDate(BigInteger batchNo, Date effectiveDate, String account, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
        TransactionReceipt transactionReceipt = null;
        try {
            transactionReceipt = asset.updateNoteBatch(batchNo, BigInteger.valueOf(effectiveDate.getTime()), true, account, resultSign);
            Boolean ipUpdate = transactionReceipt.isStatusOK() ? asset.getUpdateNoteBatchOutput(transactionReceipt).getValue1() : false;
            return DataToolUtils.handleTransaction(transactionReceipt, ipUpdate);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateNoteBatch failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.UNKNOW_ERROR);
        }

    }

    /**
     * Freeze assets according to the asset noteNo,only freeze note when note is effective.
     *
     * @param noteNo asset noteNo
     * @param account operation account address
     * @param message args hash result
     * @param rs signature object
     * @return Whether the operation was successful
     * @throws OpenLedgerBaseException
     */
    public ResponseData<Boolean> freezeNote(BigInteger noteNo, String account, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
        TransactionReceipt transactionReceipt = null;
        try {
            transactionReceipt = asset.freezeNote(noteNo, account, resultSign);
            Boolean isUpdate = transactionReceipt.isStatusOK() ? asset.getFreezeNoteOutput(transactionReceipt).getValue1() : false;
            return DataToolUtils.handleTransaction(transactionReceipt, isUpdate);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateNoteBatch failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.UNKNOW_ERROR);
        }
    }

    /**
     * unfreeze assets according to the asset noteNo，only unfreeze note when note is frozen.
     *
     * @param noteNo asset noteNo
     * @param account operation account address
     * @param message args hash result
     * @param rs signature object
     * @return Whether the operation was successful
     * @throws OpenLedgerBaseException
     */
    public ResponseData<BigInteger> unfreezeNote(BigInteger noteNo, String account, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
        TransactionReceipt transactionReceipt = null;
        try {
            transactionReceipt = asset.unfreezeNote(noteNo, account, resultSign);
            BigInteger noteStatus = transactionReceipt.isStatusOK() ? asset.getUnfreezeNoteOutput(transactionReceipt).getValue1() : BigInteger.valueOf(-1);
            return DataToolUtils.handleTransaction(transactionReceipt, noteStatus);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateNoteBatch failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.UNKNOW_ERROR);
        }
    }

    /**
     * The account query book only has the authority to query their own account records
     *
     * @param condition query parameters
     * @return query results
     * @throws Exception
     */
    public List<NonFungibleAssetRecord> query(NonFungibleCondition condition, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws Exception {
        List<NonFungibleAssetRecord> recordEntities = new ArrayList<>();
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);

            List records = asset.queryBook(condition.getIntParams(), condition.getAddressParams(), condition.getLimits(), resultSign);
            for (int i = 0; i < records.size(); i++) {
                NonFungibleAssetRecord recordEntity = NonFungibleAssetRecordBuilder.buildRecordEntity()
                        .withTermNo(new BigInteger(records.get(i).toString()))
                        .withSeq(new BigInteger(records.get(++i).toString()))
                        .withFrom(new Address(new BigInteger(records.get(++i).toString())).toString())
                        .withTo(new Address(new BigInteger(records.get(++i).toString())).toString())
                        .withNoteId(new BigInteger(records.get(++i).toString()))
                        .withDesc(records.get(++i).toString())
                        .withAsset(new Address(new BigInteger(records.get(++i).toString())).toString())
                        .withOperator(new Address(new BigInteger(records.get(++i).toString())).toString())
                        .build();
                recordEntities.add(recordEntity);
            }
        } catch (Exception e) {
            log.error("query failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.ASSET_BOOKQUERY_ERROR);
        }
        return recordEntities;
    }


    /**
     * Tear asset by asset noteNo
     *
     * @param noteNo asset noteNo
     * @param account operation account address
     * @param message args hash result
     * @param rs signature object
     * @return Whether the operation was successful
     * @throws OpenLedgerBaseException
     */
    public ResponseData<Boolean> tearNote(BigInteger noteNo, String account, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);

        TransactionReceipt transactionReceipt = null;
        Boolean isBurn = false;
        try {
            transactionReceipt = asset.tearNote(noteNo, account, resultSign);
            isBurn = transactionReceipt.isStatusOK() ? asset.getTearNoteOutput(transactionReceipt).getValue1() : false;
        } catch (Exception e) {
            log.error("query failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.UNKNOW_ERROR);
        }
        return DataToolUtils.handleTransaction(transactionReceipt, isBurn);
    }

    /**
     * Query the list of destroyed assets
     *
     * @param account query account address
     * @param message args hash result
     * @param rs signature object
     * @return list of destroyed assets
     * @throws ContractException
     */
    public List<BigInteger> getTearNotes(String account, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws ContractException {
        List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
        List<BigInteger> tearNotes = asset.getTearNotes(account, resultSign);
        return tearNotes;
    }


    /**
     * effect batch
     *
     * @param batchNo asset batchNo
     * @param message args hash result
     * @param rs signature object
     * @return Whether the operation was successful
     * @throws OpenLedgerBaseException
     */
    public ResponseData<Boolean> effectBatch(@NonNull BigInteger batchNo, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);

            TransactionReceipt transactionReceipt = asset.enableBatch(batchNo, resultSign);
            Boolean result = transactionReceipt.isStatusOK() ? asset.getEnableBatchOutput(transactionReceipt).getValue1() : false;
            return DataToolUtils.handleTransaction(transactionReceipt, result);
        } catch (Exception e) {
            log.error("enableBatch failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.UNKNOW_ERROR);
        }
    }

    /**
     * Adding book only supports OrgAdmin for operation
     *
     * @return current bookNo
     * @throws OpenLedgerBaseException
     */
    public ResponseData<BigInteger> addBook(@NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);

            TransactionReceipt transactionReceipt = asset.addBook(resultSign);
            BigInteger result = transactionReceipt.isStatusOK() ? asset.getAddBookOutput(transactionReceipt).getValue1() : BigInteger.valueOf(-1);
            return DataToolUtils.handleTransaction(transactionReceipt, result);
        } catch (Exception e) {
            log.error("addBook failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.ASSET_ADDBOOK_EROOR);
        }
    }

    public static byte[] computeIssueMsg(String assetAddress, IssueOption issueOption, BigInteger nonce) throws Exception {
        byte[] result = new byte[0];
        List<String> transactionAddress = issueOption.genAddressList(assetAddress);
        List<BigInteger> uint256Args = issueOption.genBigIntegerList();
        List<String> stringValList = issueOption.genStringValueList();
        for (String item : transactionAddress) {
            result = OpenLedgerUtils.concatByte(result, OpenLedgerUtils.convertStringToAddressByte(item));
        }
        for (BigInteger item : uint256Args) {
            result = OpenLedgerUtils.concatByte(result, OpenLedgerUtils.getBytes32(item.toByteArray()));
        }

        for (String item : stringValList) {
            result = OpenLedgerUtils.concatByte(result, item.getBytes("utf-8"));
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
