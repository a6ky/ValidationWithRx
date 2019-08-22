package com.example.mygeoapp;


import android.text.Editable;
import android.text.TextWatcher;

import com.google.firebase.database.annotations.NotNull;
import com.rengwuxian.materialedittext.MaterialEditText;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class RxEditText {

    public static Observable<String> getTextWatcherObservable(@NotNull final MaterialEditText editText) {

        final PublishSubject<String> subject = PublishSubject.create();

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                subject.onNext(editable.toString());
            }
        });

        return subject;
    }

}
