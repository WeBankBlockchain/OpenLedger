pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "../govAccount/interface/IAccountManager.sol";
import "../auth/roleRes/BaseResourceWithRole.sol";
import "../project/interface/IProject.sol";

contract BaseOrgAdmin is BaseResourceWithRole {
    modifier onlyAdmin(bytes32[4] sign, string operation, bytes detail) {
        require(checkAuth(sign, roleAdmin, operation, detail), "BaseOrgAdmin:Forbbiden ".strConcat(operation));
        _;
    }
    modifier onlyMember(bytes32[4] sign, string operation, bytes detail) {
        require(checkAuth(sign, roleMember, operation, detail), "BaseOrgAdmin:Forbbiden ".strConcat(operation));
        _;
    }
    modifier onlyAdminWithoutDetail(bytes32[4] sign, string operation) {
        bytes memory detail;
        require(checkAuth(sign, roleAdmin, operation, detail), "BaseOrgAdmin:Forbbiden ".strConcat(operation));
        _;
    }
    modifier onlyMemberithoutDetail(bytes32[4] sign, string operation) {
        bytes memory detail;
        require(checkAuth(sign, roleMember, operation, detail), "BaseOrgAdmin:Forbbiden ".strConcat(operation));
        _;
    }
    modifier onlyProject() {
        require(msg.sender == _project && holder == address(0), "BaseAccountHolder:required project call! ");
        _;
    }
    string constant roleAdmin = "ADMIN";  //本机构管理员
    string constant roleMember = "MEMBER"; //本机构成员，不包括管理员
    string[]  DEFAULT_ADMIN_OPERATION = ["grant", "revoke"];
    string[]  DEFAULT_MEM_OPERATION = ["registerAsset", "upgradeAsset", "setPrice", "deposit", "setRate", "getHolders",
    "openAccount", "addBook", "deposit", "withdrawl", "getTotalBalance", "queryBook",
    "issue", "getNoteDetail", "getAccountNoteSize", "updateNoteNo", "updateNoteProperties", "getNoteProperties", "updateNoteBatch",
    "freezeNote", "unfreezeNote", "getTotalNoteSize", "getTotalNoteSize", "getTearNotes", "enableBatch", "getBalance","cancel","freeze","unfreeze","createAccount"];

    address[] accountList;
    address accountManager;
    address _project;

    constructor (address project, address _holder) public BaseResourceWithRole(_holder){
        accountManager = IProject(_project).getAccountManager();
        _project = project;
    }

    function setHolder(address _holder) internal {
        holder = _holder;
        bytes32[4] nullBytes;
        initRoles(nullBytes);
    }

    function createHolder(address adminExternal) external onlyProject returns (address){
        address adminId = IAclManager(IProject(_project).getAclManager()).createId(adminExternal);
        require(address(0) != adminId, "BaseOrgAdmin:createId failed!");
        setHolder(adminId);
        return address(adminId);
    }

    function initRoles(bytes32[4] sign) internal returns (bool){
        createRole(roleAdmin, sign);
        addOperationToResGroup(roleAdmin, DEFAULT_ADMIN_OPERATION, sign);
        createRole(roleMember, sign);
        addOperationToResGroup(roleMember, DEFAULT_MEM_OPERATION, sign);
        return true;
    }

    //新增一个管理员
    ///[param[in] another, 新增管理员地址
    function registerAdmin(address adminExternal, bytes32[4] sign) external returns (bool){
        address adminId = IAclManager(IProject(_project).getAclManager()).createId(adminExternal);
        require(address(0) != adminId, "BaseOrgAdmin:createId failed!");
        grantId(roleAdmin, adminExternal, sign);
        return true;
    }

    //新增一个组织成员
    ///[param[in] member, 新增组织成员地址
    function registerMember(address memberExternal, bytes32[4] sign) external returns (bool){
        address memId = IAclManager(IProject(_project).getAclManager()).createId(memberExternal);
        require(address(0) != memId, "BaseOrgAdmin:createId failed!");
        grantId(roleMember, memberExternal, sign);
        return true;
    }

    //移除一个管理员
    ///[param[in] another, 目标管理员地址
    function unregisterAdmin(address adminExternal, bytes32[4] sign) external returns (bool){
        revokeId(roleAdmin, adminExternal, sign);
        return true;
    }

    //移除一个组织成员
    ///[param[in] member, 目标成员地址
    function unregisterMember(address memberExternal, bytes32[4] sign) external returns (bool){
        revokeId(roleMember, memberExternal, sign);
        return true;
    }


    function isAdmin(bytes32[4] sign, string operation, bytes detail) external view returns (bool){
        return checkAuth(sign, roleAdmin, operation, detail);
    }

    function isMember(bytes32[4] sign, string operation, bytes detail) external view returns (bool){
        return checkAuth(sign, roleMember, operation, detail);
    }


}