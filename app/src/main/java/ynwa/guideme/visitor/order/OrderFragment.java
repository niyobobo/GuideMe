package ynwa.guideme.visitor.order;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rilixtech.CountryCodePicker;

import java.util.UUID;

import dmax.dialog.SpotsDialog;
import ynwa.guideme.Config.FORMATTED_TOAST;
import ynwa.guideme.R;
import ynwa.guideme.admin.company.CompanyModel;

@SuppressLint("ValidFragment")
public class OrderFragment extends BottomSheetDialogFragment {

    public CompanyModel companyModel;
    private EditText phone, description, names;
    public DatabaseReference databaseReference;
    private CountryCodePicker ccPicker;

    public OrderFragment(CompanyModel companyModel) {
        this.companyModel = companyModel;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);
        init(view);
        return view;
    }

    private void init(View view) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Orders");
        phone = view.findViewById(R.id.et_phone);
        names = view.findViewById(R.id.editText3);
        ccPicker = view.findViewById(R.id.code_picker);
        ccPicker.setDefaultCountryUsingNameCode(getCountryCode());
        ccPicker.registerPhoneNumberTextView(phone);
        description = view.findViewById(R.id.description);
        Button btn_order = view.findViewById(R.id.btn_order);
        btn_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(phone.getText().toString()) || TextUtils.isEmpty(description.getText().toString())) {
                    FORMATTED_TOAST.info(getContext(), getString(R.string.provide_all_information));
                } else if (phone.length() < 9) {
                    FORMATTED_TOAST.info(getContext(), getString(R.string.telephone_is_short));
                } else {
                    makeOrder();
                }
            }
        });
    }

    private String getCountryCode() {
        String countryIso = null;
        assert getActivity() != null;
        TelephonyManager manager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        if (manager != null)
            countryIso = manager.getSimCountryIso();
        return countryIso;
    }

    private void makeOrder() {
        final AlertDialog dialog = new SpotsDialog(getContext());
        dialog.setCancelable(false);
        dialog.show();
        ModelOrder order = new ModelOrder(
                UUID.randomUUID().toString(),
                names.getText().toString(),
                ccPicker.getFullNumberWithPlus(),
                description.getText().toString(),
                companyModel.getKey());

        databaseReference.push().setValue(order, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                dialog.dismiss();
                FORMATTED_TOAST.success(getContext(), getString(R.string.order_text));
                dismiss();
            }
        });
    }

}
