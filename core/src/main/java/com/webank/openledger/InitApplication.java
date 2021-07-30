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

package com.webank.openledger;

import java.io.IOException;
import java.util.List;

import com.webank.openledger.contractsbak.Project;
import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.project.ProjectService;
import com.webank.openledger.core.response.ResponseData;

import lombok.extern.slf4j.Slf4j;

/**
 *  init the project
 * @author pepperli
 */
@Slf4j
public class InitApplication {
    public static void main(String[] args) throws IOException {
        try {
            Blockchain  blockchain = new Blockchain("application.properties",   true);
            ProjectService<Project> projectSDK = new ProjectService(blockchain, "");
            ResponseData<List<String>> rsp = projectSDK.createProject();
            log.info("\r\n String projectAddr = \"{}\"; \r\n    " +
                            "String accountManagerAddr = \"{}\"; \r\n   " +
                            "String authManagerAddr = \"{}\"; \r\n   " +
                            "String authCenterAddr = \"{}\";  \r\n  " +
                            "String assetManagerAddr = \"{}\";",
                    rsp.getResult().get(0),
                    rsp.getResult().get(1),
                    rsp.getResult().get(2),
                    rsp.getResult().get(3),
                    rsp.getResult().get(4));
            log.info("End");
            System.exit(0);
        } catch (Exception e) {
            log.error("error:",e);
            System.exit(1);
        }
    }
}
