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

package com.webank.openledger.demo.entity;

import lombok.Getter;
import lombok.Setter;

/**
 *  asset entity
 * @author pepperli
 */
@Setter
@Getter
public class AssetEntity {
    /**
     * asset contract address
     */
    private String address;
    /**
     * boolean is fungible
     */
    private Boolean isFungible;

    public AssetEntity(String address, Boolean isFungible) {
        this.address = address;
        this.isFungible = isFungible;
    }
}
