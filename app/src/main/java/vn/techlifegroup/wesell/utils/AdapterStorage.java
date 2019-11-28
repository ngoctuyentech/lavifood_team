package vn.techlifegroup.wesell.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.model.Product;
import vn.techlifegroup.wesell.model.WarehouseIn;

public class AdapterStorage extends RecyclerView.Adapter<AdapterStorage.WarehouseInViewHolder>  {
    @SuppressWarnings("unused")
    Context context;
    private List<WarehouseIn> items;
    private WarehouseIn warehouseIn;


    public AdapterStorage() {
        super();

    }

    public AdapterStorage(Context context, List<WarehouseIn> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public WarehouseInViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view_in_out_history, parent, false);
        return new WarehouseInViewHolder(v);

    }


    @Override
    public void onBindViewHolder(WarehouseInViewHolder holder, int position) {
        warehouseIn = items.get(position);
        holder.productName.setText(warehouseIn.getProductName());
        holder.productQuantity.setText(Utils.convertNumber(warehouseIn.getProductQuantity()));
        holder.dateIn.setText(Utils.getDate(warehouseIn.getDateIn()));

    }

    void clearData() {
        items.clear(); //clear list
        this.notifyDataSetChanged(); //let your adapter know about the changes and reload view.
    }

    @Override
    public int getItemCount() {
        if(items!=null) return items.size();
        return 0;
    }

    public class WarehouseInViewHolder extends RecyclerView.ViewHolder {
        TextView dateIn, productName, productQuantity, supplier;

        public WarehouseInViewHolder(View itemView) {
            super(itemView);
            dateIn  = (TextView) itemView.findViewById(R.id.tv_view_in_history_date);
            productName  = (TextView) itemView.findViewById(R.id.tv_view_in_history_product);
            productQuantity  = (TextView) itemView.findViewById(R.id.tv_view_in_history_product_quantity);

        }
    }


}