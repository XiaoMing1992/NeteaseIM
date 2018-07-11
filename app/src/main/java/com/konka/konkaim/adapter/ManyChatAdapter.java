package com.konka.konkaim.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.konka.konkaim.bean.TeamBean;
import com.konka.konkaim.bean.TeamDbBean;
import com.konka.konkaim.chat.activity.EditManyActivity;
import com.konka.konkaim.db.TeamDBUtil;
import com.konka.konkaim.http.HttpHelper;
import com.konka.konkaim.ui.CircleImageView;
import com.konka.konkaim.ui.DeleteManyChatWindow;
import com.konka.konkaim.ui.EditContactWindow;

import java.util.ArrayList;
import java.util.List;

import com.konka.konkaim.R;
import com.konka.konkaim.ui.DeleteContactWindow;
import com.konka.konkaim.bean.ContactBean;
import com.konka.konkaim.ui.EditManyChatWindow;
import com.konka.konkaim.ui.RecycleViewDivider;
import com.konka.konkaim.user.HomeActivity;
import com.konka.konkaim.user.UserInfoUtil;
import com.konka.konkaim.util.TimeUtil;
import com.konka.konkaim.util.Utils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NIMSDK;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

/**
 * Created by HP on 2018-5-10.
 */

public class ManyChatAdapter extends RecyclerView.Adapter<ManyChatAdapter.ManyChatAdapterHolder> {
    private Context mContext;
    //private List<NimUserInfo> contactBeens;
    private List<TeamBean> teamBeanList;

    private OnItemClickListener onItemClickListener;
    private RecyclerView recyclerView;

/*    public ManyChatAdapter(Context context, List<NimUserInfo> contactBeens) {
        this.mContext = context;
        this.contactBeens = contactBeens;
    }*/

    public ManyChatAdapter(Context context, List<TeamBean> teamBeanList) {
        this.mContext = context;
        this.teamBeanList = teamBeanList;

    }

