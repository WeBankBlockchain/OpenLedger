pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;

import "./govAccount/AccountManager.sol";
import "./govAuth/IAuthControl.sol";
import "./Constant.sol";
import "./storage/BytesMapLib.sol";
import "./interface/IAccount.sol";
import "./interface/IOrganization.sol";
import "./lib/SignLib.sol";


/*    
implementationOfAuthenticationScheme
1. Requirements and authentication component limitations：
We need to check the permissions of each account and each field read and write. The authentication component only supports function granularity, not key level.    鉴权组件核心数据结构如下：
    map[contractAddr][sig]=groupName    //Account contract address + function identifier to determine group
    map[groupName]=Group{ accountList } //Group contains a list of accounts. Check if msg.sender is authenticating in the list of accounts.

    以{contractAddr,sig,msg.sender},You need to configure permissions for each account, each function, and each MSG. sender.
In particular, the key with public read permission cannot be configured, and we need to support different permissions not only for the function, but also for the key of the parameter passed in, so it is not practical to introduce only the component.
2. Solve the problem of not being able to append rights by KEY
    Component authentication is based on function identifier Bytes 4, and the default parameter is Msg. sig, which is the first four bytes of the calldata(Msg. data).    我们需求为 get(key), set(key)=value等，函数名称基本固定，我们可以扩展为自定义bytes进行 bytes4(sha3(key));
    Now it supports key reading and writing, and later it can be extended to support adding, deleting, checking and changing.

3. authenticationTriplesConverge
    accountContractAddressConvergence：
    Each account is considered infinite, but the number of opening banks is limited, so the address of the account contract converges to the address of the bank it belongs to.    函数+key收敛：
    The keys with the same permissions are divided into a group (key Type), and the keys are transformed into key types. At the same time, functions such as getset are changed to read and write permissions mode.        即原只对msg.sig鉴权改为 byte4(sha3(mode+key)) 对key和读写模式作为联合key鉴权。
    callerAddressConvergence：
        Caller Msg. sender can be any address, converged according to the relationship between the caller and the called party (the account being checked), caller identity.
        For the account owner, the judgment logic is passed a fixed address on the caller when invoking authentication. Administrators still use their own addresses.

    Conclusion: Authentication triple is {address of enterprise account of the called party,mode+keyType, MSG. Sender converted internal account address}

4.  theTypesOfPermissionsSupportedAreAsFollows：
    Overview: The administrator (admin, opening bank) has all permissions, the account owner (owner) has full read, partial write permissions, other accounts have read but not write    后续可扩展为CRUD
    	    r	w
    admin	√	√
    owner	√	√
    owner	√	x
    other	√	x
    other	x	x

5. forTheCallerRequestingAuthenticationParty
    callTheFunctionDirectly：function check(address caller, address org,  address callee, bytes key,  bytes mode) public view returns(bool) {
    eg:：require(_authCenter.check(msg.sender, _org, address(this), key, MODE_W));

6.  superAdministratorManagesKeyType(后续可扩展为)
    createKeyTypeKeyType

7. forSuperAdministrators（theAdministratorOfTheEntireSystem）
    theAuthenticationTriadIs:{enterpriseAccountAddress, mode+keyType, msg.senderTransferredInternalAccountAddress}

    createOrg： deploy Storage: orgAddr

    eg1:：Only enterprise administrators have authority to manage enterprise or user information
        createPermissionGroup：
            createGroup org1Admin 1(white);
        addAKeyTypeToTheGroup：
            addFunctionToGroup orgAddr "radmin" "org1Admin"   //theAdministratorToRead
            addFunctionToGroup orgAddr "wadmin" "org1Admin"   //theAdministratorToWrite
        addsAnAdministratorToThisGroup（anAccountWithPermissions）：
            addAccountToGroup  admin1 org1Admin;

    eg2：Only enterprise administrators and users have the right to administer
        createPermissionGroup：
            createGroup org1NormalGroup 1(white);
        addAKeyTypeToTheGroup：
            addFunctionToGroup orgAddr "radmin" "org1NormalGroup"   //theAdministratorToRead
            addFunctionToGroup orgAddr "wadmin" "org1NormalGroup"   //theAdministratorToWrite
            addFunctionToGroup orgAddr "rowner" "org1NormalGroup";  //accountOwnerRead
            addFunctionToGroup orgAddr "wowner" "org1NormalGroup"   //accountOwnerWrite
        addsAnAdministratorToThisGroup（anAccountWithPermissions）：
            addAccountToGroup  admin1 org1NormalGroup; addAccountToGroup  0x10001 org1NormalGroup;

    eg3：Only enterprise administrators read and write, users read only not write
        createPermissionGroup：
            createGroup org1NormalGroup 1(white);
        addAKeyTypeToTheGroup：
            addFunctionToGroup orgAddr "radmin" "org1NormalGroup"   //theAdministratorToRead
            addFunctionToGroup orgAddr "wadmin" "org1NormalGroup"   //theAdministratorToWrite
            addFunctionToGroup orgAddr "rowner" "org1NormalGroup";  //accountOwnerRead
        addsAnAdministratorToThisGroup（anAccountWithPermissions）：
            addAccountToGroup  admin1 org1NormalGroup; addAccountToGroup  0x10001 org1NormalGroup;

    eg4：openToRead
        createPermissionGroup：
            createGroup org1NormalGroup 3;
        addAKeyTypeToTheGroup：
            addFunctionToGroup orgAddr "rpublic" "org1NormalGroup"   //全员读
        addsAnAdministratorToThisGroup（anAccountWithPermissions）：无须
*/

