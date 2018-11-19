package com.lzj.its.sharingpark.adapter;

import android.content.Context;
import android.graphics.Color;

import com.allen.library.SuperTextView;
import com.lzj.its.sharingpark.R;
import com.lzj.its.sharingpark.bean.SharingBean;
import com.orhanobut.logger.Logger;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.List;

/**
 * Created by lzj on 18-10-29.
 */

public class SharingAdapter extends CommonAdapter<SharingBean> {
    private Context mContext;

    public SharingAdapter(Context context, List<SharingBean> datas) {

        super(context, R.layout.item_sharing, datas);
        Logger.i(datas.toString());
        mContext = context;
    }

    @Override
    protected void convert(ViewHolder holder, SharingBean sharingBean, int position) {
        ((SuperTextView) holder.getView(R.id.share_id)).setCenterTopString(Integer.toString(sharingBean.getShareID()));
        ((SuperTextView) holder.getView(R.id.begin_time)).setCenterTopString(sharingBean.getBeginTime());
        ((SuperTextView) holder.getView(R.id.end_time)).setCenterTopString(sharingBean.getEndTime());
        ((SuperTextView) holder.getView(R.id.address)).setCenterTopString(sharingBean.getPosition()+sharingBean.getMore());
        String state = "";
        int color = 0;
        switch (sharingBean.getState()){
            case 0:
                state = "待使用";
                color = Color.GREEN;
                break;
            case 1:
                state = "使用中";
                color = Color.RED;
                break;
            case 2:
                state = "已使用";
                color = Color.BLUE;
        }
        ((SuperTextView) holder.getView(R.id.cost_state))
                .setCenterTopString(Integer.toString(sharingBean.getCost())+"信誉点")
                .setRightTopTextColor(color)
                .setRightTopString(state);
    }
}
