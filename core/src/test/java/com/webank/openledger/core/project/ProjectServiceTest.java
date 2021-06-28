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

package com.webank.openledger.core.project;


import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.webank.openledger.contracts.Project;
import com.webank.openledger.core.AccountImplTest;
import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.constant.ErrorCode;
import com.webank.openledger.core.exception.OpenLedgerBaseException;
import com.webank.openledger.core.response.ResponseData;

import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Slf4j
public class ProjectServiceTest {
    private static final String ORG_1_ADMIN_PRI_KEY = "0x4cadf8e1724eb6fd8f5b55b3371f6039a3769fd8b7c40fbce68ca1ccf61b0563";
    private static final String ORG_1_ADMIN_ADDR = "0x745f494b4646e8a64bddd20bc334a6a4e07c297c";
    private static final String ORG_1_USER_PRI_KEY = "0x1c0362fb21ea3cc00dd2dbc0307232cc50782fb355bb507f5f9312d0e10db618";
    private static final String ORG_1_USER_ADDR = "0xa1ebac1c01725ef092f0a2d7192c6ab37106cc86";
    Blockchain blockchain;
    //合约地址
    String projectAddr = "0x6df88fe42bf13bbd42b156fc8c83a438845336e1";
    String accountManagerAddr = "0xde5248911bca2468e71efc7a57e5002963917fd5";
    String authManagerAddr = "0x4ce6fc942eb79e89dbe597eac4dab78a8c29c2f1";
    String authCenterAddr = "0x1254e601ecde8bad5372ba188b26cb2052b56cde";
    String assetManagerAddr = "0xa41fac00c332b00bb36beb2772c831fb57fe2a80";
    String nonFungibleAssetManager="0x8f7b84195cfd596706fec3159cc5b083e173bada";


    //组织org
    String org1Addr = "0x5106c2658d88a4e21137e259cb684b4d3741b65e";
    String org2Addr = "0x0";
    private ProjectService<Project> projectService;

    @Before
    public void init() {
        log.info("Begin Test-----------------");
        blockchain = new Blockchain("application.properties");
        this.projectService = new ProjectService(blockchain, projectAddr);
        log.info("project superAdmin:{}", blockchain.getProjectAccount().getKeyPair().getAddress());
    }
//
//    @Test
//    public void testDeploy() throws ContractException {
//        Project project = Project.deploy(blockchain.getDefaultClient(), blockchain.getProjectAccount().getKeyPair(),
//                accountManagerAddr, authCenterAddr, assetManagerAddr,nonFungibleAssetManager);
//        projectAddr = project.getContractAddress();
//        log.info("testDeploy:{}", projectAddr);
//    }

    @Test
    public void testCreateProject() {
        ResponseData<List<String>> rsp = this.projectService.createProject();
        assertTrue(rsp.getErrorCode() == ErrorCode.SUCCESS.getCode());
        log.info("create project:{}", rsp.getResult());
        log.info("\r\n String projectAddr = \"{}\"; \r\n    " +
                        "String accountManagerAddr = \"{}\"; \r\n   " +
                        "String authManagerAddr = \"{}\"; \r\n   " +
                        "String authCenterAddr = \"{}\";  \r\n  " +
                        "String assetManagerAddr = \"{}\"; \r\n "+"String nonFungibleAssetManager=\"{}\"; ",
                rsp.getResult().get(0),
                rsp.getResult().get(1),
                rsp.getResult().get(2),
                rsp.getResult().get(3),
                rsp.getResult().get(4),
                rsp.getResult().get(5));

    }

    @Test
    public void testCreateOrganization() throws ContractException {
        log.info(projectService.getProject().getAuthCenter());
        ResponseData<String> ret = this.projectService.createOrganization();
        log.info("testCreateOrganization:{}, {}", ret.getResult(), ret.getErrMsg());
        assertEquals(ErrorCode.SUCCESS.getCode(), ret.getErrorCode().intValue());
        assertTrue(!ret.getResult().isEmpty());
    }

    @Test
    public void testAddOrgDefaultAuth() {
        this.projectService.addOrgDefaultAuth(authManagerAddr, org1Addr);
    }

    @Test
    public void testAddDefaultKeyType() {
        this.projectService.addDefaultKeyType(authCenterAddr);
    }
    CryptoKeyPair admin;
    CryptoKeyPair operator;
    CryptoSuite ecdsaCryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);

    @Test
    public void testCreateAddOrgAdmin() throws OpenLedgerBaseException, ContractException {

        String pemFile = AccountImplTest.class.getClassLoader().getResource("conf/test.pem").getPath();
        ecdsaCryptoSuite.loadAccount("pem", pemFile, "");
        admin = ecdsaCryptoSuite.getCryptoKeyPair();
        log.info(admin.getAddress());
        pemFile = AccountImplTest.class.getClassLoader().getResource("conf/test2.pem").getPath();
        ecdsaCryptoSuite.loadAccount("pem", pemFile, "");
        operator = ecdsaCryptoSuite.getCryptoKeyPair();
        LinkedHashMap<String, String> kvMap = new LinkedHashMap<>();

        List<byte[]> keyList = new ArrayList<>();
        List<byte[]> valueList = new ArrayList<>();
        List<byte[]> kvList = new ArrayList<>();
        for(Map.Entry<String,String> entry : kvMap.entrySet()){
            byte[] k = entry.getKey().getBytes(StandardCharsets.UTF_8);
            byte[] v = entry.getValue().getBytes(StandardCharsets.UTF_8);
            keyList.add(k);
            valueList.add(v);
            kvList.add(k);
            kvList.add(v);
        }

        ResponseData<String> ret = projectService.createAddOrgAdmin(org1Addr, admin.getAddress(), keyList, valueList);
        ret = projectService.createAddOrgAdmin(org1Addr, operator.getAddress(), keyList, valueList);
        log.info("testCreateAddOrgAdmin:{},{}", ret.getResult(), ret);
        assertEquals(ErrorCode.SUCCESS.getCode(), ret.getErrorCode().intValue());
        assertTrue(!ret.getResult().isEmpty());
    }

    @Test
    public void testCreateOrganizationAndAddDefaultAuth() {
        ResponseData<String> ret = this.projectService.createOrganization();
        log.info("testCreateOrganizationAndAddDefaultAuth:{}, {}", ret.getResult(), ret.getErrMsg());
        assertEquals(ErrorCode.SUCCESS.getCode(), ret.getErrorCode().intValue());
        assertTrue(!ret.getResult().isEmpty());
        this.projectService.addOrgDefaultAuth(authManagerAddr, ret.getResult());
        this.projectService.addDefaultKeyType(authCenterAddr);
    }

    @Test
    public void testProjectGet() throws ContractException {
        ResponseData<List<String>> ret = projectService.getAllOrg();
        log.info("testProjectGet:{},{}", ret.getResult(), ret);
        assertEquals(ret.getErrorCode().intValue(), ErrorCode.SUCCESS.getCode());
        log.info(projectService.getProject().getTerm());
    }

    @Test
    public void testAddOrgAdmin() throws OpenLedgerBaseException {
        String tobeAddedAdmin = "0x1";
        ResponseData<Boolean> ret = projectService.addOrgAdmin(org1Addr, ORG_1_USER_ADDR);
        assertEquals(ErrorCode.SUCCESS.getCode(), ret.getErrorCode().intValue());
        assertTrue(!ret.getResult());
    }

}