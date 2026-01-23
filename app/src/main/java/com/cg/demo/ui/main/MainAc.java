package com.cg.demo.ui.main;

import android.os.Bundle;

import com.cg.demo.BR;
import com.cg.demo.R;
import com.cg.demo.base.BaseMvvmAc;
import com.cg.demo.databinding.AcMainBinding;
import com.cg.demo.event.EventHandlers;
import com.cg.demo.network.Tip;

public class MainAc extends BaseMvvmAc<AcMainBinding, MainViewModel> {

    @Override
    protected int initContentView(Bundle savedInstanceState) {
        return R.layout.ac_main;
    }

    @Override
    protected int initVariableId() {
        return BR.viewModel;
    }

    public class ViewEvents extends EventHandlers {
        public void exit() {
            Tip.logout();
        }
    }

    @Override
    public void initEvents() {
        super.initEvents();
        binding.setEvents(new ViewEvents());
    }

    @Override
    public void initViews() {
        super.initViews();
    }
}