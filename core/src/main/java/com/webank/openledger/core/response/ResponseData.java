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

import com.webank.openledger.core.constant.ErrorCode;

import lombok.Data;
import lombok.NonNull;

@Data
public class ResponseData<T> {
    /**
     * The generic type result object.
     */
    private T result;

    /**
     * The error code.
     */
    private Integer errorCode;

    /**
     * The error message.
     */
    private String errMsg;

    /**
     * The blockchain transaction info. Note that this transaction only becomes valid (not null nor
     * blank) when a successful transaction is sent to chain with a block generated.
     */
    private TransactionInfo transactionInfo = null;

    /**
     * Instantiates a new response data.
     */
    public ResponseData() {
    }

    /**
     * Instantiates a new response data. Transaction info is left null to avoid unnecessary boxing.
     *
     * @param result the result
     * @param errorCode the return code
     */
    public ResponseData(T result, @NonNull ErrorCode errorCode) {
        this.result = result;
        this.errorCode = errorCode.getCode();
        this.errMsg = errorCode.getErrMsg();
    }

    /**
     * Instantiates a new response data with transaction info.
     *
     * @param result the result
     * @param errorCode the return code
     * @param transactionInfo transactionInfo
     */
    public ResponseData(T result, @NonNull ErrorCode errorCode, @NonNull TransactionInfo transactionInfo) {
        this.result = result;
        this.errorCode = errorCode.getCode();
        this.errMsg = errorCode.getErrMsg();
        this.transactionInfo = transactionInfo;
    }

    /**
     * Instantiates a new Response data based on the error code and error message.
     *
     * @param result the result
     * @param errorCode code number
     * @param errMsg errMsg
     */
    public ResponseData(T result, Integer errorCode, String errMsg) {
        this.result = result;
        this.errorCode = errorCode;
        this.errMsg = errMsg;
    }

    /**
     * set a ErrorCode type errorCode.
     *
     * @param errorCode the errorCode
     */
    public void setErrorCode(ErrorCode errorCode) {
        if (errorCode != null) {
            this.errorCode = errorCode.getCode();
            this.errMsg = errorCode.getErrMsg();
        }
    }
}
