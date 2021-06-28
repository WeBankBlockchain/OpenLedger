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

package com.webank.openledger.demo.service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.webank.openledger.contracts.AuthCenter;
import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.auth.AuthCenterService;
import com.webank.openledger.core.exception.OpenLedgerBaseException;
import com.webank.openledger.core.org.OrganizationService;
import com.webank.openledger.core.project.ProjectService;
import com.webank.openledger.core.response.ResponseData;
import com.webank.openledger.core.term.TermService;
import com.webank.openledger.demo.Config;
import com.webank.openledger.demo.entity.ProjectEntity;
import com.webank.openledger.demo.holder.ProjectHolder;
import com.webank.openledger.utils.OpenLedgerUtils;

import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;

/**
 *  initialize Service
 * @author pepperli
 */
@Slf4j
public class InitService {
    CryptoSuite ecdsaCryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
    ProjectService projectService;
    OrganizationService orgService;
    AuthCenterService<AuthCenter> authCenterService;
    TermService termService;
    Blockchain blockchain;
    Config config;
    String projectAddr;
    String authManagerAddr;
    String authCenterAddr;
    String accountManagerAddr;
    String orgAddr;
    String accountAddr;
    String termAddr;
    String adminPriKey;
    private static InitService instance = null;

    static {
        instance = new InitService();
    }

    private InitService() {

    }
    public static InitService getInstance() {
        return instance;
    }

    public static void main(String[] args) throws Exception {
        InitService initService = new InitService();

        initService.startInit();
        System.exit(0);
    }

    public String loadProject() {
        config = new Config("application.properties", BaseService.isJar);
        ProjectEntity project = new ProjectEntity(config.getProjectAddr(), config.getAccountManagerAddr(), config.getAuthManagerAddr(), config.getAuthCenterAddr(), config.getOrgAddr());
        ProjectHolder.setProject(project);
        return project.getProjectAddr();
    }

    public String startInit() throws Exception {
        System.out.println("init may need some time,please wait a minute");
        blockchain = new Blockchain("application.properties",BaseService.isJar);
        this.projectService = new ProjectService(blockchain, projectAddr);

        log.info("===========================Start Init=============================================");
        createProject();
        createOrganizationAndAddDefaultAuth();
        createAddOrgAdmin();
        log.info("\r\n project.addr:\"{}\"\r\n" +
                        "accountmanager.addr: \"{}\"\r\n" +
                        "authmanager.addr: \"{}\"\r\n" +
                        "authcenter.addr: \"{}\"\r\n" +
                        "org.addr:\"{}\" \r\n" +
                        "orgAdmin.addr:\"{}\" \r\n" +

                projectAddr,
                accountManagerAddr,
                authManagerAddr,
                authCenterAddr,
                orgAddr,
                accountAddr
        );

        System.out.println("project.addr:" + projectAddr);
        System.out.println("accountmanager.addr:" + accountManagerAddr);
        System.out.println("authmanager.addr:" + authManagerAddr);
        System.out.println("authcenter.addr:" + authCenterAddr);
        System.out.println("org.addr:" + orgAddr);
        System.out.println("orgAdmin.addr(logon):" + accountAddr);
        System.out.println("orgAdmin.prikey:" + adminPriKey);
        ProjectHolder.setProject(new ProjectEntity(projectAddr, accountManagerAddr, authManagerAddr, authCenterAddr, orgAddr));
        log.info("===========================End Init=============================================");
        return projectAddr;
    }


    public void createProject() throws ContractException, OpenLedgerBaseException {
        ResponseData<List<String>> rsp = this.projectService.createProject();
        projectAddr = rsp.getResult().get(0);
        accountManagerAddr = rsp.getResult().get(1);
        authManagerAddr = rsp.getResult().get(2);
        authCenterAddr = rsp.getResult().get(3);
        termAddr = projectService.getProject().getTerm();
        termService = new TermService(blockchain, projectService.getProject().getTerm());
        String termName = "test2021666";
        byte[] message = OpenLedgerUtils.computeKeccak256Hash(termName.getBytes());
        ECDSASignatureResult rs = OpenLedgerUtils.sign(blockchain.getProjectAccount().getKeyPair(), message);
        ResponseData<BigInteger> newTermNo = termService.newTerm(termName, message, rs);
        log.info("term.addr:" + termAddr);

    }

    public void createOrganizationAndAddDefaultAuth() {
        ResponseData<String> ret = this.projectService.createOrganization();
        log.info("testCreateOrganizationAndAddDefaultAuth:{}, {}", ret.getResult(), ret.getErrMsg());
        this.projectService.addOrgDefaultAuth(authManagerAddr, ret.getResult());
        this.projectService.addDefaultKeyType(authCenterAddr);
        orgAddr = ret.getResult();
        log.info("org.addr:" + ret.getResult());

    }

    public void createAddOrgAdmin() throws Exception {
        CryptoKeyPair admin = LoginService.getInstance().createKeyPair();
        LinkedHashMap<String, String> kvMap = new LinkedHashMap<>();

        List<byte[]> keyList = new ArrayList<>();
        List<byte[]> valueList = new ArrayList<>();
        List<byte[]> kvList = new ArrayList<>();
        for (Map.Entry<String, String> entry : kvMap.entrySet()) {
            byte[] k = entry.getKey().getBytes(StandardCharsets.UTF_8);
            byte[] v = entry.getValue().getBytes(StandardCharsets.UTF_8);
            keyList.add(k);
            valueList.add(v);
            kvList.add(k);
            kvList.add(v);
        }
        ResponseData<String> ret = projectService.createAddOrgAdmin(orgAddr, admin.getAddress(), keyList, valueList);
        LoginService.getInstance().login(admin.getHexPrivateKey());
        accountAddr = admin.getAddress();
        adminPriKey = admin.getHexPrivateKey();
        log.info("account.addr:" + ret.getResult());
    }


}
