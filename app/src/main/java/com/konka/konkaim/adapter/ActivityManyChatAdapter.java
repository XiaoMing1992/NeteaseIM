package com.konka.konkaim.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.konka.konkaim.R;
import com.konka.konkaim.bean.ContactBean;
import com.konka.konkaim.chat.StateUtil;
import com.konka.konkaim.chat.team.TeamAVChatItem;
import com.konka.konkaim.http.HttpHelper;
import com.konka.konkaim.ui.CircleImageView;
import com.konka.konkaim.util.Utils;
import com.netease.nimlib.sdk.NIMSDK;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

/**
 * Created by HP on 2018-5-10.
 */

public class ActivityManyChatAdapter extends RecyclerView.Adapter<ActivityManyChatAdapter.ActivityManyChatAdapterHolder> {
    private final String TAG = "ActivityManyChatAdapter";
    private Context mContext;
    private List<NimUserInfo> contactBeens;
    private List<TeamAVChatItem> teamAVChatItemList;
    private OnItemClickListener onItemClickListener;

    public ActivityManyChatAdapter(Context context, List<NimUserInfo> contactBeens, List<TeamAVChatItem> teamAVChatItemList) {
        this.mContext = context;
        this.contactBeens = contactBeens;
        this.teamAVChatItemList = teamAVChatItemList;
    }

    @Override
    public ActivityManyChatAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.activity_many_chat_item, parent, false);
        return new ActivityManyChatAdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(final ActivityManyChatAdapterHolder holder, final int position) {

        //holder.tv_remark.setText(contactBeens.get(position).getRemark());
        System.out.println("--- State="+teamAVChatItemList.get(position).getState());
        holder.tv_state.setText(getState(teamAVChatItemList.get(position).getState()));
        if (teamAVChatItemList.get(position).getState() == StateUtil.CONNECT_FAIL){
            holder.tv_state.setTextColor(mContext.getResources().getColor(R.color.color17));
        }

        //优先显示备注名
        String aliasName = NIMSDK.getFriendService().getFriendByAccount(contactBeens.get(position).getAccount())
                != null ? NIMSDK.getFriendService().getFriendByAccount(contactBeens.get(position).getAccount()).getAlias():null; //获取备注
        String resultName = !TextUtils.isEmpty(aliasName) ? aliasName : contactBeens.get(position).getName();
        if (resultName != null && Utils.length(resultName) > 10)
            holder.tv_remark.setText("" + Utils.getStrByLength(resultName, 10) + "...");
        else
            holder.tv_remark.setText(resultName);
        //holder.tv_remark.setText(resultName);

        if(!TextUtils.isEmpty(contactBeens.get(position).getAvatar())){
            System.out.println(TAG+", avatar=" + contactBeens.get(position).getAvatar());
            HttpHelper.downloadPicture(mContext, contactBeens.get(position).getAvatar(),
                    R.drawable.img_default, R.drawable.img_default, holder.head_icon); //头像
        }else {
            holder.head_icon.setImageResource(R.drawable.img_default);
        }

        //holder.head_icon.setImageResource(R.drawable.img_default);
        if (position == 0) holder.itemView.requestFocus();
    }

    @Override
    public int getItemCount() {
        return contactBeens == null ? 0 : contactBeens.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class ActivityManyChatAdapterHolder extends RecyclerView.ViewHolder {
        private CircleImageView head_icon;
        private TextView tv_state;
        private TextView tv_remark;

        public ActivityManyChatAdapterHolder(View itemView) {
            super(itemView);
            head_icon = (CircleImageView) itemView.findViewById(R.id.head_icon);
            tv_state = (TextView) itemView.findViewById(R.id.tv_state);
            tv_remark = (TextView) itemView.findViewById(R.id.tv_remark);
        }
    }

    public interface OnItemClickListener{
        void OnItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public String getState(int stateCode){
        String str="";
        switch (stateCode){
            case StateUtil.WAIT_TO_CONNECT:
                str="等待接通...";
                break;
            case StateUtil.CONNECT_FAIL:
                str="接通失败";
                break;
            case StateUtil.CHATTING:
                str="聊天中...";
                break;
            case StateUtil.CHAT_OVER:
                str="通话结束";
                break;
            case StateUtil.CHAT_FAIL:
                str="拒绝接听";
                break;
            case StateUtil.CHAT_NOT_FRIEND_FAIL:
                str="非好友，通话失败";
                break;
        }
        return str;
    }
}
