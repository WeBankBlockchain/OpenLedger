pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./auth/storage/AuthTable.sol";
import "./auth/Identity.sol";
import "./auth/interface/IdInterface.sol";


contract AclManager {
    AuthTable authTable;
    mapping(address => address) externalAndId;

    constructor() public {
        authTable = new AuthTable();
    }

    function createId(address _external) external returns (address){
        require(externalAndId[_external] == address(0), "AclManager:external address has been created");

        Identity id = new Identity(address(this), _external);
        require(address(0) != address(id), "AclManager:create id failed!");
        externalAndId[_external] = address(id);
        return address(id);
    }

    function grant(address resource, address allowId, string operation, bytes detail) public returns (bool) {
        if (msg.sender == allowId) {
            return true;
        }
        return authTable.grant(msg.sender, resource, allowId, operation, detail);
    }


    function check(address resource, address allowId, string operation, bytes detail) public returns (bool) {
        if (msg.sender == allowId) {
            return true;
        }
        return authTable.check(msg.sender, resource, allowId, operation, detail);
    }


    function revoke(address resource, address allowId, string operation, bytes detail) public returns (bool) {
        return authTable.revoke(msg.sender, resource, allowId, operation, detail);
    }

    function getIdByExternal(address _external) public view returns (address){
        require(externalAndId[_external] != address(0), "AclManager:external address has not been created");
        return externalAndId[_external];
    }

    function changeExternal(address id, address newExternal) public returns (bool){
        require(externalAndId[newExternal] == address(0), "AclManager:external address has been created");
        externalAndId[IdInterface(id).getExternalAddress()] = address(0);
        Identity(id).setExternalAddress(newExternal);
        return true;
    }


}
