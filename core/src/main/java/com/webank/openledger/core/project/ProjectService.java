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


import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.webank.openledger.contracts.AssetPool;
import com.webank.openledger.contracts.AssetPoolManager;
import com.webank.openledger.contracts.AuthCenter;
import com.webank.openledger.contracts.Currency;
import com.webank.openledger.contracts.CurrencyManager;
import com.webank.openledger.contracts.FungibleAsset;
import com.webank.openledger.contracts.FungibleAssetManager;
import com.webank.openledger.contracts.NonFunStorageManager;
import com.webank.openledger.contracts.NonFungibleAsset;
import com.webank.openledger.contracts.NonFungibleAssetManager;
import com.webank.openledger.contracts.Project;
import com.webank.openledger.contracts.gov_account.AccountManager;
import com.webank.openledger.contracts.gov_auth.AuthManager;
import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.auth.AuthCenterService;
import com.webank.openledger.core.common.BaseAsset;
import com.webank.openledger.core.constant.ErrorCode;
import com.webank.openledger.core.govAuth.AuthManagerService;
import com.webank.openledger.core.project.model.ProjectManager;
import com.webank.openledger.core.response.DataToolUtils;
import com.webank.openledger.core.response.ResponseData;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple1;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple3;
import org.fisco.bcos.sdk.contract.Contract;
import org.fisco.bcos.sdk.model.TransactionReceipt;

/**
 * Porject Service
 *
 * @param <T> object which extend contract
 */
@Slf4j
@Getter
@Setter
public class ProjectService<T extends Contract> {
    public static final String TYPE_PUBLIC = "public"; //all can read, cannot write
    public static final String TYPE_OWNER = "owner";  //owner can read, cannot write
    public static final String TYPE_ADMIN = "admin";  //admin can read, can write
    //    private AssetManager assetManager;
    public static final String ADMIN_ADDR = "0x10001";
    public static final String OPERATOR_ADDR = "0x10002";
    public static final String OWNER_ADDR = "0x10003";
    public static final String USER_ADDR = "0x10004";
    private static final String SIGNTRANSFER_METHOD_PREFIX = "getSignedTransactionFor";
    private static final String FUNC_SUBSCRIBE_TRANSFER = "subscribeTransferEvent";
    /**
     * default admin auth keys
     */
    private static final String[] FUNC_ADMIN_METHOD_PREFIX = {
            "createAccount", "cancel", "freeze", "unfreeze", "changeExternalAccount",
            "addAdmin", "removeAdmin", "createAsset",
            "setPrice", "setRate", "getTotalBalance",
            "getHolders", "openAccount", "addBook",
            "insertWithSign", "setWithSign", "addWithSign", "removeWithSign", "deposit", "withdrawal",
            "setNonFungiblePrice", "openNonFungibleAccount", "issueNonFungible",
            "accountContainsNote", "updateNoteNo", "getNoteProperties",
            "updateNoteBatch", "freezeNote", "unfreezeNote", "updateNoteProperties", "updateNoteBatch", "tearNote", "getTearNotes", "enableBatch"
    };

    /**
     * public auth keys
     */
    private static final String[] FUNC_ASSET_PUBLIC = {"queryBook"};
    /**
     * blockchain property
     */
    protected Blockchain blockchain;
    /**
     * project contract object
     */
    protected Project project;
    /**
     * authcenter contract object
     */
    private AuthCenter authCenter;

    /**
     * initializes the contract object
     *
     * @param blockchain blockchain property
     * @param contractAddress project contract addres
     */
    public ProjectService(Blockchain blockchain, String contractAddress) {
        this.blockchain = blockchain;
        if (null != contractAddress && !contractAddress.isEmpty()) {
            project = blockchain.getLedger(Blockchain.DEFAULT_LEDGERID).getContract(contractAddress, Project.class);
        }
    }

    /**
     * get contract object
     *
     * @return
     */
    public T getContractIns() {
        return project == null ? null : (T) project;
    }

