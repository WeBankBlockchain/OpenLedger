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

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.webank.openledger.core.constant.ErrorCode;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.abi.FunctionReturnDecoder;
import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.datatypes.Function;
import org.fisco.bcos.sdk.abi.datatypes.Type;
import org.fisco.bcos.sdk.abi.datatypes.Utf8String;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataToolUtils {
    private static final Logger logger = LoggerFactory.getLogger(DataToolUtils.class);
    private static final String SEPARATOR_CHAR = "-";
    private static final String DEFAULT_SALT_LENGTH = "5";
    private static final int RADIX = 10;
    private static final String KEY_CREATED = "created";
    private static final String KEY_ISSUANCEDATE = "issuanceDate";
    private static final String KEY_EXPIRATIONDATE = "expirationDate";
    private static final List<String> CONVERT_UTC_LONG_KEYLIST = new ArrayList();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final BigInteger MASK_256;
    private static final ObjectWriter OBJECT_WRITER_UN_PRETTY_PRINTER;

    static {
        MASK_256 = BigInteger.ONE.shiftLeft(256).subtract(BigInteger.ONE);
        OBJECT_MAPPER.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        OBJECT_MAPPER.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        OBJECT_WRITER_UN_PRETTY_PRINTER = OBJECT_MAPPER.writer();
        CONVERT_UTC_LONG_KEYLIST.add(KEY_CREATED);
        CONVERT_UTC_LONG_KEYLIST.add(KEY_ISSUANCEDATE);
        CONVERT_UTC_LONG_KEYLIST.add(KEY_EXPIRATIONDATE);
    }

    public static <T> String serialize(T object) {
        StringWriter write = new StringWriter();

        try {
            OBJECT_MAPPER.writeValue(write, object);
        } catch (JsonGenerationException var3) {
            logger.error("JsonGenerationException when serialize object to json", var3);
        } catch (JsonMappingException var4) {
            logger.error("JsonMappingException when serialize object to json", var4);
        } catch (IOException var5) {
            logger.error("IOException when serialize object to json", var5);
        }

        return write.toString();
    }

    public static String decodeOutputReturnString0x16(String input) {
        if (input.length() <= 10) {
            return "unknow contract error!";
        } else {
            Function function = new Function("Error", Collections.emptyList(), Collections.singletonList(new TypeReference<Utf8String>() {
            }));
            List<Type> r = FunctionReturnDecoder.decode(input.substring(10), function.getOutputParameters());
            return ((Type) r.get(0)).toString();
        }
    }

    public static <T> ResponseData<T> processReceiptMsg(TransactionReceipt receipt, T t) {
        String errorMsg = decodeOutputReturnString0x16(receipt.getOutput());
        if (errorMsg.contains("|")) {
            String errorCode = StringUtils.split(errorMsg)[0];
            return new ResponseData(t, ErrorCode.getTypeByErrorCode(Integer.valueOf(errorCode)));
        } else {
            return new ResponseData(t, ErrorCode.UNKNOW_ERROR.getCode(), errorMsg);
        }
    }

    public static <T> ResponseData<T> handleTransaction(TransactionReceipt transactionReceipt, T t) {
        ResponseData<T> responseData = null;
        if (!transactionReceipt.isStatusOK()) {
            responseData = DataToolUtils.processReceiptMsg(transactionReceipt, t);
        } else {
            responseData = new ResponseData<>(t, ErrorCode.SUCCESS, new TransactionInfo(transactionReceipt));
        }
        return responseData;
    }

}
