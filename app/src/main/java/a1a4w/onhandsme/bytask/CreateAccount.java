package a1a4w.onhandsme.bytask;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import a1a4w.onhandsme.MainActivity;
import a1a4w.onhandsme.R;
import a1a4w.onhandsme.model.Employee;

import static a1a4w.onhandsme.utils.Constants.buttonClick;
import static a1a4w.onhandsme.utils.Constants.refDatabase;

public class CreateAccount extends AppCompatActivity {
    String accType,managerEmail,userEmail;
    private ArrayAdapter<String> adpProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        Intent it = this.getIntent();
        final String emailLogin = it.getStringExtra("EmailLogin");

        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");

        final Spinner spinAccType = findViewById(R.id.spin_create_acc);
        final EditText edtEmail = findViewById(R.id.edt_create_acc_email);
        final EditText edtPass = findViewById(R.id.edt_create_acc_pass);
        final EditText edtName = findViewById(R.id.edt_create_acc_name);
        final EditText edtAddress = findViewById(R.id.edt_create_acc_address);
        final EditText edtPhone = findViewById(R.id.edt_create_acc_phone);
        final Spinner spinManagedBy = findViewById(R.id.spin_create_account_managedBy);
        spinManagedBy.setEnabled(false);

        spinAccType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(position != 0){

                    accType = (String) parent.getItemAtPosition(position);
                    spinManagedBy.setEnabled(true);
                    final List<String> listName = new ArrayList<>();
                    listName.add("Email quản lý");
                    //Toast.makeText(getApplicationContext(),"here", Toast.LENGTH_LONG).show();
                    if(accType.equals("Bán hàng")){

                        refDatabase.child(emailLogin).child("SaleManBySup").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Iterable<DataSnapshot> snapSup = dataSnapshot.getChildren();
                                for(DataSnapshot itemSup:snapSup){

                                    listName.add(itemSup.getKey().replace(",","."));

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        adpProduct = new ArrayAdapter<String>(getApplicationContext(),
                                android.R.layout.simple_list_item_1, listName);
                        adpProduct.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        spinManagedBy.setAdapter(adpProduct);
                    }

                    if(accType.equals("Giám sát")){

                        refDatabase.child(emailLogin).child("SupByASM").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Iterable<DataSnapshot> snapSup = dataSnapshot.getChildren();
                                for(DataSnapshot itemSup:snapSup){

                                    listName.add(itemSup.getKey().replace(",","."));

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        adpProduct = new ArrayAdapter<String>(getApplicationContext(),
                                android.R.layout.simple_list_item_1, listName);
                        adpProduct.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        spinManagedBy.setAdapter(adpProduct);
                    }

                    if(accType.equals("ASM")){

                        refDatabase.child(emailLogin).child("ASMByRSM").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Iterable<DataSnapshot> snapSup = dataSnapshot.getChildren();
                                for(DataSnapshot itemSup:snapSup){

                                    listName.add(itemSup.getKey().replace(",","."));

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        adpProduct = new ArrayAdapter<String>(getApplicationContext(),
                                android.R.layout.simple_list_item_1, listName);
                        adpProduct.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        spinManagedBy.setAdapter(adpProduct);
                    }

                    if(accType.equals("RSM")){

                        listName.add(userEmail);


                        adpProduct = new ArrayAdapter<String>(getApplicationContext(),
                                android.R.layout.simple_list_item_1, listName);
                        adpProduct.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        spinManagedBy.setAdapter(adpProduct);
                    }


                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinManagedBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) spinManagedBy.getSelectedView()).setTextColor(getResources().getColor(android.R.color.black));

                if(position != 0){
                    managerEmail = (String) parent.getItemAtPosition(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
       Button btnDone = findViewById(R.id.btn_create_acc);

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);

                final String email = edtEmail.getText().toString();
                final String pass = edtPass.getText().toString();
                final String name = edtName.getText().toString();
                final String address = edtAddress.getText().toString();
                final String phone = edtPhone.getText().toString();

                if(accType == null){
                    Toast.makeText(getApplicationContext(), "Vui lòng chọn cấp bậc!", Toast.LENGTH_LONG).show();

                }else if(managerEmail==null){
                    Toast.makeText(getApplicationContext(), "Vui lòng chọn email quản lý!", Toast.LENGTH_LONG).show();

              }else

                if(TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)|| TextUtils.isEmpty(name)|| TextUtils.isEmpty(address)|| TextUtils.isEmpty(phone)){
                    Toast.makeText(getApplicationContext(), "Vui lòng nhập đầy đủ!", Toast.LENGTH_LONG).show();

                }else{
                    if(accType.equals("Bán hàng")){
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()){
                                            final Employee addEmployee = new Employee(name,address,phone,email.replace(".", ","),pass,managerEmail.replace(".",","));

                                            refDatabase.child("1-System/Login").child(email.replace(".",",")).setValue(emailLogin);
                                            refDatabase.child("1-System/Role").child(emailLogin).child(email.replace(".",",")).setValue("SaleMan");

                                            refDatabase.child(emailLogin).child("Employee").child(email.replace(".",",")).setValue(addEmployee).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(getApplicationContext(), "Đã tạo TK thành công", Toast.LENGTH_LONG).show();
                                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));

                                                }
                                            });

                                            refDatabase.child(emailLogin).child("SaleManBySup").child(managerEmail.replace(".",",")).child("Tất cả").child(email.replace(".",",")).setValue(addEmployee);

                                            refDatabase.child(emailLogin).child("SaleManBySup").child(managerEmail.replace(".",",")).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if(!dataSnapshot.hasChild("Group")){
                                                        refDatabase.child(emailLogin).child("SaleManBySup").child(managerEmail.replace(".",",")).child("Group").push().child("groupName").setValue("Tất cả");
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                            //FirebaseAuth.getInstance().signOut();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(CreateAccount.this);
                                builder.setMessage("Lỗi tạo tài khoản mới, vui lòng không sử dụng trùng lặp email!");

                            }
                        });
                    }

                    if(accType.equals("Giám sát")){
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()){
                                            final Employee addEmployee = new Employee(name,address,phone,email.replace(".", ","),pass,managerEmail.replace(".",","));

                                            refDatabase.child("1-System/Login").child(email.replace(".",",")).setValue(emailLogin);
                                            refDatabase.child("1-System/Role").child(emailLogin).child(email.replace(".",",")).setValue("Supervisor");
                                            refDatabase.child(emailLogin).child("SaleManBySup").child(email.replace(".",",")).child("Group").push().setValue("Tất cả");

                                            refDatabase.child(emailLogin).child("SupByASM").child(managerEmail.replace(".",",")).child("Tất cả").child(email.replace(".",",")).setValue(addEmployee);
                                            refDatabase.child(emailLogin).child("Employee").child(email.replace(".",",")).setValue(addEmployee).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(getApplicationContext(), "Đã tạo TK thành công", Toast.LENGTH_LONG).show();
                                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));

                                                }
                                            });
                                            refDatabase.child(emailLogin).child("SupByASM").child(managerEmail.replace(".",",")).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if(!dataSnapshot.hasChild("Group")){
                                                        refDatabase.child(emailLogin).child("SupByASM").child(managerEmail.replace(".",",")).child("Group").push().child("groupName").setValue("Tất cả");
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                            Toast.makeText(getApplicationContext(), "Đã tạo TK thành công", Toast.LENGTH_LONG).show();
//FirebaseAuth.getInstance().signOut();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(CreateAccount.this);
                                builder.setMessage("Lỗi tạo tài khoản mới, vui lòng không sử dụng trùng lặp email!");

                            }
                        });
                    }

                    if(accType.equals("ASM")){
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()){
                                            final Employee addEmployee = new Employee(name,address,phone,email.replace(".", ","),pass,managerEmail.replace(".",","));

                                            refDatabase.child("1-System/Login").child(email.replace(".",",")).setValue(emailLogin);
                                            refDatabase.child("1-System/Role").child(emailLogin).child(email.replace(".",",")).setValue("ASM");
                                            Toast.makeText(getApplicationContext(), "Đã tạo TK thành công", Toast.LENGTH_LONG).show();
                                            refDatabase.child(emailLogin).child("SupByASM").child(email.replace(".",",")).child("Group").push().setValue("Tất cả");
                                            refDatabase.child(emailLogin).child("Employee").child(email.replace(".",",")).setValue(addEmployee).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(getApplicationContext(), "Đã tạo TK thành công", Toast.LENGTH_LONG).show();
                                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));

                                                }
                                            });
                                            refDatabase.child(emailLogin).child("ASMByRSM").child(managerEmail.replace(".",",")).child("Tất cả").child(email.replace(".",",")).setValue(addEmployee);
                                            refDatabase.child(emailLogin).child("Employee").child(email.replace(".",",")).setValue(addEmployee);
                                            refDatabase.child(emailLogin).child("ASMByRSM").child(managerEmail.replace(".",",")).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if(!dataSnapshot.hasChild("Group")){
                                                        refDatabase.child(emailLogin).child("ASMByRSM").child(managerEmail.replace(".",",")).child("Group").push().child("groupName").setValue("Tất cả");
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

//FirebaseAuth.getInstance().signOut();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(CreateAccount.this);
                                builder.setMessage("Lỗi tạo tài khoản mới, vui lòng không sử dụng trùng lặp email!");

                            }
                        });
                    }

                    if(accType.equals("RSM")){
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()){
                                            final Employee addEmployee = new Employee(name,address,phone,email.replace(".", ","),pass,managerEmail.replace(".",","));

                                            refDatabase.child("1-System/Login").child(email.replace(".",",")).setValue(emailLogin);
                                            refDatabase.child("1-System/Role").child(emailLogin).child(email.replace(".",",")).setValue("RSM");
                                            refDatabase.child(emailLogin).child("Employee").child(email.replace(".",",")).setValue(addEmployee).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(getApplicationContext(), "Đã tạo TK thành công", Toast.LENGTH_LONG).show();
                                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));

                                                }
                                            });
                                            refDatabase.child(emailLogin).child("ASMByRSM").child(email.replace(".",",")).child("Group").push().setValue("Tất cả");
                                            refDatabase.child(emailLogin).child("RSMByAdmin").child(managerEmail.replace(".",",")).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if(!dataSnapshot.hasChild("Group")){
                                                        refDatabase.child(emailLogin).child("RSMByAdmin").child(managerEmail.replace(".",",")).child("Group").push().child("groupName").setValue("Tất cả");
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                            Toast.makeText(getApplicationContext(), "Đã tạo TK thành công", Toast.LENGTH_LONG).show();
                                            refDatabase.child(emailLogin).child("RSMByAdmin").child(managerEmail.replace(".",",")).child("Tất cả").child(email.replace(".",",")).setValue(addEmployee);
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(CreateAccount.this);
                                builder.setMessage("Lỗi tạo tài khoản mới, vui lòng không sử dụng trùng lặp email!");

                            }
                        });
                    }
                }
            }
        });

    }
}
