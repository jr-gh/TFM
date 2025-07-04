package es.uva.estudiantes.tfm;

import static java.lang.System.exit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.File;


public class MainActivity extends AppCompatActivity {

    public static String APP_NAME = "TFM_ULTIMATE";

    public static File destinationFolderFile;

    private Button executeButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Vinculamos el botón con el código Java
        executeButton = findViewById(R.id.buttonExecution);

        // Establecemos la acción al hacer clic
        executeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ejecutamos secuencialmente en un thread separado todos los modelos
                executeModels();
            }
        });




//        File externalStorageDirectoryFile = android.os.Environment.getExternalStorageDirectory();
//        File downloadFolderFile = new File (externalStorageDirectoryFile.getAbsolutePath() + "/Documents/" + MainActivity.APP_NAME);
//        if(!downloadFolderFile.exists()) {
//            downloadFolderFile.mkdirs();
//
//System.out.println(">>>>>>>> [MainActivity] 00000000 created!!!");
//
//        }
//
//System.out.println(">>>>>>>> [MainActivity] 11111111");
//
//        for(int i=0; i < 5; i++) {
//            File outputFile = new File(downloadFolderFile, "pepe" + i + ".txt");
//            try {
//                if (outputFile.exists()) {
////                    outputFile.delete();
//                    if (outputFile.canWrite()) {
//                        System.out.println(">>>>>>>> [MainActivity] bbbbbbbb: i:" + i);
//                    }
//                }
//
//System.out.println(">>>>>>>> [MainActivity] 22222222: i:" + i);
//
//                if (downloadFolderFile.canWrite()) {
//                    System.out.println(">>>>>>>> [MainActivity] aaaaaaaa: i:" + i);
//                }
//
//                if (outputFile.createNewFile()) {
//
//System.out.println(">>>>>>>> [MainActivity] 33333333: created!!!");
//
//                    FileOutputStream outputFOS = new FileOutputStream(outputFile);
//                    BufferedWriter outputBW = new BufferedWriter(new OutputStreamWriter(outputFOS));
//
//                    outputBW.write("pepe pepe pepe" + i);
//                    outputBW.newLine();
//
//                    outputBW.close();
//                    outputFOS.close();
//                }
//            } catch (Exception e) {
//                System.out.println(">>>>>>>> [MainActivity] Error: " + e.getMessage() + " <<<<<<<<");
//                e.printStackTrace();
//            }
//        }


        File externalStorageDirectoryFile = android.os.Environment.getExternalStorageDirectory();
        this.destinationFolderFile = new File(externalStorageDirectoryFile.getAbsolutePath() + "/Documents/" + MainActivity.APP_NAME);
        if (!this.destinationFolderFile.exists()) {
            this.destinationFolderFile.mkdirs();

            System.out.println(">>>>>>>> [MainActivity] Folder created: " + APP_NAME);

        }


        //----------------------------------------------------------------------------------------------------
        // MOVENET MODELS
        //----------------------------------------------------------------------------------------------------

//        Movenet movenet_lightning_8 = new Movenet(this, Movenet.TYPE_LIGHTNING, Movenet.DATA_TYPE_UINT8);
//        movenet_lightning_8.run();
//
//        Movenet movenet_lightning_16 = new Movenet(this, Movenet.TYPE_LIGHTNING, Movenet.DATA_TYPE_FLOAT16);
//        movenet_lightning_16.run();
//
//        Movenet movenet_lightning_32 = new Movenet(this, Movenet.TYPE_LIGHTNING, Movenet.DATA_TYPE_FLOAT32);
//        movenet_lightning_32.run();
//
//
//        Movenet movenet_thunder_8 = new Movenet(this, Movenet.TYPE_THUNDER, Movenet.DATA_TYPE_UINT8);
//        movenet_thunder_8.run();
//
//        Movenet movenet_thunder_16 = new Movenet(this, Movenet.TYPE_THUNDER, Movenet.DATA_TYPE_FLOAT16);
//        movenet_thunder_16.run();
//
//        Movenet movenet_thunder_32 = new Movenet(this, Movenet.TYPE_THUNDER, Movenet.DATA_TYPE_FLOAT32);
//        movenet_thunder_32.run();

        //----------------------------------------------------------------------------------------------------


        //----------------------------------------------------------------------------------------------------
        // POSENET MODELS                   ¡¡¡¡¡¡¡¡ DESCARTADO !!!!!!!!
        //----------------------------------------------------------------------------------------------------

