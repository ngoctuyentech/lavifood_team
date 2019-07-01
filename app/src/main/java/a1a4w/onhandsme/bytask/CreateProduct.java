package a1a4w.onhandsme.bytask;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;

import a1a4w.onhandsme.R;
import a1a4w.onhandsme.model.Product;

import static a1a4w.onhandsme.utils.Constants.buttonClick;
import static a1a4w.onhandsme.utils.Constants.refDatabase;

public class CreateProduct extends AppCompatActivity {
    private FirebaseRecyclerAdapter<Product, ProductViewHolder> adapterFirebaseProduct,adapterFirebaseAddProduct;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_product);

        Intent it = this.getIntent();
        final String emailLogin = it.getStringExtra("EmailLogin");

        final EditText edtName = findViewById(R.id.edt_create_product_name);
        final EditText edtPrice = findViewById(R.id.edt_create_product_price);
        final EditText edtUnit = findViewById(R.id.edt_create_product_unit);

        RecyclerView productList = findViewById(R.id.rv_create_product_list);

        productList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        productList.setLayoutManager(linearLayoutManager);

        adapterFirebaseProduct = new FirebaseRecyclerAdapter<Product,ProductViewHolder>(
                Product.class,
                R.layout.item_product_pos,
                ProductViewHolder.class,
                refDatabase.child(emailLogin).child("Product")
        ) {
            @Override
            public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_pos,parent,false);
                return new ProductViewHolder(v);
            }


            @Override
            protected void populateViewHolder(ProductViewHolder viewHolder, Product model, int position) {
                viewHolder.productName.setText(model.getProductName());
            }
        };

        productList.setAdapter(adapterFirebaseProduct);
        adapterFirebaseProduct.notifyDataSetChanged();

        Button btnDone = findViewById(R.id.btn_create_product_done);

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);

                String name = edtName.getText().toString();
                String price = edtPrice.getText().toString();
                String unit = edtUnit.getText().toString();

                if(TextUtils.isEmpty(name) || TextUtils.isEmpty(price) ||TextUtils.isEmpty(unit) ){
                    Toast.makeText(getApplicationContext(), "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_LONG).show();

              }else{
                    String productCode = refDatabase.child(emailLogin).child("Product").push().getKey();
                    Product product = new Product(name,price,unit,productCode);
                    refDatabase.child(emailLogin).child("Product").push().setValue(product);
                }
            }
        });
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName;


        public ProductViewHolder(View itemView) {
            super(itemView);
            productName = (TextView)itemView.findViewById(R.id.tv_item_product_name);




        }
    }
}
