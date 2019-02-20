package a1a4w.onhandsme.pos;

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
import a1a4w.onhandsme.R;
import a1a4w.onhandsme.model.Employee;
import a1a4w.onhandsme.utils.Constants;
import a1a4w.onhandsme.utils.Utils;

public class StoreEmployeeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter<Employee,EmployeeViewHolder> adapterFirebase;
    private Bundle b = new Bundle();
    private String emailLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_employee);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_store_employee);
        setSupportActionBar(toolbar);

        Intent intent = this.getIntent();
        emailLogin = intent.getStringExtra("EmailLogin");

        FloatingActionButton fab_employ_magmt = (FloatingActionButton)findViewById(R.id.fab_store_employee);
        fab_employ_magmt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                Intent it = new Intent(getApplicationContext(),AddEmployeeActivity.class);
                it.putExtra("EmailLogin",emailLogin);
                startActivity(it);
            }
        });

        recyclerView = (RecyclerView)findViewById(R.id.recycler_store_employee);
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

        adapterFirebase = new FirebaseRecyclerAdapter<Employee, EmployeeViewHolder>(
                Employee.class,
                R.id.item_employee,
                EmployeeViewHolder.class,
                Constants.refDatabase.child(emailLogin).child("Z_POS_Employee")
        ) {
            @Override
            public EmployeeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_employee,parent,false);
                return new EmployeeViewHolder(v);
            }


            @Override
            protected void populateViewHolder(EmployeeViewHolder viewHolder, Employee model, int position) {
                viewHolder.employeeName.setText(model.getEmployeeName());
                viewHolder.employeePhone.setText(model.getEmployeePhone());
                Glide.with(getApplicationContext()).load(model.getEmployeeUrl()).error(R.drawable.employee).fitCenter().override(300,200).into(viewHolder.employeePic);


            }
        };

        recyclerView.setAdapter(adapterFirebase);
        adapterFirebase.notifyDataSetChanged();

    }

    private class EmployeeViewHolder extends RecyclerView.ViewHolder {
        TextView employeeName,employeePhone;
        ImageView employeePic,employeeRemove;

        EmployeeViewHolder(View itemView) {
            super(itemView);
            employeeName = (TextView) itemView.findViewById(R.id.tv_item_employee_name);
            employeePhone = (TextView) itemView.findViewById(R.id.tv_item_employee_phone);
            employeePic = (ImageView)itemView.findViewById(R.id.iv_item_employee_image);
            employeeRemove = (ImageView)itemView.findViewById(R.id.iv_item_employee_remove);

            employeeRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    final String itemKey = adapterFirebase.getRef(position).getKey();
                    AlertDialog.Builder builder = new AlertDialog.Builder(StoreEmployeeActivity.this);
                    builder.setMessage("Xóa nhân viên khỏi danh sách?");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Constants.refDatabase.child(emailLogin).child("Z_POS_Employee").child(itemKey).setValue(null);
                        }
                    });

                    builder.show();
                }
            });

        }

    }
}
