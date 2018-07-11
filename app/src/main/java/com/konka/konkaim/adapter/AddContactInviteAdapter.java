package com.konka.konkaim.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import com.konka.konkaim.R;
import com.konka.konkaim.bean.ContactBean;
import com.konka.konkaim.bean.DataSynEvent;
import com.konka.konkaim.bean.DbBean;
import com.konka.konkaim.bean.FriendBean;
import com.konka.konkaim.db.DBUtil;
import com.konka.konkaim.db.FriendDBUtil;
import com.konka.konkaim.http.HttpHelper;
import com.konka.konkaim.ui.CircleImageView;
import com.konka.konkaim.user.HomeActivity;
import com.konka.konkaim.user.UserInfoUtil;
import com.konka.konkaim.util.TimeUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.constant.VerifyType;
import com.netease.nimlib.sdk.friend.model.AddFriendData;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by HP on 2018-5-10.
 */

public class AddContactInviteAdapter extends RecyclerView.Adapter<AddContactInviteAdapter.AddContactInviteAdapterHolder> {
    private Context mContext;
    private List<NimUserInfo> contactBeens;
    private OnItemClickListener onItemClickListener;
    private List<String> tags;

    public AddContactInviteAdapter(Context context, List<NimUserInfo> contactBeens, List<String> tags) {
        this.mContext = context;
        this.contactBeens = contactBeens;
        this.tags = tags;
    }

