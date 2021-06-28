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

package com.webank.openledger.core.assetpool.entity;

import java.math.BigInteger;

import lombok.Getter;
import lombok.Setter;

/**
 * asset pool entity
 * @author pepperli
 */
@Getter
@Setter
public class PoolStatus {
    /**
     * asset pool status value
     */
    private BigInteger status;
    /**
     * name of assetpool status
     */
    private String name;
    /**
     * description os assetpool status
     */
    private String desc;

    public PoolStatus(BigInteger status, String name, String desc) {
        this.status = status;
        this.name = name;
        this.desc = desc;
    }
}
