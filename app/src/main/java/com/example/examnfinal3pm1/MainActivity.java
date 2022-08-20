package com.example.examnfinal3pm1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ktx.Firebase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private TextView txtll;
    private EditText txtnombre, txtdescripcion, txtPeriocidad, txttiempo,txtCantidad;
    private Button btnRegistrar, btnlistaver, btnfoto;

    private ImageView foto;
    private StorageReference storageReference;
    private ProgressDialog cargando;
    Bitmap thumb_bitmap = null;


    private LocationManager ubicacion;

    private DatabaseReference Medicamentos;
    private Object CropImage;
    private Instant Picasso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Medicamentos = FirebaseDatabase.getInstance().getReference("Medicamentos");
        foto = findViewById(R.id.img_foto);
        txtnombre = findViewById(R.id.txtnombre);
        txtdescripcion = findViewById(R.id.txtdescripcion);
        txtPeriocidad = findViewById(R.id.txtPeriocidad);
        txttiempo = findViewById(R.id.txttiempo);
        txtCantidad = findViewById(R.id.txtCantidad);


        btnlistaver = findViewById(R.id.btnlista);
        txtll = findViewById(R.id.ll);

        btnfoto = findViewById(R.id.btnfoto);
        btnRegistrar = findViewById(R.id.btnguardar);

        Medicamentos = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference().child("img_comprimidas");
        cargando = new ProgressDialog(this);

        btnlistaver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ListaMedicamentos.class));
            }
        });


        btnfoto.setOnClickListener(new View.OnClickListener() {
            private Object CropImage;

            @Override
            public void onClick(View view) {
                CropImage.startPickImageActivity(MainActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            Uri imgaeuri = CropImage.getPickImageResultUri(this, data);

            //Recortar la IMG

            CropImage.activity(imgaeuri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setRequestedSize(640, 480)
                    .setAspectRatio(2, 1).start(MainActivity.this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                File url = new File(resultUri.getPath());

                Picasso.with(this).load(url).into(foto);

                //comprimiendo imagen

                try {
                    thumb_bitmap = new Compressor(this).setMaxWidth(640).setMaxHeight(480).setQuality(90).compressToBitmap(url);
                } catch (IOException e) {

                    e.printStackTrace();
                }
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                final byte[] thumb_byte = byteArrayOutputStream.toByteArray();

                //fin del compresor


                btnRegistrar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (!txtnombre.getText().toString().isEmpty()
                                && !txtCantidad.getText().toString().isEmpty()
                                && !txttiempo.getText().toString().isEmpty()
                                && !txtdescripcion.getText().toString().isEmpty()
                                && !txtPeriocidad.getText().toString().isEmpty()) {

                            cargando.setTitle("Subiendo foto...");
                            cargando.setTitle("Espere por favor...");
                            cargando.show();

                            String fecha = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

                            StorageReference ref = storageReference.child(fecha);
                            UploadTask uploadTask = ref.putBytes(thumb_byte);

                            //subir imagen en storage....

                            Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw Objects.requireNonNull(task.getException());
                                    }

                                    return ref.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {


                                    Uri dowloaduri = task.getResult();

                                    String key = Medicamentos.push().getKey();
                                    String nombre = txtnombre.getText().toString();
                                    String descripcion = txtdescripcion.getText().toString();
                                    String cantidad = txtCantidad.getText().toString();
                                    String tiempo = txttiempo.getText().toString();
                                    String periocidad = txtPeriocidad.getText().toString();
                                    String url = dowloaduri.toString();



                                    Medicamentos user = new Medicamentos(key,nombre, descripcion, cantidad,tiempo, periocidad, url );

                                    Medicamentos.child("nuevos").child(key).setValue(user);

                                    Toast.makeText(MainActivity.this, "Datos exitosos", Toast.LENGTH_SHORT).show();
                                    cargando.dismiss();

                                    Toast.makeText(MainActivity.this, "Imagen cargada con exito", Toast.LENGTH_SHORT).show();


                                }
                            });

                        } else {
                            Toast.makeText(MainActivity.this, "Debe completar los campos", Toast.LENGTH_LONG).show();
                        }


                    }
                });

            }

        }

    }

}


