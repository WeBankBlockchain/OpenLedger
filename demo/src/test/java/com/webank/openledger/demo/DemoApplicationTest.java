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

package com.webank.openledger.demo;

import com.webank.openledger.demo.service.CommandService;

import org.junit.Test;

public class DemoApplicationTest {
    private static final String adminAddr = "0x5507a3f601f4d993c888be28596bc856900b0af9";
    private static final String adminprikey = "c9206d3d8c608c06d7e55287e3ddd24c2e48c3e49b81763fd4796b28095fa43c";
    private static final String clientprikey = "3e9d624d0c3310af3cf72de466f959b5c362987535e62c2b301aa7d21afe06af";
    private static final String clientAddr = "0xd1d99b85b912ffcfbe266ac317c6b88a3b06530b";
    private static final String clientprikey2 = "e3b560c7a426ae1e663476274e851409547c12272d943389f5280d0b1d6fe1d5";
    private static final String clientAddr2 = "0x02d5866bde1defc06f5343b1c602fb68dae93548";
    private static final String clientAddr3 = "0xb7b1fa39fba52b6d5f248fb2b2c83779e617340a";
    private static final String clientprikey3 = "99cdcc7bb81573229a6d0365c53c3cafa869fb067c87bace262f23685205b04d";
    private static final String fungibleAsset = "0xd448720d9ee7a0a65b9729750c50dc1ff35e587f";
    private static final String nonfungibleAsset = "0x4a9fc4e710d94843a63c832d347974a7b14cc119";

    @Test
    public void testInitProject() {
        String line = null;
        line = "init";
        System.out.println(CommandService.getInstance().exectue(line));
    }
    @Test
    public void generateKeyPair() {
        String line = null;
        line="generateAccount";
        System.out.println(CommandService.getInstance().exectue(line));
    }

    @Test
    public void testLoadProjectAndOrgService() {
        String line = null;
        line = "load";
        System.out.println(CommandService.getInstance().exectue(line));

        line = "login " + adminprikey;
        System.out.println(CommandService.getInstance().exectue(line));

        line = "whoami";
        System.out.println(CommandService.getInstance().exectue(line));

        line = "createAccount " + clientAddr3;
        System.out.println(CommandService.getInstance().exectue(line));

        line = "createAccount " + clientAddr;
        System.out.println(CommandService.getInstance().exectue(line));


        line="freeze "+clientAddr3;
        System.out.println(CommandService.getInstance().exectue(line));

        line="unfreeze "+clientAddr3;
        System.out.println(CommandService.getInstance().exectue(line));
        line="unfreeze "+clientAddr;
        System.out.println(CommandService.getInstance().exectue(line));

//        line="cancel "+clientAddr2;
//        System.out.println(CommandService.getInstance().exectue(line));

//        line="changeExternalAccount "+clientAddr3+" "+clientAddr2;
//        System.out.println(CommandService.getInstance().exectue(line));


        line = "logout" ;
        System.out.println(CommandService.getInstance().exectue(line));

    }
    @Test
    public void testCreateAsset() {
        Boolean isFungible=true;
        String line = null;
        line = "load";
        System.out.println(CommandService.getInstance().exectue(line));

        line = "login " + adminprikey;
        System.out.println(CommandService.getInstance().exectue(line));

        line = "whoami";
        System.out.println(CommandService.getInstance().exectue(line));

        line = "createAsset "+isFungible+" asset21220";
        System.out.println(CommandService.getInstance().exectue(line));

        isFungible=false;
        line = "createAsset "+isFungible+" asset2811";
        System.out.println(CommandService.getInstance().exectue(line));
    }

    @Test
    public void testFungibleAsset() {
        String depositAmount=" 100";
        String withdrawalAmount=" 1";
        String transferAmount=" 10";
        String line = null;
        line = "load";
        System.out.println(CommandService.getInstance().exectue(line));
        line = "login " + adminprikey;
        System.out.println(CommandService.getInstance().exectue(line));


        line = "loadAsset "+fungibleAsset+ " true";
        System.out.println(CommandService.getInstance().exectue(line));

        line="openAccount "+clientAddr3;
        System.out.println(CommandService.getInstance().exectue(line));

        line="openAccount "+clientAddr;
        System.out.println(CommandService.getInstance().exectue(line));

        line="deposit "+clientAddr3+depositAmount;
        System.out.println(CommandService.getInstance().exectue(line));

        line="withdrawal "+clientAddr3+ withdrawalAmount;
        System.out.println(CommandService.getInstance().exectue(line));

        line = "login " + clientprikey3;
        System.out.println(CommandService.getInstance().exectue(line));
        line="generateQuerySign";
        String sign = CommandService.getInstance().exectue(line);
        System.out.println(sign);

        line = "login " + adminprikey;
        System.out.println(CommandService.getInstance().exectue(line));
        line="getBalance "+clientAddr3+" "+sign;
        System.out.println(CommandService.getInstance().exectue(line));

    }

