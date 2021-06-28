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

package com.webank.openledger.core.exception;


import com.webank.openledger.core.constant.ErrorCode;

public class OpenLedgerBaseException extends Exception {

    /**
     * Error code.
     */
    protected int code;

    /**
     * Error message.
     */
    protected String message;

    /**
     * Construction.
     *
     * @param message the message
     * @param cause the cause
     */
    public OpenLedgerBaseException(String message, Throwable cause) {
        super(getText(-1, message), cause);
        this.code = -1;
        this.message = message;
    }

    /**
     * Construction.
     *
     * @param message the message
     */
    public OpenLedgerBaseException(String message) {
        super(getText(-1, message));
        this.code = -1;
        this.message = message;
    }

    /**
     * Construction.
     *
     * @param errorCode the code and message
     */
    public OpenLedgerBaseException(ErrorCode errorCode) {
        super(getText(errorCode.getCode(), errorCode.getErrMsg()));
        this.code = errorCode.getCode();
        this.message = errorCode.getErrMsg();
    }

    /**
     * Construction.
     *
     * @param code the code
     * @param message reason
     */
    public OpenLedgerBaseException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * getText
     *
     * @param code the code
     * @param message the message
     * @return java.lang.String
     */
    private static String getText(int code, String message) {
        return "Code: " +
                code +
                ", Message: " +
                message;
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return message;
    }
}
