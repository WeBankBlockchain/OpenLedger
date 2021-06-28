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

package com.webank.openledger.demo.holder;

import com.webank.openledger.demo.entity.ProjectEntity;

/**
 *  storing project information
 *  projects are loaded in  two ways : load by application.properties or call init
 */
public class ProjectHolder {
    private static ProjectEntity projectEntity;


    public static void setProject(ProjectEntity project){
        projectEntity=project;
    }

    public static ProjectEntity getProject() throws Exception {
        if(projectEntity==null){
            throw new Exception("project not load,please loadProject by application.properties or run initProject");
        }
        return projectEntity;
    }

    public static void clear(){
        projectEntity=null;
    }
}
