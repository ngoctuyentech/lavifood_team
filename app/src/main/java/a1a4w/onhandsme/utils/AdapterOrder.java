package a1a4w.onhandsme.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import a1a4w.onhandsme.R;
import a1a4w.onhandsme.model.OrderDetail;
import a1a4w.onhandsme.order.PreviewOrderActivivity;

import static a1a4w.onhandsme.utils.Constants.buttonClick;

public class AdapterOrder extends RecyclerView.Adapter<AdapterOrder.OrderViewHolderByTime> {
    @SuppressWarnings("unused")
    Context context;
    private List<OrderDetail> items;
    private OrderDetail order;
    private String emailLogin;

    public AdapterOrder() {
        super();

    }

    public AdapterOrder(Context context, List<OrderDetail> items,String emailLogin) {
        this.context = context;
        this.items = items;
        this.emailLogin = emailLogin;
    }

    @Override
    public AdapterOrder.OrderViewHolderByTime onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new AdapterOrder.OrderViewHolderByTime(v);

    }


    @Override
    public void onBindViewHolder(AdapterOrder.OrderViewHolderByTime holder, int position) {
        order = items.get(position);
        holder.orderName.setText(order.getOrderName());


    }

    void clearData() {
        items.clear(); //clear list
        this.notifyDataSetChanged(); //let your adapter know about the changes and reload view.
    }

    @Override
    public int getItemCount() {
        if (items != null) return items.size();
        return 0;
    }

    public class OrderViewHolderByTime extends RecyclerView.ViewHolder {
        TextView orderName;


        public OrderViewHolderByTime(View itemView) {
            super(itemView);
            orderName = (TextView) itemView.findViewById(R.id.tv_order_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(buttonClick);
                    int position = getLayoutPosition();
                    OrderDetail orderDetail = items.get(position);

                    final String itemKeyString = orderDetail.getOrderPushKey();

                    Intent intent1 = new Intent(context, PreviewOrderActivivity.class);
                    intent1.putExtra("OrderPushKey", itemKeyString);
                    intent1.putExtra("EmailLogin", emailLogin);

                    intent1.putExtra("ViewOnly",true);
                    context.startActivity(intent1);

                }
            });
        }
    }

}

