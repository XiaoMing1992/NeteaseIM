package com.konka.konkaim.chat.team;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.konka.konkaim.MainApplication;
import com.konka.konkaim.bean.DbBean;
import com.konka.konkaim.bean.TeamDbBean;
import com.konka.konkaim.chat.AVChatKit;
import com.konka.konkaim.chat.session.SessionHelper;
import com.konka.konkaim.db.DBUtil;
import com.konka.konkaim.db.TeamDBUtil;
import com.konka.konkaim.user.UserInfoUtil;
import com.konka.konkaim.util.TimeUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamBeInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.constant.TeamInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.CreateTeamResult;
import com.netease.nimlib.sdk.team.model.Team;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hzxuwen on 2015/9/25.
 */
public class TeamCreateHelper {
    private static final String TAG = TeamCreateHelper.class.getSimpleName();
    private static final int DEFAULT_TEAM_CAPACITY = 200;

    /**
     * 创建讨论组
     */
    public static void createNormalTeam(final Context context, final String roomName, final ArrayList<String> memberAccounts, final boolean isNeedBack, final RequestCallback<CreateTeamResult> callback) {

        final String teamName = "讨论组4";

        //DialogMaker.showProgressDialog(context, context.getString(com.netease.nim.uikit.R.string.empty), true);

        // 创建群
        HashMap<TeamFieldEnum, Serializable> fields = new HashMap<TeamFieldEnum, Serializable>();
        fields.put(TeamFieldEnum.Name, teamName);
        fields.put(TeamFieldEnum.BeInviteMode, TeamBeInviteModeEnum.NoAuth); //被邀请人同意方式，0-需要同意(默认),1-不需要同意。其它返回414
        fields.put(TeamFieldEnum.InviteMode, TeamInviteModeEnum.All); //群邀请模式，0-只有管理员可以邀请其他人入群（默认）,1-所有人都可以邀请其他人入群。其它返回414

        NIMClient.getService(TeamService.class).createTeam(fields, TeamTypeEnum.Advanced, "",
                memberAccounts).setCallback(
                new RequestCallback<CreateTeamResult>() {
                    @Override
                    public void onSuccess(CreateTeamResult result) {
                       // DialogMaker.dismissProgressDialog();
                        System.out.println("createNormalTeam is success, teamId is "+result.getTeam().getId());

                        ArrayList<String> failedAccounts = result.getFailedInviteAccounts(); //邀请成员群数量超限的账号列表
                        if (failedAccounts != null && !failedAccounts.isEmpty()) {
                            //TeamHelper.onMemberTeamNumOverrun(failedAccounts, context);
                            for (int i=0;i<failedAccounts.size();i++) {
                                System.out.println("createNormalTeam -->邀请成员群数量超限的账号 failedAccount is " + failedAccounts.get(i));
                            }
                        } else {
                            //Toast.makeText(DemoCache.getContext(), com.netease.nim.uikit.R.string.create_team_success, Toast.LENGTH_SHORT).show();
                        }

                        MainApplication.finishedMap.put(result.getTeam().getId(), false);
                        MainApplication.myRoomNameMap.put(result.getTeam().getId(), roomName);

                        sendMsgToTeam(result.getTeam().getId(), roomName, memberAccounts, TeamConstant.ACTION_TEAM_CHAT_INVITE);

                        System.out.println("getTeamInviteMode "+result.getTeam().getTeamInviteMode()+"  getTeamBeInviteMode "+result.getTeam().getTeamBeInviteMode()+", membercount="+result.getTeam().getMemberCount());

                        updateTeamToDb(context, result.getTeam().getId());

                        if (isNeedBack) {
                            //SessionHelper.startTeamSession(context, result.getTeam().getId(), (Activity)context, null); // 进入创建的群

                            TeamAVChatProfile.sharedInstance().setTeamAVChatting(true);
                            AVChatKit.outgoingTeamCall(context, false, result.getTeam().getId(), roomName, memberAccounts, teamName);
                        } else {
                            //SessionHelper.startTeamSession(context, result.getTeam().getId());
                            TeamAVChatProfile.sharedInstance().setTeamAVChatting(true);
                            AVChatKit.outgoingTeamCall(context, false, result.getTeam().getId(), roomName, memberAccounts, teamName);
                        }

                        if (callback != null) {
                            callback.onSuccess(result);
                        }
                    }

                    @Override
                    public void onFailed(int code) {
                        //DialogMaker.dismissProgressDialog();
                        if (code == 801) {
//                            String tip = context.getString(com.netease.nim.uikit.R.string.over_team_member_capacity, DEFAULT_TEAM_CAPACITY);
//                            Toast.makeText(DemoCache.getContext(), tip,
//                                    Toast.LENGTH_SHORT).show();
                        } else {
//                            Toast.makeText(DemoCache.getContext(), com.netease.nim.uikit.R.string.create_team_failed,
//                                    Toast.LENGTH_SHORT).show();
                        }

                        Log.e(TAG, "create team error: " + code);
                        System.out.println("--> create team error: " + code);
                    }

                    @Override
                    public void onException(Throwable exception) {
                        //DialogMaker.dismissProgressDialog();
                        exception.printStackTrace();
                        System.out.println("--> create team onException ");
                    }
                }
        );
    }

