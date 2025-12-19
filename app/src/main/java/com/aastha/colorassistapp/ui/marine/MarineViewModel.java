package com.aastha.colorassistapp.ui.marine;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MarineViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public MarineViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is marine fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}