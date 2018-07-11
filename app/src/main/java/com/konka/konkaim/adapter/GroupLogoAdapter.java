package com.konka.konkaim.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.konka.konkaim.R;
import com.konka.konkaim.http.HttpHelper;
import com.konka.konkaim.ui.CircleImageView;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.uinfo.UserService;

import java.util.List;

/**
 * Created by HP on 2018-5-10.
 */

public class GroupLogoAdapter extends RecyclerView.Adapter<GroupLogoAdapter.GroupLogoAdapterHolder> {
    private Context mContext;
    private List<String> friendAccounts;

    public GroupLogoAdapter(Context context, List<String> friendAccounts) {
        this.mContext = context;
        this.friendAccounts = friendAccounts;
    }

    @Override
    public GroupLogoAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.group_logo_item, parent, false);
        return new GroupLogoAdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(final GroupLogoAdapterHolder holder, final int position) {
        HttpHelper.downloadPicture(mContext, NIMClient.getService(UserService.class).getUserInfo(friendAccounts.get(position)) == null
                        ? null : NIMClient.getService(UserService.class).getUserInfo(friendAccounts.get(position)).getAvatar(),
                R.drawable.img_default, R.drawable.img_default, holder.user_team_logo); //头像
    }

    @Override
    public int getItemCount() {
        return friendAccounts == null ? 0 : friendAccounts.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class GroupLogoAdapterHolder extends RecyclerView.ViewHolder {
        private CircleImageView user_team_logo;

        public GroupLogoAdapterHolder(View itemView) {
            super(itemView);
            user_team_logo = (CircleImageView) itemView.findViewById(R.id.group_logo);
        }
    }

}