    /**
     * 创建高级群
     */
    public static void createAdvancedTeam(final Context context, List<String> memberAccounts) {

        String teamName = "高级群";

        //DialogMaker.showProgressDialog(context, context.getString(com.netease.nim.uikit.R.string.empty), true);
        // 创建群
        TeamTypeEnum type = TeamTypeEnum.Advanced;
        HashMap<TeamFieldEnum, Serializable> fields = new HashMap<>();
        fields.put(TeamFieldEnum.Name, teamName);
        NIMClient.getService(TeamService.class).createTeam(fields, type, "",
                memberAccounts).setCallback(
                new RequestCallback<CreateTeamResult>() {
                    @Override
                    public void onSuccess(CreateTeamResult result) {
                        Log.i(TAG, "create team success, team id =" + result.getTeam().getId() + ", now begin to update property...");
                        onCreateSuccess(context, result);
                    }

                    @Override
                    public void onFailed(int code) {
                        //DialogMaker.dismissProgressDialog();
                        String tip;
                        if (code == 801) {
//                            tip = context.getString(com.netease.nim.uikit.R.string.over_team_member_capacity,
//                                    DEFAULT_TEAM_CAPACITY);
                        } else if (code == 806) {
//                            tip = context.getString(com.netease.nim.uikit.R.string.over_team_capacity);
                        } else {
//                            tip = context.getString(com.netease.nim.uikit.R.string.create_team_failed) + ", code=" +
//                                    code;
                        }

                        //Toast.makeText(context, tip, Toast.LENGTH_SHORT).show();

                        Log.e(TAG, "create team error: " + code);
                    }

                    @Override
                    public void onException(Throwable exception) {
                      //  DialogMaker.dismissProgressDialog();
                    }
                }
        );
    }