//        Posenet posenet = new Posenet(this);
//        posenet.run();

        //----------------------------------------------------------------------------------------------------


        //----------------------------------------------------------------------------------------------------
        // BLAZEPOSE MODELS
        //----------------------------------------------------------------------------------------------------

//        BlazePose blazePoseLite = new BlazePose(this, BlazePose.TYPE_LITE);
//        blazePoseLite.run();
//
//        BlazePose blazePoseFull = new BlazePose(this, BlazePose.TYPE_FULL);
//        blazePoseFull.run();
//
//        BlazePose blazePoseHeavy = new BlazePose(this, BlazePose.TYPE_HEAVY);
//        blazePoseHeavy.run();

        //----------------------------------------------------------------------------------------------------



//        System.exit(0);
//        this.finishAffinity();
//        finishAndRemoveTask();

    }


    private void executeModels() {

        // Creamos un thread nuevo para la ejecución de tareas largas
        new Thread(() -> {
            // Actualizamos el componente de la interfaz
            runOnUiThread(() -> {
                findViewById(R.id.textViewMovenetL8).setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                findViewById(R.id.textViewMovenetL16).setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                findViewById(R.id.textViewMovenetL32).setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                findViewById(R.id.textViewMovenetT8).setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                findViewById(R.id.textViewMovenetT16).setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                findViewById(R.id.textViewMovenetT32).setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                findViewById(R.id.textViewBlazeposeL).setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                findViewById(R.id.textViewBlazeposeF).setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                findViewById(R.id.textViewBlazeposeH).setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                findViewById(R.id.textViewYolo8n).setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                findViewById(R.id.textViewYolo8s).setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                findViewById(R.id.textViewYolo8m).setBackgroundColor(ContextCompat.getColor(this, R.color.white));
            });


            // Actualizamos el etado del boton en la interfaz: deshabilitado
            runOnUiThread(() -> {
                executeButton.setEnabled(false);
            });

            //----------------------------------------------------------------------------------------------------
            // MOVENET MODELS
            //----------------------------------------------------------------------------------------------------
            Movenet movenet_lightning_8 = new Movenet(this, Movenet.TYPE_LIGHTNING, Movenet.DATA_TYPE_UINT8);
            movenet_lightning_8.run();

            Movenet movenet_lightning_16 = new Movenet(this, Movenet.TYPE_LIGHTNING, Movenet.DATA_TYPE_FLOAT16);
            movenet_lightning_16.run();

            Movenet movenet_lightning_32 = new Movenet(this, Movenet.TYPE_LIGHTNING, Movenet.DATA_TYPE_FLOAT32);
            movenet_lightning_32.run();


            Movenet movenet_thunder_8 = new Movenet(this, Movenet.TYPE_THUNDER, Movenet.DATA_TYPE_UINT8);
            movenet_thunder_8.run();

            Movenet movenet_thunder_16 = new Movenet(this, Movenet.TYPE_THUNDER, Movenet.DATA_TYPE_FLOAT16);
            movenet_thunder_16.run();

            Movenet movenet_thunder_32 = new Movenet(this, Movenet.TYPE_THUNDER, Movenet.DATA_TYPE_FLOAT32);
            movenet_thunder_32.run();
            //----------------------------------------------------------------------------------------------------


            //----------------------------------------------------------------------------------------------------
            // POSENET MODELS                   ¡¡¡¡¡¡¡¡ DESCARTADO !!!!!!!!
            //----------------------------------------------------------------------------------------------------

//        Posenet posenet = new Posenet(this);
//        posenet.run();

            //----------------------------------------------------------------------------------------------------


            //----------------------------------------------------------------------------------------------------
            // BLAZEPOSE MODELS
            //----------------------------------------------------------------------------------------------------
            BlazePose blazePoseLite = new BlazePose(this, BlazePose.TYPE_LITE);
            blazePoseLite.run();

            BlazePose blazePoseFull = new BlazePose(this, BlazePose.TYPE_FULL);
            blazePoseFull.run();

            BlazePose blazePoseHeavy = new BlazePose(this, BlazePose.TYPE_HEAVY);
            blazePoseHeavy.run();
            //----------------------------------------------------------------------------------------------------



            // Actualizamos el etado del boton en la interfaz: habilitado
            runOnUiThread(() -> {
                executeButton.setEnabled(true);
            });


        }).start();


    }
}