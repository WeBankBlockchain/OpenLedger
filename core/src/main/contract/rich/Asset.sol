pragma solidity ^0.4.26;
pragma experimental ABIEncoderV2;

import "./Interfaces.sol";

contract Asset is IAsset
{
    //注册一类新资产，注册成功后自动成为该资产的托管人。
    //暂不考虑别的机构注册资产的情形
    //\param[in] asset 注册资产的地址
    mapping(address => uint) balance;

    //托管人
    address _custody;

    function deposit(address account, uint amount) public {
        if (msg.sender == _custody) {
            balance[account] += amount;
        }
    }

    function withdraw(address account, uint amount) public {
        if (msg.sender == _custody) {
            balance[account] -= amount;
        }
    }

    function transfer(address from, address to, uint amount) public {
        if (msg.sender == from) {
            balance[from] -= amount;
            balance[to] -= amount;
        }
    }
}