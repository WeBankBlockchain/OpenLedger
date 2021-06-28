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

package com.webank.openledger.contracts.gov_auth;

import java.util.Arrays;
import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.datatypes.Bool;
import org.fisco.bcos.sdk.abi.datatypes.Function;
import org.fisco.bcos.sdk.abi.datatypes.Type;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.contract.Contract;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class IAuthControl extends Contract {
    public static final String[] BINARY_ARRAY = {"608060405234801561001057600080fd5b5061011a806100206000396000f300608060405260043610603f576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff168063b3a26939146044575b600080fd5b348015604f57600080fd5b5060cb600480360381019080803573ffffffffffffffffffffffffffffffffffffffff16906020019092919080357bffffffffffffffffffffffffffffffffffffffffffffffffffffffff19169060200190929190803573ffffffffffffffffffffffffffffffffffffffff16906020019092919050505060e5565b604051808215151515815260200191505060405180910390f35b600093925050505600a165627a7a7230582043bcc4e08f801b6cf9e4606328671082b71310bfb1c7422b8b97595f984f98a60029"};

    public static final String BINARY = String.join("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {"608060405234801561001057600080fd5b5061011a806100206000396000f300608060405260043610603f576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806328e9610c146044575b600080fd5b348015604f57600080fd5b5060cb600480360381019080803573ffffffffffffffffffffffffffffffffffffffff16906020019092919080357bffffffffffffffffffffffffffffffffffffffffffffffffffffffff19169060200190929190803573ffffffffffffffffffffffffffffffffffffffff16906020019092919050505060e5565b604051808215151515815260200191505060405180910390f35b600093925050505600a165627a7a72305820a8169dcb4d5bf0dc1af54f23ef19d34c5ee4915226e4e9dd9716de7d5fbec9540029"};

    public static final String SM_BINARY = String.join("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {"[{\"constant\":true,\"inputs\":[{\"name\":\"contractAddr\",\"type\":\"address\"},{\"name\":\"sig\",\"type\":\"bytes4\"},{\"name\":\"caller\",\"type\":\"address\"}],\"name\":\"canCallFunction\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}]"};

    public static final String ABI = String.join("", ABI_ARRAY);

    public static final String FUNC_CANCALLFUNCTION = "canCallFunction";

    protected IAuthControl(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public Boolean canCallFunction(String contractAddr, byte[] sig, String caller) throws ContractException {
        final Function function = new Function(FUNC_CANCALLFUNCTION, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.Address(contractAddr), 
                new org.fisco.bcos.sdk.abi.datatypes.generated.Bytes4(sig), 
                new org.fisco.bcos.sdk.abi.datatypes.Address(caller)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeCallWithSingleValueReturn(function, Boolean.class);
    }

    public static IAuthControl load(String contractAddress, Client client, CryptoKeyPair credential) {
        return new IAuthControl(contractAddress, client, credential);
    }

    public static IAuthControl deploy(Client client, CryptoKeyPair credential) throws ContractException {
        return deploy(IAuthControl.class, client, credential, getBinary(client.getCryptoSuite()), "");
    }
}
