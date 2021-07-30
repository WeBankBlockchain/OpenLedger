package com.webank.openledger.core.common;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.openledger.core.asset.fungible.entity.Condition;
import com.webank.openledger.core.asset.fungible.entity.RecordEntity;
import com.webank.openledger.core.asset.fungible.entity.TransferResult;
import com.webank.openledger.core.asset.nonfungible.entity.IssueNoteResult;
import com.webank.openledger.core.asset.nonfungible.entity.IssueOption;
import com.webank.openledger.core.asset.nonfungible.entity.Note;
import com.webank.openledger.core.exception.OpenLedgerBaseException;
import com.webank.openledger.core.response.ResponseData;

import lombok.NonNull;
import org.fisco.bcos.sdk.abi.FunctionReturnDecoder;
import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.datatypes.Address;
import org.fisco.bcos.sdk.abi.datatypes.Bool;
import org.fisco.bcos.sdk.abi.datatypes.Function;
import org.fisco.bcos.sdk.abi.datatypes.Type;
import org.fisco.bcos.sdk.abi.datatypes.Utf8String;
import org.fisco.bcos.sdk.abi.datatypes.generated.Bytes32;
import org.fisco.bcos.sdk.abi.datatypes.generated.StaticArray4;
import org.fisco.bcos.sdk.abi.datatypes.generated.Uint8;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple1;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple3;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple4;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple5;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple6;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;

public interface BaseCustody {
    TransactionReceipt addBookByCustody(String asset, List<byte[]> sign);

    void addBookByCustody(String asset, List<byte[]> sign, TransactionCallback callback);

    String getSignedTransactionForAddBookByCustody(String asset, List<byte[]> sign);

    Tuple2<String, List<byte[]>> getAddBookByCustodyInput(TransactionReceipt transactionReceipt);

    Tuple1<BigInteger> getAddBookByCustodyOutput(TransactionReceipt transactionReceipt);

    TransactionReceipt tearNote(String asset, BigInteger noteNo, List<byte[]> sign);

    void tearNote(String asset, BigInteger noteNo, List<byte[]> sign, TransactionCallback callback);

    String getSignedTransactionForTearNote(String asset, BigInteger noteNo, List<byte[]> sign);

    Tuple3<String, BigInteger, List<byte[]>> getTearNoteInput(TransactionReceipt transactionReceipt);

    Tuple1<Boolean> getTearNoteOutput(TransactionReceipt transactionReceipt);

    List queryBookByCustody(String asset, List<BigInteger> uintCondition, List<String> addressCondition, List<BigInteger> limit, List<byte[]> sign) throws ContractException;

    List getHolders(String asset, List<byte[]> sign) throws ContractException;

    List getAccountNotes(String asset, String account, BigInteger start, BigInteger end, List<byte[]> sign) throws ContractException;

    TransactionReceipt withdrawal(String asset, List<String> transactionAddress, BigInteger amount, List<BigInteger> typeList, List<byte[]> detailList, List<byte[]> sign);

    void withdrawal(String asset, List<String> transactionAddress, BigInteger amount, List<BigInteger> typeList, List<byte[]> detailList, List<byte[]> sign, TransactionCallback callback);

    String getSignedTransactionForWithdrawal(String asset, List<String> transactionAddress, BigInteger amount, List<BigInteger> typeList, List<byte[]> detailList, List<byte[]> sign);

    Tuple6<String, List<String>, BigInteger, List<BigInteger>, List<byte[]>, List<byte[]>> getWithdrawalInput(TransactionReceipt transactionReceipt);

    Tuple2<Boolean, List<BigInteger>> getWithdrawalOutput(TransactionReceipt transactionReceipt);

    TransactionReceipt openAccountByCustody(String asset, String account, List<byte[]> sign);

    void openAccountByCustody(String asset, String account, List<byte[]> sign, TransactionCallback callback);

    String getSignedTransactionForOpenAccountByCustody(String asset, String account, List<byte[]> sign);

    Tuple3<String, String, List<byte[]>> getOpenAccountByCustodyInput(TransactionReceipt transactionReceipt);

    Tuple1<Boolean> getOpenAccountByCustodyOutput(TransactionReceipt transactionReceipt);

    List getTearNotes(String asset, List<byte[]> sign) throws ContractException;

    Tuple2<List<byte[]>, List<byte[]>> getNoteProperties(String asset, BigInteger noteNo, List<byte[]> sign) throws ContractException;

    List getAccountNotes(String asset, BigInteger start, BigInteger end, List<byte[]> sign) throws ContractException;

