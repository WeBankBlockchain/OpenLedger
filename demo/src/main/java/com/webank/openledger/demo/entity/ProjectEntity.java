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
import lombok.extern.slf4j.Slf4j;

/**
 * project entity
 * @author pepperli
 */
@Slf4j
@Getter
@Setter
public class ProjectEntity {
    /**
     * project contract address
     */
    String projectAddr ;
    /**
     * auth manager contract address
     */
    String accountManagerAddr ;
    /**
     * authmanager contract address
     */
    String authManagerAddr;
    /**
     * authcenter contract address
     */
    String authCenterAddr ;
    /**
     *  org contract address
     */
    String orgAddr ;

    public ProjectEntity(String projectAddr, String accountManagerAddr, String authManagerAddr, String authCenterAddr, String orgAddr) {
        this.projectAddr = projectAddr;
        this.accountManagerAddr = accountManagerAddr;
        this.authManagerAddr = authManagerAddr;
        this.authCenterAddr = authCenterAddr;
        this.orgAddr = orgAddr;
    }
}
