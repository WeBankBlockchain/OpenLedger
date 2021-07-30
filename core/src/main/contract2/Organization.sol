pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./core/account/base/BaseAccountHolder.sol";
import "./core/org/BaseCustody.sol";
import "./core/org/BaseOrgAdmin.sol";

contract Organization is BaseCustody, BaseOrgAdmin, BaseAccountHolder
{

    address project;
    constructor(address _project, address _accountManager, address _holderId) public BaseCustody(_project) BaseOrgAdmin(_accountManager, _holderId) BaseAccountHolder(_project,_holderId,_accountManager,msg.sender){
        project = _project;
    }

    function getProjectTerm() public view returns (address){
        return IProject(project).getTerm();
    }

    //account manager
    function createAccount(address _external,bytes[] keyList, bytes[] valueList,bytes32[4] sign) public onlyMemberithoutDetail(sign,"createAccount")returns (bool, address) {
        bool isCreate;
        address account;
        address accountId = IAclManager(IProject(project).getAclManager()).createId(_external);

        (isCreate, account) = IAccountManager(accountManager).newAccount(project,accountId);

        accountList.push(account);
        return (true, account);
    }

    function cancel(address account,bytes32[4] sign) public onlyMember(sign,"cancel",account.addr2bytes()) returns (bool)  {
        return IAccountManager(accountManager).cancelByAdmin(account);
    }

    function freeze(address account,bytes32[4] sign) public onlyMember(sign,"freeze",account.addr2bytes())  returns (bool) {
        return IAccountManager(accountManager).freezeByAdmin(account);
    }

    function unfreeze(address account,bytes32[4] sign) public  onlyMember(sign,"unfreeze",account.addr2bytes()) returns (bool) {
        return IAccountManager(accountManager).unfreezeByAdmin(account);
    }

}

    
    
    
    
    
    
