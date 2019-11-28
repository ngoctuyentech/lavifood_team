package vn.techlifegroup.wesell.list;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.bytask.distribution.FilterEmployeeActivity;
import vn.techlifegroup.wesell.model.Employee;
import vn.techlifegroup.wesell.utils.Constants;
import vn.techlifegroup.wesell.utils.Utils;

public class EmployeeListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<Employee,EmployeeViewHolder> adapterFirebase;
    private LinearLayoutManager linearLayoutManager;
    private boolean employeeFilter;
    private String choosenYear,choosenMonth,emailLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_list);

        Intent intent = this.getIntent();
        employeeFilter = intent.getBooleanExtra("EmployeeFilter",false);
        choosenYear = intent.getStringExtra("ChoosenYear");
        choosenMonth = intent.getStringExtra("ChoosenMonth");
        emailLogin = intent.getStringExtra("EmailLogin");

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_employee_list);
        setSupportActionBar(toolbar);

        getEmployeeList();
    }

    private void getEmployeeList() {
        recyclerView = (RecyclerView)findViewById(R.id.recycler_employee_list);
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
                R.id.item_client,
                EmployeeViewHolder.class,
                Constants.refDatabase.child(emailLogin+"/Employee")
        ) {
            @Override
            public EmployeeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client,parent,false);
                return new EmployeeViewHolder(v);
            }


            @Override
            protected void populateViewHolder(EmployeeViewHolder viewHolder, Employee model, int position) {
                viewHolder.employeeName.setText(model.getEmployeeName());

            }
        };

        recyclerView.setAdapter(adapterFirebase);
        adapterFirebase.notifyDataSetChanged();

    }


    public class EmployeeViewHolder extends RecyclerView.ViewHolder {
        TextView employeeName;

        public EmployeeViewHolder(View itemView) {
            super(itemView);
            employeeName = (TextView) itemView.findViewById(R.id.tv_item_client_sale_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    DatabaseReference keyRef = adapterFirebase.getRef(position);
                    String keyString = keyRef.getKey();
                    if(employeeFilter){
                        Intent intent = new Intent(getApplicationContext(), FilterEmployeeActivity.class);
                        intent.putExtra("ChoosenMonth", choosenMonth);
                        intent.putExtra("ChoosenYear", choosenYear);
                        intent.putExtra("EmployeeCode",keyString);
                        startActivity(intent);
                    }


                }
            });




        }
    }

}
