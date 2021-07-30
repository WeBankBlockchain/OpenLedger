pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;

import "./core/account/base/BaseAccountHolder.sol";
import "./core/govAccount/BaseAccount.sol";

contract Account is BaseAccountHolder
{
    constructor (address _project,address _holder,address accountManager, address accountAdmin) BaseAccountHolder( _project, _holder, accountManager,  accountAdmin) public {
    }

}    
    
    
    
    
    
    
