pragma solidity ^0.4.26;
pragma experimental ABIEncoderV2;

import "./interfaces.sol";

contract Account is IAccount
{
    //账户主人
    IAccountHolder _owner;

    function transfer(Asset asset, address to, uint amount) public {
        if (_owner.allowTransfer(msg.sender, to, amount) ) {
            asset.transfer(this, to, amount);
        }
    }
}