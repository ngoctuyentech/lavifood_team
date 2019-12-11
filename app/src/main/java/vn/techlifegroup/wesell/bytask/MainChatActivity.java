package vn.techlifegroup.wesell.bytask;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.model.Chat;
import vn.techlifegroup.wesell.model.Product;
import vn.techlifegroup.wesell.utils.Utils;

import static vn.techlifegroup.wesell.utils.Constants.refDatabase;
import static vn.techlifegroup.wesell.utils.Constants.refEmployees;
import static vn.techlifegroup.wesell.utils.Constants.refUsers;

public class MainChatActivity extends AppCompatActivity {

    private FirebaseRecyclerAdapter<Chat, ChatBoxViewHolder> adapterFirebaseChatBox;
    //private FirebaseRecyclerAdapter<Product, ProductSameViewHolder> adapterFirebaseRecentlyOrderDia;


    public static String saleManEmail;
    //public static DatabaseReference refUserUid = refDatabase.child("Employees").child(saleManEmail);

    private String idFr;

    private String todayDate, lastDateChat;
    private String groupChoose, productChoose,keyOrderTemp,chosenProductPrice,finalPayment,discount;

    private ArrayAdapter<String> adpGroup,adpProduct;

    private ImageView ivLogo, ivMesSend, ivMesCart, ivMesReact, ivMesKeyboard, ivMesVoice;
    private TextView tvName, tvProper,tvRole;
    private TextView tvPoint,tvDiscount,tvTotal;


    private RecyclerView rvChatbox;
    private EditText edtMessage;

    private LinearLayoutManager linearLayoutManager;