    @Override
    public AddContactInviteAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.add_contact_invite_item, parent, false);
        return new AddContactInviteAdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(final AddContactInviteAdapterHolder holder, final int position) {
        holder.nickname.setText(contactBeens.get(position).getName());
        if(!TextUtils.isEmpty(contactBeens.get(position).getAvatar())) {
            System.out.println("friendAccount=" + contactBeens.get(position).getAccount());
            HttpHelper.downloadPicture(mContext, contactBeens.get(position).getAvatar(),
                    R.drawable.img_default, R.drawable.img_default, holder.head_icon);
        }else {
            holder.head_icon.setImageResource(R.drawable.img_default);
        }

        if (tags.get(position).equals(HomeActivity.FRIEND_ACCENT_ME_TAG)) {
            holder.layout_accept_tip.setVisibility(View.VISIBLE);
        } else if (tags.get(position).equals(HomeActivity.FRIEND_REJECT_ME_TAG)) {
            holder.layout_refuse_tip.setVisibility(View.VISIBLE);
        }else if (tags.get(position).equals(HomeActivity.FRIEND_ADD_ME_TAG)) {
            holder.layout_add_tip.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("--> ContactAdapter position=" + position);
                onItemClickListener.OnItemClick(position);
            }
        });

        holder.itemView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                        if (holder.layout_accept_tip.getVisibility() == View.VISIBLE
                                || holder.layout_refuse_tip.getVisibility() == View.VISIBLE) return true;

                        if (tags.get(position).equals(HomeActivity.FRIEND_ADD_ME_TAG)) {
                            if (holder.layout_accept_friend_tip.getVisibility() == View.VISIBLE)
                                return true;
                            holder.layout_add_tip.setVisibility(View.GONE);
                            holder.layout_accept_friend_tip.setVisibility(View.VISIBLE);
                            //同意好友请求
                            accept(contactBeens.get(position).getAccount());
                            return true;
                        } else if (tags.get(position).equals(HomeActivity.FRIEND_REJECT_ME_TAG)) { //收到好友的拒绝添加请求
                            addFriend(contactBeens.get(position).getAccount());
                            return true;
                        }

                    } else if (keyCode == KeyEvent.KEYCODE_MENU) {
                        if (holder.layout_accept_tip.getVisibility() == View.VISIBLE
                                || holder.layout_refuse_tip.getVisibility() == View.VISIBLE) return true;

                        if (tags.get(position).equals(HomeActivity.FRIEND_ADD_ME_TAG)) {
                            if (holder.layout_refuse_friend_tip.getVisibility() == View.VISIBLE)
                                return true;

                            holder.layout_add_tip.setVisibility(View.GONE);
                            holder.layout_refuse_friend_tip.setVisibility(View.VISIBLE);
                            //拒绝好友请求
                            reject(contactBeens.get(position).getAccount());
                            return true;
                        }

                    }
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactBeens == null ? 0 : contactBeens.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class AddContactInviteAdapterHolder extends RecyclerView.ViewHolder {
        private CircleImageView head_icon;
        private TextView nickname;
        private LinearLayout layout_refuse_tip;
        private LinearLayout layout_accept_tip;
        private LinearLayout layout_accept_friend_tip;
        private LinearLayout layout_refuse_friend_tip;
        private LinearLayout layout_add_tip;

        public AddContactInviteAdapterHolder(View itemView) {
            super(itemView);
            head_icon = (CircleImageView) itemView.findViewById(R.id.head_icon);
            nickname = (TextView) itemView.findViewById(R.id.nickname);
            layout_refuse_tip = (LinearLayout) itemView.findViewById(R.id.layout_refuse_tip);
            layout_accept_tip = (LinearLayout) itemView.findViewById(R.id.layout_accept_tip);
            layout_accept_friend_tip = (LinearLayout) itemView.findViewById(R.id.layout_accept_friend_tip);
            layout_refuse_friend_tip = (LinearLayout) itemView.findViewById(R.id.layout_refuse_friend_tip);
            layout_add_tip = (LinearLayout) itemView.findViewById(R.id.layout_add_tip);
        }
    }

    public interface OnItemClickListener {
        void OnItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private void accept(final String account) {
        //true表示同意，false表示拒绝
        NIMClient.getService(FriendService.class).ackAddFriendRequest(account, true).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                System.out.println("accept friend success param=" + param);
                //删除已经处理的好友请求记录
                List<FriendBean> friendBeanList = FriendDBUtil.queryOnlyFriend(mContext, UserInfoUtil.getAccid(), account);
                if (friendBeanList != null && friendBeanList.size() > 0) {
                    for (int i=0;i<friendBeanList.size(); i++) {
                        FriendDBUtil.delete(mContext, UserInfoUtil.getAccid(), friendBeanList.get(i).getId());
                    }
                }

                //showToast(mContext, "已同意好友请求");
                //addToDb(account);
                updateToDb(account);

                DataSynEvent event = new DataSynEvent(DataSynEvent.TYPE_ACCEPT_FRIEND, UserInfoUtil.getAccid(), null);
                event.setAccount(account);
                EventBus.getDefault().post(event); //更新adapter
            }

            @Override
            public void onFailed(int code) {
                System.out.println("accept friend fail code=" + code);
            }

            @Override
            public void onException(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    private void reject(final String account) {
        //true表示同意，false表示拒绝
        NIMClient.getService(FriendService.class).ackAddFriendRequest(account, false).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                System.out.println("reject friend success param=" + param);
                //删除已经处理的好友请求记录
                List<FriendBean> friendBeanList = FriendDBUtil.queryOnlyFriend(mContext, UserInfoUtil.getAccid(), account);
                if (friendBeanList != null && friendBeanList.size() > 0) {
                    for (int i=0;i<friendBeanList.size(); i++) {
                        FriendDBUtil.delete(mContext, UserInfoUtil.getAccid(), friendBeanList.get(i).getId());
                    }
                }

                //showToast(mContext, "已拒绝好友请求");
            }

            @Override
            public void onFailed(int code) {
                System.out.println("reject friend fail code=" + code);
            }

            @Override
            public void onException(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }


    private void addFriend(final String account) {
        final VerifyType verifyType = VerifyType.VERIFY_REQUEST; // 发起好友验证请求
        String msg = "好友请求附言";
        NIMClient.getService(FriendService.class).addFriend(new AddFriendData(account, verifyType, msg))
                .setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        System.out.println("add friend success param=" + param);
                        showToast(mContext, "已发送添加好友请求");
                    }

                    @Override
                    public void onFailed(int code) {
                        System.out.println("add friend fail code=" + code);
                    }

                    @Override
                    public void onException(Throwable exception) {
                        exception.printStackTrace();
                    }
                });
    }

    private void showToast(Context context, final String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }

    private synchronized void addToDb(String friendAccount) {
        DbBean friend_Bean = new DbBean();
//        friend_Bean.setIs_team(0);
        friend_Bean.setMy_account(UserInfoUtil.getAccid());
        friend_Bean.setFriend_account(friendAccount);
        friend_Bean.setIs_friend(1);
        friend_Bean.setChat_from(friendAccount);
        friend_Bean.setChat_to(friendAccount);

        friend_Bean.setIs_connect(-1);

        friend_Bean.setIs_out(2);

        DBUtil.add(mContext, friend_Bean);
    }

    private synchronized void updateToDb(String friendAccount) {
        List<DbBean> dbBeanList = DBUtil.queryForFriend(mContext, UserInfoUtil.getAccid(), friendAccount);
        if (dbBeanList != null && dbBeanList.size() > 0) {
            DbBean friend_Bean = new DbBean();
            friend_Bean.setId(dbBeanList.get(0).getId());
//            friend_Bean.setTeamId(dbBeanList.get(0).getTeamId());

            friend_Bean.setMy_account(dbBeanList.get(0).getMy_account());
            friend_Bean.setFriend_account(dbBeanList.get(0).getFriend_account());
            friend_Bean.setChat_from(dbBeanList.get(0).getChat_from());
            friend_Bean.setChat_to(dbBeanList.get(0).getChat_to());

            friend_Bean.setRecord_time(TimeUtil.getNowTime());  //注意，要用当前的时间去更新

//            friend_Bean.setIs_team(dbBeanList.get(0).getIs_team());
            friend_Bean.setIs_friend(dbBeanList.get(0).getIs_friend());

            //friend_Bean.setIs_connect(dbBeanList.get(0).getIs_connect());
            friend_Bean.setIs_connect(-1);

            friend_Bean.setIs_out(dbBeanList.get(0).getIs_out());

            System.out.println("update, id=" + dbBeanList.get(0).getId() + " friendAccount=" + friendAccount);//获取teamId
            DBUtil.update(mContext, friend_Bean);
        } else {
            addToDb(friendAccount);
        }
    }
}
