package com.konka.konkaim.adapter;

import android.content.Context;
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

import java.util.List;
import java.util.Map;

import com.konka.konkaim.R;
import com.konka.konkaim.bean.ContactBean;
import com.konka.konkaim.chat.StateUtil;
import com.konka.konkaim.http.HttpHelper;
import com.konka.konkaim.ui.DeleteContactWindow;
import com.konka.konkaim.ui.EditContactWindow;
import com.netease.nimlib.sdk.NIMSDK;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

/**
 * Created by HP on 2018-5-10.
 */

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactAdapterHolder> {
    private Context mContext;
    private List<NimUserInfo> contactBeens;
    private OnItemClickListener onItemClickListener;

    public ContactAdapter(Context context, List<NimUserInfo> contactBeens) {
        this.mContext = context;
        this.contactBeens = contactBeens;
    }

    @Override
    public ContactAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.record_item, parent, false);
        return new ContactAdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(final ContactAdapterHolder holder, final int position) {
        //holder.tv_remark.setText(contactBeens.get(position).getRemark());
        //holder.tv_time.setText(contactBeens.get(position).getTime());

        //优先显示备注名
        String aliasName = NIMSDK.getFriendService().getFriendByAccount(contactBeens.get(position).getAccount()).getAlias(); //获取备注
        String resultName = !TextUtils.isEmpty(aliasName) ? aliasName : contactBeens.get(position).getName();
        holder.tv_remark.setText(resultName);

        HttpHelper.downloadPicture(mContext, contactBeens.get(position).getAvatar(),
                R.drawable.img_default, R.drawable.img_default, holder.user_logo); //头像

        holder.tv_time.setText("时间...");

        if (onlineStateMap != null && onlineStateMap.get(contactBeens.get(position).getAccount()) != null && onlineStateMap.get(contactBeens.get(position).getAccount()).equals("1")) {
            holder.tv_state.setText("在线");
            holder.user_state_icon.setVisibility(View.VISIBLE);
        }
        else{
            holder.tv_state.setText("离线");
            holder.user_state_icon.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("--> ContactAdapter position=" + position);
                onItemClickListener.OnItemClick(position);
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
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_MENU) {
                        if (holder.layout_edit.getVisibility() != View.VISIBLE) {
                            holder.layout_edit.setVisibility(View.VISIBLE);
                            holder.btn_edit.requestFocus();
                            return true;
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_BACK) {
                        if (holder.layout_edit.getVisibility() == View.VISIBLE) {
                            holder.layout_edit.setVisibility(View.GONE);
                            holder.itemView.requestFocus();
                            return true;
                        }
                        return true;
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
/*                EditContactWindow editContactWindow = new EditContactWindow(mContext);
                editContactWindow.show();
                System.out.println("position=" + position + ", friendAccount=" + contactBeens.get(position).getAccount());
                editContactWindow.setFriendAccount(contactBeens.get(position).getAccount());*/
            }
        });

        holder.btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteContactWindow deleteContactWindow = new DeleteContactWindow(mContext);
                deleteContactWindow.show();
                System.out.println("position=" + position + ", friendAccount=" + contactBeens.get(position).getAccount());
                deleteContactWindow.setFriendAccount(contactBeens.get(position).getAccount());
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

    class ContactAdapterHolder extends RecyclerView.ViewHolder {
        private ImageView user_state_icon;
        private ImageView img_origin;
        private ImageView user_logo;
        private TextView tv_time;
        private TextView tv_remark;
        private TextView tv_state;
        private LinearLayout layout_edit;
        private RelativeLayout layout_info;
        private Button btn_edit;
        private Button btn_delete;

        public ContactAdapterHolder(View itemView) {
            super(itemView);
            user_state_icon = (ImageView)itemView.findViewById(R.id.user_state_icon);
            img_origin = (ImageView) itemView.findViewById(R.id.img_origin);
            user_logo = (ImageView) itemView.findViewById(R.id.user_logo);
            tv_time = (TextView) itemView.findViewById(R.id.tv_time);
            tv_remark = (TextView) itemView.findViewById(R.id.tv_remark);
            tv_state = (TextView) itemView.findViewById(R.id.tv_state);
            layout_edit = (LinearLayout) itemView.findViewById(R.id.layout_edit);
            layout_info = (RelativeLayout) itemView.findViewById(R.id.layout_info);
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

    private Map<String, String> onlineStateMap;

    public void setOnlineStateMap(Map<String, String> onlineStateMap) {
        this.onlineStateMap = onlineStateMap;
    }
}