    public static final int TYPE_DATE = 2;
    public static final int TYPE_GENERAL_RIGHT = 0;
    public static final int TYPE_GENERAL_LEFT = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_chat);

        android.support.v7.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_chat_box);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        saleManEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",");

        Intent it = this.getIntent();

        idFr = it.getStringExtra("ID Friend");

        rvChatbox     = findViewById(R.id.rv_chatbox);
        edtMessage    = findViewById(R.id.edt_mes);

        ivLogo        = findViewById(R.id.iv_logo);
        ivMesSend     = findViewById(R.id.iv_mes_send);
        ivMesCart      = findViewById(R.id.iv_mes_cart);
        ivMesKeyboard    = findViewById(R.id.iv_mes_keyboard);

        tvName        = findViewById(R.id.tv_name);
        tvRole = findViewById(R.id.tv_account_role);

        edtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                ivMesSend.setVisibility(View.GONE);
                ivMesCart.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(edtMessage.length() != 0){
                    ivMesSend.setVisibility(View.VISIBLE);
                    ivMesCart.setVisibility(View.GONE);
                }else {
                    ivMesSend.setVisibility(View.GONE);
                    ivMesCart.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        ivMesSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String messageInput = edtMessage.getText().toString();
                final String timeStamp = (Calendar.getInstance().getTime().getTime())+"";

                final Chat chatPushContent = new Chat(saleManEmail, messageInput, timeStamp);

                final Chat chatPushDate = new Chat("true", timeStamp);


                final Query lastChat =  refEmployees.child(saleManEmail).child("chat").child(idFr)
                        .limitToLast(1);

                lastChat.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Iterable <DataSnapshot> snapChat = dataSnapshot.getChildren();
                        for (DataSnapshot itemChat : snapChat) {

                            Chat chat = itemChat.getValue(Chat.class);

                            lastDateChat = Utils.getDateFromTimeStamp(Long.parseLong(chat.getTimeStamp()));

                            long timeNow = (long) (System.currentTimeMillis());

                            todayDate = DateFormat.format("dd/MM/yyyy", timeNow)+"";

                            if(todayDate.equals(lastDateChat)){

                                refEmployees.child(saleManEmail).child("chat").child(idFr).push().setValue(chatPushContent);
                                refUsers.child(idFr).child("chat").child(saleManEmail).push().setValue(chatPushContent);

                                refEmployees.child(saleManEmail).child("friend").child(idFr).child("lastContent").setValue(messageInput);
                                refEmployees.child(saleManEmail).child("friend").child(idFr).child("lastTimeStamp").setValue(timeStamp);
                                refEmployees.child(saleManEmail).child("friend").child(idFr).child("isRead").setValue(true);

                                refUsers.child(idFr).child("friend").child(saleManEmail).child("lastContent").setValue(messageInput);
                                refUsers.child(idFr).child("friend").child(saleManEmail).child("lastTimeStamp").setValue(timeStamp);
                                refUsers.child(idFr).child("friend").child(saleManEmail).child("isRead").setValue(false);


                                rvChatbox.scrollToPosition(linearLayoutManager.getItemCount() - 1);


                            }else {
                                refEmployees.child(saleManEmail).child("chat").child(idFr).push().setValue(chatPushDate);
                                refUsers.child(idFr).child("chat").child(saleManEmail).push().setValue(chatPushDate);

                                refEmployees.child(saleManEmail).child("chat").child(idFr).push().setValue(chatPushContent);
                                refUsers.child(idFr).child("chat").child(saleManEmail).push().setValue(chatPushContent);

                                refEmployees.child(saleManEmail).child("friend").child(idFr).child("lastContent").setValue(messageInput);
                                refEmployees.child(saleManEmail).child("friend").child(idFr).child("lastTimeStamp").setValue(timeStamp);
                                refEmployees.child(saleManEmail).child("friend").child(idFr).child("isRead").setValue(true);

                                refUsers.child(idFr).child("friend").child(saleManEmail).child("lastContent").setValue(messageInput);
                                refUsers.child(idFr).child("friend").child(saleManEmail).child("lastTimeStamp").setValue(timeStamp);
                                refUsers.child(idFr).child("friend").child(saleManEmail).child("isRead").setValue(false);

                                rvChatbox.scrollToPosition(linearLayoutManager.getItemCount() - 1);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                 });

                edtMessage.setText(null);

            }
        });

        refEmployees.child(saleManEmail).child("friend").child(idFr).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {

                Chat chatImage = dataSnapshot.getValue(Chat.class);

                //Glide.with(getApplicationContext()).load(chatImage.getImageFr()).apply(RequestOptions.circleCropTransform()).into(ivLogo);

                tvName.setText(chatImage.getNameFr());
                tvRole.setText(chatImage.getRoleFr());

                Glide.with(getApplicationContext()).load(chatImage.getImageFr()).into(ivLogo);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        adapterFirebaseChatBox = new FirebaseRecyclerAdapter<Chat, ChatBoxViewHolder>(
                Chat.class,
                R.layout.item_chat_right,
                ChatBoxViewHolder.class,
                refEmployees.child(saleManEmail).child("chat").child(idFr)
        ) {

            @Override
            public int getItemViewType(int position) {
                Chat chat = getItem(position);

                String id = chat.getId();

                String isDate = chat.getIsDate();

                if (isDate == null) {

                    if (id.equals(saleManEmail)) {
                        return TYPE_GENERAL_RIGHT;

                    } else {
                        return TYPE_GENERAL_LEFT;
                    }

                } else {
                    return TYPE_DATE;
                }
            }

            @Override
            public void onBindViewHolder(@NonNull ChatBoxViewHolder holder, int position, @NonNull List<Object> payloads) {

                super.onBindViewHolder(holder, position, payloads);
            }


            @Override
            public ChatBoxViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                if (viewType == TYPE_DATE) {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_date, parent, false);
                    return new ChatBoxViewHolder(v);

                }else if (viewType == TYPE_GENERAL_RIGHT) {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_right, parent, false);
                    return new ChatBoxViewHolder(v);

                }else {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_left, parent, false);
                    return new ChatBoxViewHolder(v);
                }
            }

            @SuppressLint("ResourceAsColor")
            @Override
            protected void populateViewHolder(final ChatBoxViewHolder viewHolder, final Chat model, int position) {

                Chat chat = getItem(position);

                String isDate = chat.getIsDate();

                if (isDate == null) {

                    viewHolder.tvContent.setText(model.getContent());
                    viewHolder.tvTimeS.setText(Utils.getHourFromTimeStamp(Long.parseLong(model.getTimeStamp())));

                } else {
                    viewHolder.tvTimeS.setText(Utils.getDateFromTimeStamp(Long.parseLong(model.getTimeStamp())));

                }
            }
        };

        rvChatbox.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(this);
        //linearLayoutManager.setReverseLayout(true);

        rvChatbox.setLayoutManager(linearLayoutManager);
        rvChatbox.setAdapter(adapterFirebaseChatBox);

        adapterFirebaseChatBox.notifyDataSetChanged();

        adapterFirebaseChatBox.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                linearLayoutManager.smoothScrollToPosition(rvChatbox, null, linearLayoutManager.getItemCount());
            }
        });

        rvChatbox.scrollToPosition(linearLayoutManager.getItemCount() - 1);

        ivMesCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                rvChatbox.scrollToPosition(linearLayoutManager.getItemCount() - 1);
                Toast.makeText(getApplicationContext(), "tao don hang", Toast.LENGTH_SHORT).show();

                //dialogOrder();

            }
        });



    }