    /**
     * create project
     *
     * @return
     */
    public ResponseData<List<String>> createProject() {
        try {
            ProjectManager projectManager = new ProjectManager();
            AccountManager accountManager = AccountManager.deploy(blockchain.getDefaultClient(), blockchain.getProjectAccount().getKeyPair());
            projectManager.setAccountManagerAddr(accountManager.getContractAddress());

            AuthManager authManager = AuthManager.deploy(blockchain.getDefaultClient(), blockchain.getProjectAccount().getKeyPair(),
                    BigInteger.valueOf(1), new ArrayList<>(), new ArrayList<>(), BigInteger.valueOf(0));
            projectManager.setAuthManagerAddr(authManager.getContractAddress());

            AuthCenter authCenter = AuthCenter.deploy(blockchain.getDefaultClient(), blockchain.getProjectAccount().getKeyPair(),
                    projectManager.getAccountManagerAddr(), projectManager.getAuthManagerAddr());
            projectManager.setAuthCenterAddr(authCenter.getContractAddress());

            FungibleAssetManager fungibleAssetManager = FungibleAssetManager.deploy(blockchain.getDefaultClient(), blockchain.getProjectAccount().getKeyPair());
            projectManager.setFungibleAssetManagerAddr(fungibleAssetManager.getContractAddress());

            NonFungibleAssetManager nonFungibleAssetManager = NonFungibleAssetManager.deploy(blockchain.getDefaultClient(), blockchain.getProjectAccount().getKeyPair());
            projectManager.setNonFungibleAssetManagerAddr(nonFungibleAssetManager.getContractAddress());

            NonFunStorageManager nonFunStorageManager = NonFunStorageManager.deploy(blockchain.getDefaultClient(), blockchain.getProjectAccount().getKeyPair(), nonFungibleAssetManager.getContractAddress());
           projectManager.setNonFungibleAssetStorage(nonFunStorageManager.getContractAddress());

            TransactionReceipt transactionReceipt = nonFungibleAssetManager.setStorageManager(nonFunStorageManager.getContractAddress());
            log.info("NonFunStorageManager:{}", nonFunStorageManager.getContractAddress());
            if (!transactionReceipt.isStatusOK()) {
                throw new Exception("set storage fail!");
            }

            CurrencyManager currencyManager = CurrencyManager.deploy(blockchain.getDefaultClient(), blockchain.getProjectAccount().getKeyPair());
            projectManager.setCurrencyManagerAddr(currencyManager.getContractAddress());

            AssetPoolManager assetPoolManager = AssetPoolManager.deploy(blockchain.getDefaultClient(), blockchain.getProjectAccount().getKeyPair());
            projectManager.setAssetPoolManagerAddr(assetPoolManager.getContractAddress());

            project = Project.deploy(blockchain.getDefaultClient(), blockchain.getProjectAccount().getKeyPair(),
                    projectManager.genAddressList());
            String projectAddr = project.getContractAddress();

            log.info("createProject success! superAdmin:{}, " +
                            "projectAddr:{}, accountManagerAddr:{}, authManagerAddr:{}, authCenterAddr:{}, assetManagerAddr:{}",
                    blockchain.getProjectAccount().getKeyPair().getAddress(),
                    projectAddr, projectManager.getAccountManagerAddr(), projectManager.getAuthManagerAddr(), projectManager.getAuthCenterAddr(), projectManager.getFungibleAssetManagerAddr());
            projectManager.setProjectAddr(projectAddr);
            return new ResponseData<>(projectManager.getResult(), ErrorCode.SUCCESS);

        } catch (Exception e) {
            log.error("createProject deploy contract error:{} ", e.toString());
            return new ResponseData<>(null, ErrorCode.PROJECT_DEPLOY_ERROR);
        }
    }

    /**
     * create organzation
     *
     * @return
     */
    public ResponseData<String> createOrganization() {
        TransactionReceipt transactionReceipt = project.createOrganization();
        log.info("createOrganization:{}", transactionReceipt.toString());
        String result = transactionReceipt.isStatusOK() ?
                project.getCreateOrganizationOutput(transactionReceipt).getValue1() : "";
        ResponseData<String> responseData = DataToolUtils.handleTransaction(transactionReceipt, result);
        return responseData;
    }

