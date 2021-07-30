package com.webank.openledger.core.common;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.webank.openledger.core.asset.fungible.entity.Condition;
import com.webank.openledger.core.asset.fungible.entity.RecordEntity;
import com.webank.openledger.core.asset.fungible.entity.TransferResult;
import com.webank.openledger.core.asset.nonfungible.entity.TransferNoteResult;
import com.webank.openledger.core.exception.OpenLedgerBaseException;
import com.webank.openledger.core.response.ResponseData;

import lombok.NonNull;
import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.datatypes.Address;
import org.fisco.bcos.sdk.abi.datatypes.DynamicArray;
import org.fisco.bcos.sdk.abi.datatypes.DynamicBytes;
import org.fisco.bcos.sdk.abi.datatypes.Function;
import org.fisco.bcos.sdk.abi.datatypes.Type;
import org.fisco.bcos.sdk.abi.datatypes.generated.Bytes32;
import org.fisco.bcos.sdk.abi.datatypes.generated.StaticArray4;
import org.fisco.bcos.sdk.abi.datatypes.generated.Uint256;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple5;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple6;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;


public interface BaseHolder {

    TransactionReceipt transfer(String asset, List<String> transactionAddress, List<BigInteger> noteNos, List<byte[]> stringValueList, List<byte[]> sign);

    String getSignedTransactionForTransfer(String asset, List<String> transactionAddress, List<BigInteger> noteNos, List<byte[]> stringValueList, List<byte[]> sign);

    Tuple5<String, List<String>, List<BigInteger>, List<byte[]>, List<byte[]>> getTransferAddressAddressaddressUint256uint256BytesbytesBytes32bytes324Input(TransactionReceipt transactionReceipt);

    Tuple2<Boolean, List<BigInteger>> getTransferAddressAddressaddressUint256uint256BytesbytesBytes32bytes324Output(TransactionReceipt transactionReceipt);

    TransactionReceipt transfer(String asset, List<String> transactionAddress, BigInteger amount, List<BigInteger> typeList, List<byte[]> detailList, List<byte[]> sign);

    String getSignedTransactionForTransfer(String asset, List<String> transactionAddress, BigInteger amount, List<BigInteger> typeList, List<byte[]> detailList, List<byte[]> sign);

    Tuple6<String, List<String>, BigInteger, List<BigInteger>, List<byte[]>, List<byte[]>> getTransferAddressAddressaddressUint256Int256int256BytesbytesBytes32bytes324Input(TransactionReceipt transactionReceipt);

    Tuple2<Boolean, List<BigInteger>> getTransferAddressAddressaddressUint256Int256int256BytesbytesBytes32bytes324Output(TransactionReceipt transactionReceipt);

    public BigInteger getBalance(String asset, List<byte[]> sign) throws ContractException;

    public List getAccountNotes(String asset, BigInteger start, BigInteger end, List<byte[]> sign) throws ContractException;

    public List queryBook(String asset, List<BigInteger> uintCondition, List<String> addressCondition, List<BigInteger> limit, List<byte[]> sign) throws ContractException;


    public List getAccountAssets(Boolean isFungible, List<byte[]> sign) throws ContractException;


}
