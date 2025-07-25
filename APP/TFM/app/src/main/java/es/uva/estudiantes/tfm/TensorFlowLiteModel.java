package es.uva.estudiantes.tfm;

import android.widget.TextView;

import org.tensorflow.lite.support.image.ImageProcessor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.security.InvalidParameterException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Clase base para todos los modelos
 */
public abstract class TensorFlowLiteModel {
    /**
     * Constante con el numero de keypoints a estimar
     */
    protected static int NUM_KEYPOINTS_PREDICTION = 17;

    /**
     * Objeto MainActivity principal
     */
    protected MainActivity mainActivity;

    /**
     * Objeto componente de la interfaz para mostrar el proceso del test
     */
    protected TextView component;

    /**
     * Nombre del modelo
     */
    protected String modelName;
    /**
     * Nombre del fichero del modelo
     */
    protected String fileModelName;
    /**
     * Nombre del fichero de salida con las predicciones del modelo
     */
    protected String outputPredictionsFileName;
    /**
     * Nombre del fichero de salida con los rendimientos del modelo
     */
    protected String outputPerformanceFileName;

    /**
     * Lista de strings con los nombres de las imágenes
     */
    protected List<String> imageFileNamesList;

    /**
     * Hashmap con las predicciones por imagen
     */
    protected HashMap<String, float[]> imagePredictionsMap;

    /**
     * Hashmap con los rendimientos por imagen
     */
    protected HashMap<String, String> modelPerformanceMap;

    /**
     * Procesador de las imágenes
     */
    protected ImageProcessor imageProcessor;


    /**
     * Constructor de la clase
     */
    TensorFlowLiteModel() {
        // Leemos y comprobamos la lista de las imágenes a procesar
        this.imageFileNamesList = readImageFileNames();
        if (imageFileNamesList == null || imageFileNamesList.isEmpty()) {
            throw new InvalidParameterException("[TensorFlowLiteModel] ERROR: Invalid IMAGE FILE NAME LIST, not found or empty");
        }

        // Inicializamos el mapa de resultado de predicciones del modelo
        this.imagePredictionsMap = new HashMap<String, float[]>();

        // Inicializamos el mapa de informacion de rendimiento del modelo
        this.modelPerformanceMap = new HashMap<String, String>();
    }


    /**
     * Lee los nombres de las imágenes a aestimas del fichero con la lista
     *
     * @return lista de strings con los nombres de las imágenes
     */
    private List<String> readImageFileNames() {
        List<String> imageFileNamesListTmp = new ArrayList<String>();

        InputStream inputStreamImageFileNames = getClass().getClassLoader().getResourceAsStream("imageFileNames.txt");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStreamImageFileNames));
            if (inputStreamImageFileNames != null) {
                String imageFileName;
                while ((imageFileName = reader.readLine()) != null) {
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


    /**
     * Escribe los resultados a ficheros
     */
    protected void writeResultsToFiles() {
        writePredictionsToFile();

        writePerformanceToFile();
    }


    /**
     * Escribe las predicciones por imagen a fichero
     */
    protected void writePredictionsToFile() {
        try {
            // Convertimos la información de keypoints de cada imagen en una unica linea de texto
            List<String> imagesInfo = new ArrayList<String>();
            for (Map.Entry<String, float[]> imageOutput : this.imagePredictionsMap.entrySet()) {
                String imageIdString = imageOutput.getKey();
                int imageId = Integer.parseInt(imageIdString.substring(0, imageIdString.length() - 4));

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

            // Salvamos a un fichero en disco la información de la prediccion de los keypoints de todas las imágenes
            File outputPredictions = new File(MainActivity.outputFolderFile, this.outputPredictionsFileName);
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
            System.out.println(">>>>>>>> [TensorFlowLiteModel.writePredictionsToFile] Error: " + e.getMessage() + " <<<<<<<<");
        }
    }


    /**
     * Escribe los rendimientos por imagen a fichero
     */
    private void writePerformanceToFile() {
        try {
            // Salvamos a un fichero en disco la información del rendimiento de todas las imágenes
            File outputPerformanceFile = new File(MainActivity.outputFolderFile, this.outputPerformanceFileName);
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
            System.out.println(">>>>>>>> [TensorFlowLiteModel.writePerformanceToFile] Error: " + e.getMessage() + " <<<<<<<<");
        }
    }
}
