package ynwa.guideme.admin.category;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import ynwa.guideme.R;
import ynwa.guideme.admin.company.CompanyModel;
import ynwa.guideme.admin.orders.OrdersModel;
import ynwa.guideme.visitor.category.CategoryModel;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private Context context;
    private List<CategoryModel> data;
    private DatabaseReference reference;

    CategoryAdapter(Context context, List<CategoryModel> data, DatabaseReference reference) {
        this.context = context;
        this.data = data;
        this.reference = reference;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_category_view, parent, false);
        return new CategoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final CategoryModel model = data.get(position);
        holder.category_name.setText(model.getName());

        Glide.with(context)
                .using(new FirebaseImageLoader())
                .load(model.getImageReference())
                .into(holder.category_image);

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.app_name))
                        .setMessage(R.string.deleting_category)
                        .setNegativeButton("No", null)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                reference.child("Company").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists())
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                String companyKey = snapshot.getKey();
                                                if (snapshot.getValue(CompanyModel.class).getCategory().equals(model.getId())) {
                                                    removeOrders(snapshot, companyKey, model.getId());
                                                } else
                                                    reference.child("Category").child(model.getId()).removeValue();
                                            }
                                        else
                                            reference.child("Category").child(model.getId()).removeValue();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        databaseError.toException().printStackTrace();
                                    }
                                });
                                dialog.dismiss();
                            }
                        }).show();

                return true;
            }
        });
    }

    private void removeOrders(final DataSnapshot paramSnapshot, final String companyKey, final String categoryId) {
        reference.child("Orders").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.getValue(OrdersModel.class).getCompany().equals(companyKey)) {
                        snapshot.getRef().removeValue();
                    }
                }
                paramSnapshot.getRef().removeValue();
                reference.child("Category").child(categoryId).removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
            }
        });


    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout category;
        TextView category_name;
        ImageView category_image;
        CardView cardView;

        ViewHolder(View itemView) {
            super(itemView);
            category_name = itemView.findViewById(R.id.txt_category_name);
            category_image = itemView.findViewById(R.id.iv_category_icon);
            category = itemView.findViewById(R.id.card_category);
            cardView = itemView.findViewById(R.id.card_view);
        }
    }
}
