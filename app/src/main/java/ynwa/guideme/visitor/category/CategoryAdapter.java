package ynwa.guideme.visitor.category;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;

import java.util.List;

import ynwa.guideme.R;
import ynwa.guideme.visitor.companyList.CompanyActivity;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private int selectedPosition = RecyclerView.NO_POSITION;
    private Context context;
    private List<CategoryModel> dataList;
    private Button btn_continue;

    CategoryAdapter(Context context, List<CategoryModel> dataList, Button btn_continue) {
        this.context = context;
        this.dataList = dataList;
        this.btn_continue = btn_continue;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_category_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final CategoryModel dataModel = dataList.get(position);
        holder.category_name.setText(dataModel.getName());
        holder.itemView.setSelected(selectedPosition == holder.getAdapterPosition());

        Glide.with(context)
                .using(new FirebaseImageLoader())
                .load(dataModel.getImageReference())
                .into(holder.category_image);

        holder.category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyItemChanged(selectedPosition);
                selectedPosition = holder.getAdapterPosition();
                notifyItemChanged(selectedPosition);

                if (selectedPosition != RecyclerView.NO_POSITION) {
                    btn_continue.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
                    btn_continue.setTextColor(context.getResources().getColor(R.color.white_color));
                    btn_continue.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, CompanyActivity.class);
                            intent.putExtra("info", dataModel);
                            context.startActivity(intent);
                        }
                    });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout category;
        private TextView category_name;
        private ImageView category_image;

        ViewHolder(View itemView) {
            super(itemView);
            category_name = itemView.findViewById(R.id.txt_category_name);
            category_image = itemView.findViewById(R.id.iv_category_icon);
            category = itemView.findViewById(R.id.card_category);
        }

    }
}
