pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;

contract IAuthCenter
{
    function getAccountManager() public returns (address) ;

    function getAuthManager() public returns (address);

    //用于调用方鉴权, callee is externalAccount
    function check(address caller, address callee, bytes key) public view returns (bool);

    function checkWithSign(address callee, bytes key, bytes args, bytes32[4] sign) public view returns (address, bool);

    //用于调用方鉴权, callee is UserAccount.getData()
    function check2(address caller, address org, address callee, bytes key) public view returns (bool) ;

    function check2WithSign(address org, address callee, bytes key, bytes args, bytes32[4] sign) public view returns (address, bool);


    //管理方管理key权限
    function addKeyType(bytes key, bytes value) public returns (bool) ;

    function removeKeyType(bytes key) public returns (bool) ;

    function getAllKeyType() public view returns (bytes[] memory keyList, bytes[] memory valList, uint retNum);

    function getNonceFromAccount(address account) constant returns (uint256);

    function updateNonceFromAccount(address account) internal returns (uint256);

    function checkNonce(bytes args, bytes32 message, address account)  returns (bool);

    function checkAccount(address account) public view returns (bool);

    function getInnerAccountAndStatus(address account) public view returns(address,bool);
}
