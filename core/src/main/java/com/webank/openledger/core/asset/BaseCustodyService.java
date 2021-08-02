package com.webank.openledger.core.asset;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.asset.fungible.StandardAssetService;
import com.webank.openledger.core.asset.fungible.entity.Condition;
import com.webank.openledger.core.asset.fungible.entity.RecordBuilder;
import com.webank.openledger.core.asset.fungible.entity.RecordEntity;
import com.webank.openledger.core.asset.fungible.entity.TransferResult;
import com.webank.openledger.core.asset.nonfungible.entity.IssueNoteResult;
import com.webank.openledger.core.asset.nonfungible.entity.IssueOption;
import com.webank.openledger.core.asset.nonfungible.entity.NonFungibleAssetRecord;
import com.webank.openledger.core.asset.nonfungible.entity.NonFungibleAssetRecordBuilder;
import com.webank.openledger.core.asset.nonfungible.entity.NonFungibleCondition;
import com.webank.openledger.core.asset.nonfungible.entity.Note;
import com.webank.openledger.core.common.BaseCustody;
import com.webank.openledger.core.common.ValueModel;
import com.webank.openledger.core.constant.ErrorCode;
import com.webank.openledger.core.exception.OpenLedgerBaseException;
import com.webank.openledger.core.response.DataToolUtils;
import com.webank.openledger.core.response.ResponseData;
import com.webank.openledger.utils.JsonHelper;
import com.webank.openledger.utils.OpenLedgerUtils;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.abi.datatypes.Address;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple3;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple4;
import org.fisco.bcos.sdk.contract.Contract;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;

@Slf4j
@Getter@Setter
public class BaseCustodyService<T extends Contract> {
    /**
     * contract object
     */
    protected BaseCustody custody;
    protected String assetAddress;
    protected String contractAddress;
    protected Blockchain blockchain;
    /**
     * Initialize the contract object
     *
     * @param blockchain property object
     * @param contractAddress asset contractAddress
     */
    public BaseCustodyService(Blockchain blockchain, String contractAddress,  Class custodyClass) {
        this.blockchain = blockchain;
        this.contractAddress = contractAddress;
        custody = blockchain.getLedger(Blockchain.DEFAULT_LEDGERID).getContract(contractAddress, custodyClass);
    }
    /**
     * get contract object
     *
     * @return
     */
    public T getCustody() {
        return custody == null ? null : (T) custody;
    }

    /**
     * create asset of organzation
     *
     * @param assetName asset's name custom defined
     * @param isFungible boolean is fungible
     * @param message args hash
     * @param rs sign by orgAdmin
     * @return asset address
     */
    public ResponseData<String> createAsset( String assetName, Boolean isFungible, byte[] message, ECDSASignatureResult rs) {
        TransactionReceipt transactionReceipt = custody.registerAsset(assetName, isFungible, OpenLedgerUtils.convertSignToByte(message, rs));
        String result = transactionReceipt.isStatusOK() ? custody.getRegisterAssetOutput(transactionReceipt).getValue1() : null;
        ResponseData<String> responseData = DataToolUtils.handleTransaction(transactionReceipt, result);
        return responseData;
    }

    /**
     * update asset of organzation
     *
     * @param assetName asset's name custom defined
     * @param isFungible boolean is fungible
     * @param message args hash
     * @param rs sign by orgAdmin
     * @return asset address
     */
    public ResponseData<String> upgradeAsset(String assetName, Boolean isFungible, byte[] message, ECDSASignatureResult rs) {
        TransactionReceipt transactionReceipt = custody.upgradeAsset(assetName, isFungible, OpenLedgerUtils.convertSignToByte(message, rs));
        String result = transactionReceipt.isStatusOK() ? custody.getUpgradeAssetOutput(transactionReceipt).getValue1() : null;
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
        TransactionReceipt transactionReceipt = custody.createCurrency(name, symbol, decimals);
        String result = transactionReceipt.isStatusOK() ? custody.getCreateCurrencyOutput(transactionReceipt).getValue1() : null;
        ResponseData<String> responseData = DataToolUtils.handleTransaction(transactionReceipt, result);
        return responseData;
    }

    /**
     * open account
     *
     * @param account account address
     * @param message args hash
     * @param rs sign y orgAdmin
     * @return
     * @throws OpenLedgerBaseException
     */
    public ResponseData<Boolean> openAccount(@NonNull String account, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
            TransactionReceipt transactionReceipt = custody.openAccountByCustody(assetAddress, account, resultSign);
            Boolean result = transactionReceipt.isStatusOK() ? custody.getOpenAccountByCustodyOutput(transactionReceipt).getValue1() : false;
            return DataToolUtils.handleTransaction(transactionReceipt, result);
        } catch (Exception e) {
            log.error("openAccount failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.ASSET_UNKNOW_ERROR);
        }

    }

