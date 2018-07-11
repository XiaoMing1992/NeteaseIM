package com.konka.konkaim.ui;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.konka.konkaim.bean.ContactBean;
import com.konka.konkaim.util.LogUtil;
import com.konka.konkaim.util.Utils;

import java.util.ArrayList;
import java.util.List;

import com.konka.konkaim.R;
import com.konka.konkaim.adapter.ManyChattingAddContactAdapter;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

/**
 * Created by HP on 2018-5-10.
 */

public class ManyChattingAddContactWindow extends PopupWindow implements View.OnClickListener {
    private final String TAG = "ManyChattingAddContactWindow";
    private Context mContext;
    private View mView;
    private Button btn_ok;
    private TextView show_number;
    //private List<ContactBean> contactBeens;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView contact_recyclerView;
    private ManyChattingAddContactAdapter adapter;
    private int current_member = 0;

    private List<String> hasSelectedAccounts;
    private List<String> accounts;
    private List<NimUserInfo> users;

    public ManyChattingAddContactWindow(Context context, List<String> hasSelectedAccounts, int current_member) {
        super(context);
        this.hasSelectedAccounts = hasSelectedAccounts;
        this.current_member = current_member;
        initView(context);
        initSetting();
        initData();
    }

    private void initView(Context context) {
        mContext = context;
        mView = LayoutInflater.from(mContext).inflate(R.layout.many_chatting_add_contact, null);
        setContentView(mView);
        btn_ok = (Button) mView.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(this);
        contact_recyclerView = (RecyclerView) mView.findViewById(R.id.add_contact_recyclerView);
        show_number = (TextView) mView.findViewById(R.id.show_number);
    }

    private void initSetting() {
        Utils.computeScreenSize(mContext);
        LogUtil.LogD(TAG, "ScreenWidth = " + Utils.getScreenWidth() + " ScreenHeight = " + Utils.getScreenHeight());
        setWidth(Utils.getScreenWidth());
        setHeight(Utils.getScreenHeight());
        setFocusable(true);
        setBackgroundDrawable(mContext.getResources().getDrawable(R.color.transparent));

        setAnimationStyle(R.style.AnimationRightFade);
    }

    private void initData() {
        linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        contact_recyclerView.setLayoutManager(linearLayoutManager);
        //contactBeens = new ArrayList<>();
        demo();
        //adapter = new ManyChattingAddContactAdapter(mContext, contactBeens, getCurrent_member());
        adapter = new ManyChattingAddContactAdapter(mContext, users, getCurrent_member());

        //adapter.setOnItemClickListener(onItemClickListener);
        adapter.setOnSelectItemListener(onSelectItemListener); //监听选择联系人过程
        adapter.setOnKeyItemListener(onKeyItemListener);//监听右键选择过程

        contact_recyclerView.addItemDecoration(new RecycleViewDivider(mContext, LinearLayout.VERTICAL,
                (int) mContext.getResources().getDimension(R.dimen.many_chatting_add_contact_item_dividerHeight), 0));

        contact_recyclerView.setAdapter(adapter);

        System.out.println("--> current_member=" + current_member);
        show_number.setText("" + current_member + "/5");
        if (adapter.getItemCount() <= 0) {
            btn_ok.requestFocus();
        }
        btn_ok.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    return adapter.getItemCount() <= 0;
                }
                return false;
            }
        });
    }

    private void demo() {
        System.out.println("--- 所有好友帐号信息 ---");
        accounts = NIMClient.getService(FriendService.class).getFriendAccounts(); // 获取所有好友帐号
        System.out.println("--- before delete selected account ---");
        for (int i = 0; accounts != null && i < accounts.size(); i++) {
            System.out.println(accounts.get(i));
        }
        if (accounts != null)
            accounts.removeAll(hasSelectedAccounts);
        System.out.println("--- after delete selected account ---");
        for (int i = 0; accounts != null && i < accounts.size(); i++) {
            System.out.println(accounts.get(i));
        }

        System.out.println("--- 所有好友用户资料信息 ---");
        users = NIMClient.getService(UserService.class).getUserInfoList(accounts); // 获取所有好友用户资料
        for (int i = 0; users != null && i < users.size(); i++) {
            System.out.println("mobile=" + users.get(i).getMobile() + ", name=" + users.get(i).getName());
        }
    }

    private ManyChattingAddContactAdapter.OnKeyItemListener onKeyItemListener
            = new ManyChattingAddContactAdapter.OnKeyItemListener() {
        @Override
        public void OnKeyItem(boolean flag) {
            btn_ok.requestFocus();
        }
    };

    private List<NimUserInfo> selectItems;
    private ManyChattingAddContactAdapter.OnSelectItemListener onSelectItemListener
            = new ManyChattingAddContactAdapter.OnSelectItemListener() {
        @Override
        public void OnSelectItem(List<NimUserInfo> selectItemList) {
            if (selectItemList != null) {
                selectItems = selectItemList;
                if ((getCurrent_member() + selectItemList.size()) > 5) {
                    showToast(mContext, "人数已达上限");
                } else {
                    System.out.println("OnSelectItem " + (getCurrent_member() + selectItemList.size()));
                    show_number.setText("" + (getCurrent_member() + selectItemList.size()) + "/5");
                }
            }
        }
    };

    private void showToast(Context context, final String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }

    public void show() {
        showAtLocation(mView, Gravity.RIGHT, 0, 0);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    private OnAddContactListener onAddContactListener;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok: //发起通话
                System.out.println("click btn_ok");
                if (selectItems == null || selectItems.isEmpty()){
                    dismiss();
                    return;
                }
                visit(selectItems);

                if (getCurrent_member()+selectItems.size()>5){
                    showToast(mContext, "人数已超上限");
                    return;
                }

                onAddContactListener.OnAddContact(selectItems);
                dismiss();
                break;
        }
    }

    private void visit(List<NimUserInfo> selectItemList) {
        for (int i = 0; selectItemList != null && i < selectItemList.size(); i++) {
            System.out.println("--> add account is "+selectItemList.get(i).getAccount());
        }
    }

    public void setCurrent_member(int current_member) {
        this.current_member = current_member;
        System.out.println("--> current_member=" + current_member);
        show_number.setText("" + current_member + "/5");
    }

    public int getCurrent_member() {
        return current_member;
    }


    public interface OnAddContactListener{
        void OnAddContact(List<NimUserInfo> selectItems);
    }

    public void setOnAddContactListener(OnAddContactListener onAddContactListener) {
        this.onAddContactListener = onAddContactListener;
    }
}