    /**
     * set default auth to admin、public and owner
     *
     * @param authMangerAddr authmanager contract address
     * @param orgAddr org contract address
     */
    public void addOrgDefaultAuth(String authMangerAddr, String orgAddr) {
        AuthManagerService authManagerSDK = new AuthManagerService(blockchain, authMangerAddr);
        AuthManager authManager = (AuthManager) authManagerSDK.getContractIns();

        String groupPre = orgAddr.substring(0, 5);

        String adminGroup = groupPre + "_admin";
        authManager.createGroup(adminGroup, BigInteger.valueOf(1));//1:white list
        authManager.addAccountToGroup(ADMIN_ADDR, adminGroup);
        //permissions related to account operation
        authManager.addFunctionToGroup(orgAddr, TYPE_ADMIN, adminGroup);

        String ownerGroup = groupPre + "_owner";
        authManager.createGroup(ownerGroup, BigInteger.valueOf(1));
        authManager.addAccountToGroup(ADMIN_ADDR, ownerGroup);
        authManager.addAccountToGroup(OWNER_ADDR, ownerGroup);
        //permissions related to account operation
        authManager.addFunctionToGroup(orgAddr, TYPE_OWNER, ownerGroup);


        String publicGroup = groupPre + "_public";
        authManager.createGroup(publicGroup, BigInteger.valueOf(1));
        authManager.addAccountToGroup(ADMIN_ADDR, publicGroup);
        authManager.addAccountToGroup(OWNER_ADDR, publicGroup);
        authManager.addAccountToGroup(USER_ADDR, publicGroup);
        //permissions related to account operation
        authManager.addFunctionToGroup(orgAddr, TYPE_PUBLIC, publicGroup);


    }

    /**
     * call contract function to add keytypes
     *
     * @param authCenterAddr auth center contract address
     * @return
     */
    public ResponseData<Boolean> addDefaultKeyType(String authCenterAddr) {
        AuthCenterService authCenterService = new AuthCenterService(blockchain, authCenterAddr);
        Set<String> ownerMethod = getAllAssetFunc();
        Set<String> adminMethod = getAllAdminFunc();

        Map<String, String> key2Type = new HashMap<>();
        ownerMethod.stream().forEach(item -> key2Type.put(item, TYPE_OWNER));
        Arrays.stream(FUNC_ASSET_PUBLIC).forEach(item -> key2Type.put(item, TYPE_PUBLIC));
        Arrays.stream(FUNC_ADMIN_METHOD_PREFIX).forEach(item -> key2Type.put(item, TYPE_ADMIN));
        adminMethod.stream().forEach(item -> key2Type.put(item, TYPE_ADMIN));

        for (Map.Entry<String, String> entry : key2Type.entrySet()) {
            try {
                ResponseData<Boolean> ret = authCenterService.addKeyType(entry.getKey().getBytes(StandardCharsets.UTF_8),
                        entry.getValue().getBytes(StandardCharsets.UTF_8));
                log.info("authCenter.addKeyType result:{},{},{},{}", entry.getKey(), entry.getValue(), ret.getResult(), ret);
            } catch (Exception e) {
                log.error("authCenter.addKeyType error:{},{},{}", entry.getKey(), entry.getValue(), e.toString());
            }
        }
        return new ResponseData<>(true, ErrorCode.SUCCESS);
    }

    /**
     * create account and add this account as admin to organzation
     *
     * @param org org contract address
     * @param externalAccount account external address
     * @param keyList account property keys
     * @param valueList account property values
     * @return
     */
    public ResponseData<String> createAddOrgAdmin(String org, String externalAccount, List<byte[]> keyList, List<byte[]> valueList) {
        TransactionReceipt transactionReceipt = project.createAddOrgAdmin(org, externalAccount, keyList, valueList);
        if (transactionReceipt.isStatusOK()) {
            Tuple2<Boolean, String> ret = project.getCreateAddOrgAdminOutput(transactionReceipt);
            return new ResponseData<>(ret.getValue2(), ErrorCode.SUCCESS);
        }
        ResponseData<String> responseData = DataToolUtils.handleTransaction(transactionReceipt, "");
        return responseData;
    }

    /**
     * add organzation's admin
     * requried account has been create in organzation
     *
     * @param org org contract address
     * @param externalAccount account external address
     * @return
     */
    public ResponseData<Boolean> addOrgAdmin(String org, String externalAccount) {
        TransactionReceipt transactionReceipt = project.addOrgAdmin(org, externalAccount);
        Boolean result = transactionReceipt.isStatusOK() ? project.getAddOrgAdminOutput(transactionReceipt).getValue1() : false;
        ResponseData<Boolean> responseData = DataToolUtils.handleTransaction(transactionReceipt, result);
        return responseData;
    }