    TransactionReceipt setPrice(String asset, BigInteger priceVal, List<byte[]> sign);

    void setPrice(String asset, BigInteger priceVal, List<byte[]> sign, TransactionCallback callback);

    String getSignedTransactionForSetPrice(String asset, BigInteger priceVal, List<byte[]> sign);

    Tuple3<String, BigInteger, List<byte[]>> getSetPriceInput(TransactionReceipt transactionReceipt);

    Tuple1<BigInteger> getSetPriceOutput(TransactionReceipt transactionReceipt);

    TransactionReceipt enableBatch(String asset, BigInteger batchNo, List<byte[]> sign);

    void enableBatch(String asset, BigInteger batchNo, List<byte[]> sign, TransactionCallback callback);

    String getSignedTransactionForEnableBatch(String asset, BigInteger batchNo, List<byte[]> sign);

    Tuple3<String, BigInteger, List<byte[]>> getEnableBatchInput(TransactionReceipt transactionReceipt);

    Tuple1<Boolean> getEnableBatchOutput(TransactionReceipt transactionReceipt);

    TransactionReceipt updateNoteProperties(String asset, BigInteger noteNo, List<byte[]> keys, List<byte[]> values, List<byte[]> sign);

    void updateNoteProperties(String asset, BigInteger noteNo, List<byte[]> keys, List<byte[]> values, List<byte[]> sign, TransactionCallback callback);

    String getSignedTransactionForUpdateNoteProperties(String asset, BigInteger noteNo, List<byte[]> keys, List<byte[]> values, List<byte[]> sign);

    Tuple5<String, BigInteger, List<byte[]>, List<byte[]>, List<byte[]>> getUpdateNotePropertiesInput(TransactionReceipt transactionReceipt);

    Tuple2<List<byte[]>, List<byte[]>> getUpdateNotePropertiesOutput(TransactionReceipt transactionReceipt);

    BigInteger getTotalNoteSize(String asset, List<byte[]> sign) throws ContractException;

    Boolean accountHoldNote(String asset, BigInteger noteNo, List<byte[]> sign) throws ContractException;

    TransactionReceipt updateNoteNo(String asset, BigInteger oldNoteNo, BigInteger newNoteNo, List<byte[]> sign);

    void updateNoteNo(String asset, BigInteger oldNoteNo, BigInteger newNoteNo, List<byte[]> sign, TransactionCallback callback);

    String getSignedTransactionForUpdateNoteNo(String asset, BigInteger oldNoteNo, BigInteger newNoteNo, List<byte[]> sign);

    Tuple4<String, BigInteger, BigInteger, List<byte[]>> getUpdateNoteNoInput(TransactionReceipt transactionReceipt);

    Tuple1<Boolean> getUpdateNoteNoOutput(TransactionReceipt transactionReceipt);

    TransactionReceipt unfreezeNote(String asset, BigInteger noteNo, List<byte[]> sign);

    void unfreezeNote(String asset, BigInteger noteNo, List<byte[]> sign, TransactionCallback callback);

    String getSignedTransactionForUnfreezeNote(String asset, BigInteger noteNo, List<byte[]> sign);

    Tuple3<String, BigInteger, List<byte[]>> getUnfreezeNoteInput(TransactionReceipt transactionReceipt);

    Tuple1<BigInteger> getUnfreezeNoteOutput(TransactionReceipt transactionReceipt);

      TransactionReceipt setRate(String asset, BigInteger rateVal, List<byte[]> sign);

    void setRate(String asset, BigInteger rateVal, List<byte[]> sign, TransactionCallback callback);

    String getSignedTransactionForSetRate(String asset, BigInteger rateVal, List<byte[]> sign);

    Tuple3<String, BigInteger, List<byte[]>> getSetRateInput(TransactionReceipt transactionReceipt);

    Tuple1<BigInteger> getSetRateOutput(TransactionReceipt transactionReceipt);

    TransactionReceipt issue(String asset, List<String> transactionAddress, List<BigInteger> uint256Args, List<byte[]> stringValueList, List<byte[]> sign);

    void issue(String asset, List<String> transactionAddress, List<BigInteger> uint256Args, List<byte[]> stringValueList, List<byte[]> sign, TransactionCallback callback);

    String getSignedTransactionForIssue(String asset, List<String> transactionAddress, List<BigInteger> uint256Args, List<byte[]> stringValueList, List<byte[]> sign);

