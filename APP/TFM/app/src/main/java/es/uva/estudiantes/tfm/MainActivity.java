package es.uva.estudiantes.tfm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;


/**
 *
 */
public class MainActivity extends AppCompatActivity {

    public static String APP_NAME = "TFM_ULTIMATE";

    public static File destinationFolderFile;

    private Button executeButton;

    private Button exitButton;


    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Establecemos la acción al hacer clic en el botón "EJECUTAR TEST"
        executeButton = findViewById(R.id.buttonExecution);
        executeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mostramos un diálogo de confirmación de ejecución del test
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getResources().getString(R.string.app_name))
                        .setMessage("Para un mejor rendimiento cierre todas las aplicaciones antes de ejecutar el test.\n\nEste test puede tardar más de 15 minutos ¿ejecutar el test ahora?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Ejecutamos secuencialmente en un thread separado todos los modelos sobre el dataset de imágenes selecccionadas
                                executeModels();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        // Establecemos la acción al hacer clic en el botón "SALIR"
        exitButton = findViewById(R.id.buttonExit);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mostramos un diálogo de confirmación de salida de la aplicación
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getResources().getString(R.string.app_name))
                        .setMessage("¿Salir de la aplicación?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                System.exit(0);
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

//        // Creamos la carpeta donde se generaran los ficheroas de salida
//        String destinationFolderFullPath = "/" + Environment.DIRECTORY_DOCUMENTS + "/" + MainActivity.APP_NAME;
//        this.destinationFolderFile = new File(destinationFolderFullPath);
//        if (!this.destinationFolderFile.exists()) {
//            this.destinationFolderFile.mkdirs();
//
//            System.out.println(">>>>>>>> [MainActivity] Folder created: " + APP_NAME);
//        }

        // Creamos la carpeta donde se generaran los ficheroas de salida
        File externalStorageDirectoryFile = android.os.Environment.getExternalStorageDirectory();
        this.destinationFolderFile = new File(externalStorageDirectoryFile.getAbsolutePath() + "/" + Environment.DIRECTORY_DOCUMENTS + "/" + MainActivity.APP_NAME);
        if (!this.destinationFolderFile.exists()) {
            this.destinationFolderFile.mkdirs();

System.out.println(">>>>>>>> [MainActivity] Folder created: " + APP_NAME);
        }
    }


    /**
     * Ejecuta secuencialmente en un thread separado todos los modelos sobre el dataset de imágenes selecccionadas
     */
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

            // Area de texto donde se mostrará la ruta de los ficheros de salida generados al finalizar el test
            File externalStorageDirectoryFile = android.os.Environment.getExternalStorageDirectory();
            TextView textViewResultsPath = (TextView)findViewById(R.id.textViewResultsPath);

            // Ejecutamos en el thread de la interfaz las actualizaciones de la misma
            runOnUiThread(() -> {
                // Actualizamos el estado del botón "EJECUTAR TEST" en la interfaz: DESHABILITADO
                executeButton.setEnabled(false);

                // Actualizamos el estado del botón "SALIR" en la interfaz: DESHABILITADO
                exitButton.setEnabled(false);

                // Actualizamos el estado del área de texto que muestra la ruta de los ficheros de salida generados al finalizar el test
                textViewResultsPath.setText("");
            });

            //----------------------------------------------------------------------------------------------------
            // Modelos de la familia MOVENET
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
            // Modelos de la familia POSENET                   ¡¡¡¡¡¡¡¡ DESCARTADO !!!!!!!!
            //----------------------------------------------------------------------------------------------------
//        Posenet posenet = new Posenet(this);
//        posenet.run();
            //----------------------------------------------------------------------------------------------------

            //----------------------------------------------------------------------------------------------------
            // Modelos de la familia BLAZEPOSE
            //----------------------------------------------------------------------------------------------------
            BlazePose blazePoseLite = new BlazePose(this, BlazePose.TYPE_LITE);
            blazePoseLite.run();

            BlazePose blazePoseFull = new BlazePose(this, BlazePose.TYPE_FULL);
            blazePoseFull.run();

            BlazePose blazePoseHeavy = new BlazePose(this, BlazePose.TYPE_HEAVY);
            blazePoseHeavy.run();
            //----------------------------------------------------------------------------------------------------

            //----------------------------------------------------------------------------------------------------
            // Modelos de la familia YOLO8-pose
            //----------------------------------------------------------------------------------------------------
            Yolo yolo8_nano = new Yolo(this, Yolo.TYPE_NANO);
            yolo8_nano.run();

            Yolo yolo8_small = new Yolo(this, Yolo.TYPE_SMALL);
            yolo8_small.run();

            Yolo yolo8_medium = new Yolo(this, Yolo.TYPE_MEDIUM);
            yolo8_medium.run();
            //----------------------------------------------------------------------------------------------------

            // Ejecutamos en el thread de la interfaz las actualizaciones de la misma
            runOnUiThread(() -> {
                // Actualizamos el estado del botón "EJECUTAR TEST" en la interfaz: HABILITADO
                executeButton.setEnabled(true);

                // Actualizamos el estado del botón "SALIR" en la interfaz: HABILITADO
                exitButton.setEnabled(true);

                // Actualizamos el estado del área de texto que muestra la ruta de los ficheros de salida generados al finalizar el test
//                textViewResultsPath.setText("FICHEROS GENERADOS EN: " + externalStorageDirectoryFile.getAbsolutePath() + "/Documents/" + MainActivity.APP_NAME);
//                textViewResultsPath.setText("FICHEROS GENERADOS EN: /Documents/" + MainActivity.APP_NAME);
                textViewResultsPath.setText("FICHEROS GENERADOS EN: /" + Environment.DIRECTORY_DOCUMENTS + "/" + MainActivity.APP_NAME);

//                File directory = this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
//                File desiredFilePath = new File(directory.toString());
//                System.out.println(">>>>>>>> [MainActivity] Folder documents: " + desiredFilePath);
            });

        }).start();
    }
}