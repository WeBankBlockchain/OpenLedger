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

package com.webank.openledger.core.constant;

import lombok.Getter;

/**
 *  project error code
 */
@Getter
public enum ErrorCode {
    //java sdk error code
    SUCCESS(0, "success"),


    UNKNOW_ERROR(160002, "unknow error, please check the error log."),


    TRANSACT_PARAMETER_ERROR(10103, "parameter error!"),

    PARAM_IS_BLANK(10104, "the input param is blank."),
    PARAM_IS_INVALID(10105, "the input param is invalid."),
    ACCOUNT_HAS_BEEN_FROZEN(10106, "account has been frozen."),

    JSON_ENCODE_EXCEPTION(20001, "encode Object to json failed"),
    JSON_DECODE_EXCEPTION(20002, "decode Object from json failed"),

    TRANSACTION_EXECUTE_ERROR(20003, "the transaction does not correctly executed."),
    UNKNOWN_SOLIDITY_VERSION(20004, "unknown contract version"),


    // contract error code

    ASSET_DESIPOSIT_ERROR(50002, "asset deposit fail,please check the account"),
    ASSET_WITHDRAWAL_ERROR(50003, "asset withDrawal fail,please check the account and balance"),
    ASSET_TRANSFER_ERROR(50004, "asset transfer fail,please check the account and balance"),
    CONTRACT_GETADDRESS_ERROR(50005, "get contract fail,please check the address of the contract"),
    ASSET_BOOKQUERY_ERROR(50006, "query book fail ,please check the args"),
    ASSET_ADDBOOK_EROOR(50007, "add book fail ,please check the contract"),
    ASSET_GETBALEANCE_ERROR(50008, "get balance of asset error,please check the account"),
    ASSET_GETTOTALBALEANCE_ERROR(50009, "get  total balance of asset error,please check the contract"),
    ASSET_GETACCOUNTLIST_ERROR(50010, "get account list of asset error,please check the contract"),
    ASSET_GETASSETINFO_ERROR(50011, "get asset info error,please check the contract"),
    ASSET_CREATE_ERROR(50012, "assetname is invalid"),
    ASSET_DEPLOY_ERROR(50013, "asset contract deploy fail"),
    NONHOMOASSET_VALUE_ERROR(50014,"value not support type list"),

    PROJECT_DEPLOY_ERROR(500101, "project contract deploy fail"),
    PROJECT_CREATE_ORG_ERROR(500102, "project create organization fail"),
    STORAGE_KV_SIZE_NOT_MATCH(500103, "key value list num not match"),

    CURRENCY_GETINFO_ERROR(50014, "get currency info error, please check the contract"),
    ASSET_UNKNOW_ERROR(50015, "asset unknown error,please check the error log"),
    TERM_UNKNOW_ERROR(50016, "term unknown error,please check the error log"),
    SEQUENCE_UNKNOW_ERROR(50017, "sequence unknown error,please check the error log"),
    IDENTITY_UNKNOW_ERROR(50018, " identity unknown error,please check the error log");




    /**
     * error code.
     */
    private int code;

    /**
     * error message.
     */
    private String errMsg;

    /**
     * Error Code Constructor.
     *
     * @param code The ErrorCode
     * @param errMsg The error message
     */
    ErrorCode(int code, String errMsg) {
        this.code = code;
        this.errMsg = errMsg;
    }

    /**
     * get ErrorType By errcode.
     *
     * @param errorCode the ErrorCode
     * @return errorCode
     */
    public static ErrorCode getTypeByErrorCode(int errorCode) {
        for (ErrorCode type : ErrorCode.values()) {
            if (type.getCode() == errorCode) {
                return type;
            }
        }
        return ErrorCode.UNKNOW_ERROR;
    }


}