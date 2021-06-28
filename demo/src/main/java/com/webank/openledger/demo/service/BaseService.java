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

import com.webank.openledger.contracts.AuthCenter;
import com.webank.openledger.contracts.Organization;
import com.webank.openledger.contracts.Project;
import com.webank.openledger.core.Blockchain;
import com.webank.openledger.core.auth.AuthCenterService;
import com.webank.openledger.core.org.OrganizationService;
import com.webank.openledger.core.project.ProjectService;
import com.webank.openledger.demo.entity.ProjectEntity;
import com.webank.openledger.demo.holder.ProjectHolder;

/**
 * base demo service
 * @author pepperli
 */
public class BaseService {
    protected OrganizationService<Organization> organizationService;
    protected AuthCenterService<AuthCenter> authCenterService;
    protected ProjectService<Project> projectService;
    protected Blockchain blockchain;

    public static final Boolean isJar=Boolean.TRUE;

    public void loadService() throws Exception {
        blockchain = new Blockchain("application.properties",isJar);

        ProjectEntity projectEntity = ProjectHolder.getProject();
        projectService = new ProjectService<>(blockchain,projectEntity.getProjectAddr());
        authCenterService = new AuthCenterService<>(blockchain,projectEntity.getAuthCenterAddr());
        organizationService = new OrganizationService<>(blockchain,projectEntity.getOrgAddr());
    }
}
