package a1a4w.onhandsme.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import a1a4w.onhandsme.R;
import a1a4w.onhandsme.bytask.SaleRoute;
import a1a4w.onhandsme.model.Client;
import a1a4w.onhandsme.model.MapModel;
import a1a4w.onhandsme.order.UpdateOrderActivity;
import de.hdodenhof.circleimageview.CircleImageView;
import im.delight.android.location.SimpleLocation;

import static a1a4w.onhandsme.bytask.SaleRoute.btnIn;
import static a1a4w.onhandsme.bytask.SaleRoute.btnNewOrder;
import static a1a4w.onhandsme.bytask.SaleRoute.btnOut;
import static a1a4w.onhandsme.bytask.SaleRoute.clientOnMapLat;
import static a1a4w.onhandsme.bytask.SaleRoute.clientOnMapLong;
import static a1a4w.onhandsme.bytask.SaleRoute.latitude;
import static a1a4w.onhandsme.bytask.SaleRoute.mMap;
import static a1a4w.onhandsme.utils.Constants.buttonClick;
import static a1a4w.onhandsme.utils.Constants.refDatabase;

public class AdapterMeetClient extends RecyclerView.Adapter<AdapterMeetClient.ClientViewHolder>  {
    @SuppressWarnings("unused")
    Context context;
    private List<Client> items;
    private Client client;
    private int choosenClient;
    private Activity activity;
    private String emailLogin;

    public AdapterMeetClient() {
        super();

    }

    public AdapterMeetClient(Context context, List<Client> items,Activity activity,String emailLogin) {
        this.context = context;
        this.items = items;
        this.activity = activity;
        this.emailLogin = emailLogin;
    }