/*
    private void dialogOrder(){

        keyOrderTemp = refUsers.child(userUid).child("OrderTemp").push().getKey();

        final Dialog dialog = new Dialog(MainChatActivity.this, R.style.FullWidth_Dialog);
        dialog.setContentView(R.layout.dialog_order_recently);
        dialog.show();

        Button btnSendOrder = dialog.findViewById(R.id.btn_send_order_dia_recently);
        Button btnCancel = dialog.findViewById(R.id.btn_same_order_cancel);

        final EditText edtInQuantity = dialog.findViewById(R.id.edt_add_quantity_product_order_recently);

        final Button   btnAdd        = dialog.findViewById(R.id.btn_add_product_o_r_d);

        final TextView tvAddPrice = dialog.findViewById(R.id.tv_same_order_add_price);
        final TextView tvAddTotal = dialog.findViewById(R.id.tv_same_order_add_total);
                       tvDiscount = dialog.findViewById(R.id.tv_discount_dia_recently);
                       tvTotal    = dialog.findViewById(R.id.tv_total_dia_recently);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                refUsers.child(userUid).child("OrderTemp").child(keyOrderTemp).setValue(null);
                dialog.dismiss();
            }
        });

        RecyclerView rvRecentlyOrder = dialog.findViewById(R.id.rv_order_new_dia_recently);
        rvRecentlyOrder.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManagerRecentlyOrder = new LinearLayoutManager(getApplicationContext());
        rvRecentlyOrder.setLayoutManager(linearLayoutManagerRecentlyOrder);

        adapterFirebaseRecentlyOrderDia = new FirebaseRecyclerAdapter<Product, ProductSameViewHolder>(
                Product.class,
                R.layout.item_product_same_order,
                ProductSameViewHolder.class,
                refUsers.child(userUid).child("OrderTemp").child(keyOrderTemp).child("ProductList")                    ) {
            @Override
            public ProductSameViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_same_order, parent, false);
                return new ProductSameViewHolder(v);
            }

            @Override
            protected void populateViewHolder(final ProductSameViewHolder viewHolder, final Product model, int position) {

                Float quantity = Float.parseFloat(model.getProductQuantity());
                Float price = Float.parseFloat(model.getProductPrice());
                Float total = Float.parseFloat(model.getProductTotal());

                viewHolder.productNameDia.setText(model.getProductName());
                viewHolder.productQuantityDia.setText(convertNumber(quantity+""));
                viewHolder.productPriceDia.setText(convertNumber(price+""));
                viewHolder.productTotalDia.setText(convertNumber(total+""));

            }
        };

        rvRecentlyOrder.setAdapter(adapterFirebaseRecentlyOrderDia);
        adapterFirebaseRecentlyOrderDia.notifyDataSetChanged();

        btnSendOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                refUserUid.child("OrderTemp").child(keyOrderTemp).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String timeStamp = Calendar.getInstance().getTime().getTime()+"";

                        final Order order = new Order(userUid, "", "", finalPayment, timeStamp, shop, "", "Tiền mặt", "", "0");

                        VatModel vatModel = new VatModel("0", finalPayment, finalPayment, timeStamp);
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("ProductList", dataSnapshot.child("ProductList").getValue());
                        childUpdates.put("VAT", vatModel);

                        childUpdates.put("OtherInformation",order.toMap());

                        refOrderList.child(keyOrderTemp).updateChildren(childUpdates);

                        Order orderShort = new Order(finalPayment, timeStamp, shop);

                        Map<String, Object> childUpdatesOther = new HashMap<>();

                        childUpdatesOther.put("Users/" + userUid + "/Order/Waiting/" + keyOrderTemp,orderShort);
                        childUpdatesOther.put("Order/FromApp/" + keyOrderTemp,orderShort);

                        refDatabase.updateChildren(childUpdatesOther);

                        dialog.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        final Spinner spinChooseGroup   = dialog.findViewById(R.id.spin_choose_group);
        final Spinner spinChooseProduct = dialog.findViewById(R.id.spin_choose_product);

        final List<String> listGroup = new ArrayList<String>();
        listGroup.add("Chọn nhóm");


        refProductListByGroup.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    Iterable<DataSnapshot> catSnap = dataSnapshot.getChildren();
                    for(DataSnapshot itemCat:catSnap){
                        String storeGroupName = itemCat.getKey();
                        listGroup.add(storeGroupName);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //add listGroup

        adpGroup = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_list_item_1, listGroup);
        adpGroup.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinChooseGroup.setAdapter(adpGroup);

        spinChooseGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                groupChoose = (String) parent.getItemAtPosition(position);

                final List<String> listProduct = new ArrayList<String>();

                final List<String> listProductContains = new ArrayList<String>();

                listProduct.clear();
                listProduct.add("Chọn sản phẩm");

                refProductListByGroup.child(groupChoose).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChildren()){
                            Iterable<DataSnapshot> catSnap = dataSnapshot.getChildren();
                            for(DataSnapshot itemCat:catSnap){
                                String storeName = itemCat.getKey();
                                listProduct.add(storeName);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                //add listProduct

                adpProduct = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_list_item_1, listProduct);
                adpProduct.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinChooseProduct.setAdapter(adpProduct);

                spinChooseProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {

                        productChoose = (String) parent.getItemAtPosition(position);

                        tvAddPrice.setText(null);
                        tvAddTotal.setText(null);
                        edtInQuantity.setText(null);

                        refUsers.child(userUid).child("OrderTemp").child(keyOrderTemp).child("ProductList")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChildren()) {
                                            Iterable<DataSnapshot> orderSnap = dataSnapshot.getChildren();
                                            for (DataSnapshot itemOrder : orderSnap) {

                                                Product productContains = itemOrder.getValue(Product.class);

                                                final String productName = productContains.getProductName();
                                                listProductContains.add(productName);

                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                        //add listProductContains

                        if(position != 0){

                            if (listProductContains.contains(productChoose)) {

                                Toast.makeText(getApplicationContext(), "Sản phẩm đã có", Toast.LENGTH_SHORT).show();
                                spinChooseProduct.setSelection(0);
                                tvAddPrice.setText(null);
                                edtInQuantity.setText(null);
                                tvAddTotal.setText(null);


                            }
                            else{

                                refDatabase.child("ProductList").child(productChoose).child("productPrice").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        chosenProductPrice = dataSnapshot.getValue().toString();
                                        tvAddPrice.setText(Utils.convertNumber(chosenProductPrice));

                                        //String inQuantity = edtInQuantity.getText().toString();
                                        //float addTotal = Float.parseFloat(chosenProductPrice) * Float.parseFloat(inQuantity);

                                        //tvAddTotal.setText(Utils.convertNumber(addTotal + ""));
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
                            }
                        }else{
                            tvAddPrice.setText(null);
                            tvAddTotal.setText(null);
                            edtInQuantity.setText(null);
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        edtInQuantity.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    String quantity = edtInQuantity.getText().toString();
                    if(TextUtils.isEmpty(quantity)){
                        Toast.makeText(getApplicationContext(), "Vui lòng nhập số lượng", Toast.LENGTH_LONG).show();

                    }else {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(edtInQuantity.getWindowToken(), 0);

                        if(productChoose.equals("Chọn sản phẩm")){
                            Toast.makeText(getApplicationContext(), "Vui lòng chọn sản phẩm!", Toast.LENGTH_LONG).show();
                        }else{
                            float addTotal = Float.parseFloat(chosenProductPrice) * Float.parseFloat(quantity);
                            tvAddTotal.setText(convertNumber(addTotal+""));
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(productChoose.equals("Chọn sản phẩm")){
                    Toast.makeText(getApplicationContext(), "Vui lòng chọn sản phẩm", Toast.LENGTH_LONG).show();
                }else{
                    final String keyProduct = refUsers.child(userUid).child("OrderTemp").child(keyOrderTemp).child("ProductList").push().getKey();

                    final String inQuantity = edtInQuantity.getText().toString();

                    if(TextUtils.isEmpty(inQuantity)){
                        Toast.makeText(getApplicationContext(), "Vui lòng nhập số lượng", Toast.LENGTH_SHORT).show();
                    }else{

                        float productTotal = Float.parseFloat(inQuantity) * Float.parseFloat(chosenProductPrice);

                        Product addP = new Product(productChoose, inQuantity, chosenProductPrice, productTotal+"");

                        refUserUid.child("OrderTemp").child(keyOrderTemp).child("ProductList").child(keyProduct).setValue(addP).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                refUserUid.child("OrderTemp").child(keyOrderTemp).child("ProductList").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Iterable<DataSnapshot> snapP = dataSnapshot.getChildren();
                                        float orderTotal = 0;
                                        for (DataSnapshot itemSnap:snapP){
                                            Product currentP = itemSnap.getValue(Product.class);
                                            orderTotal += Float.parseFloat(currentP.getProductTotal());
                                            finalPayment = orderTotal+"";

                                            tvTotal.setText(Utils.convertNumber(orderTotal+""));
                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        });

                        spinChooseGroup.setSelection(0);
                        spinChooseProduct.setSelection(0);
                        tvAddPrice.setText(null);
                        tvAddTotal.setText(null);
                        edtInQuantity.setText(null);

                    }
                }
            }
        });
    }

 */