    Tuple5<String, List<String>, List<BigInteger>, List<byte[]>, List<byte[]>> getIssueInput(TransactionReceipt transactionReceipt);

    Tuple3<BigInteger, List<BigInteger>, List<BigInteger>> getIssueOutput(TransactionReceipt transactionReceipt);

    TransactionReceipt freezeNote(String asset, BigInteger noteNo, List<byte[]> sign);

    void freezeNote(String asset, BigInteger noteNo, List<byte[]> sign, TransactionCallback callback);

    String getSignedTransactionForFreezeNote(String asset, BigInteger noteNo, List<byte[]> sign);

    Tuple3<String, BigInteger, List<byte[]>> getFreezeNoteInput(TransactionReceipt transactionReceipt);

    Tuple1<Boolean> getFreezeNoteOutput(TransactionReceipt transactionReceipt);

    Tuple4<List<String>, List<BigInteger>, List<BigInteger>, BigInteger> getNoteDetail(String asset, BigInteger noteNo, List<byte[]> sign) throws ContractException;

    BigInteger getTotalBalance(String asset, List<byte[]> sign) throws ContractException;

    TransactionReceipt updateNoteBatch(String asset, BigInteger batchNo, BigInteger date, Boolean isEffectiveDate, List<byte[]> sign);

    void updateNoteBatch(String asset, BigInteger batchNo, BigInteger date, Boolean isEffectiveDate, List<byte[]> sign, TransactionCallback callback);

    String getSignedTransactionForUpdateNoteBatch(String asset, BigInteger batchNo, BigInteger date, Boolean isEffectiveDate, List<byte[]> sign);

    Tuple5<String, BigInteger, BigInteger, Boolean, List<byte[]>> getUpdateNoteBatchInput(TransactionReceipt transactionReceipt);

    Tuple1<Boolean> getUpdateNoteBatchOutput(TransactionReceipt transactionReceipt);

    TransactionReceipt deposit(String asset, List<String> transactionAddress, BigInteger amount, List<BigInteger> typeList, List<byte[]> detailList, List<byte[]> sign);

    void deposit(String asset, List<String> transactionAddress, BigInteger amount, List<BigInteger> typeList, List<byte[]> detailList, List<byte[]> sign, TransactionCallback callback);

    String getSignedTransactionForDeposit(String asset, List<String> transactionAddress, BigInteger amount, List<BigInteger> typeList, List<byte[]> detailList, List<byte[]> sign);

    Tuple6<String, List<String>, BigInteger, List<BigInteger>, List<byte[]>, List<byte[]>> getDepositInput(TransactionReceipt transactionReceipt);

    Tuple2<Boolean, List<BigInteger>> getDepositOutput(TransactionReceipt transactionReceipt);

    public TransactionReceipt registerAsset(String assetName, Boolean isFungible, List<byte[]> sign);

    public void registerAsset(String assetName, Boolean isFungible, List<byte[]> sign, TransactionCallback callback);

    public String getSignedTransactionForRegisterAsset(String assetName, Boolean isFungible, List<byte[]> sign);

    public Tuple3<String, Boolean, List<byte[]>> getRegisterAssetInput(TransactionReceipt transactionReceipt);

    public Tuple2<String, Boolean> getRegisterAssetOutput(TransactionReceipt transactionReceipt);

    public TransactionReceipt upgradeAsset(String assetName, Boolean isFungible, List<byte[]> sign);

    public void upgradeAsset(String assetName, Boolean isFungible, List<byte[]> sign, TransactionCallback callback);

    public String getSignedTransactionForUpgradeAsset(String assetName, Boolean isFungible, List<byte[]> sign);

    public Tuple3<String, Boolean, List<byte[]>> getUpgradeAssetInput(TransactionReceipt transactionReceipt);

    public Tuple2<String, Boolean> getUpgradeAssetOutput(TransactionReceipt transactionReceipt);

    public TransactionReceipt createCurrency(String currencyName, String currencySymbol, BigInteger decimals);

    public void createCurrency(String currencyName, String currencySymbol, BigInteger decimals, TransactionCallback callback);

    public String getSignedTransactionForCreateCurrency(String currencyName, String currencySymbol, BigInteger decimals);

    public Tuple3<String, String, BigInteger> getCreateCurrencyInput(TransactionReceipt transactionReceipt);

    public Tuple1<String> getCreateCurrencyOutput(TransactionReceipt transactionReceipt);

    public BigInteger getBalance(String asset, String account, List<byte[]> sign) throws ContractException;

    public List getAllAssets(Boolean isFungible) throws ContractException;

}

