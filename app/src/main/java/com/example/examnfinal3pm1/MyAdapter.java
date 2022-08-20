package com.example.examnfinal3pm1;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>{

    private Context context;
    ArrayList<Medicamentos> list;
    ArrayList<Medicamentos> listaOriginal;


    public MyAdapter(Context context, ArrayList<Medicamentos> list ) {
        this.context = context;
        this.list = list;
        listaOriginal = new ArrayList<>();
        listaOriginal.addAll(list);

    }



    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.iteam,parent, false);
        return  new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder,final int position) {
        final Medicamentos item = list.get(position);
        Medicamentos user = list.get(position);

        holder.txtnombre.setText(user.getNombre());
        holder.txtdescripcion.setText(user.getDescripcion());
        Glide.with(holder.img1.getContext()).load(user.getUrl()).into(holder.img1);

        final Medicamentos infoData = list.get(position);



        holder.btnModificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DialogPlus dialogPlus =DialogPlus.newDialog(holder.img1.getContext())
                        .setContentHolder(new ViewHolder(R.layout.activity_detalle))
                        .setExpanded(true,1900)
                        .create();



                View view =dialogPlus.getHolderView();

                EditText nombre = view.findViewById(R.id.txtnombred);
                EditText descripcion = view.findViewById(R.id.txtDescripciond);
                EditText cantidad = view.findViewById(R.id.txtCantidadd);
                EditText tiempo = view.findViewById(R.id.txtTiempod);
                EditText Periocidad = view.findViewById(R.id.txtPeriocidadd);


                Button btnactualizar = view.findViewById(R.id.btnModificar);

                nombre.setText(user.getNombre());
                descripcion.setText(user.getDescripcion());
                cantidad.setText(user.getCantidad());
                tiempo.setText(user.getTiempo());
                Periocidad.setText(user.getPeriocidad());


                dialogPlus.show();


                btnactualizar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Map<String, Object> map = new HashMap<>();

                        map.put("nombre", nombre.getText().toString());
                        map.put("cantidad", cantidad.getText().toString());
                        map.put("descripcion", descripcion.getText().toString());
                        map.put("tiempo", tiempo.getText().toString());
                        map.put("periocidad", Periocidad.toString());


                        FirebaseDatabase.getInstance().getReference().child("nuevos").child(user.getKey())
                                .updateChildren(map)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(holder.txtnombre.getContext(), "actuializada Correctamente", Toast.LENGTH_SHORT).show();
                                        dialogPlus.dismiss();

                                        Intent intent = new Intent(holder.itemView.getContext(),MainActivity.class);
                                        holder.itemView.getContext().startActivity(intent);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(holder.txtnombre.getContext(), "Error al actualizar", Toast.LENGTH_SHORT).show();
                                dialogPlus.dismiss();
                            }
                        });
                    }
                });

                notifyDataSetChanged();


            }
        });


        holder.eliminarlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(holder.txtnombre.getContext());
                builder.setTitle("Estas Seguro(a)");
                builder.setMessage("Los datos no se podran recuperar");
                builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String key1 = list.get(position).getKey();
                        FirebaseDatabase.getInstance().getReference().child("nuevos")
                                .child(key1).removeValue();


                        Intent intent = new Intent(holder.itemView.getContext(),MainActivity.class);
                        holder.itemView.getContext().startActivity(intent);


                    }
                });

                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(holder.txtnombre.getContext(), "Cancelar", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.show();

            }
        });



    }


    public void filtrar(ArrayList<Medicamentos> filtroUsuarios) {
        this.list = filtroUsuarios;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {

        return list.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView img1;
        TextView txtnombre,txtdescripcion;
        Button btnModificar,eliminarlist;



        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            eliminarlist = itemView.findViewById(R.id.eliminarlist);
            btnModificar= itemView.findViewById(R.id.btndetallelist);

            img1 = itemView.findViewById(R.id.img1);
            txtnombre = itemView.findViewById(R.id.txtnombrelist);
            txtdescripcion = itemView.findViewById(R.id.txtdescripcion);







        }
    }



}


