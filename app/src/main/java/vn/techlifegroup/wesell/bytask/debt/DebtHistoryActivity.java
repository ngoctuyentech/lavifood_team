package vn.techlifegroup.wesell.bytask.debt;

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
import vn.techlifegroup.wesell.model.DebtHistory;
import vn.techlifegroup.wesell.utils.Constants;
import vn.techlifegroup.wesell.utils.Utils;

public class DebtHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter<DebtHistory,DebtHistoryViewHolder> adapterFirebase;
    private DatabaseReference refDebtClient, refDebtTime;
    private String clientName,choosenYear,choosenMonth,choosenQuarter,emailLogin;
    private int choosenYearInt, choosenMonthInt, choosenQuarterInt;
    private boolean debtByTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debt_history);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_debt_history);
        setSupportActionBar(toolbar);

        Intent intent = this.getIntent();
        clientName = intent.getStringExtra("ClientName");
        choosenYear = intent.getStringExtra("ChoosenYear");
        choosenMonth = intent.getStringExtra("ChoosenMonth");
        debtByTime = intent.getBooleanExtra("DebtByTime",false);
        emailLogin = intent.getStringExtra("EmailLogin");


        if(debtByTime){
            refDebtTime = Constants.refDatabase.child(emailLogin+"/DebtHistory").child(choosenYear+choosenMonth);
            getDebtHistoryList(refDebtTime);
        }else{

            refDebtClient = Constants.refDatabase.child(emailLogin+ "/DebtByClient").child(clientName).child(choosenYear+choosenMonth);
            getDebtHistoryList(refDebtClient);
        }

    }

    private void getDebtHistoryList(DatabaseReference ref) {
        recyclerView = (RecyclerView)findViewById(R.id.recycler_debt_history);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);


        recyclerView.addOnScrollListener(new Utils.EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                int lastVisiblePosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                recyclerView.getLayoutManager().scrollToPosition(lastVisiblePosition);
            }
        });

        adapterFirebase = new FirebaseRecyclerAdapter<DebtHistory, DebtHistoryViewHolder>(
                DebtHistory.class,
                R.id.item_debt_history,
                DebtHistoryViewHolder.class,
                ref
        ) {
            @Override
            public DebtHistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_debt_history,parent,false);
                return new DebtHistoryViewHolder(v);
            }


            @Override
            protected void populateViewHolder(DebtHistoryViewHolder viewHolder, DebtHistory model, int position) {
                viewHolder.clientName.setText(model.getClientName());
                viewHolder.oldDebt.setText(Utils.convertNumber(model.getCurrentDebt()));
                viewHolder.newDebt.setText(Utils.convertNumber(model.getNewDebt()));
                viewHolder.updateDebt.setText(Utils.convertNumber(model.getUpdateDebt()));
                viewHolder.dateDebt.setText(Utils.getDate(model.getTimeStamp()));
                viewHolder.clientRepay.setText(Utils.convertNumber(model.getClientRepay()));

            }
        };

        recyclerView.setAdapter(adapterFirebase);
        adapterFirebase.notifyDataSetChanged();
    }

    public class DebtHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView clientName,oldDebt,newDebt,updateDebt,dateDebt,clientRepay;

        public DebtHistoryViewHolder(View itemView) {
            super(itemView);
            clientName = (TextView) itemView.findViewById(R.id.tv_item_debt_history_clientname);
            oldDebt = (TextView) itemView.findViewById(R.id.tv_item_debt_history_old);
            newDebt = (TextView) itemView.findViewById(R.id.tv_item_debt_history_new);
            updateDebt = (TextView) itemView.findViewById(R.id.tv_item_debt_history_update);
            dateDebt = (TextView) itemView.findViewById(R.id.tv_item_debt_history_date);
            clientRepay = (TextView)itemView.findViewById(R.id.tv_item_debt_history_repay);


        }
    }

}