//dialogOrder

    private class ChatBoxViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvTimeS;


        public ChatBoxViewHolder(View itemView) {
            super(itemView);

            tvContent = itemView.findViewById(R.id.tv_content);
            tvTimeS = itemView.findViewById(R.id.tv_timeS);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getLayoutPosition();
                    Toast.makeText(getApplicationContext(), position+"", Toast.LENGTH_SHORT).show();

                }
            });
        }
    }
/*
    public class ProductSameViewHolder extends RecyclerView.ViewHolder {
        TextView productNameDia;
        TextView productPriceDia;
        TextView productTotalDia;

        EditText productQuantityDia;

        TextView productNameAct;
        TextView productPriceAct;
        TextView productTotalAct;
        TextView productQuantityAct;


        public ProductSameViewHolder(View itemView) {
            super(itemView);

            productNameDia = itemView.findViewById(R.id.tv_name_product_order_recently_dialog);
            productPriceDia = itemView.findViewById(R.id.tv_price_product_order_recently_dialog);
            productTotalDia = itemView.findViewById(R.id.tv_total_money_order_recently_dialog);
            productQuantityDia = itemView.findViewById(R.id.edt_quantity_product_item_order_recently_dialog);

            productNameAct = itemView.findViewById(R.id.tv_name_product);
            productPriceAct = itemView.findViewById(R.id.tv_price_product);
            productTotalAct = itemView.findViewById(R.id.tv_total_money);
            productQuantityAct = itemView.findViewById(R.id.tv_quantity_product);


            productQuantityDia.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    int position = getAdapterPosition();
                    Product clickedP = adapterFirebaseRecentlyOrderDia.getItem(position);
                    float productPrice = Float.parseFloat(clickedP.getProductPrice());
                    String productName = clickedP.getProductName();
                    String productKey = adapterFirebaseRecentlyOrderDia.getRef(position).getKey();

                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        //Toast.makeText(getApplicationContext(), "ke", Toast.LENGTH_LONG).show();
                        String quantity = productQuantityDia.getText().toString();
                        if(TextUtils.isEmpty(quantity)){
                            Toast.makeText(getApplicationContext(), "Vui lòng nhập số lượng", Toast.LENGTH_LONG).show();

                        }else{
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(productQuantityDia.getWindowToken(), 0);

                            float productTotal = productPrice * Float.parseFloat(quantity);
                            Product updateP = new Product(productName, quantity, productPrice+"", productTotal+"");
                            refUserUid.child("OrderTemp").child(keyOrderTemp).child("ProductList").child(productKey).setValue(updateP).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    refUserUid.child("OrderTemp").child(keyOrderTemp).child("ProductList").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Iterable<DataSnapshot> snapP = dataSnapshot.getChildren();
                                            float orderTotal = 0;
                                            for (DataSnapshot itemSnap:snapP){
                                                Product currentP = itemSnap.getValue(Product.class);
                                                orderTotal += Float.parseFloat(currentP.getProductTotal());
                                                finalPayment = orderTotal +"";

                                                tvTotal.setText(Utils.convertNumber(orderTotal+""));
                                            }

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            });
                        }
                        return true;
                    }
                    return false;
                }
            });
        }

    }

 */
//ProductSameViewHolder

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

}
