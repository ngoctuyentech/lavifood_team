package vn.techlifegroup.wesell.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.bytask.ActionList;
import vn.techlifegroup.wesell.bytask.OrderManActivity;
import vn.techlifegroup.wesell.bytask.PromotionList;
import vn.techlifegroup.wesell.bytask.warehouse.WarehouseManActivity;
import vn.techlifegroup.wesell.list.ClientListBySaleTeam;
import vn.techlifegroup.wesell.list.HistoryChatActivity;
import vn.techlifegroup.wesell.list.SaleList;
import vn.techlifegroup.wesell.model.Functions;
import vn.techlifegroup.wesell.model.Group;
import vn.techlifegroup.wesell.model.Functions;

import static com.facebook.FacebookSdk.getApplicationContext;
import static vn.techlifegroup.wesell.utils.Constants.refRole;

public class AdapterFunctions extends RecyclerView.Adapter<AdapterFunctions.FunctionsViewHolder> {

    Context context;
    private List<Functions> items;

    private Activity activity;

    private String name;
    private int color;

    private Functions function;

    private ArrayAdapter<String> adpProductGroup;

    private String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");

    private String role;

    public AdapterFunctions() {
        super();

    }

    public AdapterFunctions(Context context, List<Functions> items, Activity activity,String role){
        this.context = context;
        this.items = items;
        this.activity = activity;
        this.role = role;
    }

    @Override
    public FunctionsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chuc_nang, parent, false);
        return new FunctionsViewHolder(v);

    }

    @Override
    public void onBindViewHolder(FunctionsViewHolder holder, int position) {
        function = items.get(position);

        holder.logo.setImageResource(function.getIcon());
        holder.name.setText(function.getName());

    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public class FunctionsViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView logo;

        public FunctionsViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_chuc_nang);
            logo = itemView.findViewById(R.id.iv_chuc_nang);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    v.startAnimation(Constants.buttonClick);
                    int position = getAdapterPosition();
                    Functions function = items.get(position);
                    String functionName = function.getName();

                    if(role.equals("SaleMan")){
                        if (functionName.equals("đơn hàng")) {

                            Intent it = new Intent(context, OrderManActivity.class);
                            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            it.putExtra("SaleMan", true);
                            context.startActivity(it);
                        }

                        if (functionName.equals("khách hàng")) {
                            //Toast.makeText(getApplicationContext(), userUid, Toast.LENGTH_LONG).show();
                            //context.startActivity(new Intent(context, ProductPromotionActitvity.class));

                            Intent it = new Intent(context, ClientListBySaleTeam.class);
                            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            it.putExtra("SaleMan", true);
                            context.startActivity(it);
                        }

                        if (functionName.equals("chương trình")) {

                            Intent it = new Intent(context, PromotionList.class);
                            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            it.putExtra("EmailLogin", userEmail);
                            it.putExtra("SaleMan", true);
                            context.startActivity(it);
                        }

                        if (functionName.equals("thông báo")) {
                            Intent it = new Intent(context, HistoryChatActivity.class);

                            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            it.putExtra("SaleMan", true);
                            context.startActivity(it);
                        }
                    }else{

                        if (functionName.equals("đơn hàng")) {

                            Intent it = new Intent(context, OrderManActivity.class);
                            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            it.putExtra("Supervisor", true);
                            context.startActivity(it);
                        }

                        if (functionName.equals("khách hàng")) {
                            //Toast.makeText(getApplicationContext(), userUid, Toast.LENGTH_LONG).show();
                            //context.startActivity(new Intent(context, ProductPromotionActitvity.class));

                            Intent it = new Intent(context, ClientListBySaleTeam.class);
                            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            it.putExtra("Supervisor", true);
                            context.startActivity(it);
                        }

                        if (functionName.equals("chương trình")) {

                            Intent it = new Intent(context, PromotionList.class);
                            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            it.putExtra("Supervisor", true);
                            context.startActivity(it);
                        }

                        if (functionName.equals("thông báo")) {
                            Intent it = new Intent(context, HistoryChatActivity.class);

                            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            it.putExtra("Supervisor", true);
                            context.startActivity(it);
                        }

                        if (functionName.equals("đội ngũ")) {
                            Intent it = new Intent(getApplicationContext(), SaleList.class);
                            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            it.putExtra("Supervisor",true);

                            context.startActivity(it);
                        }
                    }

                }
            });

        }
    }
}


