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

package com.webank.openledger.core.account;

import java.math.BigInteger;
import java.util.HashMap;

import com.webank.openledger.contracts.Account;
import com.webank.openledger.core.AccountImplTest;
import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.auth.AuthCenterService;
import com.webank.openledger.core.constant.ErrorCode;
import com.webank.openledger.core.exception.OpenLedgerBaseException;
import com.webank.openledger.core.response.ResponseData;
import com.webank.openledger.utils.OpenLedgerUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@Slf4j
public class AccountServiceTest {
    private static CryptoSuite ecdsaCryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
    private static final String AUTH_ADDRESS = "0x0347d20c85c85afd3cb0b4844c43e9d11d8c11b9";
    private static final String ORG_ADDRESS = "0x245d2c8a83b42f48051fad589ff240e8a2565de5";
    AccountService accountService;
    Blockchain blockchain;
    CryptoKeyPair admin;
    private AuthCenterService authCenterService;
    private String contractAddress = "0xb273c2a754638feedeb1689a15babaac12471e98";

    @Before
    public void init() {
        log.info("Begin Test-----------------");
        blockchain = new Blockchain("application.properties");

        String pemFile = AccountImplTest.class.getClassLoader().getResource("conf/test.pem").getPath();
        ecdsaCryptoSuite.loadAccount("pem", pemFile, "");
        admin = ecdsaCryptoSuite.getCryptoKeyPair();
        log.info(admin.getAddress());
        if (StringUtils.isNotBlank(contractAddress)) {
            this.accountService = new AccountService(blockchain, contractAddress);
            this.authCenterService = new AuthCenterService<>(blockchain, AUTH_ADDRESS);

        }
    }

    @Test
    public void deploy() throws OpenLedgerBaseException, ContractException {
        Account account = Account.deploy(blockchain.getClient(Blockchain.DEFAULT_LEDGERID), blockchain.getProjectAccount().getKeyPair(), AUTH_ADDRESS, ORG_ADDRESS);
        log.info(account.getDeployReceipt().getMessage());
        log.info(account.getDeployReceipt().getContractAddress());
        contractAddress = account.getDeployReceipt().getContractAddress();
    }

    @Test
    public void testGetAllAssets() {
        Boolean isFungible=true;
//        accountService.contractIns.addAsset("0x7a3bc024c1127cae3c108f1e2bc49e51b067cc57", admin.getAddress(),isFungible);
        BigInteger nonce = (BigInteger) authCenterService.getNonceFromAccount(admin.getAddress()).getResult();
        byte[] message = OpenLedgerUtils.computeKeccak256HashFromBigInteger(nonce);
        ResponseData<HashMap> responseData = accountService.getAllAssets(isFungible,message, OpenLedgerUtils.sign(admin, message));
        log.info(responseData.getErrMsg());
        Assert.assertTrue(ErrorCode.SUCCESS.getCode() == responseData.getErrorCode().intValue());
        HashMap<String, String> map = responseData.getResult();

        map.entrySet().stream().forEach((entry) -> {
            System.out.print(entry.getKey());
            System.out.print(entry.getValue());
        });
    }
}