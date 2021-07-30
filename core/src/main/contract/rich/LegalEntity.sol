pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;

import "./AddressArray.sol";
import "./Identity.sol";
//import "../../contract2 2/core/auth/Identity.sol";

//拥有资产的的实体，可以是个人也可以是组织。来自组织的业务员发起的交易代表的是组织，不是业务员，因此当业务员发起交易是，需要识别出其所代表的实体。
contract LegalEntity is Identity
{
    using BytesMapLib for BytesMapLib.Map;
    using UtilLib for *;
    using LibSafeMath for uint256;
    using SignLib for bytes32[4];

    address org;
    BytesMapLib.Map _storage;
    IAuthCenter authCenter;
    address accountManager;
    address authManager;
    uint256 nonce;

    //角色组
    mapping (string => address) _roles;
    address _KYCOrg;  //KYC组织
    address _owner;  //

    //是否允许转移本实体持有的资产
    function allowTransfer(LegalEntity who, address to, uint amount) public returns(bool) {
        return true;
    }

    function createRole(string roleName, uint cap) public {
        address role =  new AddressArray(cap);
        _roles[roleName] = role;
    }

    function memeberOf(string roleName, address member) public onlyOwner view returns (bool) {
        AddressArray role = AddressArray(_roles[roleName]);
        return AddressArray.memeberOf(member);
    }

    //how many slot?
    function space(string roleName) public onlyOwner view returns (uint) {
        AddressArray role = AddressArray(_roles[roleName]);
        return role.space();
    }

    function add(string roleName, address member) public onlyOwner returns (bool) {
        AddressArray role = AddressArray(_roles[roleName]);
        return role.add(member);
    }

    function remove(string roleName, address member) public onlyOwner returns (bool) {
        AddressArray role = AddressArray(_roles[roleName]);
        return role.remove(member);
    }

    //param: authCenterAddr: Authorization center, all implementations within the contract need to call AuthCenter. check for authentication
    //       orgAddr: The management organization of the opening bank for changing the account. The account is registered by the opening bank, and the address of the opening bank can be directly filled in when opening the account
    constructor(address authCenterAddr, address orgAddr){
        bytes memory b = address(this).addr2bytes();
        _storage.insert(ID, b);
        authCenter = IAuthCenter(authCenterAddr);
        accountManager = authCenter.getAccountManager();
        authManager = authCenter.getAuthManager();
        if (orgAddr != address(0x0)) {
            org = orgAddr;
        } else {
            org = address(this);
        }
    }

    function getAuthCenter() public view returns (address) {
        return authCenter;
    }

    function getAuthManager() public view returns (address) {
        return authCenter.getAuthManager();
    }

    function getAccountManager() public view returns (address) {
        return authCenter.getAccountManager();
    }

    function getOrg() public view returns (address) {
        return org;
    }


    //theFollowingInterfacesAreGenericDataCalls
    //Key is the identity of the stored data, and Value is the content of the data
    //Authentication mode: the caller (the bank of the account) + the address of the contract to be transferred + Key + operation : read and write
    function get(bytes key) constant returns (bytes)
    {
        require(authCenter.check2(msg.sender, org, address(this), key.bytesAppend(MODE_R)), "get Forbidden!");
        return _storage.get(key);
    }

    function add(bytes key, bytes value) returns (bool replaced)
    {
        require(authCenter.check2(msg.sender, org, address(this), key.bytesAppend(MODE_W)), "add Forbidden!");
        return _storage.add(key, value);
    }

    function set(bytes key, bytes value) returns (bool replaced)
    {
        require(authCenter.check2(msg.sender, org, address(this), key.bytesAppend(MODE_W)), "set Forbidden!");
        return _storage.set(key, value);
    }

    function insert(bytes key, bytes value) returns (bool replaced)
    {
        require(authCenter.check2(msg.sender, org, address(this), key.bytesAppend(MODE_W)), "insert Forbidden!");
        return _storage.insert(key, value);
    }

    function remove(bytes key) returns (bool replaced)
    {
        require(authCenter.check2(msg.sender, org, address(this), key.bytesAppend(MODE_W)), "remove Forbidden!");
        return _storage.remove(key);
    }


    function size() constant returns (uint)
    {
        return _storage.size();
    }


    //theFollowingIsTheProxyInterface
    function getWithSign(bytes key, bytes32[4] sign) constant returns (bytes)
    {
        bytes memory args;
        args = args.bytesAppend(key);

        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, this, key.bytesAppend(MODE_R), args, sign);
        require(check, "get Forbidden!");

        return _storage.get(key);
    }

    function addWithSign(bytes key, bytes value, bytes32[4] sign) returns (bool replaced)
    {
        bytes memory args;
        args = args.bytesAppend(key).bytesAppend(value);

        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, this, key.bytesAppend(MODE_W), args, sign);
        require(check, "add Forbidden!");

        return _storage.add(key, value);
    }

    function setWithSign(bytes key, bytes value, bytes32[4] sign) returns (bool replaced)
    {
        bytes memory args;
        args = args.bytesAppend(key).bytesAppend(value);

        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, this, key.bytesAppend(MODE_W), args, sign);
        require(check, "set Forbidden!");

        return _storage.set(key, value);
    }

    function insertWithSign(bytes key, bytes value, bytes32[4] sign) returns (bool replaced)
    {
        bytes memory args;
        args = args.bytesAppend(key).bytesAppend(value);

        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, this, key.bytesAppend(MODE_W), args, sign);
        require(check, "insert Forbidden!");

        return _storage.insert(key, value);
    }

    function removeWithSign(bytes key, bytes32[4] sign) returns (bool replaced)
    {
        bytes memory args;
        args = args.bytesAppend(key);

        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, this, key.bytesAppend(MODE_W), args, sign);
        require(check, "remove Forbidden!");

        return _storage.remove(key);
    }

    function getNonce() public view returns (uint256){
        return nonce;
    }

    function updateNonce() public returns (uint256){
        nonce = nonce.add(1);
        return nonce;
    }


}