    /**
     * 群创建成功回调
     */
    private static void onCreateSuccess(final Context context, CreateTeamResult result) {
        if (result == null) {
            Log.e(TAG, "onCreateSuccess exception: team is null");
            return;
        }
        final Team team = result.getTeam();
        if (team == null) {
            Log.e(TAG, "onCreateSuccess exception: team is null");
            return;
        }

        Log.i(TAG, "create and update team success");

        //DialogMaker.dismissProgressDialog();

        // 检查有没有邀请失败的成员
        ArrayList<String> failedAccounts = result.getFailedInviteAccounts();
        if (failedAccounts != null && !failedAccounts.isEmpty()) {
            //TeamHelper.onMemberTeamNumOverrun(failedAccounts, context);
        } else {
            //Toast.makeText(DemoCache.getContext(), com.netease.nim.uikit.R.string.create_team_success, Toast.LENGTH_SHORT).show();
        }

        // 演示：向群里插入一条Tip消息，使得该群能立即出现在最近联系人列表（会话列表）中，满足部分开发者需求
        Map<String, Object> content = new HashMap<>(1);
        content.put("content", "成功创建高级群");
        IMMessage msg = MessageBuilder.createTipMessage(team.getId(), SessionTypeEnum.Team);
        msg.setRemoteExtension(content);
        CustomMessageConfig config = new CustomMessageConfig();
        config.enableUnreadCount = false;
        msg.setConfig(config);
        msg.setStatus(MsgStatusEnum.success);
        NIMClient.getService(MsgService.class).saveMessageToLocal(msg, true);

        // 发送后，稍作延时后跳转
        new Handler(context.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                //SessionHelper.startTeamSession(context, team.getId()); // 进入创建的群
            }
        }, 50);
    }

    public static void sendMsgToTeam(String teamId, String roomName, List<String> accounts, int action){
        SessionTypeEnum sessionType = SessionTypeEnum.Team;
        String text = "房间相关信息";
        IMMessage textMessage = MessageBuilder.createTextMessage(teamId, sessionType, text);
        Map<String, Object> roomMap = new HashMap<>();
        //roomMap.put("data", new Gson().toJson(new RoomRequestMessage(teamId, "audio")));
        roomMap.put("type","audio");
        roomMap.put("roomName",roomName);
        //roomMap.put("members", accounts);
        roomMap.put("callTime", (double)(System.currentTimeMillis()/1000));
        roomMap.put("action", action);
        textMessage.setRemoteExtension(roomMap);
        textMessage.setPushContent("多人通话请求");

        System.out.println("---> sendMsgToTeam type is audio, roomName is "+roomName+", teamId is "+teamId+", action="+action);

        // 发送给对方
        NIMClient.getService(MsgService.class).sendMessage(textMessage, true).setCallback(new RequestCallbackWrapper<Void>() {
            @Override
            public void onResult(int code, Void result, Throwable exception) {
                System.out.println("---> sendMsgToTeam code="+code);
                if (code == ResponseCode.RES_SUCCESS) {
                    System.out.println("---> sendMsgToTeam success ");
                }
            }
        });
    }

    private static void addTeamToDb(Context context, String teamId){
        TeamDbBean no_Bean = new TeamDbBean();
        //no_Bean.setIs_team(1);
        no_Bean.setMy_account(UserInfoUtil.getAccid());
        no_Bean.setRecord_time(TimeUtil.getNowTime());
        no_Bean.setTeamId(teamId);

        //no_Bean.setTeam_name(teamDbBeanList.get(0).getTeam_name());

        System.out.println("teamId="+teamId);//获取teamId
        TeamDBUtil.add(context, no_Bean);
    }

    private static void updateTeamToDb(Context context, String teamId){
        List<TeamDbBean> teamDbBeanList = TeamDBUtil.queryByTeamId(context, UserInfoUtil.getAccid(), teamId);
        if (teamDbBeanList != null && teamDbBeanList.size()>0) {
            TeamDbBean no_Bean = new TeamDbBean();
            no_Bean.setId(teamDbBeanList.get(0).getId());
            //no_Bean.setIs_team(teamDbBeanList.get(0).getIs_team());
            no_Bean.setMy_account(UserInfoUtil.getAccid());

            no_Bean.setRecord_time(TimeUtil.getNowTime());

            no_Bean.setTeamId(teamDbBeanList.get(0).getTeamId());

            no_Bean.setTeam_name(teamDbBeanList.get(0).getTeam_name());

            System.out.println("update, id=" + teamDbBeanList.get(0).getId()+"teamId=" + teamId);//获取teamId
            TeamDBUtil.update(context, no_Bean);
        }else {
            addTeamToDb(context,teamId);
        }
    }
}
