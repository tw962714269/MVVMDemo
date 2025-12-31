package com.cg.demo.base;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

/**
 * @author:lee
 * @Date:2025/4/2 9:34
 * @Describe:
 */
public abstract class BaseAdapter<T> extends BaseQuickAdapter<T, BaseViewHolder> {
    public BaseAdapter(int layoutResId, List<T> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, T t) {
        setViewData(holder, t, getItemPosition(t));
        setEvent(holder, t, getItemPosition(t));
    }

    public abstract void setViewData(BaseViewHolder holder, T item, int position);

    public abstract void setEvent(BaseViewHolder holder, T item, int position);
}
