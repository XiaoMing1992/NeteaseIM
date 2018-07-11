package com.konka.konkaim.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.konka.konkaim.bean.ContactBean;

import java.util.List;

import com.konka.konkaim.R;
import com.konka.konkaim.http.HttpHelper;
import com.konka.konkaim.ui.CircleImageView;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NIMSDK;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.uinfo.UserService;

/**
 * Created by HP on 2018-5-10.
 */

public class GroupOneAdapter extends RecyclerView.Adapter<GroupOneAdapter.GroupOneAdapterHolder> {
    private Context mContext;
    private List<TeamMember> teamMembers;
    private OnItemClickListener onItemClickListener;

    public GroupOneAdapter(Context context, List<TeamMember> teamMembers) {
        this.mContext = context;
        this.teamMembers = teamMembers;
    }

    @Override
    public GroupOneAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.group_one_item, parent, false);
        return new GroupOneAdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(final GroupOneAdapterHolder holder, final int position) {

        //holder.tv_remark.setText(teamMembers.get(position).getTeamNick());
        //holder.head_icon.setImageResource(contactBeens.get(position).getTime());
        //优先显示备注名
        String aliasName = NIMSDK.getFriendService().getFriendByAccount(teamMembers.get(position).getAccount())
                != null ? NIMSDK.getFriendService().getFriendByAccount(teamMembers.get(position).getAccount()).getAlias() : null; //获取备注
        String resultName = !TextUtils.isEmpty(aliasName) ? aliasName : NIMClient.getService(UserService.class).getUserInfo(teamMembers.get(position).getAccount()).getName();
        holder.tv_remark.setText(resultName);

        if (!TextUtils.isEmpty(NIMClient.getService(UserService.class).getUserInfo(teamMembers.get(position).getAccount()).getAvatar())) {
            System.out.println("friendAccount=" + teamMembers.get(position).getAccount());
            HttpHelper.downloadPicture(mContext, NIMClient.getService(UserService.class).getUserInfo(teamMembers.get(position).getAccount()).getAvatar(),
                    R.drawable.img_default, R.drawable.img_default, holder.head_icon); //头像
        } else {
            holder.head_icon.setImageResource(R.drawable.img_default);
        }


//        if (position == 0) {
//            holder.itemView.requestFocus();
//        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("--> ContactAdapter position=" + position);
                onItemClickListener.OnItemClick(position);
            }
        });


    }

    @Override
    public int getItemCount() {
        return teamMembers == null ? 0 : teamMembers.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class GroupOneAdapterHolder extends RecyclerView.ViewHolder {
        private CircleImageView head_icon;
        private TextView tv_remark;

        public GroupOneAdapterHolder(View itemView) {
            super(itemView);
            head_icon = (CircleImageView) itemView.findViewById(R.id.head_icon);
            tv_remark = (TextView) itemView.findViewById(R.id.remark);
        }
    }

    public interface OnItemClickListener {
        void OnItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
