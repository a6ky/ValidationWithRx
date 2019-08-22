package com.example.mygeoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.example.mygeoapp.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function3;
import io.reactivex.functions.Function4;

public class MainActivity extends AppCompatActivity {

    Button btnSignIn;
    Button btnRegister;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;

    RelativeLayout root;

    CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        compositeDisposable = new CompositeDisposable();

        btnSignIn = findViewById(R.id.btnSignIn);
        btnRegister = findViewById(R.id.btnRegister);

        root = findViewById(R.id.root_element);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");

        // обработка нажатия на кнопку "Регистрация"
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRegisterWindow();
            }
        });

        // обработка нажатия на кнопку "Войти"
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSingInWindow();
            }
        });
    }

    private void showSingInWindow() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Войти");
        builder.setMessage("Введите данные для входа");

        LayoutInflater inflater = LayoutInflater.from(this);
        View signInWindow = inflater.inflate(R.layout.sing_in_window, null);
        builder.setView(signInWindow);

        final MaterialEditText email = signInWindow.findViewById(R.id.emailField);
        final MaterialEditText password = signInWindow.findViewById(R.id.passField);

        builder.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.setPositiveButton("Войти", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (TextUtils.isEmpty(email.getText().toString())) {
                    Snackbar.make(root, "Введите вашу почту",
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (password.getText().toString().length() < 5) {
                    Snackbar.make(root, "Введите пароль, длинной более 5 символов",
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }

                // авторизация пользователя
                auth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        // при успешном добавлении пользователя
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                startActivity(new Intent(MainActivity.this, MapActivity.class));
                                finish();
                            }
                        })
                        // при не успешном добавлении пользователя
                        .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.d("LoginErr", "/***********************************************************/");
                        Log.d("LoginErr", "Login error: " + e.getMessage());
                        Log.d("LoginErr", "/***********************************************************/");

                        Snackbar.make(root, "Ошибка авторизации. " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });

        final AlertDialog dialog = builder.create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        Observable<String> emailObservable = RxEditText.getTextWatcherObservable(email);
        Observable<String> passwordObservable = RxEditText.getTextWatcherObservable(password);

        Disposable disposable = Observable.combineLatest(emailObservable, passwordObservable, new BiFunction<String, String, Boolean>() {
            @Override
            public Boolean apply(String email, String password) throws Exception {
                return !email.isEmpty() && !password.isEmpty();
            }
        }).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(aBoolean);
            }
        });

        compositeDisposable.add(disposable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    private void showRegisterWindow() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Зарегистрироваться");
        builder.setMessage("Введите данные для регистрации");

        LayoutInflater inflater = LayoutInflater.from(this);
        View registerWindow = inflater.inflate(R.layout.register_window, null);
        builder.setView(registerWindow);

        final MaterialEditText email = registerWindow.findViewById(R.id.emailField);
        final MaterialEditText password = registerWindow.findViewById(R.id.passField);
        final MaterialEditText name = registerWindow.findViewById(R.id.nameField);
        final MaterialEditText phone = registerWindow.findViewById(R.id.phoneField);

        phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        builder.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (TextUtils.isEmpty(email.getText().toString())) {
                    Snackbar.make(root, "Введите вашу почту",
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(name.getText().toString())) {
                    Snackbar.make(root, "Введите ваше имя",
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(phone.getText().toString())) {
                    Snackbar.make(root, "Введите ваш телефон",
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (password.getText().toString().length() < 5) {
                    Snackbar.make(root, "Введите пароль, длинной более 5 символов",
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }

                // Регистрация пользователя
                auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                User user = new User();
                                user.setEmail(email.getText().toString());
                                user.setName(name.getText().toString());
                                user.setPassword(password.getText().toString());
                                user.setPhone(phone.getText().toString());

                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Snackbar.make(root, "Пользователь добавлен", Snackbar.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.d("RegErr", "/************************************************************/");
                        Log.d("RegErr", "Registration error: " + e.getMessage());
                        Log.d("RegErr", "/************************************************************/");

                        Snackbar.make(root, "Ошибка регистрации. " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        });

        final AlertDialog dialog = builder.create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        Observable<String> emailObservable = RxEditText.getTextWatcherObservable(email);
        Observable<String> passwordObservable = RxEditText.getTextWatcherObservable(password);
        Observable<String> nameObservable = RxEditText.getTextWatcherObservable(name);
        Observable<String> phoneObservable = RxEditText.getTextWatcherObservable(phone);

        Disposable disposable = Observable.combineLatest(emailObservable, passwordObservable, nameObservable, phoneObservable,
                new Function4<String, String, String, String, Boolean>() {
                    @Override
                    public Boolean apply(String email, String password, String name, String phone) throws Exception {
                        return !email.isEmpty() && !password.isEmpty() && !name.isEmpty() && !phone.isEmpty();
                    }
                }).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(aBoolean);
            }
        });

        compositeDisposable.add(disposable);
    }
}