    @Test
    public void testFungibleAssetTransfer() {
        String transferAmount=" 10";
        String line = null;
        line = "load";
        System.out.println(CommandService.getInstance().exectue(line));

        line = "loadAsset "+fungibleAsset+ " true";
        System.out.println(CommandService.getInstance().exectue(line));
        line = "login " + clientprikey3;
        System.out.println(CommandService.getInstance().exectue(line));
        line="generateTransferSign "+clientAddr3+ " "+clientAddr+transferAmount;
        String sign =CommandService.getInstance().exectue(line);
        System.out.println(sign);
        line = "login " + adminprikey;
        System.out.println(CommandService.getInstance().exectue(line));
        line="transfer "+clientAddr3+ " "+clientAddr+transferAmount+" "+sign;
        System.out.println(CommandService.getInstance().exectue(line));
        line = "login " + clientprikey3;
        System.out.println(CommandService.getInstance().exectue(line));
        line="generateQuerySign";
        sign = CommandService.getInstance().exectue(line);
        System.out.println(sign);
        line="getBalance "+clientAddr3+" "+sign;
        System.out.println(CommandService.getInstance().exectue(line));
    }

    @Test
    public void testFungibleAssetQuery() {
        String line = null;
        line = "load";
        System.out.println(CommandService.getInstance().exectue(line));
        line = "loadAsset "+fungibleAsset+ " true";
        System.out.println(CommandService.getInstance().exectue(line));
        line = "login " + clientprikey3;
        System.out.println(CommandService.getInstance().exectue(line));
        line="generateQuerySign";
        String sign = CommandService.getInstance().exectue(line);
        System.out.println(sign);

        String conditon="[1,0,"+clientAddr3+",0]";
        line="queryBook "+conditon+" "+sign;
        System.out.println(CommandService.getInstance().exectue(line));

    }


    @Test
    public void testNonFungibleAssetIssue() {

        String line = null;
        line = "load";
        System.out.println(CommandService.getInstance().exectue(line));
        line = "login " + adminprikey;
        System.out.println(CommandService.getInstance().exectue(line));

        line = "loadAsset "+nonfungibleAsset+ " false";
        System.out.println(CommandService.getInstance().exectue(line));

        line="openAccount "+clientAddr3;
        System.out.println(CommandService.getInstance().exectue(line));

        line="openAccount "+clientAddr;
        System.out.println(CommandService.getInstance().exectue(line));

        line="issue "+ clientAddr3+" 100 2021 4 2023-10-1";
        System.out.println(CommandService.getInstance().exectue(line));

    }

    @Test
    public void testActiveBatch(){
        String line=null;
        line = "load";
        System.out.println(CommandService.getInstance().exectue(line));
        line = "login " + adminprikey;
        System.out.println(CommandService.getInstance().exectue(line));
        line = "loadAsset "+nonfungibleAsset+ " false";
        System.out.println(CommandService.getInstance().exectue(line));
        line="activeBatch 1";
        System.out.println(CommandService.getInstance().exectue(line));
    }

    @Test
    public void testNonFungibleQueryAccount() {
        String line = null;
        line = "load";
        System.out.println(CommandService.getInstance().exectue(line));
        line = "login " + adminprikey;
        System.out.println(CommandService.getInstance().exectue(line));
        line = "loadAsset "+nonfungibleAsset+ " false";
        System.out.println(CommandService.getInstance().exectue(line));
        line="isAccountHoldNote "+clientAddr3+" 20210002";
        System.out.println(CommandService.getInstance().exectue(line));

        line="getAccountNotes "+clientAddr3+" 0 10";
        System.out.println(CommandService.getInstance().exectue(line));
    }

    @Test
    public void testNonFungibleTransfer(){
        String line = null;
        line = "load";
        System.out.println(CommandService.getInstance().exectue(line));

        line = "loadAsset "+nonfungibleAsset+ " false";
        System.out.println(CommandService.getInstance().exectue(line));
        line = "login " + clientprikey3;
        System.out.println(CommandService.getInstance().exectue(line));

        String noteNos="20210003";
        line="generateTransferSign "+clientAddr3+ " "+clientAddr+" "+noteNos;
        String sign =CommandService.getInstance().exectue(line);
        System.out.println(sign);
        line = "login " + adminprikey;
        System.out.println(CommandService.getInstance().exectue(line));
        line="transfer "+clientAddr3+ " "+clientAddr+" "+noteNos+" "+sign;
        System.out.println(CommandService.getInstance().exectue(line));
    }

    @Test
    public void testNonFungibleAssetQuery() {
        String line = null;
        line = "load";
        System.out.println(CommandService.getInstance().exectue(line));
        line = "loadAsset "+nonfungibleAsset+ " false";
        System.out.println(CommandService.getInstance().exectue(line));
        line = "login " + adminprikey;
        System.out.println(CommandService.getInstance().exectue(line));
        line="generateQuerySign";
        String sign = CommandService.getInstance().exectue(line);
        System.out.println(sign);

        String conditon="[0,0,0,"+clientAddr+",0]";
        line="queryBook "+conditon+" "+sign;
        System.out.println(CommandService.getInstance().exectue(line));

    }

}