    @Override
    public ClientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client_circle, parent, false);
        return new ClientViewHolder(v);

    }


    @Override
    public void onBindViewHolder(ClientViewHolder holder, int position) {
        client = items.get(position);
        holder.clientName.setText(client.getClientName());

        holder.circleClient.setBorderColor((position==choosenClient)?context.getResources().getColor(android.R.color.holo_green_light):context.getResources().getColor(android.R.color.black));
        holder.circleClient.setBorderWidth((position==choosenClient)? 12:6);

        //holder.circleClient.setBackground((position==choosenClient)? context.getResources().getDrawable(android.R.color.white):context.getResources().getDrawable(R.drawable.border_drug_cat_accent));
        holder.circleClient.setCircleBackgroundColor((position==choosenClient)? context.getResources().getColor(android.R.color.white):context.getResources().getColor(android.R.color.transparent));
        holder.circleClient.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_client2));

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
        CircleImageView circleClient;
        TextView clientName;

        public ClientViewHolder(View itemView) {
            super(itemView);
            circleClient = (CircleImageView) itemView.findViewById(R.id.profile_image);
            clientName = itemView.findViewById(R.id.tv_item_client_circle_name);

            //emailLogin = SaleRoute.emailLogin;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(buttonClick);
                    choosenClient = getAdapterPosition();
                    AdapterMeetClient.this.notifyDataSetChanged();

                    Client client = items.get(choosenClient);
                    MapModel clientMap = client.getMap();
                    final String clientCode = client.getClientCode();
                    String clientName = client.getClientName();
                    //SaleRoute.choosenClientCode = clientCode;


                    clientOnMapLat = Double.parseDouble(clientMap.getLatitude());
                    clientOnMapLong = Double.parseDouble(clientMap.getLongitude());


                    SimpleLocation.Point agentPoint = new SimpleLocation.Point(clientOnMapLat, clientOnMapLong);
                    SimpleLocation.Point salePoint = new SimpleLocation.Point(latitude,SaleRoute.longitude);
                    double currentDistance = SimpleLocation.calculateDistance(agentPoint, salePoint);

                    Toast.makeText(context, currentDistance+"", Toast.LENGTH_LONG).show();

                    if(currentDistance>100){
                        btnIn.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
                        btnIn.setEnabled(false);
                        btnOut.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
                        btnOut.setEnabled(false);
                        btnNewOrder.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
                        btnNewOrder.setEnabled(false);
                    }


                    LatLng onMapClient = new LatLng(clientOnMapLat, clientOnMapLong);

                    mMap.addMarker(new MarkerOptions().position(onMapClient).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_client_map))
                            .title(clientName));

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(onMapClient,16.0f));

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_client_detail,null);
                    builder.setView(dialogView);

                    Dialog dialog = builder.create();
                    dialog.show();

                    final BarChart barTime = (BarChart)dialogView.findViewById(R.id.bar_client_detail_sale);

                    final TextView tvClientName = dialogView.findViewById(R.id.tv_client_detail_name);
                    final TextView tvClientAddress = dialogView.findViewById(R.id.tv_client_detail_address);

                    final Button yearSale = dialogView.findViewById(R.id.btn_client_detail_yearsale);
                    final Button monthSale = dialogView.findViewById(R.id.btn_client_detail_month_sale);
                    final Button thisMonthSale = dialogView.findViewById(R.id.btn_client_detail_thismonth);
                    //Button btnExportExcel = dialogView.findViewById(R.id.btn_client_detail_exportExcel);

                    ImageView phone = dialogView.findViewById(R.id.btn_client_detail_phone);
                    ImageView grouping = dialogView.findViewById(R.id.btn_client_detail_grouping);
                    ImageView ivOrder = dialogView.findViewById(R.id.iv_client_detail_order);
                    ImageView ivFixLoc = dialogView.findViewById(R.id.iv_client_fix_location);
                    ImageView ivSaleRoute = dialogView.findViewById(R.id.iv_client_detail_saleroute);

                    ivSaleRoute.setVisibility(View.GONE);
                    grouping.setVisibility(View.GONE);

                    yearSale.setBackground(context.getResources().getDrawable(R.drawable.border_drug_cat));
                    monthSale.setBackground(activity.getResources().getDrawable(R.drawable.border_drug_cat));
                    thisMonthSale.setBackground(activity.getResources().getDrawable(R.drawable.border_drug_cat_accent));

                    refDatabase.child(emailLogin).child("Client").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Client clientInfo = dataSnapshot.getValue(Client.class);
                            assert clientInfo != null;
                            String clientStreet = clientInfo.getClientStreet();
                            String clientProvince = clientInfo.getClientProvince();
                            tvClientName.setText(clientInfo.getClientName());
                            tvClientAddress.setText(clientStreet + ", " + clientProvince);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    DateTime dt = new DateTime();
                    final String month = dt.getMonthOfYear()+"";
                    final String year = dt.getYear()+"";

                    final List<BarEntry> monthEntries = new ArrayList<>();

                    refDatabase.child(emailLogin).child("TotalByClient").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> snapTimeSale = dataSnapshot.getChildren();


                            for(DataSnapshot itemTime:snapTimeSale){

                                String timeKey = itemTime.getKey();

                                String value = itemTime.getValue().toString();

                                if(timeKey.length()>7 ){

                                    if(timeKey.contains(year+"-"+month)){

                                        monthEntries.add(new BarEntry(Integer.parseInt(timeKey.substring(timeKey.lastIndexOf("-")+1)), Float.parseFloat(value)));

                                        BarDataSet set = new BarDataSet(monthEntries,"Doanh số theo tháng");

                                        BarData data = new BarData(set);

                                        Description description = new Description();
                                        description.setText("");

                                        barTime.getAxisRight().setDrawGridLines(false);
                                        barTime.getAxisLeft().setDrawGridLines(false);
                                        barTime.getXAxis().setDrawGridLines(false);
                                        barTime.getXAxis().setGranularityEnabled(true);
                                        //barTime.getXAxis().setDrawLabels(false);
                                        barTime.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

                                        //barTime.getXAxis().setValueFormatter(new IndexAxisValueFormatter(barEntryLabels));
                                        barTime.setDescription(description);
                                        barTime.getAxisRight().setEnabled(false);
                                        barTime.setTouchEnabled(true);
                                        //barTime.setMarker(mv);
                                        barTime.setData(data);
                                        barTime.animateXY(1000,2000);
                                        barTime.setFitBars(true); // make the x-axis fit exactly all bars
                                        barTime.invalidate(); // refresh

                                    }
                                    //barEntryLabels.add(timeKey.substring(5));

                                }


                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    ivOrder.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);
                            Intent it = new Intent(context, UpdateOrderActivity.class);
                            it.putExtra("EmailLogin", SaleRoute.emailLogin);
                            it.putExtra("ClientCode", clientCode);
                            it.putExtra("SaleMan",true);
                            it.putExtra("OutRoute",true);
                            context.startActivity(it);
                        }
                    });


                    phone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);

                            refDatabase.child(emailLogin).child("Client").child(clientCode).child("clientPhone").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String phone = dataSnapshot.getValue().toString();
                                    Intent intent = new Intent(Intent.ACTION_DIAL);
                                    intent.setData(Uri.parse("tel:" + phone));
                                    context.startActivity(intent);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    });

                    ivFixLoc.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);

                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setMessage("Cập nhật toạ khách hàng?");

                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MapModel updateMap = new MapModel(latitude+"",SaleRoute.longitude+"");
                                    refDatabase.child(emailLogin).child("Client").child(clientCode).child("map").setValue(updateMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(context, "Cập nhật hoàn tất!", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }).show();

                        }
                    });


                    yearSale.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);



                            yearSale.setBackground(context.getResources().getDrawable(R.drawable.border_drug_cat_accent));
                            monthSale.setBackground(context.getResources().getDrawable(R.drawable.border_drug_cat));
                            thisMonthSale.setBackground(context.getResources().getDrawable(R.drawable.border_drug_cat));

                            final List<BarEntry> yearEntries = new ArrayList<>();
                            final ArrayList<String> barEntryLabels = new ArrayList<>();;

                            refDatabase.child(emailLogin).child("TotalByClient").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Iterable<DataSnapshot> snapTimeSale = dataSnapshot.getChildren();


                                    for(DataSnapshot itemTime:snapTimeSale){

                                        String timeKey = itemTime.getKey();

                                        String value = itemTime.getValue().toString();

                                        if(timeKey.length()<5){

                                            barEntryLabels.add(timeKey);
                                            yearEntries.add(new BarEntry(Integer.parseInt(timeKey), Float.parseFloat(value)));

                                            BarDataSet set = new BarDataSet(yearEntries,"Doanh số theo năm");

                                            BarData data = new BarData(set);

                                            Description description = new Description();
                                            description.setText("");

                                            barTime.getAxisRight().setDrawGridLines(false);
                                            barTime.getAxisLeft().setDrawGridLines(false);
                                            barTime.getXAxis().setDrawGridLines(false);
                                            barTime.getXAxis().setGranularityEnabled(true);
                                            //barTime.getXAxis().setDrawLabels(false);
                                            barTime.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

                                            //barTime.getXAxis().setValueFormatter(new IndexAxisValueFormatter(barEntryLabels));
                                            barTime.setDescription(description);
                                            barTime.getAxisRight().setEnabled(false);
                                            barTime.setTouchEnabled(true);
                                            //barTime.setMarker(mv);
                                            barTime.setData(data);
                                            barTime.animateXY(1000,2000);
                                            barTime.setFitBars(true); // make the x-axis fit exactly all bars
                                            barTime.invalidate(); // refresh

                                        }


                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    });

                    monthSale.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);
                            yearSale.setBackground(context.getResources().getDrawable(R.drawable.border_drug_cat));
                            monthSale.setBackground(context.getResources().getDrawable(R.drawable.border_drug_cat_accent));
                            thisMonthSale.setBackground(context.getResources().getDrawable(R.drawable.border_drug_cat));

                            final List<BarEntry> monthEntries = new ArrayList<>();

                            refDatabase.child(emailLogin).child("TotalByClient").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Iterable<DataSnapshot> snapTimeSale = dataSnapshot.getChildren();


                                    for(DataSnapshot itemTime:snapTimeSale){

                                        String timeKey = itemTime.getKey();

                                        String value = itemTime.getValue().toString();

                                        if(timeKey.length()>5 && timeKey.length()<8){


                                            //barEntryLabels.add(timeKey.substring(5));

                                            monthEntries.add(new BarEntry(Integer.parseInt(timeKey.substring(5)), Float.parseFloat(value)));

                                            BarDataSet set = new BarDataSet(monthEntries,"Doanh số theo tháng");

                                            BarData data = new BarData(set);

                                            Description description = new Description();
                                            description.setText("");

                                            barTime.getAxisRight().setDrawGridLines(false);
                                            barTime.getAxisLeft().setDrawGridLines(false);
                                            barTime.getXAxis().setDrawGridLines(false);
                                            barTime.getXAxis().setGranularityEnabled(true);
                                            //barTime.getXAxis().setDrawLabels(false);
                                            barTime.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

                                            //barTime.getXAxis().setValueFormatter(new IndexAxisValueFormatter(barEntryLabels));
                                            barTime.setDescription(description);
                                            barTime.getAxisRight().setEnabled(false);
                                            barTime.setTouchEnabled(true);
                                            //barTime.setMarker(mv);
                                            barTime.setData(data);
                                            barTime.animateXY(1000,2000);
                                            barTime.setFitBars(true); // make the x-axis fit exactly all bars
                                            barTime.invalidate(); // refresh

                                        }


                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    });

                    thisMonthSale.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);

                            yearSale.setBackground(context.getResources().getDrawable(R.drawable.border_drug_cat));
                            monthSale.setBackground(context.getResources().getDrawable(R.drawable.border_drug_cat));
                            thisMonthSale.setBackground(context.getResources().getDrawable(R.drawable.border_drug_cat_accent));
                            DateTime dt = new DateTime();
                            final String month = dt.getMonthOfYear()+"";
                            final String year = dt.getYear()+"";

                            final List<BarEntry> monthEntries = new ArrayList<>();

                            refDatabase.child(emailLogin).child("TotalByClient").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Iterable<DataSnapshot> snapTimeSale = dataSnapshot.getChildren();


                                    for(DataSnapshot itemTime:snapTimeSale){

                                        String timeKey = itemTime.getKey();

                                        String value = itemTime.getValue().toString();


                                        if(timeKey.length()>7 ){

                                            if(timeKey.contains(year+"-"+month)){

                                                monthEntries.add(new BarEntry(Integer.parseInt(timeKey.substring(timeKey.lastIndexOf("-")+1)), Float.parseFloat(value)));

                                                BarDataSet set = new BarDataSet(monthEntries,"Doanh số theo ngày");

                                                BarData data = new BarData(set);

                                                Description description = new Description();
                                                description.setText("");

                                                barTime.getAxisRight().setDrawGridLines(false);
                                                barTime.getAxisLeft().setDrawGridLines(false);
                                                barTime.getXAxis().setDrawGridLines(false);
                                                barTime.getXAxis().setGranularityEnabled(true);
                                                //barTime.getXAxis().setDrawLabels(false);
                                                barTime.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

                                                //barTime.getXAxis().setValueFormatter(new IndexAxisValueFormatter(barEntryLabels));
                                                barTime.setDescription(description);
                                                barTime.getAxisRight().setEnabled(false);
                                                barTime.setTouchEnabled(true);
                                                //barTime.setMarker(mv);
                                                barTime.setData(data);
                                                barTime.animateXY(1000,2000);
                                                barTime.setFitBars(true); // make the x-axis fit exactly all bars
                                                barTime.invalidate(); // refresh

                                            }
                                            //barEntryLabels.add(timeKey.substring(5));

                                        }


                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    });



                }


            });
        }
    }


}