package es.uva.estudiantes.tfm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import org.tensorflow.lite.support.image.ImageProcessor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TensorFlowLiteModel {

    protected static int NUM_KEYPOINTS_PREDICTION = 17;

    protected Context context;

    protected String fileModelName;
    protected String outputPredictionsFileName;
    protected String outputPerformanceFileName;


    protected List<String> imageFileNamesList;
//    protected HashMap<String, float[]> imageAnnotationsMap;

    protected HashMap<String, float[]> imagePredictionsMap;

    protected HashMap<String, String> modelPerformanceMap;

    protected ImageProcessor imageProcessor;


    /**
     *
     */
    TensorFlowLiteModel() {
        // Leemos y comprobamos la lista de las imagenes a procesar
        this.imageFileNamesList = readImageFileNames();
        if (imageFileNamesList == null || imageFileNamesList.isEmpty()) {
            throw new InvalidParameterException("[TensorFlowLiteModel] ERROR: Invalid IMAGE FILE NAME LIST, not found or empty");
        }

//        // Leemos y comprobamos el fichero de anotaciones
//        this.imageAnnotationsMap = readCsvAnnotationsFile();
//        if (imageAnnotationsMap == null || imageAnnotationsMap.isEmpty()) {
//            throw new InvalidParameterException("[TensorFlowLiteModel] ERROR: Invalid ANNOTATIONS FILE, not found or empty");
//        }

        // Inicializamos el mapa de resultado de predicciones del modelo
        this.imagePredictionsMap = new HashMap<String, float[]>();

        // Inicializamos el mapa de informacion de rendimiento del modelo
        this.modelPerformanceMap = new HashMap<String, String>();
    }


    /**
     * @return
     */
    private List<String> readImageFileNames() {
        List<String> imageFileNamesListTmp = new ArrayList<String>();

        InputStream inputStreamImageFileNames = getClass().getClassLoader().getResourceAsStream("imageFileNames.txt");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStreamImageFileNames));
            if (inputStreamImageFileNames != null) {
                String imageFileName;
                while ((imageFileName = reader.readLine()) != null) {

//System.out.println(">>>>>>>> [TensorFlowLiteModel] IMAGE >>>>>>>> " + imageFileName);

                    imageFileNamesListTmp.add(imageFileName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStreamImageFileNames.close();
            } catch (Throwable ignore) {
            }
        }

        return imageFileNamesListTmp;
    }


