package com.aastha.colorassistapp.ui.marine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.aastha.colorassistapp.databinding.FragmentMarineBinding;

public class MarineFragment extends Fragment {

    private FragmentMarineBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MarineViewModel marineViewModel =
                new ViewModelProvider(this).get(MarineViewModel.class);

        binding = FragmentMarineBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textMarine;
        marineViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}