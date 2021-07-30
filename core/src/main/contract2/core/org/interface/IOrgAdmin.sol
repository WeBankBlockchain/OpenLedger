pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

//组织管理接口
contract IOrgAdmin
{
    function isAdmin(bytes32[4] sign,string operation, bytes detail) external view returns(bool);

    function isMember(bytes32[4] sign,string operation, bytes detail) external view returns(bool);
}