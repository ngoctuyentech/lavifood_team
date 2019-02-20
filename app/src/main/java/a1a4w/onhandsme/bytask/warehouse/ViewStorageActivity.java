package a1a4w.onhandsme.bytask.warehouse;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

import java.util.Objects;

import a1a4w.onhandsme.R;
import a1a4w.onhandsme.model.Product;
import a1a4w.onhandsme.utils.Constants;
import a1a4w.onhandsme.utils.Utils;

public class ViewStorageActivity extends AppCompatActivity {

    private FirebaseRecyclerAdapter<Product,ProductViewHolder> adapterFirebase;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private DatabaseReference refProductStorage;
    private String emailLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_storage);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_view_storage);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Intent intent = this.getIntent();
        emailLogin = intent.getStringExtra("EmailLogin");

        recyclerView = (RecyclerView)findViewById(R.id.recycler_view_storage);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        refProductStorage = Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan");
        adapterFirebase = new FirebaseRecyclerAdapter<Product,ProductViewHolder>(
                Product.class,
                R.id.item_product_view_storage,
                ProductViewHolder.class,
                refProductStorage
        ) {
            @Override
            public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_view_storage,parent,false);
                return new ProductViewHolder(v);
            }


            @Override
            protected void populateViewHolder(ProductViewHolder viewHolder, Product model, int position) {
                viewHolder.productName.setText(model.getProductName());
                viewHolder.productQuantity.setText(model.getUnitQuantity());

            }
        };

        recyclerView.setAdapter(adapterFirebase);
        adapterFirebase.notifyDataSetChanged();
    }

    private class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productQuantity;

        ProductViewHolder(View itemView) {
            super(itemView);
            productName = (TextView)itemView.findViewById(R.id.tv_item_product_view_storage_name);
            productQuantity = (TextView) itemView.findViewById(R.id.tv_item_product_view_storage_quantity);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
