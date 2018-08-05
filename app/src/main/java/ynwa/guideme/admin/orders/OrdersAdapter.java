package ynwa.guideme.admin.orders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import ynwa.guideme.Config.FORMATTED_TOAST;
import ynwa.guideme.R;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {
    private List<OrdersModel> list;
    private Context context;

    OrdersAdapter(List<OrdersModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final OrdersModel model = list.get(position);
        holder.desc.setText(model.getDescription());
        holder.company.setText(model.getCompany());
        holder.tel.setText(model.getTelephone());
        holder.name.setText(model.getNames());
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if (inflater != null) {
                    View view = inflater.inflate(R.layout.send_message_layout, null);
                    TextView number = view.findViewById(R.id.txt_number);
                    number.setText(model.getTelephone());
                    final EditText message = view.findViewById(R.id.edt_message);
                    message.setText("Hello " + model.getNames() + " \n");
                    Button sendBtn = view.findViewById(R.id.btn_send_msg);

                    final AlertDialog dialog = new AlertDialog.Builder(context)
                            .setView(view)
                            .show();

                    sendBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (TextUtils.isEmpty(message.getText().toString()))
                                FORMATTED_TOAST.error(context, context.getString(R.string.provide_all_information));
                            else {
                                sendMessage(model.getTelephone(), message.getText().toString(), dialog);
                            }
                        }
                    });

                }
            }
        });
    }

    private void sendMessage(final String telephone, final String message, final AlertDialog dialog) {
        final android.app.AlertDialog alertDialog = new SpotsDialog(context);
        alertDialog.setCancelable(false);
        alertDialog.show();

        /*
         * You can replace this url with a url directing to your SMS gateway
         */

        String url = "https://bulksms.vsms.net/eapi/submission/send_sms/2/2.0?" +
                "username=xxxxxxxxxxx&password=xxxxxxxxx&msisdn=" + telephone + "&message=" + message;
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        alertDialog.dismiss();
                        dialog.dismiss();
                        FORMATTED_TOAST.success(context, "MESSAGE SENT");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        alertDialog.dismiss();
                        error.printStackTrace();
                        FORMATTED_TOAST.error(context, "MESSAGE FAILED TO SENT");
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView company, tel, desc, name;
        private ConstraintLayout layout;

        ViewHolder(View itemView) {
            super(itemView);
            company = itemView.findViewById(R.id.companyName);
            tel = itemView.findViewById(R.id.telephone);
            desc = itemView.findViewById(R.id.description);
            layout = itemView.findViewById(R.id.constraint);
            name = itemView.findViewById(R.id.names);
        }
    }
}