    /**
     * whether account has been exist in this project
     *
     * @param externalAccount account  external address
     * @return boolean is exist
     */
    public ResponseData<Boolean> hasAccount(String externalAccount) {
        TransactionReceipt transactionReceipt = project.hasAccount(externalAccount);
        Boolean result = transactionReceipt.isStatusOK() ? project.getHasAccountOutput(transactionReceipt).getValue1() : false;
        ResponseData<Boolean> responseData = DataToolUtils.handleTransaction(transactionReceipt, result);
        return responseData;
    }

    /**
     * get orgs of project
     *
     * @return
     */
    public ResponseData<List<String>> getAllOrg() {
        List<String> orgList = new ArrayList<>();

        TransactionReceipt transactionReceipt = project.getAllOrg();
        if (!transactionReceipt.isStatusOK()) {
            return DataToolUtils.processReceiptMsg(transactionReceipt, orgList);
        }
        Tuple1<List<String>> ret = project.getGetAllOrgOutput(transactionReceipt);
        return new ResponseData<>(ret.getValue1(), ErrorCode.SUCCESS);
    }

    /**
     * get assets of project
     *
     * @param isFungible boolean is fungible
     * @return
     */
    public ResponseData<HashMap> getAllAsset(Boolean isFungible) {
        HashMap<String, String> assets = new HashMap<>();

        TransactionReceipt transactionReceipt = project.getAllAsset(isFungible);
        if (!transactionReceipt.isStatusOK()) {
            return DataToolUtils.handleTransaction(transactionReceipt, assets);
        }

        Tuple3<List<String>, List<String>, BigInteger> ret = project.getGetAllAssetOutput(transactionReceipt);
        if (ret.getValue1().size() != ret.getValue2().size()) {
            return DataToolUtils.handleTransaction(transactionReceipt, assets);
        }

        int num = ret.getValue3().intValue();
        for (int i = 0; i < ret.getValue1().size() && i < num; i++) {
            assets.put(ret.getValue1().get(i), ret.getValue2().get(i));
        }

        return DataToolUtils.handleTransaction(transactionReceipt, assets);
    }

    /**
     * Gets a collection of all method names for an asset through a reflection class method
     *
     * @return
     */
    private Set<String> getAllAssetFunc() {
        Class[] assets = new Class[]{BaseAsset.class, FungibleAsset.class, Currency.class, NonFungibleAsset.class};
        Set<String> ownerMethodName = new HashSet<>();
        for (Class assetClass : assets) {
            Method[] methods = assetClass.getDeclaredMethods();
            Arrays.stream(methods).forEach(item -> {
                Class[] paramTypes = item.getParameterTypes();
                Set<Class> set = Arrays.stream(paramTypes).filter(param -> param.equals(List.class)).collect(Collectors.toSet());
                if (set.size() > 0 && !item.getName().startsWith(SIGNTRANSFER_METHOD_PREFIX) && !FUNC_SUBSCRIBE_TRANSFER.equals(item.getName())) {
                    if (paramTypes.length > 1 &&
                            Arrays.stream(FUNC_ADMIN_METHOD_PREFIX).filter(m -> item.getName().startsWith(m)).count() == 0 &&
                            Arrays.stream(FUNC_ASSET_PUBLIC).filter(f -> f.equals(item.getName())).count() == 0) {
                        ownerMethodName.add(item.getName());
                    }
                }
            });
            log.info("ownermethod:" + ownerMethodName.toString());
        }

        return ownerMethodName;
    }

    /**
     * Gets the administrator permission set through reflection.
     *
     * @return
     */
    private Set<String> getAllAdminFunc() {
        Class[] assets = new Class[]{AssetPool.class,};
        Set<String> adminMethodName = new HashSet<>();
        for (Class assetClass : assets) {
            Method[] methods = assetClass.getDeclaredMethods();
            Arrays.stream(methods).forEach(item -> {
                Class[] paramTypes = item.getParameterTypes();
                Set<Class> set = Arrays.stream(paramTypes).filter(param -> param.equals(List.class)).collect(Collectors.toSet());
                if (set.size() > 0 && !item.getName().startsWith(SIGNTRANSFER_METHOD_PREFIX) && !FUNC_SUBSCRIBE_TRANSFER.equals(item.getName())) {
                    adminMethodName.add(item.getName());
                }
            });
            log.info("adminMethodName:" + adminMethodName.toString());
        }

        return adminMethodName;
    }

}
