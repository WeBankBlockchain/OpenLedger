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

package com.webank.openledger.core.project.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * project manager entity
 * @author pepperli
 */
@Setter
@Getter
@ToString
public class ProjectManager {
    /**
     * project contract address
     */
    private String projectAddr;
    /**
     * account manager contract address
     */
    private String accountManagerAddr;
    /**
     * authmanager contract address
     */
    private String authManagerAddr;
    /**
     * authcenter contract address
     */
    private String authCenterAddr;
    /**
     * fungibleasser manager contract address
     */
    private String fungibleAssetManagerAddr;
    /**
     * nonfungibleasset manager contract address
     */
    private String nonFungibleAssetManagerAddr;
    /**
     * currency manager contract address
     */
    private String currencyManagerAddr;
    /**
     * assetpool manager contract address
     */
    private String assetPoolManagerAddr;
    /**
     * nonFungibleAssetStorage
     */
    private String nonFungibleAssetStorage;

    /**
     * generate address list
     * @return
     */
    public List<String> genAddressList() {
        List<String> managers = new ArrayList<>();
        managers.add(accountManagerAddr);
        managers.add(authCenterAddr);
        managers.add(fungibleAssetManagerAddr);
        managers.add(nonFungibleAssetManagerAddr);
        managers.add(currencyManagerAddr);
        managers.add(assetPoolManagerAddr);
        managers.add(nonFungibleAssetStorage);
        return managers;
    }

    /**
     * generate createProject result
     * @return
     */
    public List<String> getResult() {
        List<String> managers = new ArrayList<>();
        managers.add(projectAddr);
        managers.add(accountManagerAddr);
        managers.add(authManagerAddr);
        managers.add(authCenterAddr);
        managers.add(fungibleAssetManagerAddr);
        managers.add(nonFungibleAssetManagerAddr);
        managers.add(currencyManagerAddr);
        managers.add(assetPoolManagerAddr);
        return managers;
    }
}
