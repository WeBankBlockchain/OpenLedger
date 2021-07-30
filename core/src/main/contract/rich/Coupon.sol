pragma solidity ^0.4.26;
pragma experimental ABIEncoderV2;

import "./Interfaces.sol";
import "./AddressArray.sol";
import "./PropStorage.sol";

//优惠券，印花等小面值资产
//每个合约存储一批coupon，该批次coupon除ID和owner其他信息均相同
//完整ID格式：前缀+序号+后缀
//coupon流程 1. 发行（issue）, 2. deposit，托管 3. transfer...
contract Coupon is IAsset
{
    //注册一类新资产，注册成功后自动成为该资产的托管人。
    //暂不考虑别的机构注册资产的情形
    //\param[in] asset 注册资产的地址
    mapping(address => uint) balance;
    address _issuer; //发行人账户地址
    uint _amount; //总数量
    string _prefix;  //前缀
    string _postfix;  //后缀

    AddressArray _inventory; //库存

    //to do 格式化编号为固定长度
    AddressArray _SNs; //编号

    //每个coupon的所有人
    mapping(string => address) _owners;


    PropStorage _prop; //coupon属性，每一批所有属性都一样
    //托管人
    address _custody;

    function deposit(address account, uint amount) public {
        if (msg.sender == _custody) {
//            balance[account] += amount;
            _issuer = account; //存入发行人账户
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

    //根据ID获取已经发行的coupon
    function acquire(string sn) public returns(address) {
        address _ret = ox0;
        if (_inventory.memeberOf(address(sn))) {
            _inventory.remove(address(sn));
            _ret = getAccount(msg.sender);
            _owners[sn] = _ret;
        }

        return _ret;
    }

    //发行coupon
    function issuer(string prefix, string postfix, uint startsn, uint amount) public {
        if (msg.sender == _issuer || msg.sender == _custody) {
            _inventory.add(genSN(startsn));  //
            }
    }

    //格式转换
    function genSN(uint startsn) public returns (string) {
        return "";  //
    }


}