package com.webank.openledger.core.asset;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.asset.fungible.StandardAssetService;
import com.webank.openledger.core.asset.fungible.entity.Condition;
import com.webank.openledger.core.asset.fungible.entity.RecordBuilder;
import com.webank.openledger.core.asset.fungible.entity.RecordEntity;
import com.webank.openledger.core.asset.fungible.entity.TransferResult;
import com.webank.openledger.core.asset.nonfungible.entity.NonFungibleAssetRecord;
import com.webank.openledger.core.asset.nonfungible.entity.NonFungibleAssetRecordBuilder;
import com.webank.openledger.core.asset.nonfungible.entity.NonFungibleCondition;
import com.webank.openledger.core.asset.nonfungible.entity.TransferNoteResult;
import com.webank.openledger.core.common.BaseHolder;
import com.webank.openledger.core.constant.ErrorCode;
import com.webank.openledger.core.exception.OpenLedgerBaseException;
import com.webank.openledger.core.response.DataToolUtils;
import com.webank.openledger.core.response.ResponseData;
import com.webank.openledger.utils.OpenLedgerUtils;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.abi.datatypes.Address;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.contract.Contract;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;

@Slf4j
@Setter
@Getter
public class AccountHolderService<T extends Contract> {
    /**
     * contract object
     */
    protected BaseHolder accountHolder;
    protected String assetAddress;
    public AccountHolderService(Blockchain blockchain, String contractAddress,String assetAddress,Class holderClass) {
        accountHolder = blockchain.getLedger(Blockchain.DEFAULT_LEDGERID).getContract(contractAddress, holderClass);
        this.assetAddress=assetAddress;
    }

    public T getAccountHolder(){
        return accountHolder==null?null: (T) accountHolder;
    }

    /**
     * transfer
     *
     * @param fromAddress account of payments
     * @param toAddress account of receipts
     * @param amount transaction amount
     * @param operationType operation type custom defined
     * @param desc description
     * @return
     * @throws OpenLedgerBaseException
     */
    public ResponseData<TransferResult> transferFungibleAsset( @NonNull String operatorAddress, @NonNull String fromAddress, @NonNull String toAddress, @NonNull BigInteger amount, int operationType, String desc, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<String> addressList = StandardAssetService.genAddress(fromAddress, toAddress, operatorAddress, assetAddress, null);
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);

            TransactionReceipt transactionReceipt = accountHolder.transfer(
                    assetAddress,
                    addressList,
                    amount,
                    StandardAssetService.genType(operationType),
                    StandardAssetService.genDetail(desc, null),
                    resultSign);
            TransferResult tr = null;
            Tuple2<Boolean, List<BigInteger>> response = transactionReceipt.isStatusOK() ? accountHolder.getTransferAddressAddressaddressUint256Int256int256BytesbytesBytes32bytes324Output(transactionReceipt) : null;
            if (response != null && response.getValue1()) {
                tr = new TransferResult(response.getValue1(), response.getValue2().get(0), response.getValue2().get(1));
            }
            return DataToolUtils.handleTransaction(transactionReceipt, tr);
        } catch (Exception e) {
            log.error("transfer failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.ASSET_TRANSFER_ERROR);
        }
    }
    /**
     * balance of account
     *
     * @return balance of account
     * @throws OpenLedgerBaseException
     */
    public BigInteger getBalance(@NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException {
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
            return accountHolder.getBalance(assetAddress, resultSign);
        } catch (Exception e) {
            log.error("getBalance failed:{}", e);
            throw new OpenLedgerBaseException(ErrorCode.ASSET_GETBALEANCE_ERROR);
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
            List<BigInteger> notes = accountHolder.getAccountNotes(account,start, end, resultSign);
            return notes;
        } catch (ContractException e) {
            log.error("getAccountNotes failed:{}", e);
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
    public ResponseData<List<TransferNoteResult>> transferNonFungibleAsset(String operator, String from, String to, List<BigInteger> notes, String desc, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws OpenLedgerBaseException, UnsupportedEncodingException {
        List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);
        List<String> transactionAddress = new ArrayList<>();
        transactionAddress.add(operator);
        transactionAddress.add(assetAddress);
        transactionAddress.add(from);
        transactionAddress.add(to);

        List<byte[]> stringValList = new ArrayList<>();
        stringValList.add(desc.getBytes("utf-8"));

        TransactionReceipt transactionReceipt;
        try {
            transactionReceipt = accountHolder.transfer(assetAddress,transactionAddress, notes, stringValList, resultSign);
            Tuple2<Boolean, List<BigInteger>> response = transactionReceipt.isStatusOK() ? accountHolder.getTransferAddressAddressaddressUint256uint256BytesbytesBytes32bytes324Output(transactionReceipt) : null;

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
     * The account query only has the authority to query their own account records
     *
     * @param condition 查询参数封装对象
     * @return
     * @throws Exception 账本查询
     * todo recommoned 数据mysql查询
     */
    public List<RecordEntity> queryFungible( Condition condition, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws Exception {
        List<RecordEntity> recordEntities = new ArrayList<>();
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);

            List records = accountHolder.queryBook(assetAddress,condition.getIntParams(), condition.getAddressParams(), condition.getLimits(), resultSign);
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
    public List<NonFungibleAssetRecord> queryNonFungible(NonFungibleCondition condition, @NonNull byte[] message, @NonNull ECDSASignatureResult rs) throws Exception {
        List<NonFungibleAssetRecord> recordEntities = new ArrayList<>();
        try {
            List<byte[]> resultSign = OpenLedgerUtils.convertSignToByte(message, rs);

            List records = accountHolder.queryBook(assetAddress,condition.getIntParams(), condition.getAddressParams(), condition.getLimits(), resultSign);
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
     * get account's asset
     * @param isFungible boolean is fungible
     * @param message args hash
     * @param rs sign by account or orgAdmin
     * @return
     */
    public ResponseData<List> getAccountAssets(Boolean isFungible,byte[] message, ECDSASignatureResult rs) {
        HashMap<String, String> assets = new HashMap<>();
        List<String> ret;
        try {
            ret = accountHolder.getAccountAssets(isFungible, OpenLedgerUtils.convertSignToByte(message, rs));
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
