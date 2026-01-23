package com.cg.demo.ui.main;

import com.cg.demo.R;
import com.cg.demo.base.BaseAdapter;
import com.cg.demo.bean.Message;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

/**
 * @author:lee
 * @Date:2026/1/21 15:19
 * @Describe:
 */
public class SwipeMessageAdapter extends BaseAdapter<Message> {
    public SwipeMessageAdapter(int layoutResId, List<Message> data) {
        super(layoutResId, data);
    }

    @Override
    public void setViewData(BaseViewHolder holder, Message item, int position) {
        holder.setText(R.id.tv_content, item.getContent())
                .setText(R.id.tv_name, item.getName());
    }

    @Override
    public void setEvent(BaseViewHolder holder, Message item, int position) {

    }
}
