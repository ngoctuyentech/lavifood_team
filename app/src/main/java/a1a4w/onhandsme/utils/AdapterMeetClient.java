package a1a4w.onhandsme.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import a1a4w.onhandsme.R;
import a1a4w.onhandsme.model.Client;
import a1a4w.onhandsme.model.WarehouseIn;

public class AdapterMeetClient extends RecyclerView.Adapter<AdapterMeetClient.ClientViewHolder>  {
    @SuppressWarnings("unused")
    Context context;
    private List<Client> items;
    private Client client;


    public AdapterMeetClient() {
        super();

    }

    public AdapterMeetClient(Context context, List<Client> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public ClientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meet_client, parent, false);
        return new ClientViewHolder(v);

    }


    @Override
    public void onBindViewHolder(ClientViewHolder holder, int position) {
        client = items.get(position);
        holder.clientName.setText(client.getClientName());
        holder.clientAddress.setText(client.getClientName());
        holder.meetDate.setText(Utils.getDateCurrentTimeZone(Long.parseLong(client.getTimeStamp())));

    }

    void clearData() {
        items.clear(); //clear list
        this.notifyDataSetChanged(); //let your adapter know about the changes and reload view.
    }

    @Override
    public int getItemCount() {
        if(items!=null) return items.size();
        return 0;
    }

    public class ClientViewHolder extends RecyclerView.ViewHolder {
        TextView clientName,clientAddress,meetDate;

        public ClientViewHolder(View itemView) {
            super(itemView);
            clientName = (TextView) itemView.findViewById(R.id.tv_item_meet_client_name);
            clientAddress = itemView.findViewById(R.id.tv_item_meet_client_address);
            meetDate = itemView.findViewById(R.id.tv_item_meet_client_time);

        }
    }



}