//    /**
//     *
//     * @return
//     */
//    private HashMap<String, float[]> readCsvAnnotationsFile() {
//        HashMap<String, float[]> imageAnnotationsMapTmp = new HashMap<String, float[]>();
//
//        InputStream inputStreamImageFileNames = getClass().getClassLoader().getResourceAsStream("annotations.csv");
//        try {
//            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStreamImageFileNames));
//            if (inputStreamImageFileNames != null) {
//                String annotationLine;
//                String[] annotationsArray;
//
//                String imageId;
//                float[] imagePoints = new float[34];                                                                            // ********
//
//                String[] imagePointsStrTmp;
//
//                while ((annotationLine = reader.readLine()) != null) {
//                    annotationsArray = annotationLine.split(",");
//
//                    imageId = annotationsArray[0];
//
//                    imagePointsStrTmp = Arrays.copyOfRange(annotationsArray, 1, annotationsArray.length);
//                    for(int i = 0; i < imagePointsStrTmp.length; i++){
//                        imagePoints[i] = Float.valueOf(imagePointsStrTmp[i]);
//
////System.out.print(", " + imagePoints[i]);
//                    }
//
////System.out.println(">>>>>>>> [TensorFlowLiteModel] ANNOTATION >>>>>>>> imageId: " + imageId + "; imagePoints: " + imagePoints.length);
//
//                    imageAnnotationsMapTmp.put(imageId, imagePoints);
//                }
//            }
//        }
//        catch(Exception e ){
//            e.printStackTrace();
//        }
//        finally {
//            try {
//                inputStreamImageFileNames.close();
//            }
//            catch (Throwable ignore) {
//            }
//        }
//
//        return imageAnnotationsMapTmp;
//    }


    protected void writeResultsToFiles() {
        writePredictionsToFile();

        writePerformanceToFile();
    }


    protected void writePredictionsToFile() {
        try {
            // Convertimos la información de keypoints de cada imagen en una unica linea de texto
            List<String> imagesInfo = new ArrayList<String>();
            for (Map.Entry<String, float[]> imageOutput : this.imagePredictionsMap.entrySet()) {
                String imageIdString = imageOutput.getKey();
                int imageId = Integer.parseInt(imageIdString.substring(0, imageIdString.length()-4));

                float[] imageKeypointsArray = imageOutput.getValue();
                String imageKeypoints = "";

                for (int j = 0; j < imageKeypointsArray.length; j++) {
                    imageKeypoints += imageKeypointsArray[j];
                    if (j < imageKeypointsArray.length - 1) {
                        imageKeypoints += ",";
                    }
                }

                String imageLine = "{\"image_id\":" + imageId + ",\"category_id\":1," + "\"keypoints\":[" + imageKeypoints + "], \"score\":0}";
                imagesInfo.add(imageLine);
            }


            // Salvamos a un fichero en disco la información de la prediccion de los keypoints de todas las imagenes
            File outputPredictions = new File(MainActivity.destinationFolderFile, this.outputPredictionsFileName);
            if (outputPredictions.exists()) {
                outputPredictions.delete();
            }

            if (outputPredictions.createNewFile()) {
                FileOutputStream outputFOS = new FileOutputStream(outputPredictions);
                BufferedWriter outputBW = new BufferedWriter(new OutputStreamWriter(outputFOS));

                outputBW.write("[");
                outputBW.newLine();

                for (int i = 0; i < imagesInfo.size(); i++) {
                    String imagePredictionLine = imagesInfo.get(i);

                    if (i < imagesInfo.size() - 1) {
                        imagePredictionLine += ",";
                    }
                    outputBW.write(imagePredictionLine);
                    outputBW.newLine();
                }

                outputBW.write("]");

                outputBW.close();
                outputFOS.close();
            }

        } catch (Exception e) {
            System.out.println(">>>>>>>> [TensorFlowLiteModel] Error: " + e.getMessage() + " <<<<<<<<");
        }

//        checkExternalMedia();
//        writeToSDFile();

    }


    private void writePerformanceToFile() {
        try {
            // Salvamos a un fichero en disco la información del rendimiento de todas las imagenes
            File outputPerformanceFile = new File(MainActivity.destinationFolderFile, this.outputPerformanceFileName);
            if (outputPerformanceFile.exists()) {
                outputPerformanceFile.delete();
            }

            if (outputPerformanceFile.createNewFile()) {
                FileOutputStream outputFOS = new FileOutputStream(outputPerformanceFile);
                BufferedWriter outputBW = new BufferedWriter(new OutputStreamWriter(outputFOS));

                for (Map.Entry<String, String> imageInfo : this.modelPerformanceMap.entrySet()) {
                    String imageId = imageInfo.getKey();
                    String imageData = this.modelPerformanceMap.get(imageId);

                    String imagePredictionLine = imageId + "," + imageData;
                    outputBW.write(imagePredictionLine);
                    outputBW.newLine();
                }

                outputBW.close();
                outputFOS.close();
            }
        } catch (Exception e) {
            System.out.println(">>>>>>>> [TensorFlowLiteModel] Error: " + e.getMessage() + " <<<<<<<<");
        }
    }




//    protected void writeResultsToFile() {
//        File externalStorageDirectoryFile = android.os.Environment.getExternalStorageDirectory();
//        File downloadFolderFile = new File (externalStorageDirectoryFile.getAbsolutePath() + "/download/" + MainActivity.APP_NAME);
//        if(!downloadFolderFile.exists()) {
//            downloadFolderFile.mkdirs();
//        }
//
//        try {
//            // Convertimos la información de keypoints de cada imagen en una unica linea de texto
//            HashMap<String, String> imagesInfo = new HashMap<String, String>();
//            for (Map.Entry<String, float[]> imageOutput : this.imagePredictionsMap.entrySet()) {
//                String imageId = imageOutput.getKey();
//
//                float[] imageKeypointsArray = imageOutput.getValue();
//                String imageKeypoints = "";
//
//                for (int j = 0; j < imageKeypointsArray.length; j++) {
//                    imageKeypoints += imageKeypointsArray[j];
//                    if (j < imageKeypointsArray.length - 1) {
//                        imageKeypoints += ",";
//                    }
//                }
//                imagesInfo.put(imageId, imageKeypoints);
//            }
//
//            // Salvamos a un fichero en disco la información de la prediccion de los keypoints de todas las imagenes
//            File outputPredictions = new File(downloadFolderFile, this.outputPredictionsFileName);
//            writeToFile(outputPredictions, imagesInfo);
//
//            // Salvamos a un fichero en disco la información del rendimiento de todas las imagenes
//            File outputPerformanceFile = new File(downloadFolderFile, this.outputPerformanceFileName);
//            writeToFile(outputPerformanceFile, this.modelPerformanceMap);
//
//        } catch (Exception e) {
//            System.out.println(">>>>>>>> [TensorFlowLiteModel] Error: " + e.getMessage() + " <<<<<<<<");
//        }
//
////        checkExternalMedia();
////        writeToSDFile();
//
//    }


