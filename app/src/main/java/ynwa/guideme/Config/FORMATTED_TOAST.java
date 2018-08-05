package ynwa.guideme.Config;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.dmoral.toasty.Toasty;

public class FORMATTED_TOAST {

    public static void success(Context context, String message) {
        Toast toast = Toasty.success(context, message, Toast.LENGTH_SHORT, true);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void error(Context context, String message) {
        Toast toast = Toasty.error(context, message, Toast.LENGTH_SHORT, true);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void warning(Context context, String message) {
        Toast toast = Toasty.warning(context, message, Toast.LENGTH_SHORT, true);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void info(Context context, String message) {
        Toast toast = Toasty.info(context, message, Toast.LENGTH_SHORT, true);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static void hideKeyboard(@NonNull Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager manager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (manager != null) {
                manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
}