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

package com.webank.openledger.core.auth;


import java.util.HashMap;

import com.webank.openledger.contractsbak.AuthCenter;
import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.constant.ErrorCode;
import com.webank.openledger.core.response.ResponseData;

import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.junit.Before;
import org.junit.Test;

import static com.webank.openledger.core.project.ProjectService.TYPE_ADMIN;
import static org.junit.Assert.assertTrue;

@Slf4j
public class AuthCenterServiceTest {
    Blockchain blockchain;
    //合约地址
    //合约地址
    String projectAddr = "0xe0a3187acc7c4c9f0a07079183b9888fffb59929";
    String accountManagerAddr = "0x87d8ef987158d6a2e72081f8cad9fc55d80a0d86";
    String authManagerAddr = "0x074fce963f1221614bd9201dc57fe5e5de4a1029";
    String authCenterAddr = "0xf8ea74dc218c90eab8c59cdbb40613451a1dce7e";
    String assetManagerAddr = "0xbc170b771aa548e5c8ae0e7faa13f2d4a660d134";
    String nonFungibleAssetManager="0x74f347d71cadf5fbc9ab7c87ffd959f433693d8a";

    //组织org
    String org1Addr = "0xe3dba2a34f8de0e7b1d40270e880ab3b1d80a50a";
    private AuthCenterService<AuthCenter> authCenterSDK;

    @Before
    public void init() {
        log.info("Begin Test-----------------");
        blockchain = new Blockchain("application.properties");
        this.authCenterSDK = new AuthCenterService(blockchain, authCenterAddr);
        log.info("project superAdmin:{}", blockchain.getProjectAccount().getKeyPair().getAddress());
    }

    @Test
    public void testDeploy() throws ContractException {
        AuthCenter authCenter = AuthCenter.deploy(blockchain.getDefaultClient(), blockchain.getProjectAccount().getKeyPair(),
                accountManagerAddr, authManagerAddr);
        authCenterAddr = authCenter.getContractAddress();
        log.info("testDeploy:{}", authCenterAddr);
    }

    @Test
    public void testGetAllKeyType() {
        ResponseData<HashMap> rsp = this.authCenterSDK.getAllKeyType();
        log.info("getAllKeyType:{}, {}", rsp.getResult(), rsp);
        assertTrue(!rsp.getResult().isEmpty());
        rsp.getResult().forEach((k, v) -> {
            log.info("key2type:{},{}", new String((byte[]) k), new String((byte[]) v));
        });
    }

    @Test
    public void testAddKeyType() {
        String key = "getAllAssets";
        String type = TYPE_ADMIN;
        ResponseData<Boolean> rsp = this.authCenterSDK.addKeyType(key.getBytes(), type.getBytes());
        log.info("addKeyType:{}, {}", rsp.getResult(), rsp);
        assertTrue(rsp.getErrorCode() == ErrorCode.SUCCESS.getCode());
    }

    @Test
    public void testRemoveKeyType() {
        String key = "name";
        ResponseData<Boolean> rsp = this.authCenterSDK.removeKeyType(key.getBytes());
        log.info("removeKeyType:{}, {}", rsp.getResult(), rsp);
        assertTrue(rsp.getErrorCode() == ErrorCode.SUCCESS.getCode());
    }


}