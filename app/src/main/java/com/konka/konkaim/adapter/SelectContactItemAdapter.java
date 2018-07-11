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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.konka.konkaim.R;
import com.konka.konkaim.bean.ContactBean;
import com.konka.konkaim.http.HttpHelper;
import com.konka.konkaim.ui.CircleImageView;
import com.konka.konkaim.user.HomeActivity;
import com.konka.konkaim.user.UserInfoUtil;
import com.konka.konkaim.util.Utils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NIMSDK;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

/**
 * Created by HP on 2018-5-10.
 */

public class SelectContactItemAdapter extends RecyclerView.Adapter<SelectContactItemAdapter.SelectContactItemAdapterHolder> {
    private Context mContext;
    private List<NimUserInfo> contactBeens;
    private OnItemClickListener onItemClickListener;
    private OnSelectItemListener onSelectItemListener;
    private List<NimUserInfo> selectItemList; //存放选择的联系人

    public SelectContactItemAdapter(Context context, List<NimUserInfo> contactBeens, Map<String, String> onlineStateMap) {
        this.mContext = context;
        this.contactBeens = contactBeens;
        this.selectItemList = new ArrayList<>();
        this.onlineStateMap = onlineStateMap;
    }

    @Override
    public SelectContactItemAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.select_contact_item, parent, false);
        return new SelectContactItemAdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(final SelectContactItemAdapterHolder holder, final int position) {

        //只可以呼叫在线状态的联系人
        if (onlineStateMap != null && onlineStateMap.get(contactBeens.get(position).getAccount()) != null
                && onlineStateMap.get(contactBeens.get(position).getAccount()).equals("1")) {
            holder.state.setText("在线");
            holder.online_icon.setVisibility(View.VISIBLE);
        } else{
            holder.state.setText("离线");
            holder.online_icon.setVisibility(View.GONE);
        }

        //优先显示备注名
        String aliasName = NIMSDK.getFriendService().getFriendByAccount(contactBeens.get(position).getAccount()).getAlias(); //获取备注
        String resultName = !TextUtils.isEmpty(aliasName) ? aliasName : contactBeens.get(position).getName();
        if (Utils.length(resultName) > 10)
            holder.remark.setText(""+Utils.getStrByLength(resultName, 10) + "..." );
        else
            holder.remark.setText(resultName);

        //holder.head_icon.setImageResource(contactBeens.get(position).getTime());

        if(!TextUtils.isEmpty(contactBeens.get(position).getAvatar())){
            System.out.println("friendAccount=" + contactBeens.get(position).getAccount());
            HttpHelper.downloadPicture(mContext, contactBeens.get(position).getAvatar(),
                    R.drawable.img_default, R.drawable.img_default, holder.head_icon); //头像
        }else {
            holder.head_icon.setImageResource(R.drawable.img_default);
        }

        if (position == 0){
            holder.itemView.requestFocus();
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("--> ContactAdapter position="+position);
                //if ()
                onItemClickListener.OnItemClick(position);
            }
        });

        holder.itemView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN){
                    if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)){
                        if (holder.online_icon.getVisibility() == View.GONE){
                            showToast(mContext, "对方离线，不能通话");
                            return true;
                        }
                        if (holder.select_tip.getVisibility() != View.VISIBLE) {
                            if (selectItemList != null && selectItemList.size() < 4) {
                                System.out.println("add select position is "+position);
                                selectItemList.add(contactBeens.get(position));
                                holder.select_tip.setVisibility(View.VISIBLE);
                            }else if (selectItemList != null && selectItemList.size() >= 4){
                                showToast(mContext, "人数已达上限");
                            }
                            onSelectItemListener.OnSelectItem(selectItemList);
                            return true;
                        }else/* if (holder.select_tip.getVisibility() == View.GONE)*/{
                            if (selectItemList != null && selectItemList.size() >0) {
                                //selectItemList.add();
                                System.out.println("cancel select position is "+position);
                                selectItemList.remove(contactBeens.get(position));
                                holder.select_tip.setVisibility(View.GONE);
                            }
                            onSelectItemListener.OnSelectItem(selectItemList);
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

    class SelectContactItemAdapterHolder extends RecyclerView.ViewHolder {
        private ImageView select_tip;
        private CircleImageView head_icon;
        private TextView remark;
        private TextView state;
        private ImageView online_icon;

        public SelectContactItemAdapterHolder(View itemView) {
            super(itemView);
            online_icon = (ImageView) itemView.findViewById(R.id.online_icon);
            select_tip = (ImageView) itemView.findViewById(R.id.select_tip);
            head_icon = (CircleImageView) itemView.findViewById(R.id.head_icon);
            remark = (TextView) itemView.findViewById(R.id.remark);
            state = (TextView) itemView.findViewById(R.id.state);
        }
    }

    public interface OnItemClickListener{
        void OnItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnSelectItemListener{
        void OnSelectItem(List<NimUserInfo> selectItemList);
    }

    public void setOnSelectItemListener(OnSelectItemListener onSelectItemListener){
        this.onSelectItemListener = onSelectItemListener;
    }

    private Map<String, String> onlineStateMap;

    public void setOnlineStateMap(Map<String, String> onlineStateMap) {
        this.onlineStateMap = onlineStateMap;
    }

    private void showToast(Context context, final String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }
}
