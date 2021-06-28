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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.openledger.core.constant.ErrorCode;
import com.webank.openledger.core.response.ResponseData;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;

/**
 * organzation service
 * @author pepperli
 */
@Slf4j
public class OrgService extends BaseService {
    private static OrgService instance = null;

    static {
        instance = new OrgService();
    }

    private OrgService() {
        try {
            this.loadService();
        } catch (Exception e) {
            log.error("load service failed: ", e);
        }
    }

    public static OrgService getInstance() {
        return instance;
    }


    public String createAccount(String userAddr, String signVal) throws Exception {
        Map<String, Object> signAndMsg = ClientService.getInstance().createAccountSignAndMsg(userAddr);
        HashMap<String, String> kvMap = new HashMap<>();
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
        ResponseData<String> ret = organizationService.createAccount(userAddr, keyList, valueList,
                (byte[]) signAndMsg.get("message"), StringUtils.isBlank(signVal) ?
                        new ECDSASignatureResult((String) signAndMsg.get("sign")) : new ECDSASignatureResult(signVal));
        if (ret == null || ErrorCode.SUCCESS.getCode() != ret.getErrorCode()) {
            throw new Exception("createAccount  failed :" + (ret == null ? "" : ret.getErrMsg()));
        }
        return userAddr;
    }

    public Boolean freeze(String accountAddr, String signVal) throws Exception {
        Map<String, Object> signAndMsg = ClientService.getInstance().freezeSignAndMsg(accountAddr);
        ResponseData<Boolean> ret = organizationService.freeze(accountAddr, (byte[]) signAndMsg.get("message"), StringUtils.isBlank(signVal) ?
                new ECDSASignatureResult((String) signAndMsg.get("sign")) : new ECDSASignatureResult(signVal));
        if (ret == null || ErrorCode.SUCCESS.getCode() != ret.getErrorCode()) {
            throw new Exception(" freezeAccount failed :" +( ret == null ? "" : ret.getErrMsg()));
        }
        return ret.getResult();
    }

    public Boolean unfreeze(String accountAddr, String signVal) throws Exception {
        Map<String, Object> signAndMsg = ClientService.getInstance().unfreezeSignAndMsg(accountAddr);

        ResponseData<Boolean> ret = organizationService.unfreeze(accountAddr, (byte[]) signAndMsg.get("message"), StringUtils.isBlank(signVal) ?
                new ECDSASignatureResult((String) signAndMsg.get("sign")) : new ECDSASignatureResult(signVal));
        if (ret == null || ErrorCode.SUCCESS.getCode() != ret.getErrorCode()) {
            throw new Exception("unfreezeAccount failed :" +( ret == null ? "" : ret.getErrMsg()));
        }
        return ret.getResult();
    }

    public Boolean cancel(String accountAddr, String signVal) throws Exception {
        Map<String, Object> signAndMsg = ClientService.getInstance().cancelSignAndMsg(accountAddr);
        ResponseData<Boolean> ret = organizationService.cancel(accountAddr, (byte[]) signAndMsg.get("message"), StringUtils.isBlank(signVal) ?
                new ECDSASignatureResult((String) signAndMsg.get("sign")) : new ECDSASignatureResult(signVal));
        if (ret == null || ErrorCode.SUCCESS.getCode() != ret.getErrorCode()) {
            throw new Exception("cancelAccount failed :" +( ret == null ? "" : ret.getErrMsg()));
        }
        return ret.getResult();
    }

    public Boolean changeExternalAccount(String accountAddr, String newAccountAddr, String signVal) throws Exception {
        Map<String, Object> signAndMsg = ClientService.getInstance().changeExternalAccountSignAndMsg(accountAddr,newAccountAddr);
        ResponseData<Boolean> ret = organizationService.changeExternalAccount(accountAddr, newAccountAddr,  (byte[]) signAndMsg.get("message"), StringUtils.isBlank(signVal) ?
                new ECDSASignatureResult((String) signAndMsg.get("sign")) : new ECDSASignatureResult(signVal));
        if (ret == null || ErrorCode.SUCCESS.getCode() != ret.getErrorCode()) {
            throw new Exception("cancelAccount failed :" + (ret == null ? "" : ret.getErrMsg()));
        }
        return ret.getResult();
    }
}