    @Override
    public ManyChatAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.many_chat_item, parent, false);
        return new ManyChatAdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(final ManyChatAdapterHolder holder, final int position) {
        many_chat_flag_edit_has_show = false;
        many_chat_flag_edit_has_show_position = -1;

        if (position == 0) {
            holder.itemView.requestFocus();
        }
        //holder.tv_remark.setText(contactBeens.get(position).getRemark());
        //holder.tv_time.setText(contactBeens.get(position).getTime());

        holder.tv_group_count.setText("(" + teamBeanList.get(position).getMemberList().size() + ")"); //群聊人数
        String group_name = "";

        List<String> friendAccounts = new ArrayList<>();
        for (int i = 0; i < teamBeanList.get(position).getMemberList().size(); i++) {
            friendAccounts.add(teamBeanList.get(position).getMemberList().get(i).getAccount());
        }

        if (teamBeanList.get(position).getTeamNickname() != null){
            group_name = teamBeanList.get(position).getTeamNickname();
        }else {
            for (int i = 0; i < teamBeanList.get(position).getMemberList().size(); i++) {
                //优先显示备注名
                String aliasName = NIMSDK.getFriendService().getFriendByAccount(teamBeanList.get(position).getMemberList().get(i).getAccount()) == null
                        ? null : NIMSDK.getFriendService().getFriendByAccount(teamBeanList.get(position).getMemberList().get(i).getAccount()).getAlias(); //获取备注
                String name = NIMClient.getService(UserService.class).getUserInfo(teamBeanList.get(position).getMemberList().get(i).getAccount()).getName(); //获取昵称
                String resultName = !TextUtils.isEmpty(aliasName) ? aliasName : name;
                //String resultName = !TextUtils.isEmpty(aliasName) ? aliasName : teamBeanList.get(position).getMemberList().get(i).getTeamNick();
                //String resultName =teamBeanList.get(position).getMemberList().get(i).getTeamNick();
                group_name += resultName;
                group_name += " ";
            }
        }

        String teamId = teamBeanList.get(position).getTeamId();
        List<TeamDbBean> teamDbBeanList = TeamDBUtil.queryByTeamId(mContext, UserInfoUtil.getAccid(), teamId);
        String team_name = null;

        holder.tv_time.setVisibility(View.VISIBLE);
        if (teamDbBeanList != null && teamDbBeanList.size() > 0) {
            System.out.println("teamId="+teamId+"--> ManyChatAdapter record time is " + teamDbBeanList.get(0).getRecord_time()+", teamName="+teamDbBeanList.get(0).getTeam_name());
            holder.tv_time.setText(teamDbBeanList.get(0).getRecord_time());

            team_name = teamDbBeanList.get(0).getTeam_name();
        }

        if (team_name != null){
            group_name = team_name;
        }
        System.out.println("group_name=" + group_name + ", length=" + Utils.length(group_name));
        if (Utils.length(group_name) > 7)
            holder.tv_group_name.setText("" + Utils.getStrByLength(group_name, 7) + "...(" + teamBeanList.get(position).getMemberList().size() + ")");
        else
            holder.tv_group_name.setText(group_name + "(" + teamBeanList.get(position).getMemberList().size() + ")");


        final int num = teamBeanList.get(position).getMemberList().size();
        if (num == 2){
            holder.layout_img_2.setVisibility(View.VISIBLE);
            holder.layout_img_3.setVisibility(View.GONE);
            holder.layout_img_4.setVisibility(View.GONE);
            holder.layout_img_5.setVisibility(View.GONE);

            if (NIMClient.getService(UserService.class)
                    .getUserInfo(teamBeanList.get(position).getMemberList().get(0).getAccount()) == null){
                holder.img1.setImageResource(R.drawable.img_default);
            }else {
                String img1Str = NIMClient.getService(UserService.class)
                        .getUserInfo(teamBeanList.get(position).getMemberList().get(0).getAccount()).getAvatar();
                if (TextUtils.isEmpty(img1Str)){
                    holder.img1.setImageResource(R.drawable.img_default);
                }else {
                    HttpHelper.downloadPicture(mContext, img1Str,
                            R.drawable.img_default, R.drawable.img_default, holder.img1); //头像
                }
            }

//            HttpHelper.downloadPicture(mContext, NIMClient.getService(UserService.class)
//                            .getUserInfo(teamBeanList.get(position).getMemberList().get(0).getAccount()) == null
//                            ? null : NIMClient.getService(UserService.class)
//                            .getUserInfo(teamBeanList.get(position).getMemberList().get(0).getAccount()).getAvatar(),
//                    R.drawable.img_default, R.drawable.img_default, holder.img1); //头像

//            HttpHelper.downloadPicture(mContext, NIMClient.getService(UserService.class)
//                            .getUserInfo(teamBeanList.get(position).getMemberList().get(1).getAccount()) == null
//                            ? null : NIMClient.getService(UserService.class)
//                            .getUserInfo(teamBeanList.get(position).getMemberList().get(1).getAccount()).getAvatar(),
//                    R.drawable.img_default, R.drawable.img_default, holder.img2); //头像

            if (NIMClient.getService(UserService.class)
                    .getUserInfo(teamBeanList.get(position).getMemberList().get(1).getAccount()) == null){
                holder.img1.setImageResource(R.drawable.img_default);
            }else {
                String img2Str = NIMClient.getService(UserService.class)
                        .getUserInfo(teamBeanList.get(position).getMemberList().get(1).getAccount()).getAvatar();
                if (TextUtils.isEmpty(img2Str)){
                    holder.img2.setImageResource(R.drawable.img_default);
                }else {
                    HttpHelper.downloadPicture(mContext, img2Str,
                            R.drawable.img_default, R.drawable.img_default, holder.img2); //头像
                }
            }

        }else if (num == 3){
            holder.layout_img_2.setVisibility(View.GONE);
            holder.layout_img_3.setVisibility(View.VISIBLE);
            holder.layout_img_4.setVisibility(View.GONE);
            holder.layout_img_5.setVisibility(View.GONE);
//            HttpHelper.downloadPicture(mContext, NIMClient.getService(UserService.class)
//                            .getUserInfo(teamBeanList.get(position).getMemberList().get(0).getAccount()) == null
//                            ? null : NIMClient.getService(UserService.class)
//                            .getUserInfo(teamBeanList.get(position).getMemberList().get(0).getAccount()).getAvatar(),
//                    R.drawable.img_default, R.drawable.img_default, holder.img3); //头像
//            HttpHelper.downloadPicture(mContext, NIMClient.getService(UserService.class)
//                            .getUserInfo(teamBeanList.get(position).getMemberList().get(1).getAccount()) == null
//                            ? null : NIMClient.getService(UserService.class)
//                            .getUserInfo(teamBeanList.get(position).getMemberList().get(1).getAccount()).getAvatar(),
//                    R.drawable.img_default, R.drawable.img_default, holder.img4); //头像
//            HttpHelper.downloadPicture(mContext, NIMClient.getService(UserService.class)
//                            .getUserInfo(teamBeanList.get(position).getMemberList().get(2).getAccount()) == null
//                            ? null : NIMClient.getService(UserService.class)
//                            .getUserInfo(teamBeanList.get(position).getMemberList().get(2).getAccount()).getAvatar(),
//                    R.drawable.img_default, R.drawable.img_default, holder.img5); //头像

            if (NIMClient.getService(UserService.class)
                    .getUserInfo(teamBeanList.get(position).getMemberList().get(0).getAccount()) == null){
                holder.img3.setImageResource(R.drawable.img_default);
            }else {
                String img3Str = NIMClient.getService(UserService.class)
                        .getUserInfo(teamBeanList.get(position).getMemberList().get(0).getAccount()).getAvatar();
                if (TextUtils.isEmpty(img3Str)){
                    holder.img3.setImageResource(R.drawable.img_default);
                }else {
                    HttpHelper.downloadPicture(mContext, img3Str,
                            R.drawable.img_default, R.drawable.img_default, holder.img3); //头像
                }
            }

            if (NIMClient.getService(UserService.class)
                    .getUserInfo(teamBeanList.get(position).getMemberList().get(1).getAccount()) == null){
                holder.img4.setImageResource(R.drawable.img_default);
            }else {
                String img4Str = NIMClient.getService(UserService.class)
                        .getUserInfo(teamBeanList.get(position).getMemberList().get(1).getAccount()).getAvatar();
                if (TextUtils.isEmpty(img4Str)){
                    holder.img4.setImageResource(R.drawable.img_default);
                }else {
                    HttpHelper.downloadPicture(mContext, img4Str,
                            R.drawable.img_default, R.drawable.img_default, holder.img4); //头像
                }
            }

            if (NIMClient.getService(UserService.class)
                    .getUserInfo(teamBeanList.get(position).getMemberList().get(2).getAccount()) == null){
                holder.img5.setImageResource(R.drawable.img_default);
            }else {
                String img5Str = NIMClient.getService(UserService.class)
                        .getUserInfo(teamBeanList.get(position).getMemberList().get(2).getAccount()).getAvatar();
                if (TextUtils.isEmpty(img5Str)){
                    holder.img5.setImageResource(R.drawable.img_default);
                }else {
                    HttpHelper.downloadPicture(mContext, img5Str,
                            R.drawable.img_default, R.drawable.img_default, holder.img5); //头像
                }
            }

        }else if (num == 4){
            holder.layout_img_2.setVisibility(View.GONE);
            holder.layout_img_3.setVisibility(View.GONE);
            holder.layout_img_4.setVisibility(View.VISIBLE);
            holder.layout_img_5.setVisibility(View.GONE);
//            HttpHelper.downloadPicture(mContext, NIMClient.getService(UserService.class)
//                            .getUserInfo(teamBeanList.get(position).getMemberList().get(0).getAccount()) == null
//                            ? null : NIMClient.getService(UserService.class)
//                            .getUserInfo(teamBeanList.get(position).getMemberList().get(0).getAccount()).getAvatar(),
//                    R.drawable.img_default, R.drawable.img_default, holder.img6); //头像
//            HttpHelper.downloadPicture(mContext, NIMClient.getService(UserService.class)
//                            .getUserInfo(teamBeanList.get(position).getMemberList().get(1).getAccount()) == null
//                            ? null : NIMClient.getService(UserService.class)
//                            .getUserInfo(teamBeanList.get(position).getMemberList().get(1).getAccount()).getAvatar(),
//                    R.drawable.img_default, R.drawable.img_default, holder.img7); //头像
//            HttpHelper.downloadPicture(mContext, NIMClient.getService(UserService.class)
//                            .getUserInfo(teamBeanList.get(position).getMemberList().get(2).getAccount()) == null
//                            ? null : NIMClient.getService(UserService.class)
//                            .getUserInfo(teamBeanList.get(position).getMemberList().get(2).getAccount()).getAvatar(),
//                    R.drawable.img_default, R.drawable.img_default, holder.img8); //头像
//            HttpHelper.downloadPicture(mContext, NIMClient.getService(UserService.class)
//                            .getUserInfo(teamBeanList.get(position).getMemberList().get(3).getAccount()) == null
//                            ? null : NIMClient.getService(UserService.class)
//                            .getUserInfo(teamBeanList.get(position).getMemberList().get(3).getAccount()).getAvatar(),
//                    R.drawable.img_default, R.drawable.img_default, holder.img9); //头像

            if (NIMClient.getService(UserService.class)
                    .getUserInfo(teamBeanList.get(position).getMemberList().get(0).getAccount()) == null){
                holder.img6.setImageResource(R.drawable.img_default);
            }else {
                String img6Str = NIMClient.getService(UserService.class)
                        .getUserInfo(teamBeanList.get(position).getMemberList().get(0).getAccount()).getAvatar();
                if (TextUtils.isEmpty(img6Str)){
                    holder.img6.setImageResource(R.drawable.img_default);
                }else {
                    HttpHelper.downloadPicture(mContext, img6Str,
                            R.drawable.img_default, R.drawable.img_default, holder.img6); //头像
                }
            }

            if (NIMClient.getService(UserService.class)
                    .getUserInfo(teamBeanList.get(position).getMemberList().get(1).getAccount()) == null){
                holder.img7.setImageResource(R.drawable.img_default);
            }else {
                String img7Str = NIMClient.getService(UserService.class)
                        .getUserInfo(teamBeanList.get(position).getMemberList().get(1).getAccount()).getAvatar();
                if (TextUtils.isEmpty(img7Str)){
                    holder.img7.setImageResource(R.drawable.img_default);
                }else {
                    HttpHelper.downloadPicture(mContext, img7Str,
                            R.drawable.img_default, R.drawable.img_default, holder.img7); //头像
                }
            }

            if (NIMClient.getService(UserService.class)
                    .getUserInfo(teamBeanList.get(position).getMemberList().get(2).getAccount()) == null){
                holder.img8.setImageResource(R.drawable.img_default);
            }else {
                String img8Str = NIMClient.getService(UserService.class)
                        .getUserInfo(teamBeanList.get(position).getMemberList().get(2).getAccount()).getAvatar();
                if (TextUtils.isEmpty(img8Str)){
                    holder.img8.setImageResource(R.drawable.img_default);
                }else {
                    HttpHelper.downloadPicture(mContext, img8Str,
                            R.drawable.img_default, R.drawable.img_default, holder.img8); //头像
                }
            }

            if (NIMClient.getService(UserService.class)
                    .getUserInfo(teamBeanList.get(position).getMemberList().get(3).getAccount()) == null){
                holder.img9.setImageResource(R.drawable.img_default);
            }else {
                String img9Str = NIMClient.getService(UserService.class)
                        .getUserInfo(teamBeanList.get(position).getMemberList().get(3).getAccount()).getAvatar();
                if (TextUtils.isEmpty(img9Str)){
                    holder.img9.setImageResource(R.drawable.img_default);
                }else {
                    HttpHelper.downloadPicture(mContext, img9Str,
                            R.drawable.img_default, R.drawable.img_default, holder.img9); //头像
                }
            }

        }else if (num == 5){
            holder.layout_img_2.setVisibility(View.GONE);
            holder.layout_img_3.setVisibility(View.GONE);
            holder.layout_img_4.setVisibility(View.GONE);
            holder.layout_img_5.setVisibility(View.VISIBLE);

//            HttpHelper.downloadPicture(mContext, NIMClient.getService(UserService.class)
//                            .getUserInfo(teamBeanList.get(position).getMemberList().get(0).getAccount()) == null
//                            ? null : NIMClient.getService(UserService.class)
//                            .getUserInfo(teamBeanList.get(position).getMemberList().get(0).getAccount()).getAvatar(),
//                    R.drawable.img_default, R.drawable.img_default, holder.img10); //头像
//            HttpHelper.downloadPicture(mContext, NIMClient.getService(UserService.class)
//                            .getUserInfo(teamBeanList.get(position).getMemberList().get(1).getAccount()) == null
//                            ? null : NIMClient.getService(UserService.class)
//                            .getUserInfo(teamBeanList.get(position).getMemberList().get(1).getAccount()).getAvatar(),
//                    R.drawable.img_default, R.drawable.img_default, holder.img11); //头像
//            HttpHelper.downloadPicture(mContext, NIMClient.getService(UserService.class)
//                            .getUserInfo(teamBeanList.get(position).getMemberList().get(2).getAccount()) == null
//                            ? null : NIMClient.getService(UserService.class)
//                            .getUserInfo(teamBeanList.get(position).getMemberList().get(2).getAccount()).getAvatar(),
//                    R.drawable.img_default, R.drawable.img_default, holder.img12); //头像
//            HttpHelper.downloadPicture(mContext, NIMClient.getService(UserService.class)
//                            .getUserInfo(teamBeanList.get(position).getMemberList().get(3).getAccount()) == null
//                            ? null : NIMClient.getService(UserService.class)
//                            .getUserInfo(teamBeanList.get(position).getMemberList().get(3).getAccount()).getAvatar(),
//                    R.drawable.img_default, R.drawable.img_default, holder.img13); //头像
//            HttpHelper.downloadPicture(mContext, NIMClient.getService(UserService.class)
//                            .getUserInfo(teamBeanList.get(position).getMemberList().get(4).getAccount()) == null
//                            ? null : NIMClient.getService(UserService.class)
//                            .getUserInfo(teamBeanList.get(position).getMemberList().get(4).getAccount()).getAvatar(),
//                    R.drawable.img_default, R.drawable.img_default, holder.img14); //头像

            if (NIMClient.getService(UserService.class)
                    .getUserInfo(teamBeanList.get(position).getMemberList().get(0).getAccount()) == null){
                holder.img10.setImageResource(R.drawable.img_default);
            }else {
                String img10Str = NIMClient.getService(UserService.class)
                        .getUserInfo(teamBeanList.get(position).getMemberList().get(0).getAccount()).getAvatar();
                if (TextUtils.isEmpty(img10Str)){
                    holder.img10.setImageResource(R.drawable.img_default);
                }else {
                    HttpHelper.downloadPicture(mContext, img10Str,
                            R.drawable.img_default, R.drawable.img_default, holder.img10); //头像
                }
            }

            if (NIMClient.getService(UserService.class)
                    .getUserInfo(teamBeanList.get(position).getMemberList().get(1).getAccount()) == null){
                holder.img11.setImageResource(R.drawable.img_default);
            }else {
                String img11Str = NIMClient.getService(UserService.class)
                        .getUserInfo(teamBeanList.get(position).getMemberList().get(1).getAccount()).getAvatar();
                if (TextUtils.isEmpty(img11Str)){
                    holder.img11.setImageResource(R.drawable.img_default);
                }else {
                    HttpHelper.downloadPicture(mContext, img11Str,
                            R.drawable.img_default, R.drawable.img_default, holder.img11); //头像
                }
            }

            if (NIMClient.getService(UserService.class)
                    .getUserInfo(teamBeanList.get(position).getMemberList().get(2).getAccount()) == null){
                holder.img12.setImageResource(R.drawable.img_default);
            }else {
                String img12Str = NIMClient.getService(UserService.class)
                        .getUserInfo(teamBeanList.get(position).getMemberList().get(2).getAccount()).getAvatar();
                if (TextUtils.isEmpty(img12Str)){
                    holder.img12.setImageResource(R.drawable.img_default);
                }else {
                    HttpHelper.downloadPicture(mContext, img12Str,
                            R.drawable.img_default, R.drawable.img_default, holder.img12); //头像
                }
            }

            if (NIMClient.getService(UserService.class)
                    .getUserInfo(teamBeanList.get(position).getMemberList().get(3).getAccount()) == null){
                holder.img13.setImageResource(R.drawable.img_default);
            }else {
                String img13Str = NIMClient.getService(UserService.class)
                        .getUserInfo(teamBeanList.get(position).getMemberList().get(3).getAccount()).getAvatar();
                if (TextUtils.isEmpty(img13Str)){
                    holder.img13.setImageResource(R.drawable.img_default);
                }else {
                    HttpHelper.downloadPicture(mContext, img13Str,
                            R.drawable.img_default, R.drawable.img_default, holder.img13); //头像
                }
            }

            if (NIMClient.getService(UserService.class)
                    .getUserInfo(teamBeanList.get(position).getMemberList().get(4).getAccount()) == null){
                holder.img14.setImageResource(R.drawable.img_default);
            }else {
                String img14Str = NIMClient.getService(UserService.class)
                        .getUserInfo(teamBeanList.get(position).getMemberList().get(4).getAccount()).getAvatar();
                if (TextUtils.isEmpty(img14Str)){
                    holder.img14.setImageResource(R.drawable.img_default);
                }else {
                    HttpHelper.downloadPicture(mContext, img14Str,
                            R.drawable.img_default, R.drawable.img_default, holder.img14); //头像
                }
            }
        }


/*        System.out.println("num=" + num + ", d=" + d);
        GroupLogoAdapter adapter = new GroupLogoAdapter(mContext, friendAccounts);
        LinearLayoutManager manyLinearLayoutManager = new LinearLayoutManager(mContext);
        manyLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        holder.group_logo_recyclerView.setLayoutManager(manyLinearLayoutManager);
*//*        holder.group_logo_recyclerView.addItemDecoration(new RecycleViewDivider(mContext, LinearLayout.HORIZONTAL,
                0, num*((int) mContext.getResources().getDimension(R.dimen.group_logo_item_dividerWidth))));*//*

        holder.group_logo_recyclerView.removeItemDecoration(new RecycleViewDivider(mContext, LinearLayout.HORIZONTAL,
                0, d));
        holder.group_logo_recyclerView.addItemDecoration(new RecycleViewDivider(mContext, LinearLayout.HORIZONTAL,
                0, d));

        holder.group_logo_recyclerView.setAdapter(adapter);*/



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("--> ManyChatAdapter position=" + position);
                onItemClickListener.OnItemClick(position);
            }
        });

        holder.itemView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                System.out.println("keyCode=" + keyCode + ", event.getKeyCode()=" + event.getKeyCode() + ", event.getAction()=" + event.getAction());
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
                        if (holder.layout_edit.getVisibility() == View.GONE) {
                            holder.layout_edit.setVisibility(View.VISIBLE);
                            holder.btn_edit.requestFocus();

                            many_chat_flag_edit_has_show = true; //控制
                            many_chat_flag_edit_has_show_position = position;

                            return true;
                        }
                    } else if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                        if (holder.layout_edit.getVisibility() == View.VISIBLE) {
                            holder.layout_edit.setVisibility(View.GONE);
                            holder.itemView.requestFocus();
                            return true;
                        }
                    } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT
                            || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                        if (holder.layout_edit.getVisibility() == View.VISIBLE) {
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        holder.btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("to EditManyChatWindow, teamId is " + teamBeanList.get(position).getTeamId());
//                EditManyChatWindow editManyChatWindow = new EditManyChatWindow(mContext, position, teamBeanList.get(position).getTeamId());
//                editManyChatWindow.setOnEditTeamListener(onEditTeamListener);
//                editManyChatWindow.show();

                Intent intent = new Intent(mContext, EditManyActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("teamId", teamBeanList.get(position).getTeamId());
                ((Activity)mContext).startActivityForResult(intent, 1);

            }
        });

        holder.btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("to DeleteManyChatWindow, teamId is " + teamBeanList.get(position).getTeamId()
                        + ", is teamCreator " + teamBeanList.get(position).getTeamCreator().equals(UserInfoUtil.getAccid()));

                NIMClient.getService(TeamService.class).queryMemberList(teamBeanList.get(position).getTeamId()).setCallback(new RequestCallbackWrapper<List<TeamMember>>() {
                    @Override
                    public void onResult(int code, final List<TeamMember> members, Throwable exception) {
                        if (code == ResponseCode.RES_SUCCESS && members != null) {
                            System.out.println("to DeleteManyChatWindow, members.size()=" + members.size());
                            boolean isTeamCreator = teamBeanList.get(position).getTeamCreator().equals(UserInfoUtil.getAccid());
                            /*if (teamBeanList.get(position).getTeamCreator().equals(UserInfoUtil.getAccid())) {
                                //if (members.size() >= 2 && )

                               *//* Toast.makeText(mContext, "群主不能退群", Toast.LENGTH_SHORT).show();
                            } else {*//*


                            }*/
                            DeleteManyChatWindow deleteManyChatWindow = new DeleteManyChatWindow(mContext, teamBeanList.get(position).getTeamId(), position, isTeamCreator);
                            deleteManyChatWindow.setOnQuitListener(onQuitListener);
                            deleteManyChatWindow.show();
                        }
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return teamBeanList == null ? 0 : teamBeanList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ManyChatAdapterHolder extends RecyclerView.ViewHolder {
        private ImageView img_origin;
        private ImageView group_logo;
        private TextView tv_time;
        //private TextView tv_remark;
        private TextView tv_group_name;
        private TextView tv_group_count;
        private LinearLayout layout_edit;
        private RelativeLayout layout_info;
        private Button btn_edit;
        private Button btn_delete;
        private RecyclerView group_logo_recyclerView;

        private LinearLayout layout_img_2;
        private CircleImageView img1;
        private CircleImageView img2;

        private LinearLayout layout_img_3;
        private CircleImageView img3;
        private CircleImageView img4;
        private CircleImageView img5;

        private LinearLayout layout_img_4;
        private CircleImageView img6;
        private CircleImageView img7;
        private CircleImageView img8;
        private CircleImageView img9;

        private LinearLayout layout_img_5;
        private CircleImageView img10;
        private CircleImageView img11;
        private CircleImageView img12;
        private CircleImageView img13;
        private CircleImageView img14;

        public ManyChatAdapterHolder(View itemView) {
            super(itemView);
            img_origin = (ImageView) itemView.findViewById(R.id.img_origin);
            group_logo = (ImageView) itemView.findViewById(R.id.group_logo);
            tv_time = (TextView) itemView.findViewById(R.id.tv_time);
            //tv_remark = (TextView) itemView.findViewById(R.id.tv_remark);
            tv_group_name = (TextView) itemView.findViewById(R.id.tv_group_name);
            tv_group_count = (TextView) itemView.findViewById(R.id.tv_group_count);
            layout_edit = (LinearLayout) itemView.findViewById(R.id.layout_edit);
            layout_info = (RelativeLayout) itemView.findViewById(R.id.layout_info);
            btn_edit = (Button) itemView.findViewById(R.id.btn_edit);
            btn_delete = (Button) itemView.findViewById(R.id.btn_delete);
            //group_logo_recyclerView = (RecyclerView) itemView.findViewById(R.id.group_logo_recyclerView);

            layout_img_2 = (LinearLayout)itemView.findViewById(R.id.layout_img_2);
            img1 = (CircleImageView) itemView.findViewById(R.id.img1);
            img2 = (CircleImageView) itemView.findViewById(R.id.img2);

            layout_img_3 = (LinearLayout)itemView.findViewById(R.id.layout_img_3);
            img3 = (CircleImageView) itemView.findViewById(R.id.img3);
            img4 = (CircleImageView) itemView.findViewById(R.id.img4);
            img5 = (CircleImageView) itemView.findViewById(R.id.img5);

            layout_img_4 = (LinearLayout)itemView.findViewById(R.id.layout_img_4);
            img6 = (CircleImageView) itemView.findViewById(R.id.img6);
            img7 = (CircleImageView) itemView.findViewById(R.id.img7);
            img8 = (CircleImageView) itemView.findViewById(R.id.img8);
            img9 = (CircleImageView) itemView.findViewById(R.id.img9);

            layout_img_5 = (LinearLayout)itemView.findViewById(R.id.layout_img_5);
            img10 = (CircleImageView) itemView.findViewById(R.id.img10);
            img11 = (CircleImageView) itemView.findViewById(R.id.img11);
            img12 = (CircleImageView) itemView.findViewById(R.id.img12);
            img13 = (CircleImageView) itemView.findViewById(R.id.img13);
            img14 = (CircleImageView) itemView.findViewById(R.id.img14);
        }
    }

    public interface OnItemClickListener {
        void OnItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public DeleteManyChatWindow.OnQuitListener onQuitListener = new DeleteManyChatWindow.OnQuitListener() {
        @Override
        public void onQuit(final int position, boolean quit) {
            if (quit) {
                System.out.println("--->DeleteManyChatWindow position = " + position);
                recyclerView.getChildAt(position).findViewById(R.id.layout_edit).setVisibility(View.GONE);
                many_chat_flag_edit_has_show = false;
                many_chat_flag_edit_has_show_position = -1;

                //DBUtil.delete(mContext, UserInfoUtil.getAccid(), teamBeanList.get(position).getTeamId()); //从数据库里面删除记录

                TeamDBUtil.delete(mContext, UserInfoUtil.getAccid(), teamBeanList.get(position).getTeamId()); //从数据库里面删除记录

                teamBeanList.remove(position);
                notifyItemRemoved(position);
                notifyDataSetChanged();

/*                if (teamBeanList.isEmpty()) {
                    Toast.makeText(mContext, "删空群了", Toast.LENGTH_SHORT).show();
                }else {

                    if (position > 0) {
                        recyclerView.getChildAt(position - 1 - mLayoutMgr.findFirstVisibleItemPosition()).requestFocus();
                    } else if (position == 0) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                recyclerView.getChildAt(position).requestFocus();
                            }
                        }, 200);
                    }
                }*/


            } else {
                Toast.makeText(mContext, "删除群人失败", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onDismissTeam(int position, boolean dismiss) {
            if (dismiss) {
                System.out.println("--->DeleteManyChatWindow position = " + position);
                //recyclerView.getChildAt(position).findViewById(R.id.layout_edit).setVisibility(View.GONE);
                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(many_chat_flag_edit_has_show_position);
                ((ManyChatAdapter.ManyChatAdapterHolder) viewHolder).itemView.findViewById(R.id.layout_edit).setVisibility(View.GONE);

                many_chat_flag_edit_has_show = false;
                many_chat_flag_edit_has_show_position = -1;

                //DBUtil.delete(mContext, UserInfoUtil.getAccid(), teamBeanList.get(position).getTeamId()); //从数据库里面删除记录
                TeamDBUtil.delete(mContext, UserInfoUtil.getAccid(), teamBeanList.get(position).getTeamId()); //从数据库里面删除记录

                teamBeanList.remove(position);
                notifyItemRemoved(position);
                notifyDataSetChanged();

/*                if (teamBeanList.isEmpty()) {
                    Toast.makeText(mContext, "删空群了", Toast.LENGTH_SHORT).show();
                }else {

                    if (position > 0) {
                        recyclerView.getChildAt(position - 1 - mLayoutMgr.findFirstVisibleItemPosition()).requestFocus();
                    } else if (position == 0) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                recyclerView.getChildAt(position).requestFocus();
                            }
                        }, 200);
                    }
                }*/


            } else {
                Toast.makeText(mContext, "退群失败", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private Handler handler = new Handler();
    private LinearLayoutManager mLayoutMgr;

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public void setmLayoutMgr(LinearLayoutManager mLayoutMgr) {
        this.mLayoutMgr = mLayoutMgr;
    }

    //    private DeleteManyChatWindow.OnQuitListener onQuitListener;
//    public void setOnQuitListener(DeleteManyChatWindow.OnQuitListener onQuitListener) {
//        this.onQuitListener = onQuitListener;
//    }

    public static boolean many_chat_flag_edit_has_show = false;
    public static int many_chat_flag_edit_has_show_position = -1;

    public void isTeamEmpty(final String teamId) {
        NIMClient.getService(TeamService.class).queryMemberList(teamId).setCallback(new RequestCallbackWrapper<List<TeamMember>>() {
            @Override
            public void onResult(int code, final List<TeamMember> members, Throwable exception) {

            }
        });
    }

    private EditManyChatWindow.OnEditTeamListener onEditTeamListener = new EditManyChatWindow.OnEditTeamListener() {
        @Override
        public void onEditTeam(int position, String TeamId, boolean success, String newNname) {
            if (success) {
                if (Utils.length(newNname) > 7)
                    ((TextView) recyclerView.getChildAt(position).findViewById(R.id.tv_group_name)).setText("" + Utils.getStrByLength(newNname, 7) + "...(" + teamBeanList.get(position).getMemberList().size() + ")");
                else
                    ((TextView) recyclerView.getChildAt(position).findViewById(R.id.tv_group_name)).setText(newNname + "(" + teamBeanList.get(position).getMemberList().size() + ")");

                recyclerView.getChildAt(position).findViewById(R.id.layout_edit).setVisibility(View.GONE);
                recyclerView.getChildAt(position).requestFocus();

                many_chat_flag_edit_has_show = false;
                many_chat_flag_edit_has_show_position = -1;
            } else {

            }
        }
    };

}
