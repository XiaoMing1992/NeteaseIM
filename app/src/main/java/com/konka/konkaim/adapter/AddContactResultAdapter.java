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
import com.konka.konkaim.http.HttpHelper;
import com.konka.konkaim.ui.CircleImageView;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

/**
 * Created by HP on 2018-5-10.
 */

public class AddContactResultAdapter extends RecyclerView.Adapter<AddContactResultAdapter.AddContactResultAdapterHolder> {
    private Context mContext;
    private List<NimUserInfo> contactBeens;
    private OnItemClickListener onItemClickListener;

    public AddContactResultAdapter(Context context, List<NimUserInfo> contactBeens) {
        this.mContext = context;
        this.contactBeens = contactBeens;
    }

    @Override
    public AddContactResultAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.add_contact_result_item, parent, false);
        return new AddContactResultAdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(final AddContactResultAdapterHolder holder, final int position) {
        holder.nickname.setText(contactBeens.get(position).getName());
        if(!TextUtils.isEmpty(contactBeens.get(position).getAvatar())) {
            System.out.println("friendAccount=" + contactBeens.get(position).getAccount());
            HttpHelper.downloadPicture(mContext, contactBeens.get(position).getAvatar(),
                    R.drawable.img_default, R.drawable.img_default ,holder.head_icon);
        }else {
            holder.head_icon.setImageResource(R.drawable.img_default);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("--> ContactAdapter position="+position);
                onItemClickListener.OnItemClick(position);
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

    class AddContactResultAdapterHolder extends RecyclerView.ViewHolder {
        private CircleImageView head_icon;
        private TextView nickname;
        private TextView tip;

        public AddContactResultAdapterHolder(View itemView) {
            super(itemView);
            head_icon = (CircleImageView) itemView.findViewById(R.id.head_icon);
            nickname = (TextView) itemView.findViewById(R.id.nickname);
            tip = (TextView) itemView.findViewById(R.id.tip);
        }
    }

    public interface OnItemClickListener{
        void OnItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
}