    /**
     * deposit asset to account
     *
     * @param account transaction account
     * @param amount transaction amount
     * @param operationType operation type custom defined
     * @param desc description
     * @return transaction result
     * @throws OpenLedgerBaseException
     */
    public ResponseData<TransferResult> deposit(@NonNull String operatorAddress, @NonNull String account, @NonNull BigInteger amount, int operationType, String desc, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<String> addressList = StandardAssetService.genAddress(null, account, operatorAddress, assetAddress, null);
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
            TransactionReceipt transactionReceipt = custody.deposit(
                    assetAddress,
                    addressList,
                    amount,
                    StandardAssetService.genType(operationType),
                    StandardAssetService.genDetail(desc, null),
                    resultSign);
            TransferResult tr = null;
            Tuple2<Boolean, List<BigInteger>> response = transactionReceipt.isStatusOK() ? custody.getDepositOutput(transactionReceipt) : null;
            if (response != null && response.getValue1()) {
                tr = new TransferResult(response.getValue1(), response.getValue2().get(0), response.getValue2().get(1));
            }

            return DataToolUtils.handleTransaction(transactionReceipt, tr);
        } catch (Exception e) {
            log.error("deposit failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.ASSET_DESIPOSIT_ERROR);
        }
    }

    /**
     * withdrawal asset from account
     *
     * @param account transaction account
     * @param amount transaction amount
     * @param operationType operation type custom defined
     * @param desc description
     * @return transaction result
     * @throws OpenLedgerBaseException
     */
    public ResponseData<TransferResult> withdrawal(@NonNull String operatorAddress, @NonNull String account, @NonNull BigInteger amount, int operationType, String desc, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<String> addressList = StandardAssetService.genAddress(account, null, operatorAddress, assetAddress, null);
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
            TransactionReceipt transactionReceipt = custody.withdrawal(
                    assetAddress,
                    addressList,
                    amount,
                    StandardAssetService.genType(operationType),
                    StandardAssetService.genDetail(desc, null),
                    resultSign);
            TransferResult tr = null;
            Tuple2<Boolean, List<BigInteger>> response = transactionReceipt.isStatusOK() ? custody.getWithdrawalOutput(transactionReceipt) : null;
            if (response != null && response.getValue1()) {
                tr = new TransferResult(response.getValue1(), response.getValue2().get(0), response.getValue2().get(1));
            }
            return DataToolUtils.handleTransaction(transactionReceipt, tr);
        } catch (Exception e) {
            log.error("withdrawal failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.ASSET_WITHDRAWAL_ERROR);
        }
    }


    /**
     * Getting a list of accounts only supports the OrgAdmin operate     * Getting a list of accounts only supports the OrgAdmin operate
     *
     * @param message args hash
     * @param rs sign object
     * @return
     * @throws OpenLedgerBaseException
     */
    public List<String> getHolders(@NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
            List resultList = custody.getHolders(assetAddress, resultSign);
            List<String> assetList = new ArrayList<>();
            resultList.stream().forEach(item -> {
                assetList.add(item.toString());
            });
            return assetList;
        } catch (Exception e) {
            log.error("getAccountList failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.ASSET_GETACCOUNTLIST_ERROR);
        }
    }


    /**
     * Getting the total asset is supported only by the OrgAdmin operation
     *
     * @param message args hash
     * @param rs sign by org admin
     * @return
     * @throws OpenLedgerBaseException
     */
    public BigInteger getTotalBalance(@NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
            return custody.getTotalBalance(assetAddress, resultSign);
        } catch (Exception e) {
            log.error("getTotalBalance failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.ASSET_GETTOTALBALEANCE_ERROR);
        }
    }


    /**
     * Adding ledgers only supports OrgAdmin for operation
     *
     * @param message args hash
     * @param rs sign by orgadmin
     * @return
     * @throws OpenLedgerBaseException
     */
    public ResponseData<BigInteger> addBook(@NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);

