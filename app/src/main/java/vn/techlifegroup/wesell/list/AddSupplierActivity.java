package vn.techlifegroup.wesell.list;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.bytask.warehouse.WarehouseManActivity;
import vn.techlifegroup.wesell.model.Supplier;
import vn.techlifegroup.wesell.utils.Constants;

public class AddSupplierActivity extends AppCompatActivity {
    private EditText edtSupplierName;
    private EditText edtSupplierProduct;
    private Button btnAddSupplier;
    private String emailLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_supplier);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_add_supplier);
        setSupportActionBar(toolbar);

        edtSupplierName = (EditText)findViewById(R.id.edt_add_supplier_supplier_name);
        edtSupplierProduct = (EditText)findViewById(R.id.edt_add_supplier_supplier_product);
        btnAddSupplier = (Button)findViewById(R.id.btn_add_supplier_add);

        Intent intent = this.getIntent();
        emailLogin = intent.getStringExtra("EmailLogin");

        btnAddSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String supplierName = edtSupplierName.getText().toString();
                final String supplierProduct = edtSupplierProduct.getText().toString();
                if(TextUtils.isEmpty(supplierName)){
                    Toast.makeText(getApplicationContext(),"Vui lòng nhập tên NCC", Toast.LENGTH_LONG).show();
                }else if(TextUtils.isEmpty(supplierProduct)){
                    Toast.makeText(getApplicationContext(),"Vui lòng nhập sản phẩm của NCC", Toast.LENGTH_LONG).show();
                }else{
                    Supplier supplier = new Supplier(supplierName,supplierProduct);
                    Constants.refDatabase.child(emailLogin+"/Supplier").child(supplierName).setValue(supplier);

                    Intent it = new Intent(getApplicationContext(), WarehouseManActivity.class);
                    it.putExtra("EmailLogin",emailLogin);
                    startActivity(it);
                }
            }
        });

    }
}
