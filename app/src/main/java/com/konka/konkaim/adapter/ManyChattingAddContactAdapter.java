package com.konka.konkaim.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.konka.konkaim.bean.ContactBean;

import java.util.ArrayList;
import java.util.List;

import com.konka.konkaim.R;
import com.konka.konkaim.user.HomeActivity;
import com.netease.nimlib.sdk.NIMSDK;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

/**
 * Created by HP on 2018-5-10.
 */

public class ManyChattingAddContactAdapter extends RecyclerView.Adapter<ManyChattingAddContactAdapter.ManyChattingAddContactAdapterHolder> {
    private Context mContext;
    private List<NimUserInfo> contactBeens;
    private OnItemClickListener onItemClickListener;
    private OnSelectItemListener onSelectItemListener;
    private OnKeyItemListener onKeyItemListener;
    private List<NimUserInfo> selectItemList; //存放选择的联系人
    private int current_member = 0; //已经在聊天的人数

    public ManyChattingAddContactAdapter(Context context, List<NimUserInfo> contactBeens, int current_member) {
        this.mContext = context;
        this.contactBeens = contactBeens;
        this.current_member = current_member;
        this.selectItemList = new ArrayList<>();
    }

    @Override
    public ManyChattingAddContactAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.many_chatting_add_contact_item, parent, false);
        return new ManyChattingAddContactAdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(final ManyChattingAddContactAdapterHolder holder, final int position) {
        //holder.tv_remark.setText("" + contactBeens.get(position).getRemark());

        //优先显示备注名
        String aliasName = NIMSDK.getFriendService().getFriendByAccount(contactBeens.get(position).getAccount()).getAlias(); //获取备注
        String resultName = !TextUtils.isEmpty(aliasName) ? aliasName : contactBeens.get(position).getName();
        holder.tv_remark.setText(resultName);

        if (position == 0) holder.itemView.requestFocus();

/*        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("--> ContactAdapter position="+position);
                onItemClickListener.OnItemClick(position);
            }
        });*/


        holder.select_icon.setTag(R.id.select_icon, "0");

        holder.itemView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                        System.out.println("OnKeyListener KEYCODE_ENTER " + position);

                        boolean online = isOnline(contactBeens.get(position).getAccount());
                        if (!online){
                            Toast.makeText(mContext, "对方离线，不能通话", Toast.LENGTH_SHORT).show();
                            return true;
                        }

                        System.out.println("before-->" + holder.select_icon.getTag(R.id.select_icon));
                        if (holder.select_icon.getTag(R.id.select_icon).equals("0")) {
                            holder.select_icon.setImageResource(R.drawable.team_chat_add_contact_select_tip_icon);
                            holder.select_icon.setTag(R.id.select_icon, "1");

                            if (selectItemList != null && (selectItemList.size() + current_member) < 5) {
                                System.out.println("OnKeyListener add " + position);
                                selectItemList.add(contactBeens.get(position));
                            } else if (selectItemList != null && (selectItemList.size() + current_member) >= 5) {
                                Toast.makeText(mContext, "群聊最多可添加5人", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            holder.select_icon.setImageResource(R.drawable.team_chat_add_contact_unselected_tip_icon);
                            holder.select_icon.setTag(R.id.select_icon, "0");

                            if (selectItemList != null && !selectItemList.isEmpty()) {
                                System.out.println("OnKeyListener remove " + position);
                                selectItemList.remove(contactBeens.get(position));
                            }

                        }
                        System.out.println("after-->" + holder.select_icon.getTag(R.id.select_icon));


/*                        if (selectItemList != null && (selectItemList.size() + current_member) < 5) {
                            System.out.println("OnKeyListener add " + position);
                            selectItemList.add(contactBeens.get(position));
                        }*/
                        onSelectItemListener.OnSelectItem(selectItemList);
                        return true;

                    } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        System.out.println("OnKeyListener KEYCODE_DPAD_RIGHT");
                        onKeyItemListener.OnKeyItem(true);
                        return true;
                    }

                    /*else if (keyCode == KeyEvent.KEYCODE_BACK){
                        if (holder.layout_edit.getVisibility() == View.VISIBLE){
                            holder.layout_edit.setVisibility(View.GONE);
                            holder.itemView.requestFocus();
                            return true;
                        }
                        return true;
                    }else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                            || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        if (holder.layout_edit.getVisibility() == View.VISIBLE) {
                            return true;
                        }
                    }*/
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

    class ManyChattingAddContactAdapterHolder extends RecyclerView.ViewHolder {
        private ImageView select_icon;
        private TextView tv_remark;

        public ManyChattingAddContactAdapterHolder(View itemView) {
            super(itemView);
            select_icon = (ImageView) itemView.findViewById(R.id.select_icon);
            tv_remark = (TextView) itemView.findViewById(R.id.tv_remark);
        }
    }

    public interface OnItemClickListener {
        void OnItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnSelectItemListener {
        void OnSelectItem(List<NimUserInfo> selectItemList);
    }

    public void setOnSelectItemListener(OnSelectItemListener onSelectItemListener) {
        this.onSelectItemListener = onSelectItemListener;
    }

    public interface OnKeyItemListener {
        void OnKeyItem(boolean flag);
    }

    public void setOnKeyItemListener(OnKeyItemListener onKeyItemListener) {
        this.onKeyItemListener = onKeyItemListener;
    }

    private boolean isOnline(String account) {
        boolean result = false;
        //只可以呼叫在线状态的联系人
        if (HomeActivity.onlineStateMap != null && HomeActivity.onlineStateMap.get(account) != null
                && HomeActivity.onlineStateMap.get(account).equals("1")) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }
}
