package com.konka.konkaim.chat.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.konka.konkaim.R;
import com.konka.konkaim.adapter.GroupOneAdapter;
import com.konka.konkaim.api.HttpListener;
import com.konka.konkaim.bean.GroupBean;
import com.konka.konkaim.bean.TeamDbBean;
import com.konka.konkaim.db.TeamDBUtil;
import com.konka.konkaim.http.HttpHelper;
import com.konka.konkaim.ui.RecycleViewDivider;
import com.konka.konkaim.user.UserInfoUtil;
import com.konka.konkaim.util.TimeUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.util.ArrayList;
import java.util.List;

public class EditManyActivity extends AppCompatActivity {
    private final String TAG = "EditManyActivity";
    private Context mContext;
    private TextView tv_group_count;
    private EditText et_remark;

    private RecyclerView group_recyclerView;
    private GroupOneAdapter groupOneAdapter;
    private List<TeamMember> myTeamMembers;
    private LinearLayoutManager linearLayoutManager;
    private String myTeamId;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_many);
        initView();
        initData();
    }

    private void initView() {
        tv_group_count = (TextView) findViewById(R.id.tv_group_count);
        et_remark = (EditText)findViewById(R.id.et_remark);
        group_recyclerView = (RecyclerView) findViewById(R.id.group_recyclerView);
        et_remark.requestFocus();
        listener();
    }


    private void listener() {
        et_remark.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                System.out.println(s);
                System.out.println(String.valueOf(s).length());
                if (String.valueOf(s).length()==15) {
                    Toast.makeText(mContext, "已经输够15个字符，不能再输入", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });

        et_remark.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                System.out.println("EditManyChatWindow --->actionId is "+actionId);
                //当actionId == XX_SEND 或者 XX_DONE时都触发
                //或者event.getKeyCode == ENTER 且 event.getAction == ACTION_DOWN时也触发
                //注意，这是一定要判断event != null。因为在某些输入法上会返回null。
                if (actionId == EditorInfo.IME_ACTION_SEND
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode() && KeyEvent.ACTION_DOWN == event.getAction())) {
                    //处理事件
                    System.out.println("EditManyChatWindow --->myTeamId=" +myTeamId+" new teamName "+et_remark.getText().toString().trim());
                    if (et_remark.getText().toString().trim().isEmpty()){
                        Toast.makeText(mContext, "你还没有输入内容", Toast.LENGTH_SHORT).show();
                    }else if (et_remark.getText().toString().trim().length()>15){
                        Toast.makeText(mContext, "字数限制为15", Toast.LENGTH_SHORT).show();
                    }else {
                        //modifyTeamName(UserInfoUtil.getAccid(), myTeamId, et_remark.getText().toString().trim());
                        updateTeamNick(myTeamId, et_remark.getText().toString().trim());

                        Intent intent = new Intent();
                        intent.putExtra("position", position);
                        intent.putExtra("teamId", myTeamId);
                        intent.putExtra("success", true);
                        intent.putExtra("newName", et_remark.getText().toString().trim());
                        setResult(2, intent);
                        //onEditTeamListener.onEditTeam(position, myTeamId, true, et_remark.getText().toString().trim());
                        finish();
                    }
                    return true;
                }else if (event != null
                        && (KeyEvent.KEYCODE_DPAD_UP == event.getKeyCode() || KeyEvent.KEYCODE_DPAD_DOWN == event.getKeyCode())
                        && KeyEvent.ACTION_DOWN == event.getAction()) {

                    return true;
                }
                return false;
            }
        });
    }

    private void initData() {
        mContext = EditManyActivity.this;

        HttpHelper.setHttpListener(httpListener);
        Intent intent = getIntent();

        this.myTeamId = intent.getStringExtra("teamId");
        this.position = intent.getIntExtra("position",-1);

        System.out.println("------->EditManyChatWindow myTeamId=" + myTeamId);

        linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        group_recyclerView.setLayoutManager(linearLayoutManager);
        group_recyclerView.addItemDecoration(new RecycleViewDivider(mContext, LinearLayoutManager.HORIZONTAL, 0,
                (int) mContext.getResources().getDimension(R.dimen.group_one_item_dividerWidth)));
        myTeamMembers = new ArrayList<>();
        //demo();
        groupOneAdapter = new GroupOneAdapter(mContext, myTeamMembers);
        groupOneAdapter.setOnItemClickListener(onItemClickListener);
        group_recyclerView.setAdapter(groupOneAdapter);

        getTeamMember(myTeamId);

        //findTeamName(UserInfoUtil.getAccid(), teamId); //获取team 的名字



        //findTeamName(UserInfoUtil.getAccid(), myTeamId);

        refreshUI(myTeamId);
    }

    private void refreshUI(String teamId) {
        tv_group_count.setText("(" + myTeamMembers.size() + ")");
        String teamNickName = "";
        List<TeamDbBean> teamDbBeanList = TeamDBUtil.queryByTeamId(mContext, UserInfoUtil.getAccid(), teamId);
        if (teamDbBeanList!=null && !teamDbBeanList.isEmpty()){
            teamNickName = teamDbBeanList.get(0).getTeam_name();
        }
        if (teamNickName != null) {
            et_remark.setText(teamNickName /*+ "(" + myTeamMembers.size() + ")"*/);
            et_remark.setSelection(teamNickName.length());
        }
        et_remark.requestFocus();
    }

    private GroupOneAdapter.OnItemClickListener onItemClickListener = new GroupOneAdapter.OnItemClickListener() {
        @Override
        public void OnItemClick(int position) {
            System.out.println("--> OnItemClick position=" + position);
        }
    };


    public void getTeamMember(final String teamId) {
        NIMClient.getService(TeamService.class).queryMemberList(teamId).setCallback(new RequestCallbackWrapper<List<TeamMember>>() {
            @Override
            public void onResult(int code, final List<TeamMember> members, Throwable exception) {

                System.out.println("EditManyChatWindow, code=" + code + ", members.size=" + (members != null ? members.size() : -1));
                if (code == ResponseCode.RES_SUCCESS && members != null) {
                    for (int i = 0; i < members.size(); i++) {
                        System.out.println("queryMemberList,teamId" + members.get(i).getTid() + ", account=" + members.get(i).getAccount()
                                + ", teamNick=" + members.get(i).getTeamNick() + ", Extension:" + members.get(i).getExtension());
                    }
                    myTeamMembers.addAll(members);

                    //teamBeanList.add(new TeamBean(teamId, members));
                    groupOneAdapter.notifyDataSetChanged();//刷新数据
                    refreshUI(teamId); //刷新界面
                }
            }
        });
    }

    private HttpListener<GroupBean> httpListener = new HttpListener<GroupBean>() {
        @Override
        public void fail(Throwable e, String type) {
            if (type.equals(HttpHelper.FIND_GROUP_CHAT_TYPE)) { //获取群聊名称

            } else if (type.equals(HttpHelper.UPDATE_GROUP_CHAT_TYPE)) { //修改群聊名称
                System.out.println("修改群聊备注失败");
            }
        }

        @Override
        public void success(GroupBean groupBean, String type) {
            if (groupBean != null) {
                System.out.println("" + groupBean.toString());
            }

            if (type.equals(HttpHelper.FIND_GROUP_CHAT_TYPE)) { //获取群聊名称

                if (groupBean != null) {
                    System.out.println(groupBean.toString());
                    //UserInfoUtil.setAccid(baseBean.getAccid());
                    //UserInfoUtil.setToken(baseBean.getToken());
                    //UserInfoUtil.setCode(baseBean.getCode());
                    //UserInfoUtil.setDesc(baseBean.getDesc());

                    String remarkStr = groupBean.getName();
                    et_remark.setText(remarkStr);
                    et_remark.setSelection(remarkStr.length());

                    //NIMClient.getService(UserService.class).getUserInfo(baseBean.getAccid());

                }
            } else if (type.equals(HttpHelper.UPDATE_GROUP_CHAT_TYPE)) { //修改群聊名称
                if (groupBean != null) {
                    System.out.println("update group chat name, code=" + groupBean.getCode());
                    if (groupBean.getCode().equals("301")) {
                        System.out.println("---> 修改群聊备注失败");
/*                        Intent intent = new Intent();
                        intent.putExtra("position", position);
                        intent.putExtra("teamId", myTeamId);
                        intent.putExtra("success", false);
                        intent.putExtra("newName", null);
                        setResult(2, intent);*/
                        //onEditTeamListener.onEditTeam(position, myTeamId, false, null);
                    }else if (groupBean.getCode().equals("200")){
/*                        Intent intent = new Intent();
                        intent.putExtra("position", position);
                        intent.putExtra("teamId", myTeamId);
                        intent.putExtra("success", true);
                        intent.putExtra("newName", groupBean.getName());
                        setResult(2, intent);*/
                        //onEditTeamListener.onEditTeam(position, myTeamId, true, groupBean.getName());
                    }
                }
            }
        }
    };


    private void updateTeamNick(String teamId, final String newNickname){
        List<TeamDbBean> teamDbBeanList = TeamDBUtil.queryByTeamId(mContext, UserInfoUtil.getAccid(), teamId);
        if (teamDbBeanList != null && teamDbBeanList.size() > 0) {
            TeamDbBean no_Bean = new TeamDbBean();
            no_Bean.setId(teamDbBeanList.get(0).getId());
            // no_Bean.setIs_team(teamDbBeanList.get(0).getIs_team());
            no_Bean.setMy_account(UserInfoUtil.getAccid());

            no_Bean.setRecord_time(teamDbBeanList.get(0).getRecord_time());

            no_Bean.setTeamId(teamDbBeanList.get(0).getTeamId());

            no_Bean.setTeam_name(newNickname);

            System.out.println("update, id=" + teamDbBeanList.get(0).getId() + "teamId=" + teamId+", newNickname="+newNickname);//获取teamId
            TeamDBUtil.update(mContext, no_Bean);
        } else {
            addTeamToDb(teamId, newNickname);
        }



/*        // newNickname为修改后自己的群昵称
        NIMClient.getService(TeamService.class).updateMyTeamNick(teamId, newNickname).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                onEditTeamListener.onEditTeam(position, myTeamId, true, newNickname);
            }

            @Override
            public void onFailed(int code) {
                System.out.println("code="+code);
                onEditTeamListener.onEditTeam(position, myTeamId, false, null);
            }

            @Override
            public void onException(Throwable exception) {
                exception.printStackTrace();
                onEditTeamListener.onEditTeam(position, myTeamId, false, null);
            }
        });*/
    }

    private void addTeamToDb(String teamId, String newNickname) {
        TeamDbBean no_Bean = new TeamDbBean();
        //no_Bean.setIs_team(1);
        no_Bean.setMy_account(UserInfoUtil.getAccid());
        no_Bean.setRecord_time(TimeUtil.getNowTime());
        no_Bean.setTeamId(teamId);
        no_Bean.setTeam_name(newNickname);
        System.out.println("teamId=" + teamId);//获取teamId
        TeamDBUtil.add(mContext, no_Bean);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if ((KeyEvent.KEYCODE_DPAD_UP == event.getKeyCode() || KeyEvent.KEYCODE_DPAD_DOWN == event.getKeyCode())
                && KeyEvent.ACTION_DOWN == event.getAction()) {

            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
