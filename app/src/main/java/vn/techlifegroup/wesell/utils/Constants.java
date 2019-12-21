package vn.techlifegroup.wesell.utils;

import android.view.animation.AlphaAnimation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by toila on 26/12/2016.
 */
public class Constants {

    public static String userUid = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",");
    public static DatabaseReference refUserUid = FirebaseDatabase.getInstance().getReference().child(userUid);

    public static DatabaseReference refDatabase = FirebaseDatabase.getInstance().getReference();
    public static AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);

    public static DatabaseReference refLogin = FirebaseDatabase.getInstance().getReference().child("Login");
    public static DatabaseReference refRole= FirebaseDatabase.getInstance().getReference().child("Role");

    public static DatabaseReference refProductGroup = FirebaseDatabase.getInstance().getReference().child("ProductGroup");
    public static DatabaseReference refProductListByGroup = FirebaseDatabase.getInstance().getReference().child("ProductListByGroup");
    public static DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference().child("Users");


    public static DatabaseReference refOrder = FirebaseDatabase.getInstance().getReference().child("Order");
    public static DatabaseReference refOrderList = FirebaseDatabase.getInstance().getReference().child("OrderList");

    public static DatabaseReference refProductPOS= FirebaseDatabase.getInstance().getReference().child("Z_POS_Product");
    public static DatabaseReference refBillPOS= FirebaseDatabase.getInstance().getReference().child("Z_POS_Bill");
    public static DatabaseReference refEmployeeBill= FirebaseDatabase.getInstance().getReference().child("Z_POS_EmployeeBill");


    public static DatabaseReference refShopPOS= FirebaseDatabase.getInstance().getReference().child("Z_POS_Shop");
    public static DatabaseReference refBillByTime= FirebaseDatabase.getInstance().getReference().child("SaleMan").child("Z_POS_BillByTime");
    public static DatabaseReference refBillTotalByTime= FirebaseDatabase.getInstance().getReference().child("SaleMan").child("Z_POS_BillTotalByTime");
    public static DatabaseReference refBillTotalByTimeTest= FirebaseDatabase.getInstance().getReference().child("SaleMan").child("Z_POS_BillTotalByTimeTest");

    public static DatabaseReference refBillTotalHistory= FirebaseDatabase.getInstance().getReference().child("SaleMan").child("Z_POS_BillTotalHistory");
    public static DatabaseReference refPOSProductSale= FirebaseDatabase.getInstance().getReference().child("SaleMan").child("Z_POS_ProductSale");
    public static DatabaseReference refPOSPromotionQuantity= FirebaseDatabase.getInstance().getReference().child("SaleMan").child("Z_POS_PromotionQuantity");

    public static DatabaseReference refPOSProductSaleHistory= FirebaseDatabase.getInstance().getReference().child("SaleMan").child("Z_POS_ProductSaleHistory");

    public static DatabaseReference refPOSBillCheck= FirebaseDatabase.getInstance().getReference().child("Z_POS_BillCheck");
    public static DatabaseReference refPOSShopCashOutCheck= FirebaseDatabase.getInstance().getReference().child("Z_POS_ShopCashOutCheck");
    public static DatabaseReference refPOSShopCashOutHistory= FirebaseDatabase.getInstance().getReference().child("Z_POS_ShopCashOutHistory");


    public static DatabaseReference refPOSShopCashFirst= FirebaseDatabase.getInstance().getReference().child("Z_POS_ShopCashFirst");

    public static StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    public static DatabaseReference refClient = FirebaseDatabase.getInstance().getReference().child("Client");
    public static DatabaseReference refClientMan = FirebaseDatabase.getInstance().getReference().child("ClientMan");
    public static DatabaseReference refSupplier = FirebaseDatabase.getInstance().getReference().child("Supplier");
    public static DatabaseReference refEmployee = FirebaseDatabase.getInstance().getReference().child("Employee");
    public static DatabaseReference refEmployees = FirebaseDatabase.getInstance().getReference().child("Employees");
    public static DatabaseReference refWarehouseMan = FirebaseDatabase.getInstance().getReference().child("WarehouseMan");

    public static DatabaseReference refStorage = FirebaseDatabase.getInstance().getReference().child("Storage");


}
