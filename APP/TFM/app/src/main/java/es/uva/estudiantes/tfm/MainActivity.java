package es.uva.estudiantes.tfm;


import static java.lang.System.exit;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;


import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.CastOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.common.ops.QuantizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import org.tensorflow.lite.InterpreterFactory;
import org.tensorflow.lite.InterpreterApi;





public class MainActivity extends AppCompatActivity {

    public static String APP_NAME = "TFM_final";

    public static File destinationFolderFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



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
        this.destinationFolderFile = new File (externalStorageDirectoryFile.getAbsolutePath() + "/Documents/" + MainActivity.APP_NAME);
        if(!this.destinationFolderFile.exists()) {
            this.destinationFolderFile.mkdirs();

System.out.println(">>>>>>>> [MainActivity] 00000000 created!!!");

        }


        //----------------------------------------------------------------------------------------------------
        // MOVENET MODELS
        //----------------------------------------------------------------------------------------------------

        Movenet movenet_lightning_8 = new Movenet(this, Movenet.TYPE_LIGHTNING, Movenet.DATA_TYPE_UINT8);
        movenet_lightning_8.run();
        System.out.println("----------------------------------------------------------------------------------------------------");

        Movenet movenet_lightning_16 = new Movenet(this, Movenet.TYPE_LIGHTNING, Movenet.DATA_TYPE_FLOAT16);
        movenet_lightning_16.run();
        System.out.println("----------------------------------------------------------------------------------------------------");

        Movenet movenet_lightning_32 = new Movenet(this, Movenet.TYPE_LIGHTNING, Movenet.DATA_TYPE_FLOAT32);
        movenet_lightning_32.run();
        System.out.println("----------------------------------------------------------------------------------------------------");



        Movenet movenet_thunder_8 = new Movenet(this, Movenet.TYPE_THUNDER, Movenet.DATA_TYPE_UINT8);
        movenet_thunder_8.run();
        System.out.println("----------------------------------------------------------------------------------------------------");

        Movenet movenet_thunder_16 = new Movenet(this, Movenet.TYPE_THUNDER, Movenet.DATA_TYPE_FLOAT16);
        movenet_thunder_16.run();
        System.out.println("----------------------------------------------------------------------------------------------------");

        Movenet movenet_thunder_32 = new Movenet(this, Movenet.TYPE_THUNDER, Movenet.DATA_TYPE_FLOAT32);
        movenet_thunder_32.run();
        System.out.println("----------------------------------------------------------------------------------------------------");


        //----------------------------------------------------------------------------------------------------




        //----------------------------------------------------------------------------------------------------
        // POSENET MODELS                   ¡¡¡¡¡¡¡¡DESCARTADOS!!!!!!!!
        //----------------------------------------------------------------------------------------------------

//        Posenet posenet = new Posenet(this);
//        posenet.run();

//        System.out.println("----------------------------------------------------------------------------------------------------");

        //----------------------------------------------------------------------------------------------------






        //----------------------------------------------------------------------------------------------------
        // BLAZEPOSE MODELS
        //----------------------------------------------------------------------------------------------------
//
//        BlazePose blazePoseLite = new BlazePose(this, BlazePose.TYPE_LITE);
//        blazePoseLite.run();
//        System.out.println("----------------------------------------------------------------------------------------------------");
//
//        BlazePose blazePoseFull = new BlazePose(this, BlazePose.TYPE_FULL);
//        blazePoseFull.run();
//        System.out.println("----------------------------------------------------------------------------------------------------");
//
//        BlazePose blazePoseHeavy = new BlazePose(this, BlazePose.TYPE_HEAVY);
//        blazePoseHeavy.run();
//        System.out.println("----------------------------------------------------------------------------------------------------");

        //----------------------------------------------------------------------------------------------------



//        System.exit(0);
//        this.finishAffinity();
//        finishAndRemoveTask();


    }







}