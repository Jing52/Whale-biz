package com.whale.framework.process.constants;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.constants
 * @Description:
 * @Date: 2022/12/7 10:12 AM
 */
public class OriginalSqlConstant {

    public final static String SELECT_ACT_RU_IDENTITYLINK_1 = "SELECT * FROM ACT_RU_IDENTITYLINK WHERE TYPE_ = 'candidate' AND GROUP_ID_ in (%s)";

    public final static String SELECT_ACT_RU_IDENTITYLINK_2 = "SELECT * FROM ACT_RU_IDENTITYLINK WHERE TYPE_ = 'candidate' AND TASK_ID_ = %s";

    public final static String SELECT_ACT_RU_IDENTITYLINK_3 = "SELECT * FROM ACT_RU_IDENTITYLINK WHERE TYPE_ = 'candidate' AND TASK_ID_ in (%s)";

    public final static String UPDATE_ACT_RU_TASK_1 = "update ACT_RU_TASK set OWNER_ = ?, ASSIGNEE_ = ?, DELEGATION_ = ? WHERE ID_ = ?";
}
