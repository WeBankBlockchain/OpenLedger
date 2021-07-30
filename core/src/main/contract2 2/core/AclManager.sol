pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./auth/storage/AuthTable.sol";
import "./auth/interface/IdInterface.sol";

contract AclManager {

    AuthTable authTable;
    mapping(address => address) externalAndId;

    constructor() public {
        authTable = new AuthTable();
    }


    function grant(address resource, address allowId, string operation, bytes detail) public returns (bool) {
        return authTable.grant(msg.sender, resource, allowId, operation, detail);
    }


    function check(address resource, address allowId, string operation, bytes detail) public returns (bool) {
        return authTable.check(msg.sender, resource, allowId, operation, detail);
    }


    function revoke(address resource, address allowId, string operation, bytes detail) public returns (bool) {
        return authTable.revoke(msg.sender, resource, allowId, operation, detail);
    }

    function getIdByExternal(address _external) public view returns (address){
        return externalAndId[_external];
    }

    function changeExternal(address id, address newExternal) public returns (bool){
        externalAndId[IdInterface(id).getExternalAddress()] = address(0);
        IdInterface(id).setExternalAddress(newExternal);
        return true;
    }

    function initExternal(address id, address _external) public returns (bool){
        require(id != address(0) && msg.sender == id, "AclManager:required id call");
        require(externalAndId[_external] == address(0), "AclManager:external address has been created");
        externalAndId[_external] = id;
        return true;
    }

}
