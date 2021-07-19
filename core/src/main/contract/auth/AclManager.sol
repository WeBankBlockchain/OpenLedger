pragma solidity ^0.4.0;

import "./AuthTable.sol";

contract AclManager {
    AuthTable authTable;
    constructor() public {
        authTable = new AuthTable();
    }

    function grant(address resource, address allowId, string operation) public returns (bool) {
        string detail;
        return authTable.grant(msg.sender, resource, allowId, operation, detail);
    }

    function grant(address resource, address allowId, string operation, string detail) public returns (bool) {
        return authTable.grant(msg.sender, resource, allowId, operation, detail);
    }


    function check(address resource, address allowId, string operation) public returns (bool) {
        string detail;
        return authTable.check(msg.sender, resource, allowId, operation, detail);
    }

    function check(address resource, address allowId, string operation, string detail) public returns (bool) {
        return authTable.check(msg.sender, resource, allowId, operation, detail);
    }

    function revoke(address resource, address allowId, string operation) public returns (bool) {
        string detail;
        return authTable.revoke(msg.sender, resource, allowId, operation, detail);
    }

    function revoke(address resource, address allowId, string operation, string detail) public returns (bool) {
        return authTable.revoke(msg.sender, resource, allowId, operation, detail);

    }
}
