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

package com.webank.openledger.core.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.webank.openledger.utils.JsonHelper;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

@Slf4j
public class ValueModelTest {

    @Test
    public void testGetByteVal() throws IOException {
        String value1 = "test";
        ValueModel valueModel = new ValueModel(value1);
        byte[] valBytes = valueModel.getByteVal(valueModel);

        ValueModel vm1 = JsonHelper.getObjectMapper().readValue(valBytes, ValueModel.class);
        log.info(vm1.toString());
        assertTrue(vm1.getValue() instanceof String);

        int value2 = 123;
        ValueModel valueModel2 = new ValueModel(value2);
        byte[] valBytes2 = valueModel2.getByteVal(valueModel2);

        ValueModel vm2 = JsonHelper.getObjectMapper().readValue(valBytes2, ValueModel.class);
        log.info(vm2.toString());
        assertTrue(vm2.getValue() instanceof Integer);


        double value3 = 123.123;
        ValueModel valueModel3 = new ValueModel(value3);
        byte[] valBytes3 = valueModel3.getByteVal(valueModel3);

        ValueModel vm3 = JsonHelper.getObjectMapper().readValue(valBytes3, ValueModel.class);
        log.info(vm3.toString());
        assertTrue(vm3.getValue() instanceof Double);


        List<String> testString = new ArrayList<>();
        testString.add("1");
        testString.add("2");
        ValueModel valueModel4 = new ValueModel(testString);
        byte[] valBytes4 = valueModel4.getByteVal(valueModel4);
        ValueModel vm4 = JsonHelper.getObjectMapper().readValue(valBytes4, ValueModel.class);
        log.info(vm4.toString());
        assertTrue(vm4.getValue() instanceof List);


        List<ValueModel> testList = new ArrayList<>();
        testList.add(valueModel);
        testList.add(valueModel2);
        ValueModel valueModel5 = new ValueModel(testList);
        byte[] valBytes5 = valueModel5.getByteVal(valueModel5);
        ValueModel vm5 = JsonHelper.getObjectMapper().readValue(valBytes5, ValueModel.class);
        log.info(vm5.toString());

        User user = new User();
        user.setName("aaa");
        user.setSex(1);
        ValueModel valueModel6 = new ValueModel(user);
        byte[] valBytes6 = valueModel6.getByteVal(valueModel6);
        ValueModel<User> vm6 = JsonHelper.getObjectMapper().readValue(valBytes6, ValueModel.class);
        log.info(vm6.toString());
        User user2 = vm6.getValue();
        log.info(vm6.getValue().toString());
        log.info(user2.getName());

        List<User> testList2 = new ArrayList<>();
        testList2.add(user);
        User userb = new User();
        userb.setName("aaa2");
        userb.setSex(2);
        testList2.add(userb);

        ValueModel valueModel7 = new ValueModel(testList2);
        byte[] valBytes7 = valueModel7.getByteVal(valueModel7);
        ValueModel<List<User>> vm7 = JsonHelper.getObjectMapper().readValue(valBytes7, ValueModel.class);
        List<User> testList5 = vm7.getValue(new TypeReference<List<User>>() {
        });
        User test2 = testList5.get(0);
        log.info(test2.getName());
        List<User> testList6 = vm7.getValue();
        log.info(vm7.toString());
    }

}
