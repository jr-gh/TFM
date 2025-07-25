package es.uva.estudiantes.tfm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * Clase actividad principal de la APP
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Nombre de la APP
     */
    public static String APP_NAME = "TFM_PoseTest";

    /**
     * Nombre del fichero zip de salida
     */
    public static String ZIP_NAME = "TFM_PoseTest.zip";

    /**
     * Nombre del folder para los ficheros de salida
     */
    public static File outputFolderFile;

    private Button executeButton;

    private Button shareButton;

    private Button exitButton;


    /**
     * Evento de creación de la actividad
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
                        .setMessage("Para optimizar los resultados de rendimiento cierre todas las aplicaciones antes de ejecutar el test.\n\nEste test puede tardar entre 15 y 30 minutos dependiendo del dispositivo ¿ejecutar el test ahora?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Ejecutamos secuencialmente en un thread separado todos los modelos sobre el dataset de imágenes selecccionadas
                                executeModels();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        // Establecemos la acción al hacer clic en el botón "COMPARTIR RESULTADOS"
        shareButton = findViewById(R.id.buttonShare);
        shareButton.setOnClickListener(v -> {
            File outputZipFile = new File(this.outputFolderFile, this.ZIP_NAME);

            Uri uri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    outputZipFile
            );

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/zip");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Compartir ZIP de resultados"));
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
                                // Ejecutamos la salida de la aplicación
                                System.exit(0);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        // Creamos la carpeta donde se generaran los ficheroas de salida
        File externalStorageDirectoryFile = android.os.Environment.getExternalStorageDirectory();
        this.outputFolderFile = new File(externalStorageDirectoryFile.getAbsolutePath() + "/" + Environment.DIRECTORY_DOCUMENTS + "/" + MainActivity.APP_NAME);
        if (!this.outputFolderFile.exists()) {
            this.outputFolderFile.mkdirs();
        }
    }


    /**
     * Ejecuta secuencialmente en un thread separado todos los modelos sobre el dataset de imágenes selecccionadas
     */
    private void executeModels() {

        // Creamos un thread nuevo para la ejecución de tareas largas
        new Thread(() -> {
            // Ejecutamos en el thread de la interfaz las actualizaciones del color de estado los componentes
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

            // Ejecutamos en el thread de la interfaz las actualizaciones de los componentes botones
            runOnUiThread(() -> {
                // Actualizamos el estado del botón "EJECUTAR TEST" en la interfaz: DESHABILITADO
                executeButton.setEnabled(false);

                // Actualizamos el estado del botón "COMPARTIR RESULTADOS" en la interfaz: DESHABILITADO
                shareButton.setEnabled(false);

                // Actualizamos el estado del botón "SALIR" en la interfaz: DESHABILITADO
                exitButton.setEnabled(false);
            });

            //----------------------------------------------------------------------------------------------------
            // Ejecutamos los modelos de la familia MOVENET
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
            // Ejecutamos los modelos de la familia BLAZEPOSE
            //----------------------------------------------------------------------------------------------------
            BlazePose blazePoseLite = new BlazePose(this, BlazePose.TYPE_LITE);
            blazePoseLite.run();

            BlazePose blazePoseFull = new BlazePose(this, BlazePose.TYPE_FULL);
            blazePoseFull.run();

            BlazePose blazePoseHeavy = new BlazePose(this, BlazePose.TYPE_HEAVY);
            blazePoseHeavy.run();
            //----------------------------------------------------------------------------------------------------

            //----------------------------------------------------------------------------------------------------
            // Ejecutamos los modelos de la familia YOLO8-pose
            //----------------------------------------------------------------------------------------------------
            Yolo yolo8_nano = new Yolo(this, Yolo.TYPE_NANO);
            yolo8_nano.run();

            Yolo yolo8_small = new Yolo(this, Yolo.TYPE_SMALL);
            yolo8_small.run();

            Yolo yolo8_medium = new Yolo(this, Yolo.TYPE_MEDIUM);
            yolo8_medium.run();
            //----------------------------------------------------------------------------------------------------

            // Ejecutamos en el thread de la interfaz las actualizaciones de los componentes botones
            runOnUiThread(() -> {
                // Actualizamos el estado del botón "EJECUTAR TEST" en la interfaz: HABILITADO
                executeButton.setEnabled(true);

                // Actualizamos el estado del botón "COMPARTIR RESULTADOS" en la interfaz: DESHABILITADO
                shareButton.setEnabled(true);

                // Actualizamos el estado del botón "SALIR" en la interfaz: HABILITADO
                exitButton.setEnabled(true);
            });

            // Generamos el zip de salida con todos los ficheros generados
            generateOutputZipFile();

        }).start();
    }


    /**
     * Genera un zip con todos los ficheros de salida
     */
    private void generateOutputZipFile() {
        try {
            // Creamos el fichero zip
            File outputFolderZipFile = new File(this.outputFolderFile, this.ZIP_NAME);
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(outputFolderZipFile));

            // Recuperamos la lista de ficheros de la carpeta destino
            File[] fileList = this.outputFolderFile.listFiles();

            // Añadimos los ficheros de salida eal fichero zip y los vamos borrando
            if (fileList != null) {
                for (File fileTemp : fileList) {
                    if (fileTemp.isFile() && !fileTemp.getName().equalsIgnoreCase(this.ZIP_NAME)) {
                        FileInputStream fis = new FileInputStream(fileTemp);
                        ZipEntry zipEntry = new ZipEntry(fileTemp.getName());
                        zipOutputStream.putNextEntry(zipEntry);

                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = fis.read(buffer)) > 0) {
                            zipOutputStream.write(buffer, 0, length);
                        }

                        zipOutputStream.closeEntry();
                        fis.close();

                        // Borramos el fichero de salida que hemos añadido al zip
                        fileTemp.delete();
                    }
                }
            }

            zipOutputStream.close();

        } catch (Exception e) {
            System.out.println(">>>>>>>> [MainActivity.generateOutputZipFile] Error: " + e.getMessage() + " <<<<<<<<");
        }
    }
}