package ynwa.guideme.admin.company;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.List;

import dmax.dialog.SpotsDialog;
import ynwa.guideme.Config.FORMATTED_TOAST;
import ynwa.guideme.R;
import ynwa.guideme.admin.orders.OrdersModel;

import static android.app.Activity.RESULT_OK;

public class CompanyAdapter extends RecyclerView.Adapter<CompanyAdapter.ViewHolder> {

    private Context context;
    private List<CompanyModel> mArrayList;
    private Fragment fragment;
    private ImageView imageView;
    private String companyKey;
    private StorageReference storageReference;
    private DatabaseReference reference;

    CompanyAdapter(Context context, List<CompanyModel> list, StorageReference storageReference, Fragment fragment, DatabaseReference reference) {
        this.context = context;
        mArrayList = list;
        this.storageReference = storageReference;
        this.fragment = fragment;
        this.reference = reference;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_company_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final CompanyModel model = mArrayList.get(position);
        holder.name.setText(model.getName());
        holder.position.setText(context.getString(R.string.lat_long, String.valueOf(model.getLatitude()),
                String.valueOf(model.getLongitude())));
        holder.category.setText(model.getCategory());
        holder.contact.setText(model.getContact());
        holder.address.setText(model.getRoad());
        holder.price.setText(model.getPrices());
        holder.share.setVisibility(View.GONE);
        holder.order.setVisibility(View.GONE);
        holder.mImageView.setVisibility(View.GONE);
        holder.desc.setText(model.getDescription());
        Glide.with(context)
                .using(new FirebaseImageLoader())
                .load(model.getStorageReference())
                .placeholder(R.mipmap.img_default)
                .error(R.mipmap.img_default)
                .into(holder.image);

        holder.actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView = holder.image;
                companyKey = model.getKey();
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(16, 9)
                        .start(context, fragment);
            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.app_name))
                        .setMessage("Do you wand to delete this category?")
                        .setNegativeButton("No", null)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                reference.child("Orders").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            if (snapshot.getValue(OrdersModel.class).getCompany().equals(model.getKey()))
                                                snapshot.getRef().removeValue();
                                        }
                                        reference.child("Company").child(model.getKey()).removeValue();
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri filepath_uri = result.getUri();
                imageView.setImageURI(filepath_uri);
                updateCompanyImage(filepath_uri);
            } else
                FORMATTED_TOAST.warning(context, context.getString(R.string.canceled));
        }
    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

    private void updateCompanyImage(final Uri image_uri) {

        final android.app.AlertDialog dialog = new SpotsDialog(context);
        dialog.show();
        dialog.setCancelable(false);

        StorageReference reference = storageReference.child("Images")
                .child("company")
                .child(companyKey)
                .child("picture.jpg");
        reference.putFile(image_uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                dialog.dismiss();
                FORMATTED_TOAST.success(context, context.getString(R.string.image_inserted));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                FORMATTED_TOAST.error(context, e.getMessage());
            }
        });
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name, address, contact, category, price, position, desc;
        private FloatingActionButton actionButton;
        private ImageView image;
        private CardView cardView;
        private TextView share, order;
        private ImageView mImageView;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.c_name);
            address = itemView.findViewById(R.id.c_address);
            contact = itemView.findViewById(R.id.c_contact);
            category = itemView.findViewById(R.id.c_category);
            position = itemView.findViewById(R.id.c_position);
            actionButton = itemView.findViewById(R.id.fab);
            image = itemView.findViewById(R.id.profileImage);
            cardView = itemView.findViewById(R.id.card_company);
            share = itemView.findViewById(R.id.share);
            order = itemView.findViewById(R.id.order);
            mImageView = itemView.findViewById(R.id.getLocation);
            price = itemView.findViewById(R.id.c_prices);
            desc = itemView.findViewById(R.id.description);
        }
    }
}
