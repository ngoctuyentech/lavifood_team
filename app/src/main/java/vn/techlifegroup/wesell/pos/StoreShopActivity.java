package vn.techlifegroup.wesell.pos;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.model.Shop;
import vn.techlifegroup.wesell.utils.Constants;
import vn.techlifegroup.wesell.utils.Utils;

public class StoreShopActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter<Shop,ShopViewHolder> adapterFirebase;
    private String emailLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_shop);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_store_shop);
        setSupportActionBar(toolbar);

        Intent intent = this.getIntent();
        emailLogin = intent.getStringExtra("EmailLogin");

        FloatingActionButton fab_employ_magmt = (FloatingActionButton)findViewById(R.id.fab_store_shop);
        fab_employ_magmt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                Intent it = new Intent(getApplicationContext(),AddShopActivity.class);
                it.putExtra("EmailLogin",emailLogin);
                startActivity(it);
            }
        });

        recyclerView = (RecyclerView)findViewById(R.id.recycler_store_shop);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.addOnScrollListener(new Utils.EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                int lastFirstVisiblePosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                recyclerView.getLayoutManager().scrollToPosition(lastFirstVisiblePosition);
            }
        });

        adapterFirebase = new FirebaseRecyclerAdapter<Shop, ShopViewHolder>(
                Shop.class,
                R.id.item_shop,
                ShopViewHolder.class,
                Constants.refDatabase.child(emailLogin).child("Z_POS_Shop")
        ) {
            @Override
            public ShopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop,parent,false);
                return new ShopViewHolder(v);
            }


            @Override
            protected void populateViewHolder(ShopViewHolder viewHolder, Shop model, int position) {
                viewHolder.shopName.setText(model.getShopName());
                viewHolder.shopAddress.setText(model.getShopAddress());
                Glide.with(getApplicationContext()).load(model.getShopUrl()).error(R.drawable.storefront).fitCenter().override(300,200).into(viewHolder.shopPic);


            }
        };

        recyclerView.setAdapter(adapterFirebase);
        adapterFirebase.notifyDataSetChanged();

    }

    private class ShopViewHolder extends RecyclerView.ViewHolder {
        TextView shopName,shopAddress;
        ImageView shopPic,shopRemove;

        ShopViewHolder(View itemView) {
            super(itemView);
            shopName = (TextView) itemView.findViewById(R.id.tv_item_client_sale_name);
            shopAddress = (TextView) itemView.findViewById(R.id.tv_item_shop_address);
            shopPic = (ImageView)itemView.findViewById(R.id.iv_item_shop_pic);
            shopRemove = (ImageView)itemView.findViewById(R.id.iv_item_shop_remove);

            shopRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    final String itemKey = adapterFirebase.getRef(position).getKey();
                    AlertDialog.Builder builder = new AlertDialog.Builder(StoreShopActivity.this);
                    builder.setMessage("Xóa cửa hàng khỏi danh sách?");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Constants.refDatabase.child(emailLogin).child("Z_POS_Shop").child(itemKey).setValue(null);
                        }
                    });

                    builder.show();
                }
            });

        }

    }
}