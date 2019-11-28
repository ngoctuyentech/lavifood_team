package vn.techlifegroup.wesell.bytask;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.model.Product;
import vn.techlifegroup.wesell.model.Promotion;

import static vn.techlifegroup.wesell.utils.Constants.buttonClick;
import static vn.techlifegroup.wesell.utils.Constants.refDatabase;

public class CreateProgram extends AppCompatActivity {
    private boolean orderDis,productDis,bgm;
    private String promotionKey,choosenProductDis,choosenProductBuy,choosenProductGet,choosenProductGetCode;
    private ArrayAdapter<String> adpProduct;
    private FirebaseRecyclerAdapter<Product, ProductViewHolder> adapterFirebaseProductDis;
    private FirebaseRecyclerAdapter<Promotion, ProductBGMHolder> adapterProductBGM;

    private DatabaseReference refPromotionMan;
    private int mYear, mMonth, mDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_program);

        Intent it = this.getIntent();
        final String emailLogin = it.getStringExtra("EmailLogin");

        promotionKey = refDatabase.child(emailLogin).child("PromotionMan").push().getKey();

        refPromotionMan = refDatabase.child(emailLogin).child("PromotionMan").child(promotionKey);

        Switch swOrderDis = findViewById(R.id.sw_program_order_discount);
        Switch swProductDiscount =findViewById(R.id.sw_program_product_discount);
        Switch swBGM = findViewById(R.id.sw_program_buy_getmore);

        final ConstraintLayout csOrderDis = findViewById(R.id.cs_order_discount);
        final ConstraintLayout csProductDis = findViewById(R.id.cs_product_discount);
        final ConstraintLayout csBGM = findViewById(R.id.cs_bgm);

        csOrderDis.setVisibility(View.GONE);
        csProductDis.setVisibility(View.GONE);
        csBGM.setVisibility(View.GONE);

        swOrderDis.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    csOrderDis.setVisibility(View.VISIBLE);
                    orderDis = true;

                }else{
                    csOrderDis.setVisibility(View.GONE);
                    orderDis = true;
                    refPromotionMan.child("orderDiscount").setValue(null);

                }
            }
        });

        swProductDiscount.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    csProductDis.setVisibility(View.VISIBLE);
                    productDis = true;
                }else{
                    csProductDis.setVisibility(View.GONE);
                    productDis = false;
                    refPromotionMan.child("ProductDiscount").setValue(null);

                }
            }
        });

        swBGM.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    csBGM.setVisibility(View.VISIBLE);
                    bgm = true;

                }else{
                    csBGM.setVisibility(View.GONE);
                    bgm = true;

                }
            }
        });

        final Spinner spinProductDis = findViewById(R.id.spin_program_productDis);
        final Spinner spinProductBuy = findViewById(R.id.spin_program_buy);
        Spinner spinProductGet = findViewById(R.id.spin_program_get);

        final List<String> products = new ArrayList<>();

        products.add("Chọn sản phẩm");

        refDatabase.child(emailLogin).child("Product").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapProduct = dataSnapshot.getChildren();
                for(DataSnapshot itemProduct:snapProduct){
                    Product p = itemProduct.getValue(Product.class);
                    String productName = p.getProductName();
                    products.add(productName);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        adpProduct = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_list_item_1, products);
        adpProduct.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinProductDis.setAdapter(adpProduct);
        spinProductBuy.setAdapter(adpProduct);
        spinProductGet.setAdapter(adpProduct);

        spinProductDis.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) spinProductDis.getSelectedView()).setTextColor(getResources().getColor(android.R.color.black));

                if(position != 0)
                    choosenProductDis = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinProductBuy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) spinProductBuy.getSelectedView()).setTextColor(getResources().getColor(android.R.color.black));

                if(position != 0)
                    choosenProductBuy = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinProductGet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != 0){
                    choosenProductGet = (String) parent.getItemAtPosition(position);
                    refDatabase.child(emailLogin).child("Product").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> snapProduct = dataSnapshot.getChildren();
                            for (DataSnapshot itemProduct:snapProduct) {
                                Product p = itemProduct.getValue(Product.class);
                                if(p.getProductName().equals(choosenProductGet)){
                                    choosenProductGetCode = itemProduct.getKey();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        final EditText edtProgramName = findViewById(R.id.edt_program_name);
        final EditText edtProgramStart = findViewById(R.id.edt_program_start);
        final EditText edtProgramEnd = findViewById(R.id.edt_program_end);

        final EditText edtOrderDis = findViewById(R.id.edt_program_order_discount);
        final EditText edtProductDis = findViewById(R.id.edt_program_product_discount);
        final EditText edtBuyQuantity = findViewById(R.id.edt_program_bgm_buy);
        final EditText edtGetQuantity = findViewById(R.id.edt_program_bgm_get);

        edtProgramStart.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    DatePickerDialog datePickerDialog = new DatePickerDialog(CreateProgram.this,
                            new DatePickerDialog.OnDateSetListener() {

                                @Override
                                public void onDateSet(DatePicker view, int year,
                                                      int monthOfYear, int dayOfMonth) {

                                    edtProgramStart.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                                }
                            }, mYear, mMonth, mDay);
                    datePickerDialog.show();
                }

            }
        });

        edtProgramEnd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    DatePickerDialog datePickerDialog = new DatePickerDialog(CreateProgram.this,
                            new DatePickerDialog.OnDateSetListener() {

                                @Override
                                public void onDateSet(DatePicker view, int year,
                                                      int monthOfYear, int dayOfMonth) {

                                    edtProgramEnd.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                                }
                            }, mYear, mMonth, mDay);
                    datePickerDialog.show();
                }
            }
        });

        edtBuyQuantity.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    if (orderDis )
                        csOrderDis.setVisibility(View.GONE);
                    if(productDis)
                        csProductDis.setVisibility(View.GONE);


                }else{
                    if (orderDis )
                        csOrderDis.setVisibility(View.VISIBLE);
                    if(productDis)
                        csProductDis.setVisibility(View.VISIBLE);
                }
            }
        });

        edtGetQuantity.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    if (orderDis )
                        csOrderDis.setVisibility(View.GONE);
                    if(productDis)
                        csProductDis.setVisibility(View.GONE);


                }else{
                    if (orderDis )
                        csOrderDis.setVisibility(View.VISIBLE);
                    if(productDis)
                        csProductDis.setVisibility(View.VISIBLE);
                }
            }
        });

        final TextView tvOrderDis = findViewById(R.id.tv_program_orderDis);

        final RecyclerView listProductDiscount = findViewById(R.id.rv_program_product_discount);
        final RecyclerView listBGM = findViewById(R.id.rv_program_bgm);

        listProductDiscount.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        listProductDiscount.setLayoutManager(linearLayoutManager);

        adapterFirebaseProductDis = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.layout.item_product_discount,
                ProductViewHolder.class,
                refPromotionMan.child("ProductDiscount")
        ) {
            @Override
            public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_discount,parent,false);
                return new ProductViewHolder(v);
            }


            @Override
            protected void populateViewHolder(ProductViewHolder viewHolder, Product model, int position) {
                viewHolder.name.setText(model.getProductName());
                viewHolder.discount.setText(model.getProductDiscount()+"%");
            }
        };

        listProductDiscount.setAdapter(adapterFirebaseProductDis);
        adapterFirebaseProductDis.notifyDataSetChanged();

        listBGM.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(getApplicationContext());
        listBGM.setLayoutManager(linearLayoutManager2);

        adapterProductBGM = new FirebaseRecyclerAdapter<Promotion, ProductBGMHolder>(
                Promotion.class,
                R.layout.item_product_bgm,
                ProductBGMHolder.class,
                refPromotionMan.child("BGM")
        ) {
            @Override
            public ProductBGMHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_bgm,parent,false);
                return new ProductBGMHolder(v);
            }


            @Override
            protected void populateViewHolder(ProductBGMHolder viewHolder, Promotion model, int position) {
                viewHolder.buyName.setText(model.getPromotionBuyName());
                viewHolder.getName.setText(model.getPromotionGetName());
                viewHolder.getQuantity.setText(model.getPromotionGetQuantity());
                viewHolder.buyQuantity.setText(model.getPromotionBuyQuantity());
            }
        };

        listBGM.setAdapter(adapterProductBGM);
        adapterProductBGM.notifyDataSetChanged();

        Button btnOrderDis = findViewById(R.id.btn_program_order_discount_create);
        Button btnProductDisAdd = findViewById(R.id.btn_program_product_discount);
        Button btnBGM = findViewById(R.id.btn_program_bgm);

        Button btnDone = findViewById(R.id.btn_program_done);

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);

                final String name = edtProgramName.getText().toString();
                final String start = edtProgramStart.getText().toString();
                final String end = edtProgramEnd.getText().toString();

                if(TextUtils.isEmpty(name) || TextUtils.isEmpty(start) || TextUtils.isEmpty(end)){
                    Toast.makeText(getApplicationContext(), "Vui lòng nhập tên chương trình, ngày bắt đầu và kết thúc!", Toast.LENGTH_LONG).show();

                }else{

                    if(orderDis || productDis || bgm ){
                        if(orderDis){
                            refPromotionMan.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild("orderDiscount")){
                                        Promotion promotion = new Promotion(name,start,end);
                                        Map<String, Object> postValues = promotion.toMap();
                                        //Map<String, Object> childUpdates = new HashMap<>();
                                        //childUpdates.put("/",postValues);
                                        refPromotionMan.updateChildren(postValues).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Intent it = new Intent(getApplicationContext(),PromotionList.class);
                                                it.putExtra("EmailLogin",emailLogin);
                                                it.putExtra("Admin",true);
                                                startActivity(it);
                                            }
                                        });
                                    }else{
                                        Toast.makeText(getApplicationContext(), "Vui lòng khởi tạo chương trình chiết khấu đơn hàng!", Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        if(productDis){
                            refPromotionMan.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild("ProductDiscount")){
                                        Promotion promotion = new Promotion(name,start,end);
                                        Map<String, Object> postValues = promotion.toMap();
                                        //Map<String, Object> childUpdates = new HashMap<>();
                                        //childUpdates.put("/",postValues);
                                        refPromotionMan.updateChildren(postValues).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Intent it = new Intent(getApplicationContext(),PromotionList.class);
                                                it.putExtra("EmailLogin",emailLogin);
                                                it.putExtra("Admin",true);
                                                startActivity(it);
                                            }
                                        });
                                    }else{
                                        Toast.makeText(getApplicationContext(), "Vui lòng khởi tạo chương trình chiết khấu theo sản phẩm!", Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        if(bgm){
                            refPromotionMan.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild("BGM")){
                                        Promotion promotion = new Promotion(name,start,end);
                                        Map<String, Object> postValues = promotion.toMap();
                                        //Map<String, Object> childUpdates = new HashMap<>();
                                        //childUpdates.put("/",postValues);
                                        refPromotionMan.updateChildren(postValues).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Intent it = new Intent(getApplicationContext(),PromotionList.class);
                                                it.putExtra("EmailLogin",emailLogin);
                                                it.putExtra("Admin",true);
                                                startActivity(it);
                                            }
                                        });
                                    }else{
                                        Toast.makeText(getApplicationContext(), "Vui lòng khởi tạo chương trình tặng kèm!", Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }



                    }else{
                        Toast.makeText(getApplicationContext(), "Chưa có chương trình nào được khởi tạo!", Toast.LENGTH_LONG).show();

                    }

                }
            }
        });

        btnOrderDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);

                String orderDis = edtOrderDis.getText().toString();
                if(TextUtils.isEmpty(orderDis)){
                    Toast.makeText(getApplicationContext(), "Vui lòng nhập mức CK!", Toast.LENGTH_LONG).show();

                }else{
                    edtOrderDis.setText("");
                    tvOrderDis.setText(orderDis+"%");
                    refDatabase.child(emailLogin).child("PromotionMan").child(promotionKey).child("orderDiscount").setValue(orderDis);


                    Toast.makeText(getApplicationContext(), "Đã khởi tạo!", Toast.LENGTH_LONG).show();

                }
            }
        });


        btnProductDisAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);

                String productDis = edtProductDis.getText().toString();

                if(choosenProductDis != null){
                    if(TextUtils.isEmpty(productDis)){
                        Toast.makeText(getApplicationContext(), "Vui lòng nhập chiết khấu!", Toast.LENGTH_LONG).show();
                    }else{
                        edtProductDis.setText("");
                        Product pDis = new Product(choosenProductDis,productDis);
                        refPromotionMan.child("ProductDiscount").push().setValue(pDis).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                listProductDiscount.setVisibility(View.VISIBLE);
                            }
                        });
                    }

                }else{
                    Toast.makeText(getApplicationContext(), "Vui lòng chọn sản phẩm!", Toast.LENGTH_LONG).show();
                }


            }
        });

        btnBGM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);

                if (orderDis )
                    csOrderDis.setVisibility(View.VISIBLE);
                if(productDis)
                    csProductDis.setVisibility(View.VISIBLE);

                String buyQuantity = edtBuyQuantity.getText().toString();
                String getQuantity = edtGetQuantity.getText().toString();

                if(choosenProductBuy!=null && choosenProductGet!=null && choosenProductGetCode!=null){
                    if(TextUtils.isEmpty(buyQuantity) || TextUtils.isEmpty(getQuantity)){
                        Toast.makeText(getApplicationContext(), "Vui lòng nhập đủ số lượng mua và tặng",Toast.LENGTH_LONG).show();
                    }else{
                        Promotion promotion = new Promotion(choosenProductBuy,choosenProductGet,buyQuantity,getQuantity,choosenProductGetCode);
                        refPromotionMan.child("BGM").push().setValue(promotion).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                listBGM.setVisibility(View.VISIBLE);

                            }
                        });
                    }

                }else{
                    Toast.makeText(getApplicationContext(), "Vui lòng chọn đầy đủ 2 sản phẩm!", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView name,discount,quantity;

        public ProductViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.tv_item_product_dis_name);
            discount = itemView.findViewById(R.id.tv_item_product_dis_discount);

        }
    }

    public static class ProductBGMHolder extends RecyclerView.ViewHolder {
        TextView buyName,getName,buyQuantity,getQuantity;

        public ProductBGMHolder(View itemView) {
            super(itemView);
            buyName = (TextView) itemView.findViewById(R.id.tv_item_bgm_buy);
            getName = itemView.findViewById(R.id.tv_bgm_get);
            buyQuantity = itemView.findViewById(R.id.tv_bgm_buy_quantity);
            getQuantity = itemView.findViewById(R.id.tv_bgm_get_quantity);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        refPromotionMan.setValue(null);
    }
}
