package ynwa.guideme.visitor.companyList;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;

import java.util.ArrayList;
import java.util.List;

import ynwa.guideme.R;
import ynwa.guideme.admin.company.CompanyModel;
import ynwa.guideme.visitor.companyList.viewLocation.CompanyLocationActivity;
import ynwa.guideme.visitor.order.OrderFragment;

public class AdapterCompany extends RecyclerView.Adapter<AdapterCompany.ViewHolder> {

    private Context context;
    private List<CompanyModel> mArrayList;
    private List<CompanyModel> mFilteredList = new ArrayList<>();
    private FragmentManager fragmentManager;
    public static String EXTRA;

    AdapterCompany(Context context, List<CompanyModel> list, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        mArrayList = list;
        mFilteredList.addAll(mArrayList);
    }

    @NonNull
    @Override
    public AdapterCompany.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_company_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final CompanyModel model = mArrayList.get(position);

        holder.actionButton.setVisibility(View.GONE);
        holder.name.setText(model.getName());
        holder.position.setText(context.getString(R.string.lat_long, String.valueOf(model.getLatitude()),
                String.valueOf(model.getLongitude())));
        holder.category.setVisibility(View.GONE);
        holder.categoryLabel.setVisibility(View.GONE);
        holder.contact.setText(model.getContact());
        holder.address.setText(model.getRoad());
        holder.price.setText(model.getPrices());
        holder.desc.setText(model.getDescription());
        Glide.with(context)
                .using(new FirebaseImageLoader())
                .load(model.getStorageReference())
                .placeholder(R.mipmap.img_default)
                .error(R.mipmap.img_default)
                .into(holder.imageView);

        holder.getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, CompanyLocationActivity.class)
                        .putExtra(EXTRA, model));
            }
        });

        holder.order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OrderFragment orderFragment = new OrderFragment(model);
                orderFragment.show(fragmentManager, orderFragment.getTag());
            }
        });

        holder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "Please check out on this: \n\nName:" + model.getName() +
                        "\nContact:" + model.getContact() +
                        "\nAddress:" + model.getRoad() +
                        "\nDescription:" + model.getDescription();
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                context.startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

    public void filter(String query) {
        mArrayList.clear();
        if (query.isEmpty()) {
            mArrayList.addAll(mFilteredList);
        } else {
            query = query.toLowerCase();
            for (CompanyModel model : mFilteredList) {

                if (model.getCategory().toLowerCase().contains(query)
                        || model.getName().toLowerCase().contains(query)
                        || model.getContact().toLowerCase().contains(query)
                        || model.getRoad().toLowerCase().contains(query)
                        || model.getDescription().toLowerCase().contains(query)) {

                    mArrayList.add(model);
                }
            }
        }
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name, address, contact, category, price, position, categoryLabel, desc;
        private FloatingActionButton actionButton;
        private ImageView imageView;
        private TextView share, order;
        private ImageView getLocation;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.c_name);
            address = itemView.findViewById(R.id.c_address);
            contact = itemView.findViewById(R.id.c_contact);
            category = itemView.findViewById(R.id.c_category);
            position = itemView.findViewById(R.id.c_position);
            actionButton = itemView.findViewById(R.id.fab);
            imageView = itemView.findViewById(R.id.profileImage);
            share = itemView.findViewById(R.id.share);
            order = itemView.findViewById(R.id.order);
            getLocation = itemView.findViewById(R.id.getLocation);
            price = itemView.findViewById(R.id.c_prices);
            categoryLabel = itemView.findViewById(R.id.txtCatName);
            desc = itemView.findViewById(R.id.description);
        }
    }
}
