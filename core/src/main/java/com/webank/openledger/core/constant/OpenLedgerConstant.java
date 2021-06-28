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

import java.math.BigInteger;

/**
 *  project constant
 */
public class OpenLedgerConstant {

    /**
     * The Constant GAS_PRICE.
     */
    public static final BigInteger GAS_PRICE = new BigInteger("99999999999");

    /**
     * The Constant GAS_LIMIT.
     */
    public static final BigInteger GAS_LIMIT = new BigInteger("9999999999999");


    public static final String DEFAULT_LEDGER_ID = "1";

    public static final Integer TIMEOUT = 8;

    public static final Integer CORE_POOL_SIZE = 100;

    public static final Integer MAX_POOL_SIZE = 200;

    public static final Integer QUEUE_CAPACITY = 1000;

    public static final Integer KEEP_ALIVE_SECONDS = 60;

    public static final String PATH_SEPARATOR = "/";

    public static final String PRIVATE_KEY_SUFFIX = ".pem";

    public static final String PUBLIC_KEY_SUFFIX = ".pub.pem";

    public static final String HEX_HEADER = "0x";

    public static final String PRIVATE_KEY_DESC = "PRIVATE KEY";

    public static final String PUBLIC_KEY_DESC = "PUBLIC KEY";

    public static final String ALGORITHM = "ECDSA";

    public static final String CURVE_TYPE = "SECP256k1";

    public static final String TRNSACTION_RECEIPT_STATUS_FAILED = "0x16";

    public static final String AUTH_MANAGER_NO_PERMISSION = "Forbidden";

    public static final String TRNSACTION_RECEIPT_STATUS_SUCCESS = "0x0";
}
