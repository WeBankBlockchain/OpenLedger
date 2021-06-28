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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.webank.openledger.core.asset.fungible.entity.Condition;
import com.webank.openledger.core.asset.nonfungible.entity.NonFungibleCondition;
import com.webank.openledger.demo.holder.AssetHolder;

import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;

/**
 * commandline management services
 * @author pepperli
 */
public class CommandService {
    private static CommandService instance = null;

    static {
        instance = new CommandService();
    }

    private CommandService() {
    }

    public static CommandService getInstance() {
        return instance;
    }

    public static String[] COMMANDS = new String[]{"init", "load", "login", "logon", "whoami", "generateAccount", "createAccount", "freeze", "unfreeze", "cancel",
            "changeExternalAccount", "createAsset", "loadAsset", "openAccount", "deposit", "withdrawal", "generateTransferSign", "transfer", "generateQuerySign",
            "isAccountHoldNote", "getAccountNotes", "queryBook", "getBalance","issue","activeBatch","help", "quit"};


    public String getHelpWords() {
        StringBuffer helpBuffer = new StringBuffer();
        helpBuffer.append("init                      create a new project which includes org and orgadmin\r\n");
        helpBuffer.append("load                      load a project from application.properties\r\n");
        helpBuffer.append("login                     load account by privatekey\r\n");
        helpBuffer.append("logout                    exit current logon account\r\n");
        helpBuffer.append("whoami                    return current logon account\r\n");
        helpBuffer.append("\r\n");
        helpBuffer.append("AccountManagement:\r\n");
        helpBuffer.append("generateAccount           create new keypair\r\n");
        helpBuffer.append("createAccount             create orgaccount by orgadmin\r\n");
        helpBuffer.append("freeze                    freeze orgaccount by orgadmin\r\n");
        helpBuffer.append("unfreeze                  unfreeze orgaccount by orgadmin\r\n");
        helpBuffer.append("cancel                    cancel orgaccount by orgadmin\n");
        helpBuffer.append("changeExternalAccount     change orgaccount address by orgadmin\r\n");
        helpBuffer.append("\r\n");
        helpBuffer.append("AssetManagement:\r\n");
        helpBuffer.append("createAsset               create  asset by orgadmin\r\n");
        helpBuffer.append("loadAsset                 load asset by assetAddrees\r\n");
        helpBuffer.append("openAccount               open an asset account\r\n");
        helpBuffer.append("deposit                   deposit amount to account\r\n");
        helpBuffer.append("withdrawal                withdrawal amount from account\r\n");
        helpBuffer.append("generateTransferSign      generate transfer sign,only support fungibleAsset\r\n");
        helpBuffer.append("transfer                  transfer amount from $fromAddr to $toAddr\r\n");
        helpBuffer.append("generateQuerySign         generate account query sign\r\n");
        helpBuffer.append("getBalance                return balance of client\r\n");
        helpBuffer.append("issue                     issue nonfungible assets\r\n");
        helpBuffer.append("generateTransferSign      generate transfer sign,only support nonfungibleAsset\r\n");
        helpBuffer.append("transfer                  transfer noteNos from $fromAddr to $toAddr \r\n");
        helpBuffer.append("activeBatch               active nonFungibleAsset\r\n");
        helpBuffer.append("isAccountHoldNote         check if client hold this noteNo\r\n");
        helpBuffer.append("getAccountNotes           return account's noteNos\r\n");
        helpBuffer.append("queryBook                 return records of transactions by conditons\r\n");

        return helpBuffer.toString();
    }


