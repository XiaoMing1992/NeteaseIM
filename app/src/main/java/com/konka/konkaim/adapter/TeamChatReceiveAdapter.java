package com.konka.konkaim.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.konka.konkaim.R;
import com.konka.konkaim.http.HttpHelper;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.util.List;

/**
 * Created by HP on 2018-5-10.
 */

public class TeamChatReceiveAdapter extends RecyclerView.Adapter<TeamChatReceiveAdapter.AddContactResultAdapterHolder> {
    private Context mContext;
    //private List<ContactBean> contactBeens;

    private List<String> accounts;
    private OnItemClickListener onItemClickListener;

    public TeamChatReceiveAdapter(Context context, List<String> accounts) {
        this.mContext = context;
        this.accounts = accounts;
    }

    @Override
    public AddContactResultAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.team_chat_receive_item, parent, false);
        return new AddContactResultAdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(final AddContactResultAdapterHolder holder, final int position) {

        HttpHelper.downloadPicture(mContext, NIMClient.getService(UserService.class).getUserInfo(accounts.get(position)).getAvatar(),
                R.drawable.img_default, R.drawable.img_default ,holder.member_head_icon);

/*        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("--> ContactAdapter position="+position);
                onItemClickListener.OnItemClick(position);
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return accounts == null ? 0 : accounts.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class AddContactResultAdapterHolder extends RecyclerView.ViewHolder {
        private ImageView member_head_icon;

        public AddContactResultAdapterHolder(View itemView) {
            super(itemView);
            member_head_icon = (ImageView) itemView.findViewById(R.id.member_head_icon);
        }
    }

    public interface OnItemClickListener{
        void OnItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
}
