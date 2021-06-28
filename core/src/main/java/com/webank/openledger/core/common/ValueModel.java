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
import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * the struct defined value in storage
 * @author pepperli
 */

@Slf4j
@Getter
@Setter
public class ValueModel<T> implements Serializable {
    public static int TYPE_BIGINTEGER = 0;
    public static int TYPE_STRING = 1;
    public static int TYPE_ADDRESS = 2;

    private transient  T value;
    private Class type;
    private Boolean isByte;

    public ValueModel(T value, Class type) {
        this.value = value;
        this.type = type;
    }

    public ValueModel(T value) {
        this.value = value;
        this.type = parseValueType(value);
    }

    public ValueModel() {

    }

    public static byte[] getByteVal(ValueModel vm) {
        try {
            if(vm.getValue() instanceof byte[]){
                return (byte[]) vm.getValue();
            }
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsBytes(vm);
        } catch (Exception e) {
            log.error("getByte failed:{}", e);
            return null;
        }
    }

    public static <T> T get(Class<T> clz, Object o) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        if (isListTypeClass(clz) || clz.isArray()) {
            return mapper.convertValue(o, new TypeReference<T>() {
            });
        }
        return mapper.readValue(mapper.writeValueAsBytes(o), clz);
    }

    public static <T> T get(TypeReference<T> typeReference, Object o) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(o, typeReference);
    }

    /**
     * 判断指定类是否是List的子类或者父类
     *
     * @param clz
     * @return
     */
    private static boolean isListTypeClass(Class clz) {
        try {
            return List.class.isAssignableFrom(clz) || clz.newInstance() instanceof List;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * parse the type of value
     *
     * @param value
     * @return
     */
    private Class parseValueType(Object value) {
        return value.getClass();
    }

    public <T> T getValue(TypeReference typeReference) throws IOException {
        if (typeReference == null) {
            return (T) get(type, value);
        }
        return (T) get(typeReference, value);
    }

    public  T getValue() throws IOException {
        return (T) get(type, value);
    }

    @Override
    public String toString() {
        return "ValueModel{" +
                "value=" + value +
                ", type=" + type +
                '}';

    }


}

