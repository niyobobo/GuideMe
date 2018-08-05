package ynwa.guideme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import dmax.dialog.SpotsDialog;
import ynwa.guideme.Config.FORMATTED_TOAST;
import ynwa.guideme.admin.AdminHome;
import ynwa.guideme.visitor.category.Category;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(MainActivity.this, AdminHome.class));
            finish();
        }

        setContentView(R.layout.activity_main);
        Button admin = findViewById(R.id.btn_admin);
        Button travel = findViewById(R.id.btn_travel);
        admin.setOnClickListener(this);
        travel.setOnClickListener(this);
    }

    @SuppressLint("InflateParams")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_travel:
                startActivity(new Intent(this, Category.class));
                break;

            case R.id.btn_admin:

                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                if (inflater != null) {
                    View view = inflater.inflate(R.layout.admin_login, null, false);
                    final AlertDialog alert = new AlertDialog.Builder(this).setView(view).show();

                    final EditText email = view.findViewById(R.id.et_email);
                    final EditText password = view.findViewById(R.id.et_password);
                    Button loginBtn = view.findViewById(R.id.btn_login);
                    loginBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            String user_email = email.getText().toString();
                            String user_password = password.getText().toString();

                            if (TextUtils.isEmpty(user_email) || TextUtils.isEmpty(user_password))
                                FORMATTED_TOAST.error(MainActivity.this, getString(R.string.provide_all_information));
                            else if (!FORMATTED_TOAST.isEmailValid(user_email))
                                FORMATTED_TOAST.error(MainActivity.this, getString(R.string.collect_email));
                            else if (password.length() < 6)
                                FORMATTED_TOAST.info(MainActivity.this, getString(R.string.password_is_short));
                            else {
                                alert.dismiss();
                                final android.app.AlertDialog dialog = new SpotsDialog(MainActivity.this);
                                dialog.setCancelable(true);
                                dialog.show();

                                firebaseAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                dialog.dismiss();
                                                if (!task.isSuccessful()) {
                                                    FORMATTED_TOAST.error(MainActivity.this, getString(R.string.credentials_not_collect));
                                                } else {
                                                    startActivity(new Intent(MainActivity.this, AdminHome.class));
                                                    finish();
                                                }
                                            }
                                        });
                            }
                        }
                    });
                }
                break;
        }
    }
}
