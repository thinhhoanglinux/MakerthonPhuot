package com.example.tkkil.phuot.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tkkil.phuot.Interface.ItemClickListener;
import com.example.tkkil.phuot.Models.Group;
import com.example.tkkil.phuot.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {
    private Context mContext;
    private List<Group> groups;
    private GoogleMap mMap;
    private Marker marker;
    HashMap<String,Marker> hashMap = new HashMap<>();

    public GroupAdapter(Context mContext, List<Group> groups, GoogleMap mMap, Marker marker) {
        this.mContext = mContext;
        this.groups = groups;
        this.mMap = mMap;
        this.marker = marker;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Group item = groups.get(position);
        holder.txtvNameGroup.setText(item.getName());
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        myRef.child("Users").child(item.getHost()).child("fullname").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                holder.txtvFullname.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder ab = new AlertDialog.Builder(mContext,R.style.MyDialogTheme);
                ab.setTitle("DELETE");
                ab.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        myRef.child("Groups").child(item.getName()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    groups.remove(item);
                                    notifyDataSetChanged();
                                    Toast.makeText(mContext, "Deleted!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
                ab.setNegativeButton("NO",null);
                AlertDialog alertDialog = ab.create();
                alertDialog.show();
            }
        });
        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                if(isLongClick){

                }else{
                    myRef.child("Groups").child(item.getName()).child("members").addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            String[] a = dataSnapshot.getValue().toString().split(" ");
                            double lat = Double.parseDouble(a[0]);
                            double lng = Double.parseDouble(a[1]);

                           /* MarkerOptions options = new MarkerOptions();
                            options.position(new LatLng(10.8,106));
                            mMap.addMarker(options);
*/
//                            hashMap.put(dataSnapshot.getKey(),marker);
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView txtvNameGroup, txtvQuantity, txtvFullname;
        Button btnDelete;
        ItemClickListener itemClickListener;

        ViewHolder(View itemView) {
            super(itemView);
            txtvNameGroup = itemView.findViewById(R.id.txtvNameGroup);
            txtvQuantity = itemView.findViewById(R.id.txtvQuantity);
            txtvFullname = itemView.findViewById(R.id.txtvFullname);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onClick(view, getAdapterPosition(), false);
        }

        @Override
        public boolean onLongClick(View view) {
            itemClickListener.onClick(view, getAdapterPosition(), true);
            return true;
        }
    }
}
