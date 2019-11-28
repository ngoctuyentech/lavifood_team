package vn.techlifegroup.wesell.list;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

import vn.techlifegroup.wesell.MainActivity;
import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.bytask.CreateAccount;
import vn.techlifegroup.wesell.model.Employee;
import vn.techlifegroup.wesell.model.Setting;
import de.hdodenhof.circleimageview.CircleImageView;

import static vn.techlifegroup.wesell.utils.Constants.buttonClick;
import static vn.techlifegroup.wesell.utils.Constants.refDatabase;

public class AccountList extends AppCompatActivity {
    private RecyclerView rvRSM, rvASM, rvSup,rvSale;
    TextView tvTotalAcc,tvTotalRSM,tvTotalASM,tvTotalSup,tvTotalSale;
    FloatingActionButton fabAddAcc;
    CircleImageView ivAdmin;
    private String emailLogin,userEmail;
    private DatabaseReference refCompany;
    private FirebaseRecyclerAdapter<Employee, SaleViewHolder> adapterRSM;
    private FirebaseRecyclerAdapter<Employee, SaleASMViewHolder> adapterASM ;
    private FirebaseRecyclerAdapter<Employee, SaleSupViewHolder> adapterSup ;
    private FirebaseRecyclerAdapter<Employee, SaleManViewHolder> adapterSale ;

