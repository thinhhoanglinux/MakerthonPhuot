package com.example.tkkil.phuot.Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tkkil.phuot.Models.Group;
import com.example.tkkil.phuot.Models.User;
import com.example.tkkil.phuot.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int REQUEST_SELECT_PICTURE = 999;
    private StorageReference mStorage;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    NavigationView myNav;
    private DrawerLayout myDrawer;
    private ProgressDialog loading;
    private Uri filepath;
    Bitmap bitmap;
    RecyclerView rcvListGroup;
    //Dialog
    EditText edtNameGroup, edtPwdGroup;
    //Header-Nav
    private CircleImageView nav_avatar;
    private TextView nav_name, nav_email;
    //Dialog avatar
    private CircleImageView dialog_avatar;
    private User user;
    FirebaseRecyclerAdapter adapterabc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStorage = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference();
        loading = new ProgressDialog(this, R.style.MyDialogTheme);
        loading.setTitle("LOADING");
        loading.setMessage("Please wait...");
        init();
        initToolbar();

        //Recycler
        rcvListGroup = findViewById(R.id.rcvListGroup);
        rcvListGroup.setHasFixedSize(true);
        rcvListGroup.setLayoutManager(new LinearLayoutManager(this));

        Query query = myRef.child("Groups");
        FirebaseRecyclerOptions<Group> options = new FirebaseRecyclerOptions.Builder<Group>().setQuery(query,Group.class).build();
        adapterabc = new FirebaseRecyclerAdapter<Group,ViewHolder>(options) {

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group,parent,false);
                return new ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(ViewHolder holder, final int position, final Group model) {
                holder.txtvName.setText(model.getName());
                holder.txtvTime.setText(model.getTime());

                holder.btnJoin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        myRef.child("Users").child(model.getHost()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                User user = dataSnapshot.getValue(User.class);
                                Toast.makeText(MainActivity.this, ""+user.getFullname(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
        };
        rcvListGroup.setAdapter(adapterabc);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapterabc.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapterabc.stopListening();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView txtvName,txtvQuantity,txtvTime;
        Button btnJoin;
        ViewHolder(View itemView) {
            super(itemView);
            txtvName = itemView.findViewById(R.id.txtvName);
            txtvQuantity = itemView.findViewById(R.id.txtvQuantity);
            txtvTime = itemView.findViewById(R.id.txtvTime);
            btnJoin = itemView.findViewById(R.id.btnJoin);
        }
    }

    @Override
    public void onBackPressed() {
        if (myDrawer.isDrawerOpen(GravityCompat.START)) {
            myDrawer.closeDrawer(GravityCompat.START);
        } else {
            AlertDialog.Builder ab_exit = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
            ab_exit.setTitle("EXIT");
            ab_exit.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            ab_exit.setNegativeButton("NO", null);
            final AlertDialog dialog_ab_exit = ab_exit.create();
            dialog_ab_exit.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    dialog_ab_exit.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorRed));
                }
            });
            dialog_ab_exit.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_group:
                addGroupDialog();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_PICTURE && resultCode == RESULT_OK && data != null) {
            filepath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filepath);
                dialog_avatar.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_log_out:
                AlertDialog.Builder ab = new AlertDialog.Builder(this, R.style.MyDialogTheme);
                ab.setTitle("SIGN OUT");
                ab.setNegativeButton("NO", null);
                ab.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAuth.signOut();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    }
                });
                final AlertDialog alertDialog = ab.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorRed));
                    }
                });
                alertDialog.show();
                break;
            case R.id.action_change_password:
                startActivity(new Intent(MainActivity.this, ChangePasswordActivity.class));
                break;
            case R.id.action_change_info:
                startActivity(new Intent(MainActivity.this, ChangeInformationActivity.class));
                break;
            default:
                break;
        }
        return true;
    }

    private boolean Validate() {
        boolean isValidate = true;
        if (TextUtils.isEmpty(edtNameGroup.getText().toString().trim())) {
            edtNameGroup.setError("Please enter Name!");
            isValidate = false;
        }
        if (TextUtils.isEmpty(edtPwdGroup.getText().toString().trim())) {
            edtPwdGroup.setError("Please enter Password!");
            isValidate = false;
        } else {
            if (edtPwdGroup.getText().toString().trim().length() < 6) {
                edtPwdGroup.setError("Password is too short!");
                isValidate = false;
            }
        }
        return isValidate;
    }

    private void addGroupDialog() {
        AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_new_group, null);
        ab.setView(view);

        ab.setPositiveButton("CREATE", null);
        ab.setNegativeButton("CANCEL", null);

        edtNameGroup = view.findViewById(R.id.edtNameGroup);
        edtPwdGroup = view.findViewById(R.id.edtPwdGroup);

        final AlertDialog dialog = ab.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                Button Positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Positive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!Validate()) {
                            return;
                        }
                        dialog.dismiss();
                        Snackbar.make(myDrawer, "Success", Snackbar.LENGTH_SHORT).show();
                        Group group = new Group();
                        group.setHost(mAuth.getCurrentUser().getUid());
                        group.setName(edtNameGroup.getText().toString().trim());
                        group.setPass(edtPwdGroup.getText().toString().trim());
                        group.setTime(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Calendar.getInstance().getTime()));
                        myRef.child("Groups").push().setValue(group)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Snackbar.make(myDrawer, "Success", Snackbar.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(myDrawer, "Error: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
            }
        });


        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Home");
        if (mAuth.getCurrentUser() != null)
            toolbar.setSubtitle(mAuth.getCurrentUser().getEmail());

        myDrawer = findViewById(R.id.myDrawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, myDrawer, toolbar, R.string.openDrawer, R.string.closeDrawer);
        myDrawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private int onGetHeightStatus() {
        int result = 0;
        int resourceId = MainActivity.this.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void init() {
        myNav = findViewById(R.id.myNav);
        myNav.setNavigationItemSelectedListener(this);
        final View header_nav = myNav.getHeaderView(0);
        header_nav.setPadding(0, onGetHeightStatus(), 0, 0);

        nav_avatar = header_nav.findViewById(R.id.nav_avatar);
        nav_name = header_nav.findViewById(R.id.nav_name);
        nav_email = header_nav.findViewById(R.id.nav_email);
        FirebaseUser mUser = mAuth.getCurrentUser();
        if (mUser != null) {
            myRef.child("Users/" + mUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    user = new User();
                    user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        if (TextUtils.isEmpty(user.getAvatar())) {
                            user.setAvatar("AAA");
                        }
                        Picasso.with(MainActivity.this)
                                .load(user.getAvatar())
                                .placeholder(R.drawable.defaut_avatar)
                                .centerCrop()
                                .fit()
                                .into(nav_avatar);
                        nav_name.setText(user.getFullname());
                        nav_email.setText(user.getEmail());
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        nav_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, nav_avatar, Gravity.END);
                popupMenu.getMenuInflater().inflate(R.menu.avatar, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.action_change_avatar) {
                            final AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
                            ab.setTitle("CHANGE AVATAR");
                            ab.setIcon(R.drawable.ic_account_box_green_24dp);
                            @SuppressLint("InflateParams")
                            View dialog_change_avatar = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_change_avatar, null);
                            ab.setView(dialog_change_avatar);
                            dialog_avatar = dialog_change_avatar.findViewById(R.id.dialog_avatar);
                            ab.setPositiveButton("CHANGE", null);
                            ab.setNeutralButton("SELECT PICTURE", null);
                            ab.setNegativeButton("NO", null);
                            final AlertDialog alertDialog = ab.create();
                            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialogInterface) {
                                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorBlue));
                                    Button b = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                                    Button c = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                    b.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent(Intent.ACTION_PICK);
                                            intent.setType("image/*");
                                            startActivityForResult(intent, REQUEST_SELECT_PICTURE);
                                        }
                                    });
                                    c.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (dialog_avatar.getDrawable() == null) {
                                                Toasty.error(MainActivity.this, "Please choose picture!", Snackbar.LENGTH_SHORT, true).show();
                                            } else {
                                                loading.show();
                                                StorageReference storageReference = mStorage.child("images").child(UUID.randomUUID().toString());
                                                storageReference.putFile(filepath)
                                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                            @Override
                                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                Uri downloadUri = taskSnapshot.getDownloadUrl();
                                                                myRef.child("Users/" + mAuth.getCurrentUser().getUid() + "/avatar").setValue(downloadUri);
                                                                loading.dismiss();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Snackbar.make(myDrawer, "Error: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                                                loading.dismiss();
                                                            }
                                                        });
                                                alertDialog.dismiss();
                                            }
                                        }
                                    });
                                }
                            });
                            alertDialog.setCanceledOnTouchOutside(false);
                            alertDialog.show();
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });
    }
}