//    private void writeToFile(File outputFile, HashMap<String, String> imagesInfo) {
//        try {
//            if (outputFile.exists()) {
//                outputFile.delete();
//            }
//
//            if (outputFile.createNewFile()) {
//                FileOutputStream outputFOS = new FileOutputStream(outputFile);
//                BufferedWriter outputBW = new BufferedWriter(new OutputStreamWriter(outputFOS));
//
//                for (Map.Entry<String, String> imageInfo : imagesInfo.entrySet()) {
//                    String imageId = imageInfo.getKey();
//                    String imageData = imagesInfo.get(imageId);
//
//                    String imagePredictionLine = imageId + "," + imageData;
//                    outputBW.write(imagePredictionLine);
//                    outputBW.newLine();
//                }
//
//                outputBW.close();
//                outputFOS.close();
//            }
//        } catch (Exception e) {
//            System.out.println(">>>>>>>> [TensorFlowLiteModel] Error: " + e.getMessage() + " <<<<<<<<");
//        }
//    }





    /**
     *
     */
    protected abstract void run();


//    /**
//     *
//     */
//    protected void analize() {
//
//    }









//    private void checkExternalMedia(){
//        boolean mExternalStorageAvailable = false;
//        boolean mExternalStorageWriteable = false;
//        String state = Environment.getExternalStorageState();
//
//        if (Environment.MEDIA_MOUNTED.equals(state)) {
//            // Can read and write the media
//            mExternalStorageAvailable = mExternalStorageWriteable = true;
//        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
//            // Can only read the media
//            mExternalStorageAvailable = true;
//            mExternalStorageWriteable = false;
//        } else {
//            // Can't read or write
//            mExternalStorageAvailable = mExternalStorageWriteable = false;
//        }
////        tv.append("\n\nExternal Media: readable=" +mExternalStorageAvailable+" writable="+mExternalStorageWriteable);
//    }
//
//    /** Method to write ascii text characters to file on SD card. Note that you must add a
//     WRITE_EXTERNAL_STORAGE permission to the manifest file or this method will throw
//     a FileNotFound Exception because you won't have write permission. */
//
//    private void writeToSDFile(){
//
//        // Find the root of the external storage.
//        // See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal
//
//        File root = android.os.Environment.getExternalStorageDirectory();
////        tv.append("\nExternal file system root: "+root);
//
//        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder
//
//        File dir = new File (root.getAbsolutePath() + "/download");
//        dir.mkdirs();
//        File file = new File(dir, "myData.txt");
//
//        try {
//            FileOutputStream f = new FileOutputStream(file);
//            PrintWriter pw = new PrintWriter(f);
//            pw.println("Hi , How are you");
//            pw.println("Hello");
//            pw.flush();
//            pw.close();
//            f.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            System.out.println("******* File not found. Did you" + " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
////        tv.append("\n\nFile written to "+file);
//    }
//
//    /** Method to read in a text file placed in the res/raw directory of the application. The
//     method reads in all lines of the file sequentially. */
//
//    private void readRaw(){
////        tv.append("\nData read from res/raw/textfile.txt:");
//        InputStream is = context.getResources().openRawResource(R.raw.textfile);
//        InputStreamReader isr = new InputStreamReader(is);
//        BufferedReader br = new BufferedReader(isr, 8192);    // 2nd arg is buffer size
//
//        // More efficient (less readable) implementation of above is the composite expression
//    /*BufferedReader br = new BufferedReader(new InputStreamReader(
//            this.getResources().openRawResource(R.raw.textfile)), 8192);*/
//
//        try {
//            String test;
//            while (true){
//                test = br.readLine();
//                // readLine() returns null if no more lines in the file
//                if(test == null) break;
////                tv.append("\n"+"    "+test);
//            }
//            isr.close();
//            is.close();
//            br.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
////        tv.append("\n\nThat is all");
//    }



}