    private  int choosenRSM,choosenASM,choosenSup,totalAcc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_list);

        Intent it = this.getIntent();
        emailLogin = it.getStringExtra("EmailLogin");

        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
        refCompany = refDatabase.child(emailLogin);

        rvRSM = findViewById(R.id.rv_account_list_rsm);
        rvASM = findViewById(R.id.rv_account_list_asm);
        rvSup = findViewById(R.id.rv_account_list_sup);
        rvSale = findViewById(R.id.rv_account_list_sale);

        tvTotalAcc = findViewById(R.id.tv_account_list_total_acc);
        tvTotalRSM = findViewById(R.id.tv_account_list_total_rsm);
        tvTotalASM = findViewById(R.id.tv_account_list_total_asm);
        tvTotalSup = findViewById( R.id.tv_account_list_total_sup);
        tvTotalSale = findViewById(R.id.tv_account_list_total_sale);

        fabAddAcc = findViewById(R.id.fab_account_list_add_acc);

        fabAddAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick)    ;
                refCompany.child("Setting").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Setting setting = dataSnapshot.getValue(Setting.class);
                        if(Integer.parseInt(setting.getBuyNumber())>= Integer.parseInt(setting.getCurrentNumber())){

                            Intent it = new Intent(getApplicationContext(), CreateAccount.class);
                            it.putExtra("EmailLogin",emailLogin);
                            //it.putExtra("ASM",asm);
                            startActivity(it);
                        }else{
                            AlertDialog.Builder builder = new AlertDialog.Builder(AccountList.this);
                            builder.setMessage("Số tài khoản hiện tại đã đạt mức tối đa so với đăng ký ban đầu!");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

        ivAdmin = findViewById(R.id.iv_account_list_admin);

        ivAdmin.setImageDrawable(getResources().getDrawable(R.drawable.icon_user));


        getRSM();

        refCompany.child("Employee").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tvTotalAcc.setText(dataSnapshot.getChildrenCount()+"");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        refCompany.child("ASMByRSM").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tvTotalRSM.setText(String.format("%d RSM", dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        refCompany.child("SupByASM").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tvTotalASM.setText(String.format("%d ASM", dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        refCompany.child("SaleManBySup").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tvTotalSup.setText(String.format("%d G.Sát", dataSnapshot.getChildrenCount()));

                Iterable<DataSnapshot> snapSup = dataSnapshot.getChildren();
                final int[] totalSaleCount = {0};
                for(DataSnapshot itemSup:snapSup){
                    DatabaseReference refSup = itemSup.getRef();
                    refSup.child("Tất cả").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            long saleCount = dataSnapshot.getChildrenCount();
                            totalSaleCount[0] = (int) (totalSaleCount[0] + saleCount);
                            tvTotalSale.setText(String.format("%d B.hàng", totalSaleCount[0]));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        refCompany.child("ASMByRSM").limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tvTotalASM.setText(String.format("%d ASM", dataSnapshot.getChildrenCount()));

                Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();
                String firstEmail = it.next().getKey();
                getASM(refCompany.child("ASMByRSM").child(firstEmail).child("Tất cả"));
                refCompany.child("ASMByRSM").child(firstEmail).child("Tất cả").limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();
                        String firstEmail = it.next().getKey();
                        getSUP(refCompany.child("SupByASM").child(firstEmail).child("Tất cả"));
                        refCompany.child("SupByASM").child(firstEmail).child("Tất cả").limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();
                                String firstEmail = it.next().getKey();
                                getSale(refCompany.child("SaleManBySup").child(firstEmail).child("Tất cả"));

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
              //Toast.makeText(getApplicationContext(), firstEmail, Toast.LENGTH_LONG).show();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void getSale(DatabaseReference child) {
        rvSale.setHasFixedSize(true);
        //StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        rvSale.setLayoutManager(linearLayoutManager);


        adapterSale = new FirebaseRecyclerAdapter<Employee, SaleManViewHolder>(
                Employee.class,
                R.layout.item_saleman,
                SaleManViewHolder.class,
                child
        ) {
            @Override
            public SaleManViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saleman, parent, false);
                return new SaleManViewHolder(v);
            }


            @Override
            protected void populateViewHolder(SaleManViewHolder viewHolder, Employee model, int position) {
                viewHolder.saleName.setText(model.getEmployeeName());


                viewHolder.circleSale.setImageDrawable(getResources().getDrawable(R.drawable.icon_saleman));            }
        };

        rvSale.setAdapter(adapterSale);
        adapterSale.notifyDataSetChanged();
    }

    private void getSUP(DatabaseReference child) {
        rvSup.setHasFixedSize(true);
        //StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        rvSup.setLayoutManager(linearLayoutManager);


        adapterSup = new FirebaseRecyclerAdapter<Employee, SaleSupViewHolder>(
                Employee.class,
                R.layout.item_saleman,
                SaleSupViewHolder.class,
                child
        ) {
            @Override
            public SaleSupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saleman, parent, false);
                return new SaleSupViewHolder(v);
            }


            @Override
            protected void populateViewHolder(SaleSupViewHolder viewHolder, Employee model, int position) {
                viewHolder.saleName.setText(model.getEmployeeName());

                viewHolder.circleSale.setBorderColor((position==choosenSup)? getResources().getColor(android.R.color.holo_green_light):getResources().getColor(android.R.color.black));
                viewHolder.circleSale.setBorderWidth((position==choosenSup)? 12:6);

                //holder.circleClient.setBackground((position==choosenClient)? context.getResources().getDrawable(android.R.color.white):context.getResources().getDrawable(R.drawable.border_drug_cat_accent));
                viewHolder.circleSale.setCircleBackgroundColor((position==choosenSup)? getResources().getColor(android.R.color.white):getResources().getColor(android.R.color.transparent));
                viewHolder.circleSale.setImageDrawable(getResources().getDrawable(R.drawable.icon_saleman));            }
        };

        rvSup.setAdapter(adapterSup);
        adapterSup.notifyDataSetChanged();
    }

    private void getASM(DatabaseReference child) {
        rvASM.setHasFixedSize(true);
        //StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        rvASM.setLayoutManager(linearLayoutManager);


        adapterASM = new FirebaseRecyclerAdapter<Employee, SaleASMViewHolder>(
                Employee.class,
                R.layout.item_saleman,
                SaleASMViewHolder.class,
                child
        ) {
            @Override
            public SaleASMViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saleman, parent, false);
                return new SaleASMViewHolder(v);
            }


            @Override
            protected void populateViewHolder(SaleASMViewHolder viewHolder, Employee model, int position) {
                viewHolder.saleName.setText(model.getEmployeeName());

                viewHolder.circleSale.setBorderColor((position==choosenASM)? getResources().getColor(android.R.color.holo_green_light):getResources().getColor(android.R.color.black));
                viewHolder.circleSale.setBorderWidth((position==choosenASM)? 12:6);

                //holder.circleClient.setBackground((position==choosenClient)? context.getResources().getDrawable(android.R.color.white):context.getResources().getDrawable(R.drawable.border_drug_cat_accent));
                viewHolder.circleSale.setCircleBackgroundColor((position==choosenASM)? getResources().getColor(android.R.color.white):getResources().getColor(android.R.color.transparent));
                viewHolder.circleSale.setImageDrawable(getResources().getDrawable(R.drawable.icon_saleman));            }
        };

        rvASM.setAdapter(adapterASM);
        adapterASM.notifyDataSetChanged();
    }

    private void getRSM() {
        //rvRSM = (RecyclerView) findViewById(R.id.rv_account_list_rsm);
        rvRSM.setHasFixedSize(true);
        //StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        rvRSM.setLayoutManager(linearLayoutManager);


        adapterRSM = new FirebaseRecyclerAdapter<Employee, SaleViewHolder>(
                Employee.class,
                R.layout.item_saleman,
                SaleViewHolder.class,
                refCompany.child("RSMByAdmin").child(userEmail).child("Tất cả")
        ) {
            @Override
            public SaleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
             View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saleman, parent, false);
                return new SaleViewHolder(v);
            }


            @Override
            protected void populateViewHolder(SaleViewHolder viewHolder, Employee model, int position) {
                viewHolder.saleName.setText(model.getEmployeeName());
                viewHolder.circleSale.setBorderColor((position==choosenRSM)? getResources().getColor(android.R.color.holo_green_light):getResources().getColor(android.R.color.black));
                viewHolder.circleSale.setBorderWidth((position==choosenRSM)? 12:6);

                //holder.circleClient.setBackground((position==choosenClient)? context.getResources().getDrawable(android.R.color.white):context.getResources().getDrawable(R.drawable.border_drug_cat_accent));
                viewHolder.circleSale.setCircleBackgroundColor((position==choosenRSM)? getResources().getColor(android.R.color.white):getResources().getColor(android.R.color.transparent));
                viewHolder.circleSale.setImageDrawable(getResources().getDrawable(R.drawable.icon_saleman));
            }
        };

        rvRSM.setAdapter(adapterRSM);
        adapterRSM.notifyDataSetChanged();
    }

    public class SaleViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleSale;
        TextView saleName;

        public SaleViewHolder(View itemView) {
            super(itemView);
            circleSale = (CircleImageView) itemView.findViewById(R.id.profile_image);
            saleName = itemView.findViewById(R.id.tv_item_sale_circle_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(buttonClick);

                    choosenRSM = getAdapterPosition();
                    adapterRSM.notifyDataSetChanged();
                    Employee employee = adapterRSM.getItem(choosenRSM);
                    String firstEmail = employee.getEmployeeEmail();

                    choosenASM = 0;
                    choosenSup = 0;

                    getASM(refCompany.child("ASMByRSM").child(firstEmail).child("Tất cả"));
                    refCompany.child("ASMByRSM").child(firstEmail).child("Tất cả").limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();
                            String firstEmail = it.next().getKey();
                            getSUP(refCompany.child("SupByASM").child(firstEmail).child("Tất cả"));
                            refCompany.child("SupByASM").child(firstEmail).child("Tất cả").limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();
                                    String firstEmail = it.next().getKey();
                                    getSale(refCompany.child("SaleManBySup").child(firstEmail).child("Tất cả"));

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            });

        }
    }

    public class SaleASMViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleSale;
        TextView saleName;

        public SaleASMViewHolder(View itemView) {
            super(itemView);
            circleSale = (CircleImageView) itemView.findViewById(R.id.profile_image);
            saleName = itemView.findViewById(R.id.tv_item_sale_circle_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(buttonClick);

                    choosenASM = getAdapterPosition();
                    adapterASM.notifyDataSetChanged();
                    Employee employee = adapterASM.getItem(choosenASM);
                    String firstEmail = employee.getEmployeeEmail();
                    choosenSup = 0;
                    getSUP(refCompany.child("SupByASM").child(firstEmail).child("Tất cả"));
                    refCompany.child("SupByASM").child(firstEmail).child("Tất cả").limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();
                            String firstEmail = it.next().getKey();
                            getSale(refCompany.child("SaleManBySup").child(firstEmail).child("Tất cả"));

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });                }
            });
        }
    }


    public class SaleSupViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleSale;
        TextView saleName;

        public SaleSupViewHolder(View itemView) {
            super(itemView);
            circleSale = (CircleImageView) itemView.findViewById(R.id.profile_image);
            saleName = itemView.findViewById(R.id.tv_item_sale_circle_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(buttonClick);

                    choosenSup = getAdapterPosition();
                    adapterSup.notifyDataSetChanged();
                    Employee employee = adapterSup.getItem(choosenSup);
                    String firstEmail = employee.getEmployeeEmail();

                    getSale(refCompany.child("SaleManBySup").child(firstEmail).child("Tất cả"));

                }
            });
        }
    }

    public class SaleManViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleSale;
        TextView saleName;

        public SaleManViewHolder(View itemView) {
            super(itemView);
            circleSale = (CircleImageView) itemView.findViewById(R.id.profile_image);
            saleName = itemView.findViewById(R.id.tv_item_sale_circle_name);


        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
    }
}
