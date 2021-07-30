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

package com.webank.openledger.contractsbak.gov_account;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.abi.FunctionEncoder;
import org.fisco.bcos.sdk.abi.FunctionReturnDecoder;
import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.datatypes.Address;
import org.fisco.bcos.sdk.abi.datatypes.Bool;
import org.fisco.bcos.sdk.abi.datatypes.Event;
import org.fisco.bcos.sdk.abi.datatypes.Function;
import org.fisco.bcos.sdk.abi.datatypes.Type;
import org.fisco.bcos.sdk.abi.datatypes.generated.Bytes32;
import org.fisco.bcos.sdk.abi.datatypes.generated.Uint8;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple1;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.contract.Contract;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.eventsub.EventCallback;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class UserAccount extends Contract {
    public static final String[] BINARY_ARRAY = {"60806040526000600260146101000a81548160ff021916908360ff16021790555034801561002c57600080fd5b506040516080806113df833981018060405281019080805190602001909291908051906020019092919080519060200190929190805190602001909291905050508383336000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555081600160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555080600260006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550505061014b82610195640100000000026401000000009004565b80600360006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055505050505061031c565b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614151561027f576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260298152602001807f574542617369634163636f756e743a206f6e6c79206f776e657220697320617581526020017f74686f72697a65642e000000000000000000000000000000000000000000000081525060400191505060405180910390fd5b806000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055503073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff167fc66d1d23a5b7baf1f496bb19f580d7b12070ad5a08a758c990db97d961fa33a660405160405180910390a350565b6110b48061032b6000396000f3006080604052600436106100ba576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680630fb3844c146100bf57806313af4035146100f05780632b3c0a40146101335780633bc5de301461018e57806361dfdae6146101e557806362a5af3b146102405780636a28f0001461026f5780638866eaec1461029e578063b2623cb0146102cd578063b2bdfa7b14610324578063d195143c1461037b578063ea8a1af0146103d2575b600080fd5b3480156100cb57600080fd5b506100d4610401565b604051808260ff1660ff16815260200191505060405180910390f35b3480156100fc57600080fd5b50610131600480360381019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610414565b005b34801561013f57600080fd5b50610174600480360381019080803573ffffffffffffffffffffffffffffffffffffffff16906020019092919050505061059b565b604051808215151515815260200191505060405180910390f35b34801561019a57600080fd5b506101a3610729565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b3480156101f157600080fd5b50610226600480360381019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610753565b604051808215151515815260200191505060405180910390f35b34801561024c57600080fd5b506102556108e2565b604051808215151515815260200191505060405180910390f35b34801561027b57600080fd5b50610284610b70565b604051808215151515815260200191505060405180910390f35b3480156102aa57600080fd5b506102b3610e0c565b604051808215151515815260200191505060405180910390f35b3480156102d957600080fd5b506102e2610e28565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b34801561033057600080fd5b50610339610e4e565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b34801561038757600080fd5b50610390610e73565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b3480156103de57600080fd5b506103e7610e9d565b604051808215151515815260200191505060405180910390f35b600260149054906101000a900460ff1681565b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff161415156104fe576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260298152602001807f574542617369634163636f756e743a206f6e6c79206f776e657220697320617581526020017f74686f72697a65642e000000000000000000000000000000000000000000000081525060400191505060405180910390fd5b806000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055503073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff167fc66d1d23a5b7baf1f496bb19f580d7b12070ad5a08a758c990db97d961fa33a660405160405180910390a350565b6000600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614806106465750600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16145b15156106e0576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260228152602001807f426173654163636f756e743a206f6e6c79206163636f756e74206d616e61676581526020017f722e00000000000000000000000000000000000000000000000000000000000081525060400191505060405180910390fd5b816000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060019050919050565b6000600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16905090565b6000600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614806107fe5750600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16145b1515610898576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260228152602001807f426173654163636f756e743a206f6e6c79206163636f756e74206d616e61676581526020017f722e00000000000000000000000000000000000000000000000000000000000081525060400191505060405180910390fd5b81600360006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060019050919050565b6000600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16148061098d5750600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16145b1515610a27576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260228152602001807f426173654163636f756e743a206f6e6c79206163636f756e74206d616e61676581526020017f722e00000000000000000000000000000000000000000000000000000000000081525060400191505060405180910390fd5b610a2f610e0c565b1515610ac9576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040180806020018281038252602b8152602001807f426173654163636f756e743a206f6e6c79206163636f756e742073746174757381526020017f206973206e6f726d616c2e00000000000000000000000000000000000000000081525060400191505060405180910390fd5b6001600260146101000a81548160ff021916908360ff1602179055507f667265657a6500000000000000000000000000000000000000000000000000007f7d78a1adf6a29dad801d43ddd0c4478ec0cbf1bd9bfdd2e007d90429959f363e30604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390a26001905090565b6000600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff161480610c1b5750600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16145b1515610cb5576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260228152602001807f426173654163636f756e743a206f6e6c79206163636f756e74206d616e61676581526020017f722e00000000000000000000000000000000000000000000000000000000000081525060400191505060405180910390fd5b6001600260149054906101000a900460ff1660ff16141515610d6557604051","7f08c379a000000000000000000000000000000000000000000000000000000000815260040180806020018281038252602d8152602001807f426173654163636f756e743a206f6e6c79206163636f756e742073746174757381526020017f2069732061626e6f726d616c2e0000000000000000000000000000000000000081525060400191505060405180910390fd5b6000600260146101000a81548160ff021916908360ff1602179055507f756e667265657a650000000000000000000000000000000000000000000000007f7d78a1adf6a29dad801d43ddd0c4478ec0cbf1bd9bfdd2e007d90429959f363e30604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390a26001905090565b600080600260149054906101000a900460ff1660ff1614905090565b600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b6000600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16905090565b6000600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff161480610f485750600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16145b1515610fe2576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260228152602001807f426173654163636f756e743a206f6e6c79206163636f756e74206d616e61676581526020017f722e00000000000000000000000000000000000000000000000000000000000081525060400191505060405180910390fd5b60028060146101000a81548160ff021916908360ff1602179055507f63616e63656c00000000000000000000000000000000000000000000000000007f7d78a1adf6a29dad801d43ddd0c4478ec0cbf1bd9bfdd2e007d90429959f363e30604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390a260019050905600a165627a7a723058209bc26b9624b1f3cb0198101f2bd9141c4ade4355f17e25efbbbd2030d678a37c0029"};

    public static final String BINARY = String.join("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {"60806040526000600260146101000a81548160ff021916908360ff16021790555034801561002c57600080fd5b506040516080806113df833981018060405281019080805190602001909291908051906020019092919080519060200190929190805190602001909291905050508383336000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555081600160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555080600260006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550505061014b82610195640100000000026401000000009004565b80600360006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055505050505061031c565b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614151561027f576040517fc703cb120000000000000000000000000000000000000000000000000000000081526004018080602001828103825260298152602001807f574542617369634163636f756e743a206f6e6c79206f776e657220697320617581526020017f74686f72697a65642e000000000000000000000000000000000000000000000081525060400191505060405180910390fd5b806000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055503073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff167f480107a875206c9f5ec6e8b65d989106e27d0fc8b130625b25997540ddfc334a60405160405180910390a350565b6110b48061032b6000396000f3006080604052600436106100ba576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806305282c70146100bf57806308165cd6146101025780631e9117501461013157806328e914891461018c57806335968e68146101e3578063398fc781146102125780634292918f146102695780638b011498146102c05780638f55e2251461031b578063df3150aa1461034a578063e211e0c114610379578063ede7ddf6146103d0575b600080fd5b3480156100cb57600080fd5b50610100600480360381019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610401565b005b34801561010e57600080fd5b50610117610588565b604051808215151515815260200191505060405180910390f35b34801561013d57600080fd5b50610172600480360381019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610824565b604051808215151515815260200191505060405180910390f35b34801561019857600080fd5b506101a16109b3565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b3480156101ef57600080fd5b506101f86109d8565b604051808215151515815260200191505060405180910390f35b34801561021e57600080fd5b506102276109f4565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b34801561027557600080fd5b5061027e610a1a565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b3480156102cc57600080fd5b50610301600480360381019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610a44565b604051808215151515815260200191505060405180910390f35b34801561032757600080fd5b50610330610bd2565b604051808215151515815260200191505060405180910390f35b34801561035657600080fd5b5061035f610e60565b604051808215151515815260200191505060405180910390f35b34801561038557600080fd5b5061038e61104b565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b3480156103dc57600080fd5b506103e5611075565b604051808260ff1660ff16815260200191505060405180910390f35b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff161415156104eb576040517fc703cb120000000000000000000000000000000000000000000000000000000081526004018080602001828103825260298152602001807f574542617369634163636f756e743a206f6e6c79206f776e657220697320617581526020017f74686f72697a65642e000000000000000000000000000000000000000000000081525060400191505060405180910390fd5b806000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055503073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff167f480107a875206c9f5ec6e8b65d989106e27d0fc8b130625b25997540ddfc334a60405160405180910390a350565b6000600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614806106335750600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16145b15156106cd576040517fc703cb120000000000000000000000000000000000000000000000000000000081526004018080602001828103825260228152602001807f426173654163636f756e743a206f6e6c79206163636f756e74206d616e61676581526020017f722e00000000000000000000000000000000000000000000000000000000000081525060400191505060405180910390fd5b6001600260149054906101000a900460ff1660ff1614151561077d576040517fc703cb1200000000000000000000000000000000000000000000000000000000815260040180806020018281038252602d8152602001807f426173654163636f756e743a206f6e6c79206163636f756e742073746174757381526020017f2069732061626e6f726d616c2e0000000000000000000000000000000000000081525060400191505060405180910390fd5b6000600260146101000a81548160ff021916908360ff1602179055507f756e667265657a650000000000000000000000000000000000000000000000007f598b76607bab91793e04db590052049ff4ca46cfc234328da5536f5169790af730604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390a26001905090565b6000600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614806108cf5750600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16145b1515610969576040517fc703cb120000000000000000000000000000000000000000000000000000000081526004018080602001828103825260228152602001807f426173654163636f756e743a206f6e6c79206163636f756e74206d616e61676581526020017f722e00000000000000000000000000000000000000000000000000000000000081525060400191505060405180910390fd5b81600360006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060019050919050565b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b600080600260149054906101000a900460ff1660ff1614905090565b600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b6000600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16905090565b6000600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff161480610aef5750600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16145b1515610b89576040517fc703cb120000000000000000000000000000000000000000000000000000000081526004018080602001828103825260228152602001807f426173654163636f756e743a206f6e6c79206163636f756e74206d616e61676581526020017f722e00000000000000000000000000000000000000000000000000000000000081525060400191505060405180910390fd5b816000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060019050919050565b6000600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff161480610c7d5750600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16145b1515610d17576040517fc703cb120000000000000000000000000000000000000000000000000000000081526004018080602001828103825260228152602001807f426173654163636f756e743a206f6e6c7920616363","6f756e74206d616e61676581526020017f722e00000000000000000000000000000000000000000000000000000000000081525060400191505060405180910390fd5b610d1f6109d8565b1515610db9576040517fc703cb1200000000000000000000000000000000000000000000000000000000815260040180806020018281038252602b8152602001807f426173654163636f756e743a206f6e6c79206163636f756e742073746174757381526020017f206973206e6f726d616c2e00000000000000000000000000000000000000000081525060400191505060405180910390fd5b6001600260146101000a81548160ff021916908360ff1602179055507f667265657a6500000000000000000000000000000000000000000000000000007f598b76607bab91793e04db590052049ff4ca46cfc234328da5536f5169790af730604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390a26001905090565b6000600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff161480610f0b5750600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16145b1515610fa5576040517fc703cb120000000000000000000000000000000000000000000000000000000081526004018080602001828103825260228152602001807f426173654163636f756e743a206f6e6c79206163636f756e74206d616e61676581526020017f722e00000000000000000000000000000000000000000000000000000000000081525060400191505060405180910390fd5b60028060146101000a81548160ff021916908360ff1602179055507f63616e63656c00000000000000000000000000000000000000000000000000007f598b76607bab91793e04db590052049ff4ca46cfc234328da5536f5169790af730604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390a26001905090565b6000600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16905090565b600260149054906101000a900460ff16815600a165627a7a72305820dad3df1bec63bca2e0b1144d3958d2db66e2e5e49d697b2a01d5dd5c0619b6ea0029"};

    public static final String SM_BINARY = String.join("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {"[{\"constant\":true,\"inputs\":[],\"name\":\"_status\",\"outputs\":[{\"name\":\"\",\"type\":\"uint8\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"owner\",\"type\":\"address\"}],\"name\":\"setOwner\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"owner\",\"type\":\"address\"}],\"name\":\"setOwnerByManager\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"getData\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"data\",\"type\":\"address\"}],\"name\":\"setData\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"freeze\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"unfreeze\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"isNormal\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"_accountManager\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"_owner\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"getAccountAdmin\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"cancel\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"name\":\"accountManager\",\"type\":\"address\"},{\"name\":\"admin\",\"type\":\"address\"},{\"name\":\"owner\",\"type\":\"address\"},{\"name\":\"data\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"owner\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"contractAddress\",\"type\":\"address\"}],\"name\":\"LogSetOwner\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"eventType\",\"type\":\"bytes32\"},{\"indexed\":false,\"name\":\"contractAddress\",\"type\":\"address\"}],\"name\":\"LogBaseAccount\",\"type\":\"event\"}]"};

    public static final String ABI = String.join("", ABI_ARRAY);

    public static final String FUNC__STATUS = "_status";

    public static final String FUNC_SETOWNER = "setOwner";

    public static final String FUNC_SETOWNERBYMANAGER = "setOwnerByManager";

    public static final String FUNC_GETDATA = "getData";

    public static final String FUNC_SETDATA = "setData";

    public static final String FUNC_FREEZE = "freeze";

    public static final String FUNC_UNFREEZE = "unfreeze";

    public static final String FUNC_ISNORMAL = "isNormal";

    public static final String FUNC__ACCOUNTMANAGER = "_accountManager";

    public static final String FUNC__OWNER = "_owner";

    public static final String FUNC_GETACCOUNTADMIN = "getAccountAdmin";

    public static final String FUNC_CANCEL = "cancel";

    public static final Event LOGSETOWNER_EVENT = new Event("LogSetOwner", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
    ;

    public static final Event LOGBASEACCOUNT_EVENT = new Event("LogBaseAccount", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>(true) {}, new TypeReference<Address>() {}));
    ;

    protected UserAccount(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public BigInteger _status() throws ContractException {
        final Function function = new Function(FUNC__STATUS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}));
        return executeCallWithSingleValueReturn(function, BigInteger.class);
    }

    public TransactionReceipt setOwner(String owner) {
        final Function function = new Function(
                FUNC_SETOWNER, 
                Arrays.<Type>asList(new Address(owner)),
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void setOwner(String owner, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_SETOWNER, 
                Arrays.<Type>asList(new Address(owner)),
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForSetOwner(String owner) {
        final Function function = new Function(
                FUNC_SETOWNER, 
                Arrays.<Type>asList(new Address(owner)),
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<String> getSetOwnerInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_SETOWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<String>(

                (String) results.get(0).getValue()
                );
    }

    public TransactionReceipt setOwnerByManager(String owner) {
        final Function function = new Function(
                FUNC_SETOWNERBYMANAGER, 
                Arrays.<Type>asList(new Address(owner)),
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void setOwnerByManager(String owner, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_SETOWNERBYMANAGER, 
                Arrays.<Type>asList(new Address(owner)),
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForSetOwnerByManager(String owner) {
        final Function function = new Function(
                FUNC_SETOWNERBYMANAGER, 
                Arrays.<Type>asList(new Address(owner)),
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<String> getSetOwnerByManagerInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_SETOWNERBYMANAGER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<String>(

                (String) results.get(0).getValue()
                );
    }

    public Tuple1<Boolean> getSetOwnerByManagerOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_SETOWNERBYMANAGER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<Boolean>(

                (Boolean) results.get(0).getValue()
                );
    }

    public TransactionReceipt getData() {
        final Function function = new Function(
                FUNC_GETDATA, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void getData(TransactionCallback callback) {
        final Function function = new Function(
                FUNC_GETDATA, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForGetData() {
        final Function function = new Function(
                FUNC_GETDATA, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<String> getGetDataOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_GETDATA, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<String>(

                (String) results.get(0).getValue()
                );
    }

    public TransactionReceipt setData(String data) {
        final Function function = new Function(
                FUNC_SETDATA, 
                Arrays.<Type>asList(new Address(data)),
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void setData(String data, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_SETDATA, 
                Arrays.<Type>asList(new Address(data)),
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForSetData(String data) {
        final Function function = new Function(
                FUNC_SETDATA, 
                Arrays.<Type>asList(new Address(data)),
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<String> getSetDataInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_SETDATA, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<String>(

                (String) results.get(0).getValue()
                );
    }

    public Tuple1<Boolean> getSetDataOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_SETDATA, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<Boolean>(

                (Boolean) results.get(0).getValue()
                );
    }

    public TransactionReceipt freeze() {
        final Function function = new Function(
                FUNC_FREEZE, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void freeze(TransactionCallback callback) {
        final Function function = new Function(
                FUNC_FREEZE, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForFreeze() {
        final Function function = new Function(
                FUNC_FREEZE, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<Boolean> getFreezeOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_FREEZE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<Boolean>(

                (Boolean) results.get(0).getValue()
                );
    }

    public TransactionReceipt unfreeze() {
        final Function function = new Function(
                FUNC_UNFREEZE, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void unfreeze(TransactionCallback callback) {
        final Function function = new Function(
                FUNC_UNFREEZE, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForUnfreeze() {
        final Function function = new Function(
                FUNC_UNFREEZE, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<Boolean> getUnfreezeOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_UNFREEZE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<Boolean>(

                (Boolean) results.get(0).getValue()
                );
    }

    public TransactionReceipt isNormal() {
        final Function function = new Function(
                FUNC_ISNORMAL, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void isNormal(TransactionCallback callback) {
        final Function function = new Function(
                FUNC_ISNORMAL, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForIsNormal() {
        final Function function = new Function(
                FUNC_ISNORMAL, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<Boolean> getIsNormalOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_ISNORMAL, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<Boolean>(

                (Boolean) results.get(0).getValue()
                );
    }

    public String _accountManager() throws ContractException {
        final Function function = new Function(FUNC__ACCOUNTMANAGER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeCallWithSingleValueReturn(function, String.class);
    }

    public String _owner() throws ContractException {
        final Function function = new Function(FUNC__OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeCallWithSingleValueReturn(function, String.class);
    }

    public TransactionReceipt getAccountAdmin() {
        final Function function = new Function(
                FUNC_GETACCOUNTADMIN, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void getAccountAdmin(TransactionCallback callback) {
        final Function function = new Function(
                FUNC_GETACCOUNTADMIN, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForGetAccountAdmin() {
        final Function function = new Function(
                FUNC_GETACCOUNTADMIN, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<String> getGetAccountAdminOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_GETACCOUNTADMIN, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<String>(

                (String) results.get(0).getValue()
                );
    }

    public TransactionReceipt cancel() {
        final Function function = new Function(
                FUNC_CANCEL, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void cancel(TransactionCallback callback) {
        final Function function = new Function(
                FUNC_CANCEL, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForCancel() {
        final Function function = new Function(
                FUNC_CANCEL, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<Boolean> getCancelOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_CANCEL, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<Boolean>(

                (Boolean) results.get(0).getValue()
                );
    }

    public List<LogSetOwnerEventResponse> getLogSetOwnerEvents(TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = extractEventParametersWithLog(LOGSETOWNER_EVENT, transactionReceipt);
        ArrayList<LogSetOwnerEventResponse> responses = new ArrayList<LogSetOwnerEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            LogSetOwnerEventResponse typedResponse = new LogSetOwnerEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.contractAddress = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public void subscribeLogSetOwnerEvent(String fromBlock, String toBlock, List<String> otherTopics, EventCallback callback) {
        String topic0 = eventEncoder.encode(LOGSETOWNER_EVENT);
        subscribeEvent(ABI,BINARY,topic0,fromBlock,toBlock,otherTopics,callback);
    }

    public void subscribeLogSetOwnerEvent(EventCallback callback) {
        String topic0 = eventEncoder.encode(LOGSETOWNER_EVENT);
        subscribeEvent(ABI,BINARY,topic0,callback);
    }

    public List<LogBaseAccountEventResponse> getLogBaseAccountEvents(TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = extractEventParametersWithLog(LOGBASEACCOUNT_EVENT, transactionReceipt);
        ArrayList<LogBaseAccountEventResponse> responses = new ArrayList<LogBaseAccountEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            LogBaseAccountEventResponse typedResponse = new LogBaseAccountEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.eventType = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.contractAddress = (String) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public void subscribeLogBaseAccountEvent(String fromBlock, String toBlock, List<String> otherTopics, EventCallback callback) {
        String topic0 = eventEncoder.encode(LOGBASEACCOUNT_EVENT);
        subscribeEvent(ABI,BINARY,topic0,fromBlock,toBlock,otherTopics,callback);
    }

    public void subscribeLogBaseAccountEvent(EventCallback callback) {
        String topic0 = eventEncoder.encode(LOGBASEACCOUNT_EVENT);
        subscribeEvent(ABI,BINARY,topic0,callback);
    }

    public static UserAccount load(String contractAddress, Client client, CryptoKeyPair credential) {
        return new UserAccount(contractAddress, client, credential);
    }

    public static UserAccount deploy(Client client, CryptoKeyPair credential, String accountManager, String admin, String owner, String data) throws ContractException {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Address(accountManager),
                new Address(admin),
                new Address(owner),
                new Address(data)));
        return deploy(UserAccount.class, client, credential, getBinary(client.getCryptoSuite()), encodedConstructor);
    }

    public static class LogSetOwnerEventResponse {
        public TransactionReceipt.Logs log;

        public String owner;

        public String contractAddress;
    }

    public static class LogBaseAccountEventResponse {
        public TransactionReceipt.Logs log;

        public byte[] eventType;

        public String contractAddress;
    }
}
