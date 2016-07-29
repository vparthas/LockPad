package com.varunp.padlock.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.varunp.padlock.utils.file.FileManager;
import com.varunp.padlock.utils.Globals;
import com.varunp.padlock.utils.password.JsonWrapper;
import com.varunp.padlock.R;
import com.varunp.padlock.utils.password.PasswordEncryptionService;
import com.varunp.padlock.utils.password.PasswordRecoveryManager;
import com.varunp.padlock.utils.settings.SettingsManager;

import net.dealforest.sample.crypt.AES256Cipher;

import java.io.File;

public class LoginActivity extends AppCompatActivity
{

    FileManager m_fm;
    byte[][] m_passwordData;

    EditText m_passwordEntry;
    RelativeLayout page_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Login");

        SettingsManager.init(getApplicationContext());

        initViews();
    }

    ProgressDialog progressDialog;

    private void initViews()
    {
        page_layout = (RelativeLayout)findViewById(R.id.login_rl);

        m_passwordEntry = (EditText)findViewById(R.id.login_entry);
        m_passwordEntry.setOnEditorActionListener(new EditText.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    final View view = getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }

                    Snackbar.make(view, "Checking password...", Snackbar.LENGTH_INDEFINITE).setAction("Action", null).show();

                    new Thread(new Runnable() {
                        @Override
                        public void run()
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    checkPassword(m_passwordEntry.getText().toString(), view);
                                }
                            });
                        }
                    }).start();

                    return true;
                }

                return false;
            }
        });
    }

    private void checkPassword(String s, View view)
    {
        boolean valid;
        try
        {
            valid = PasswordEncryptionService.authenticate(s, m_passwordData[0],
                    m_passwordData[1]);
        }
        catch (Exception e)
        {
            valid = false;
        }

        if(valid)
        {
            AES256Cipher.setKey(AES256Cipher.generateKey(s));
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        else
        {
            Snackbar.make(view, "Incorrect password...", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        m_fm = new FileManager(getApplication());
        String loginDataRaw = m_fm.readFile(FileManager.FILE_INTERNAL, Globals.FILENAME_LOGIN_INFO);
        m_passwordData = JsonWrapper.readLoginData(loginDataRaw);

        if(m_passwordData == null)
        {
            showSetupDialog();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_recovery_button:
                showRecoveryDialog();
                break;
        }
        return true;
    }

    private void showRecoveryDialog()
    {
        final PasswordRecoveryManager manager = new PasswordRecoveryManager(getApplicationContext());

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_password_recovery);
        dialog.setCancelable(true);

        final TextView questionView = (TextView) dialog.findViewById(R.id.dialog_recovery_question);
        questionView.setText(manager.getData().question);

        final EditText editText_answer = (EditText) dialog.findViewById(R.id.dialog_recovery_answer);

        final EditText editText_pw1 = (EditText) dialog.findViewById(R.id.dialog_recovery_password_1);
        final EditText editText_pw2 = (EditText) dialog.findViewById(R.id.dialog_recovery_password_2);

        final TextView statusView = (TextView) dialog.findViewById(R.id.dialog_recovery_status);
        final TextView titleView = (TextView) dialog.findViewById(R.id.dialog_recovery_title);

        Button button_ok = (Button) dialog.findViewById(R.id.dialog_recovery_ok);
        button_ok.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (editText_answer.getVisibility() == View.VISIBLE)
                {
                    if (!manager.recoveryValid(editText_answer.getText().toString()))
                    {
                        if (statusView.getVisibility() != View.VISIBLE)
                        {
                            statusView.setText("Incorrect");
                            showStatusView();
                        }
                    }
                    else
                    {
                        titleView.setText("Change password:");

                        hideAnswerView();
                        hideQuestionView();

                        showPasswordViews();

                        if (statusView.getVisibility() != View.VISIBLE)
                        {
                            statusView.setText("Too short");
                            showStatusView();
                        }
                    }
                }
                else
                {
                    final String pw1 = editText_pw1.getText().toString();
                    String pw2 = editText_pw2.getText().toString();

                    if(!pw1.equals(pw2))
                    {
                        statusView.setText("Passwords do not match.");
                        return;
                    }

                    if(pw1.length() < 6)
                    {
                        statusView.setText("Too short");
                        return;
                    }

                    showRecoveryProgressDialog(manager, editText_answer.getText().toString(), pw1);

                    dialog.dismiss();
                }
            }

            private void showStatusView()
            {
                statusView.setVisibility(View.VISIBLE);
                statusView.setAlpha(0.0f);
                statusView.animate()
                        .translationY(statusView.getHeight())
                        .alpha(1.0f);
            }

            private void hideAnswerView()
            {
                editText_answer.setAlpha(1.0f);
                editText_answer.animate()
                        .translationY(0)
                        .alpha(0.0f)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                editText_answer.setVisibility(View.GONE);
                            }
                        });
            }

            private void showPasswordViews()
            {
                editText_pw1.setVisibility(View.VISIBLE);
                editText_pw1.setAlpha(0.0f);
                editText_pw1.animate()
                        .translationY(editText_pw1.getHeight())
                        .alpha(1.0f);

                editText_pw2.setVisibility(View.VISIBLE);
                editText_pw2.setAlpha(0.0f);
                editText_pw2.animate()
                        .translationY(editText_pw2.getHeight())
                        .alpha(1.0f);
            }

            private void hideQuestionView()
            {
                questionView.setAlpha(1.0f);
                questionView.animate()
                        .translationY(0)
                        .alpha(0.0f)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                questionView.setVisibility(View.GONE);
                            }
                        });
            }

        });

        Button button_cancel = (Button) dialog.findViewById(R.id.dialog_recovery_cancel);
        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

    private void showRecoveryProgressDialog(final PasswordRecoveryManager manager, final String answer, final String pw1)
    {
        final ProgressDialog progress = ProgressDialog.show(this, "dialog title", "dialog message", true);

        new Thread(new Runnable() {
            @Override
            public void run()
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            manager.recover(answer, pw1);
                        }
                        catch (Exception e)
                        {
                            Log.d("PWRecov", e.getMessage());
                            Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                        }

                        String loginDataRaw = m_fm.readFile(FileManager.FILE_INTERNAL, Globals.FILENAME_LOGIN_INFO);
                        m_passwordData = JsonWrapper.readLoginData(loginDataRaw);

                        progress.dismiss();
                    }
                });
            }
        }).start();
    }

    private void showSetupDialog()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.setup_dialog_message))
                .setTitle(getString(R.string.setup_dialog_title));

        builder.setPositiveButton(getString(R.string.setup_dialog_okButton),
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        startActivity(new Intent(getApplicationContext(), SetupActivity.class));
                        dialog.dismiss();
                    }
                });

        builder.setNegativeButton(getString(R.string.setup_dialog_cancelButton),
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        finish();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }
}