            TransactionReceipt transactionReceipt = custody.addBookByCustody(assetAddress, resultSign);
            BigInteger result = transactionReceipt.isStatusOK() ? custody.getAddBookByCustodyOutput(transactionReceipt).getValue1() : BigInteger.valueOf(-1);
            return DataToolUtils.handleTransaction(transactionReceipt, result);
        } catch (Exception e) {
            log.error("addBook failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.ASSET_ADDBOOK_EROOR);
        }
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
            TransactionReceipt transactionReceipt = custody.setPrice(assetAddress, price, resultSign);
            BigInteger curPrice = transactionReceipt.isStatusOK() ? custody.getSetPriceOutput(transactionReceipt).getValue1() : BigInteger.valueOf(-1);
            ResponseData<BigInteger> responseData = DataToolUtils.handleTransaction(transactionReceipt, curPrice);
            return responseData;
        } catch (Exception e) {
            log.error("setPrice failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.ASSET_UNKNOW_ERROR);
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
            transactionReceipt = custody.issue(issueOption.getAssetAddress(), issueOption.genAddressList(), issueOption.genBigIntegerList(), issueOption.genStringValueList(), resultSign);
            Tuple3<BigInteger, List<BigInteger>, List<BigInteger>> response = transactionReceipt.isStatusOK() ? custody.getIssueOutput(transactionReceipt) : null;
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
    public Note getNoteDetail(BigInteger noteNo,  @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
        try {
            Tuple4<List<String>, List<BigInteger>, List<BigInteger>, BigInteger> response = custody.getNoteDetail(assetAddress,noteNo, resultSign);
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
     * Query the list of assets for the account
     *
     * @param start list queries the starting value,start from 0.
     * @param end lists the end value of the query
     * @param message hash result
     * @param rs Signature object
     * @return asset list
     * @throws OpenLedgerBaseException
     */
    public List<BigInteger> getAccountNotes(String account,BigInteger start, BigInteger end, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);

        try {
            List<BigInteger> notes = custody.getAccountNotes(account, start, end, resultSign);
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
    public ResponseData<Boolean> updateNoteNo(BigInteger oldNoteNo, BigInteger newNoteNo, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);

        TransactionReceipt transactionReceipt = null;
        Boolean isUpdate;
        try {
            transactionReceipt = custody.updateNoteNo(assetAddress,oldNoteNo, newNoteNo, resultSign);
            isUpdate = transactionReceipt.isStatusOK() ? custody.getUpdateNoteNoOutput(transactionReceipt).getValue1() : false;
            return DataToolUtils.handleTransaction(transactionReceipt, isUpdate);
        } catch (Exception e) {
            log.error("updateNoteNo failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.UNKNOW_ERROR);
        }
    }

    /**
     * Update the asset properties
     *
     * @param noteNo Asset noteNo
     * @param items property list
     * @param message hash result
     * @param rs Signature object
     * @return A list of current asset properties
     * @throws OpenLedgerBaseException
     */
    public ResponseData<Map<String, Object>> updateNoteProperties( BigInteger noteNo, HashMap<String, Object> items, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
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
            transactionReceipt = custody.updateNoteProperties(assetAddress,noteNo, keys, values, resultSign);
            Tuple2<List<byte[]>, List<byte[]>> itemKeyValues = transactionReceipt.isStatusOK() ? custody.getUpdateNotePropertiesOutput(transactionReceipt) : null;
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
            Tuple2<List<byte[]>, List<byte[]>> itemKeyValues = custody.getNoteProperties(assetAddress,noteNo, resultSign);
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
            transactionReceipt = custody.updateNoteBatch(assetAddress,batchNo, BigInteger.valueOf(expirationDate.getTime()), false, resultSign);
            Boolean ipUpdate = transactionReceipt.isStatusOK() ? custody.getUpdateNoteBatchOutput(transactionReceipt).getValue1() : false;
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
            transactionReceipt = custody.updateNoteBatch(assetAddress,batchNo, BigInteger.valueOf(effectiveDate.getTime()), true, resultSign);
            Boolean ipUpdate = transactionReceipt.isStatusOK() ? custody.getUpdateNoteBatchOutput(transactionReceipt).getValue1() : false;
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
     * @param message args hash result
     * @param rs signature object
     * @return Whether the operation was successful
     * @throws OpenLedgerBaseException
     */
    public ResponseData<Boolean> freezeNote(BigInteger noteNo,  @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
        TransactionReceipt transactionReceipt = null;
        try {
            transactionReceipt = custody.freezeNote(assetAddress,noteNo, resultSign);
            Boolean isUpdate = transactionReceipt.isStatusOK() ? custody.getFreezeNoteOutput(transactionReceipt).getValue1() : false;
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
     * @param message args hash result
     * @param rs signature object
     * @return Whether the operation was successful
     * @throws OpenLedgerBaseException
     */
    public ResponseData<BigInteger> unfreezeNote(BigInteger noteNo,  @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
        TransactionReceipt transactionReceipt = null;
        try {
            transactionReceipt = custody.unfreezeNote(assetAddress,noteNo, resultSign);
            BigInteger noteStatus = transactionReceipt.isStatusOK() ? custody.getUnfreezeNoteOutput(transactionReceipt).getValue1() : BigInteger.valueOf(-1);
            return DataToolUtils.handleTransaction(transactionReceipt, noteStatus);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateNoteBatch failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.UNKNOW_ERROR);
        }
    }

    /**
     * Tear asset by asset noteNo
     *
     * @param noteNo asset noteNo
     * @param message args hash result
     * @param rs signature object
     * @return Whether the operation was successful
     * @throws OpenLedgerBaseException
     */
    public ResponseData<Boolean> tearNote(BigInteger noteNo,  @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);

        TransactionReceipt transactionReceipt = null;
        Boolean isBurn = false;
        try {
            transactionReceipt = custody.tearNote(assetAddress, noteNo, resultSign);
            isBurn = transactionReceipt.isStatusOK() ? custody.getTearNoteOutput(transactionReceipt).getValue1() : false;
        } catch (Exception e) {
            log.error("query failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.UNKNOW_ERROR);
        }
        return DataToolUtils.handleTransaction(transactionReceipt, isBurn);
    }

    /**
     * Query the list of destroyed assets
     *
     * @param message args hash result
     * @param rs signature object
     * @return list of destroyed assets
     * @throws ContractException
     */
    public List<BigInteger> getTearNotes(@NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws ContractException {
        List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
        List<BigInteger> tearNotes = custody.getTearNotes(assetAddress, resultSign);
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

            TransactionReceipt transactionReceipt = custody.enableBatch(assetAddress, batchNo, resultSign);
            Boolean result = transactionReceipt.isStatusOK() ? custody.getEnableBatchOutput(transactionReceipt).getValue1() : false;
            return DataToolUtils.handleTransaction(transactionReceipt, result);
        } catch (Exception e) {
            log.error("enableBatch failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.UNKNOW_ERROR);
        }
    }

    /**
     * The account query only has the authority to query their own account records
     *
     * @param condition 查询参数封装对象
     * @return
     * @throws Exception 账本查询
     * todo recommoned 数据mysql查询
     */
    public List<RecordEntity> queryFungibleByCustody(Condition condition, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws Exception {
        List<RecordEntity> recordEntities = new ArrayList<>();
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);

            List records = custody.queryBookByCustody(assetAddress,condition.getIntParams(), condition.getAddressParams(), condition.getLimits(), resultSign);
            for (int i = 0; i < records.size(); i++) {
                RecordEntity recordEntity = RecordBuilder.buildRecordEntity()
                        .withTermNo(new BigInteger(records.get(i).toString()))
                        .withSeq(new BigInteger(records.get(++i).toString()))
                        .withFrom(new Address(new BigInteger(records.get(++i).toString())).toString())
                        .withTo(new Address(new BigInteger(records.get(++i).toString())).toString())
                        .withAmount(new BigInteger(records.get(++i).toString()))
                        .withDesc(records.get(++i).toString())
                        .withAsset(new Address(new BigInteger(records.get(++i).toString())).toString())
                        .withOperator(new Address(new BigInteger(records.get(++i).toString())).toString())
                        .withTransactionType(new BigInteger(records.get(++i).toString()))
                        .withOperationType(new BigInteger(records.get(++i).toString()))
                        .withRelateAsset(new Address(new BigInteger(records.get(++i).toString())).toString())
                        .withSubject(records.get(++i).toString())
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
     * The account query book only has the authority to query their own account records
     *
     * @param condition query parameters
     * @return query results
     * @throws Exception
     */
    public List<NonFungibleAssetRecord> queryNonFungibleByCustody(NonFungibleCondition condition, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws Exception {
        List<NonFungibleAssetRecord> recordEntities = new ArrayList<>();
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);

            List records = custody.queryBookByCustody(assetAddress,condition.getIntParams(), condition.getAddressParams(), condition.getLimits(), resultSign);
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
     * balance of account
     *
     * @return balance of account
     * @throws OpenLedgerBaseException
     */
    public BigInteger getBalance(@NonNull String account,@NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
            return custody.getBalance(assetAddress,account, resultSign);
        } catch (Exception e) {
            log.error("getBalance failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.ASSET_GETBALEANCE_ERROR);
        }
    }

    /**
     * get account's asset
     * @param isFungible boolean is fungible
     * @param message args hash
     * @param rs sign by account or orgAdmin
     * @return
     */
    public ResponseData<List> getAllAssets(Boolean isFungible,byte[] message, ECDSASignatureResult rs) {
        HashMap<String, String> assets = new HashMap<>();
        List<String> ret;
        try {
            ret = custody.getAllAssets(isFungible);
        } catch (ContractException e) {
            log.error("getAllAssets failed:{}", e);
            return new ResponseData(assets, ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }

        if (ret.size() != ret.size()) {
            return new ResponseData(assets, ErrorCode.STORAGE_KV_SIZE_NOT_MATCH);
        }

        return new ResponseData<>(ret, ErrorCode.SUCCESS);
    }


}
