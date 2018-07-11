package com.konka.konkaim.adapter;

import android.content.Context;
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

import com.konka.konkaim.R;
import com.konka.konkaim.bean.DataSynEvent;
import com.konka.konkaim.bean.DbBean;
import com.konka.konkaim.db.DBUtil;
import com.konka.konkaim.http.HttpHelper;
import com.konka.konkaim.ui.CircleImageView;
import com.konka.konkaim.ui.DeleteContactWindow;
import com.konka.konkaim.ui.EditContactWindow;
import com.konka.konkaim.user.HomeActivity;
import com.konka.konkaim.user.UserInfoUtil;
import com.konka.konkaim.util.TimeUtil;
import com.konka.konkaim.util.Utils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NIMSDK;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Map;

/**
 * Created by HP on 2018-5-10.
 */

public class ChatRecordAdapter extends RecyclerView.Adapter<ChatRecordAdapter.ChatRecordAdapterHolder> {
    private Context mContext;
    private List<DbBean> contactBeens;
    private OnItemClickListener onItemClickListener;

    public ChatRecordAdapter(Context context, List<DbBean> contactBeens) {
        this.mContext = context;
        this.contactBeens = contactBeens;
    }

    @Override
    public ChatRecordAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.chat_record_adapter, parent, false);
        return new ChatRecordAdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(final ChatRecordAdapterHolder holder, final int position) {
        chat_record_flag_edit_has_show = false;
        chat_record_flag_edit_has_show_position = -1;

        final String friendAccount = contactBeens.get(position).getFriend_account();
        System.out.println("-------------------------------> position=" + position);

        System.out.println("id=" + contactBeens.get(position).getId() + "--->friendAccount is "
                + friendAccount + " time is " + contactBeens.get(position).getRecord_time()
                + " is_out is " + contactBeens.get(position).getIs_out() + ", is_connect=" + contactBeens.get(position).getIs_connect());

        if (position == 0 && !ManyChatAdapter.many_chat_flag_edit_has_show) {
            holder.itemView.requestFocus();
        }
        holder.layout_info.setVisibility(View.VISIBLE);
        holder.layout_team_info.setVisibility(View.GONE);

        //优先显示备注名
        String aliasName = NIMSDK.getFriendService().getFriendByAccount(friendAccount) == null
                ? null : NIMSDK.getFriendService().getFriendByAccount(friendAccount).getAlias(); //获取备注
        //String resultName = !TextUtils.isEmpty(aliasName) ? aliasName : contactBeens.get(position).getName();
        String name = NIMClient.getService(UserService.class).getUserInfo(friendAccount) == null
                ? null : NIMClient.getService(UserService.class).getUserInfo(friendAccount).getName(); //获取昵称
        String resultName = !TextUtils.isEmpty(aliasName) ? aliasName : name;

        if (contactBeens.get(position).getIs_connect() == -2)
            holder.tv_remark.setTextColor(mContext.getResources().getColor(R.color.color5));
        else
            holder.tv_remark.setTextColor(mContext.getResources().getColor(R.color.color1));

        if (resultName != null && Utils.length(resultName) > 10)
            holder.tv_remark.setText("" + Utils.getStrByLength(resultName, 10) + "...");
        else
            holder.tv_remark.setText(resultName);


//        System.out.println("friendAccount=" + friendAccount + ", avator=" + NIMClient.getService(UserService.class).getUserInfo(friendAccount).getAvatar());

        if (NIMClient.getService(UserService.class).getUserInfo(friendAccount) != null) {
            if (!TextUtils.isEmpty(NIMClient.getService(UserService.class).getUserInfo(friendAccount).getAvatar())) {
                System.out.println("friendAccount=" + friendAccount);
                HttpHelper.downloadPicture(mContext, NIMClient.getService(UserService.class).getUserInfo(friendAccount).getAvatar(),
                        R.drawable.img_default, R.drawable.img_default, holder.user_logo); //头像
            }else {
                holder.user_logo.setImageResource(R.drawable.img_default);
            }
        } else {
            holder.user_logo.setImageResource(R.drawable.img_default);
        }

        if (HomeActivity.onlineStateMap != null && HomeActivity.onlineStateMap.get(friendAccount) != null
                && HomeActivity.onlineStateMap.get(friendAccount).equals("1")) {
            holder.tv_state.setText("在线");
            holder.user_state_icon.setVisibility(View.VISIBLE);
        } else {
            holder.tv_state.setText("离线");
            holder.user_state_icon.setVisibility(View.GONE);
        }

        if (contactBeens.get(position).getIs_out() == 0) {
            holder.img_origin.setVisibility(View.GONE);
            holder.tv_time.setVisibility(View.VISIBLE);
            System.out.println("" + contactBeens.get(position).getRecord_time());
            holder.tv_time.setText("" + contactBeens.get(position).getRecord_time()); //显示时间
        } else if (contactBeens.get(position).getIs_out() == 1) {
            holder.img_origin.setVisibility(View.VISIBLE);
            holder.tv_time.setVisibility(View.VISIBLE);
            System.out.println("" + contactBeens.get(position).getRecord_time());
            holder.tv_time.setText("" + contactBeens.get(position).getRecord_time()); //显示时间
        } else if (contactBeens.get(position).getIs_out() == 2) {
            //holder.layout_top.setVisibility(View.GONE);
            holder.img_origin.setVisibility(View.GONE);
            holder.tv_time.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isMyFriend = NIMClient.getService(FriendService.class).isMyFriend(friendAccount);
                if (!isMyFriend) {
                    showToast(mContext, "对方非好友，不能通话");
                    DBUtil.delete(mContext, UserInfoUtil.getAccid(), contactBeens.get(position).getId()); //从数据库里面删除记录
                    DataSynEvent event = new DataSynEvent(DataSynEvent.TYPE_REFRESH_FRIEND, UserInfoUtil.getAccid(), null);
                    EventBus.getDefault().post(event); //更新adapter
                    return;
                }

                //只可以呼叫在线状态的联系人
                if (HomeActivity.onlineStateMap != null && HomeActivity.onlineStateMap.get(friendAccount) != null
                        && HomeActivity.onlineStateMap.get(friendAccount).equals("1")) {
                    System.out.println("--> ContactAdapter position=" + position);
                    onItemClickListener.OnItemClick(position);
                } else {
                    System.out.println("--> ContactAdapter position=" + position + " 离线");
                    showToast(mContext, "对方离线，不能通话");
                }
            }
        });

        holder.btn_edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    holder.btn_edit.setTextColor(mContext.getResources().getColor(R.color.color1));
                } else {
                    holder.btn_edit.setTextColor(mContext.getResources().getColor(R.color.color6));
                }
            }
        });

        holder.btn_delete.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    holder.btn_delete.setTextColor(mContext.getResources().getColor(R.color.color1));
                } else {
                    holder.btn_delete.setTextColor(mContext.getResources().getColor(R.color.color6));
                }
            }
        });

        holder.itemView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                System.out.println("keyCode=" + keyCode + ", event.getKeyCode()=" + event.getKeyCode() + ", event.getAction()=" + event.getAction());

                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_MENU) {
                        if (holder.layout_edit.getVisibility() != View.VISIBLE) {
                            holder.layout_edit.setVisibility(View.VISIBLE);
                            holder.btn_edit.requestFocus();

                            chat_record_flag_edit_has_show = true; //控制
                            chat_record_flag_edit_has_show_position = position;

                            return true;
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_BACK) {

                        if (holder.layout_edit.getVisibility() == View.VISIBLE) {
                            holder.layout_edit.setVisibility(View.GONE);
                            //holder.itemView.requestFocus();
                            return true;
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                            || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
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
                EditContactWindow editContactWindow = new EditContactWindow(mContext, friendAccount);
                System.out.println("position=" + position + ", friendAccount=" + friendAccount);
                editContactWindow.setPosition(position);
                editContactWindow.setOnEditFriendListener(onEditFriendListener);
                editContactWindow.show();
            }
        });

        holder.btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteContactWindow deleteContactWindow = new DeleteContactWindow(mContext);
                deleteContactWindow.show();
                System.out.println("position=" + position + ", friendAccount=" + friendAccount);
                deleteContactWindow.setFriendAccount(friendAccount);
                deleteContactWindow.setPosition(position);
                deleteContactWindow.setOnDeleteListener(onDeleteListener);
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

    public class ChatRecordAdapterHolder extends RecyclerView.ViewHolder {
        private ImageView user_state_icon;
        private ImageView img_origin;
        private CircleImageView user_logo;
        private TextView tv_time;
        private TextView tv_remark;
        private TextView tv_state;
        private LinearLayout layout_top;
        private LinearLayout layout_edit;
        private RelativeLayout layout_info;

        //team
        private RelativeLayout layout_team_info;
        private ImageView team_state_icon;
        private ImageView img_team_origin;
        private ImageView user_team_logo;
        private TextView tv_team_time;
        private TextView tv_team_remark;
        private TextView tv_team_state;

        private Button btn_edit;
        private Button btn_delete;

        public ChatRecordAdapterHolder(View itemView) {
            super(itemView);
            user_state_icon = (ImageView) itemView.findViewById(R.id.user_state_icon);
            img_origin = (ImageView) itemView.findViewById(R.id.img_origin);
            user_logo = (CircleImageView) itemView.findViewById(R.id.user_logo);
            tv_time = (TextView) itemView.findViewById(R.id.tv_time);
            tv_remark = (TextView) itemView.findViewById(R.id.tv_remark);
            tv_state = (TextView) itemView.findViewById(R.id.tv_state);
            layout_top = (LinearLayout) itemView.findViewById(R.id.layout_top);
            layout_edit = (LinearLayout) itemView.findViewById(R.id.layout_edit);
            layout_info = (RelativeLayout) itemView.findViewById(R.id.layout_info);

            //team
            team_state_icon = (ImageView) itemView.findViewById(R.id.team_state_icon);
            img_team_origin = (ImageView) itemView.findViewById(R.id.img_team_origin);
            user_team_logo = (ImageView) itemView.findViewById(R.id.user_team_logo);
            tv_team_time = (TextView) itemView.findViewById(R.id.tv_team_time);
            tv_team_remark = (TextView) itemView.findViewById(R.id.tv_team_remark);
            tv_team_state = (TextView) itemView.findViewById(R.id.tv_team_state);
            layout_team_info = (RelativeLayout) itemView.findViewById(R.id.layout_team_info);

            btn_edit = (Button) itemView.findViewById(R.id.btn_edit);
            btn_delete = (Button) itemView.findViewById(R.id.btn_delete);
        }
    }

    public interface OnItemClickListener {
        void OnItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

/*    private Map<String, String> onlineStateMap;

    public void setOnlineStateMap(Map<String, String> onlineStateMap) {
        this.onlineStateMap = onlineStateMap;
    }*/

    public static boolean chat_record_flag_edit_has_show = false;
    public static int chat_record_flag_edit_has_show_position = -1;

    private void showToast(Context context, final String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }

    private RecyclerView recyclerView;

    private Handler handler = new Handler();
    private LinearLayoutManager mLayoutMgr;

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public void setmLayoutMgr(LinearLayoutManager mLayoutMgr) {
        this.mLayoutMgr = mLayoutMgr;
    }

    private DeleteContactWindow.OnDeleteListener onDeleteListener = new DeleteContactWindow.OnDeleteListener() {
        @Override
        public void onDelete(final int position, boolean quit) {
            if (quit) {
                //recyclerView.getChildAt(position).findViewById(R.id.layout_edit).setVisibility(View.GONE);
                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(chat_record_flag_edit_has_show_position);
                ((ChatRecordAdapter.ChatRecordAdapterHolder) viewHolder).itemView.findViewById(R.id.layout_edit).setVisibility(View.GONE);

                chat_record_flag_edit_has_show = false;
                chat_record_flag_edit_has_show_position = -1;

                DBUtil.delete(mContext, UserInfoUtil.getAccid(), contactBeens.get(position).getId()); //从数据库里面删除记录

                contactBeens.remove(position);
                notifyItemRemoved(position);
                notifyDataSetChanged();

/*                if (contactBeens.isEmpty()) {
                    showToast(mContext, "为空了");
                }else {

                    if (position > 0) {
                        //recyclerView.getChildAt(position - 1 - mLayoutMgr.findFirstVisibleItemPosition()).findViewById(R.id.layout_edit).setVisibility(View.GONE);
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
                showToast(mContext, "删除失败");
            }
        }
    };

    private EditContactWindow.OnEditFriendListener onEditFriendListener = new EditContactWindow.OnEditFriendListener() {
        @Override
        public void onEditFriend(int position, String friendAccount, boolean success) {
            System.out.println("onEditFriend, friendAccount is " + friendAccount + ", position=" + position + ", success=" + success);

            if (success) {
                //优先显示备注名
                String aliasName = NIMSDK.getFriendService().getFriendByAccount(friendAccount) == null
                        ? null : NIMSDK.getFriendService().getFriendByAccount(friendAccount).getAlias(); //获取备注
                //String resultName = !TextUtils.isEmpty(aliasName) ? aliasName : contactBeens.get(position).getName();
                String name = NIMClient.getService(UserService.class).getUserInfo(friendAccount) == null
                        ? null : NIMClient.getService(UserService.class).getUserInfo(friendAccount).getName(); //获取昵称
                String resultName = !TextUtils.isEmpty(aliasName) ? aliasName : name;

                TextView textView = recyclerView.getChildAt(position).findViewById(R.id.tv_remark);
                textView.setText(resultName);

                recyclerView.getChildAt(position).findViewById(R.id.layout_edit).setVisibility(View.GONE);
                recyclerView.getChildAt(position).requestFocus();

                chat_record_flag_edit_has_show = false;
                chat_record_flag_edit_has_show_position = -1;
            } else {

            }
        }
    };
}