    public String exectue(String line) {
        String result = null;
        String[] commandAndArgs = line.split("\\s+");
        String command = commandAndArgs[0];

        try {
            switch (command) {
                case "help":
                    result = getHelpWords();
                    break;
                case "init":
                    result = "create Project:" + InitService.getInstance().startInit();
                    break;
                case "load":
                    result = "load Project:" + InitService.getInstance().loadProject();
                    break;
                case "login":
                    result = (commandAndArgs.length < 2) ? null : ("logon " + LoginService.getInstance().login(commandAndArgs[1]));
                    break;
                case "logout":
                    result = !LoginService.getInstance().logout() ? "logout faild!" : "logout successfully!";
                    break;
                case "whoami":
                    result = "logon account:" + LoginService.getInstance().getAccount();
                    break;
                case "generateAccount":
                    CryptoKeyPair cryptoKeyPair = LoginService.getInstance().createKeyPair();
                    result = "address:" + cryptoKeyPair.getAddress() + "\r\n prikey:" + cryptoKeyPair.getHexPrivateKey();
                    break;
                case "createAccount":
                    result = (commandAndArgs.length < 2) ? null : "createAccount: " + OrgService.getInstance().createAccount(commandAndArgs[1], null);
                    break;
                case "freeze":
                    result = (commandAndArgs.length < 2) ? null : "freeze result: " + OrgService.getInstance().freeze(commandAndArgs[1], null);
                    break;
                case "unfreeze":
                    result = (commandAndArgs.length < 2) ? null : "unfreeze result: " + OrgService.getInstance().unfreeze(commandAndArgs[1], null);
                    break;
                case "cancel":
                    result = (commandAndArgs.length < 2) ? null : "cancel result: " + OrgService.getInstance().cancel(commandAndArgs[1], null);
                    break;
                case "changeExternalAccount":
                    result = (commandAndArgs.length < 3) ? null : "cancel result: " + OrgService.getInstance().changeExternalAccount(commandAndArgs[1], commandAndArgs[2], null);
                    break;
                case "createAsset":
                    result = (commandAndArgs.length < 3) ? null : "create asset:" + AssetService.getInstance().createAsset(Boolean.valueOf(commandAndArgs[1]), commandAndArgs[2], null);
                    break;
                case "loadAsset":
                    result = (commandAndArgs.length < 3) ? null : "load asset:" + AssetService.getInstance().loadAsset(commandAndArgs[1], Boolean.valueOf(commandAndArgs[2]));
                    break;
                case "openAccount":
                    result = (commandAndArgs.length < 2) ? null : "openAccount:" + AssetService.getInstance().openAccount(commandAndArgs[1], null);
                    break;
                case "deposit":
                    result = (commandAndArgs.length < 3) ? null : "deposit successfully! transaction result:" + AssetService.getInstance().deposit(commandAndArgs[1], new BigInteger(commandAndArgs[2]), null).toString();
                    break;
                case "withdrawal":
                    result = (commandAndArgs.length < 3) ? null : "withdrawal successfully! transaction result:" + AssetService.getInstance().withdrawal(commandAndArgs[1], new BigInteger(commandAndArgs[2]), null).toString();
                    break;
                case "issue":
                    if (commandAndArgs.length < 5) {
                        break;
                    }
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    Date expiraData = format.parse(commandAndArgs[5]);
                    result = (commandAndArgs.length < 6) ? null : "issue successfully! issue noteNos:" + AssetService.getInstance().issue(commandAndArgs[1], new BigInteger(commandAndArgs[2]), new BigInteger(commandAndArgs[3]), new BigInteger(commandAndArgs[4]), expiraData, null);
                    break;
                case "generateTransferSign": {
                    if (commandAndArgs.length < 4) {
                        break;
                    }
                    if (AssetHolder.isFungible()) {
                        result = ClientService.getInstance().genFungibleTransferSign(commandAndArgs[1], commandAndArgs[2], new BigInteger(commandAndArgs[3]));
                    } else {
                        List<BigInteger> noteNos = Arrays.asList(commandAndArgs[3].split(",")).stream().map(s -> new BigInteger(s)).collect(Collectors.toList());
                        result = ClientService.getInstance().genNonFungibleTransferSign(commandAndArgs[1], commandAndArgs[2], noteNos);
                    }
                    break;
                }
                case "generateQuerySign": {
                    result = ClientService.getInstance().genGetSign();
                    break;
                }
                case "transfer":
                    result = (commandAndArgs.length < 5) ? null : "transfer successfully!  transaction result:" +
                            (!AssetHolder.isFungible() ?
                                    AssetService.getInstance().transfer(commandAndArgs[1], commandAndArgs[2], Arrays.asList(commandAndArgs[3].split(",")).stream().map(s -> new BigInteger(s)).collect(Collectors.toList()), commandAndArgs[4]) :
                                    AssetService.getInstance().transfer(commandAndArgs[1], commandAndArgs[2], new BigInteger(commandAndArgs[3]), commandAndArgs[4]));
                    break;
                case "activeBatch":
                    result = (commandAndArgs.length < 2) ? null : "result:" + AssetService.getInstance().activeBatch(new BigInteger(commandAndArgs[1]), null);
                    break;
                case "getBalance":
                    result = (commandAndArgs.length < 3) ? null : "result:" + AssetService.getInstance().getBalance(commandAndArgs[1], commandAndArgs[2]);
                    break;
                case "isAccountHoldNote":
                    result = (commandAndArgs.length < 3) ? null : "result:" + AssetService.getInstance().isAccountHoldNote(commandAndArgs[1], new BigInteger(commandAndArgs[2]), null);
                    break;
                case "getAccountNotes":
                    result = (commandAndArgs.length < 4) ? null : "result:" + AssetService.getInstance().getAccountNotes(commandAndArgs[1], new BigInteger(commandAndArgs[2]), new BigInteger(commandAndArgs[3]), null);
                    break;
                case "queryBook": {
                    if (commandAndArgs.length < 3) {
                        break;
                    }
                    String[] conditions = commandAndArgs[1].replace("[", "").replace("]", "").split(",");
                    if (conditions.length < 4) {
                        break;
                    }
                    BigInteger termNo = StringUtils.isBlank(conditions[0]) ? null : new BigInteger(conditions[0]);
                    BigInteger seqNo = StringUtils.isBlank(conditions[1]) ? null : new BigInteger(conditions[1]);
                    String from = StringUtils.isBlank(conditions[2]) ? null : conditions[2];
                    String to = StringUtils.isBlank(conditions[3]) ? null : conditions[3];
                    if (AssetHolder.isFungible()) {
                        result = AssetService.getInstance().queryFungibleBook(new Condition(termNo, seqNo, from, to), commandAndArgs[2]).toString();
                    } else {
                        BigInteger noteNo = conditions.length < 5 ? BigInteger.valueOf(0) : StringUtils.isBlank(conditions[4]) ? null : new BigInteger(conditions[4]);
                        result = AssetService.getInstance().queryNonFungibleBook(new NonFungibleCondition(termNo, seqNo, from, to, noteNo, null), commandAndArgs[2]).toString();
                    }
                    break;

                }
                default:
                    result = "command or args not verify!";
                    break;
            }
        } catch (Exception e) {
            result = e.getMessage();
            return result;
        }
        if (StringUtils.isBlank(result)) {
            result = "command or args not verify!";
        }
        return result;
    }
}