contract AuthCenter is Constant
{
    using BytesMapLib for BytesMapLib.Map;
    using SignLib for bytes32[4];
    using UtilLib for *;

    modifier onlyOwner() {
        require(msg.sender == owner, "AuthCenter: only owner is authorized.");
        _;
    }

    address owner;
    //账户组件：有修改，Account增加data字段
    AccountManager accountManager;
    //权限组件：有修改，group增加权限类型3-ALL， 鉴权支持 合约+sig 方式
    IAuthControl authManager;
    // 权限列表
    BytesMapLib.Map keyTypes;

    event logCheck(address caller, address callerOrg, address callee, address calleeOrg, bytes key);

    constructor(address accountManagerAddr, address authManagerAddr){
        owner = msg.sender;

        //init gov account
        accountManager = AccountManager(accountManagerAddr);

        //init gov auth
        authManager = IAuthControl(authManagerAddr);

        keyTypes.insert(ROLE_R, TYPE_PUBLIC);
        keyTypes.insert(ROLE_W, TYPE_ADMIN);
    }

    function getOwner() public returns (address) {
        return owner;
    }

    function getAccountManager() public returns (address) {
        return address(accountManager);
    }

    function getAuthManager() public returns (address) {
        return address(authManager);
    }


    //The Callee is externalAccount is used to call the authentication authority
    function check(address caller, address callee, bytes key) public view returns (bool) {
        if (caller == address(0x0) || callee == address(0x0) || key.length == 0) {
            return false;
        }

        //Key must be configured, and unconfigured keys are considered to have no permissions
        //You can also configure ALL keys to be the same permissions, key ALL, for Storage is a list of scenarios
        bytes memory keyType = keyTypes.get(key);
        if (keyType.length == 0) {
            keyType = keyTypes.get(KEY_ALL);
            if (keyType.length == 0) {
                keyType = key;
            }
        }

        //getsTheOrganizationToWhichTheCallerBelongs
        address innerCaller = accountManager.getUserAccount(caller);
        if (innerCaller == 0x0) {
            return false;
        }
        //        UserAccount callerUA = UserAccount(innerCaller);
        //        address callerOrg =  Account(callerUA.getData()).getOrg();

        //getsTheOrganizationOfTheCalledParty
        address innerCallee = accountManager.getUserAccount(callee);
        if (innerCallee == 0x0) {
            return false;
        }
        UserAccount calleeUA = UserAccount(innerCallee);
        address calleeOrg = calleeUA.getAccountAdmin();
        // Account(calleeUA.getData()).getOrg();

        //thisCanBeExpandedToMultipleRoles
        address roleAddr = USER_ADDR;
        if (IOrganization(calleeOrg).isInternalAccountAdmin(innerCaller)) {
            roleAddr = ADMIN_ADDR;
        } else if (caller == callee) {
            roleAddr = OWNER_ADDR;
        }

        emit logCheck(caller, 0x0, callee, calleeOrg, key);

        bytes4 sig = bytes4(sha3(keyType));
        return authManager.canCallFunction(calleeOrg, sig, roleAddr);
    }

    function checkWithSign(address callee, bytes key, bytes args, bytes32[4] sign) public view returns (address, bool) {
        address txOrigin = sign.checkSign();
        if (txOrigin == 0x0) {
            return (txOrigin, false);
        }
        bytes32 message = sign[0];
        if (args.length > 0) {
            require(this.checkNonce(args, message, txOrigin), "args or nonce not verify");
        }
        return (txOrigin, this.check(txOrigin, callee, key));
    }

    //usedToInvokeThePartyAuthentication, callee is UserAccount.getData()
    function check2(address caller, address org, address callee, bytes key) public view returns (bool) {
        if (caller == address(0x0) || org == address(0x0) || key.length == 0) {
            return false;
        }

        //Key must be configured, and unconfigured keys are considered to have no permissions
        //You can also configure ALL keys to be the same permissions, key ALL, for Storage is a list of scenarios
        bytes memory keyType = keyTypes.get(key);
        if (keyType.length == 0) {
            keyType = keyTypes.get(KEY_ALL);
            if (keyType.length == 0) {
                keyType = key;
            }
        }

        //获取主调方所属组织, caller is account or org
        address roleAddr;
        address callerOrg;
        if (caller == org) {
            roleAddr = ADMIN_ADDR;
        } else {
            address innerCaller = accountManager.getUserAccount(caller);
            if (innerCaller != 0x0) {

                UserAccount callerUA = UserAccount(innerCaller);
                callerOrg = IAccount(callerUA.getData()).getOrg();
                roleAddr = USER_ADDR;
                if (IOrganization(callerOrg).isInternalAccountAdmin(innerCaller)) {
                    roleAddr = ADMIN_ADDR;
                } else if (innerCaller == callee || callerUA.getData() == callee) {
                    roleAddr = OWNER_ADDR;
                }
            } else {
                roleAddr = caller;
            }
        }
        emit logCheck(caller, callerOrg, callee, org, key);

        bytes4 sig = bytes4(sha3(keyType));
        return authManager.canCallFunction(org, sig, roleAddr);
    }

    function check2WithSign(address org, address callee, bytes key, bytes args, bytes32[4] sign) public view returns (address, bool) {
        address txOrigin = sign.checkSign();
        if (txOrigin == 0x0) {
            return (txOrigin, false);
        }
        bytes32 message = sign[0];
        if (args.length > 0) {
            require(this.checkNonce(args, message, txOrigin), "args or nonce not verify");
        }
        return (txOrigin, this.check2(txOrigin, org, callee, key));
    }


    //theAdministratorManagesTheKeyPermissions
    function addKeyType(bytes key, bytes value) public onlyOwner returns (bool) {
        if (key.length == 0) {
            return false;
        }
        return keyTypes.insert(key, value);
    }

    function removeKeyType(bytes key) public onlyOwner returns (bool) {
        if (key.length == 0) {
            return false;
        }
        return keyTypes.remove(key);
    }

    function getAllKeyType() public view returns (bytes[] memory keyList, bytes[] memory valList, uint retNum){
        return keyTypes.getAll();
    }

    function getNonceFromAccount(address account) constant returns (uint256){
        //getsTheOrganizationOfTheCalledParty
        address innerCallee = accountManager.getUserAccount(account);
        if (innerCallee == 0x0) {
            return 0;
        }
        UserAccount calleeUA = UserAccount(innerCallee);
        IAccount calleeAccount = IAccount(calleeUA.getData());

        return calleeAccount.getNonce();
    }

    function updateNonceFromAccount(address account) internal returns (uint256){
        //getsTheOrganizationOfTheCalledParty
        address innerCallee = accountManager.getUserAccount(account);
        if (innerCallee == 0x0) {
            return 0;
        }
        UserAccount calleeUA = UserAccount(innerCallee);
        IAccount calleeAccount = IAccount(calleeUA.getData());
        calleeAccount.updateNonce();
        return calleeAccount.getNonce();
    }

    function checkNonce(bytes args, bytes32 message, address account)  returns (bool){
        if (address(0) == account) {
            return false;
        }

        bytes memory nonce = getNonceFromAccount(account).uint2bytes();
        bytes memory origin = args.bytesAppend(nonce);

        bytes32 argsHash = keccak256(origin);
        if (argsHash != message) {
            return false;
        }
        updateNonceFromAccount(account);
        return true;

    }

    function checkAccount(address account) public view returns (bool){
        //getsTheOrganizationOfTheCalledParty
        address innerCallee = accountManager.getUserAccount(account);
        if (innerCallee == 0x0) {
            return false;
        }
        UserAccount calleeUA = UserAccount(innerCallee);
        return calleeUA.isNormal();
    }

    function getInnerAccountAndStatus(address account) public view returns (address, bool){
        //getsTheOrganizationOfTheCalledParty
        address innerCallee = accountManager.getUserAccount(account);
        if (innerCallee == 0x0) {
            return (address(0), false);
        }
        UserAccount calleeUA = UserAccount(innerCallee);
        return (innerCallee, calleeUA.isNormal());
    }


}
