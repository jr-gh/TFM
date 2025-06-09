package es.uva.estudiantes.tfm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.InterpreterApi;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BlazePose extends TensorFlowLiteModel {

    public final static int TYPE_LITE = 1;
    public final static int TYPE_FULL = 2;
    public final static int TYPE_HEAVY = 3;

    private final static float MIN_VISIBILITY = 0.2f;
    private final static float MAX_VISIBILITY = 0.6f;

    private int inputWidth = 256;
    private int inputHeight = 256;

    // Mapa con los keypoints de COCO (17 puntos) que usaremos de las estimaciones de BlazePose (33 puntos)
    public static final Map<Integer, Integer> COCO_BLAZEPOSE_KEYPOINTS_MAP;

    static {
        COCO_BLAZEPOSE_KEYPOINTS_MAP = new HashMap<>();
        COCO_BLAZEPOSE_KEYPOINTS_MAP.put(0, 0);    // Nose
        COCO_BLAZEPOSE_KEYPOINTS_MAP.put(1, 2);    // Left eye (center)
        COCO_BLAZEPOSE_KEYPOINTS_MAP.put(2, 5);    // Right eye (center)
        COCO_BLAZEPOSE_KEYPOINTS_MAP.put(3, 7);    // Left ear
        COCO_BLAZEPOSE_KEYPOINTS_MAP.put(4, 8);    // Right ear
        COCO_BLAZEPOSE_KEYPOINTS_MAP.put(5, 11);   // Left shoulder
        COCO_BLAZEPOSE_KEYPOINTS_MAP.put(6, 12);   // Right shoulder
        COCO_BLAZEPOSE_KEYPOINTS_MAP.put(7, 13);   // Left elbow
        COCO_BLAZEPOSE_KEYPOINTS_MAP.put(8, 14);   // Right elbow
        COCO_BLAZEPOSE_KEYPOINTS_MAP.put(9, 15);   // Left wrist
        COCO_BLAZEPOSE_KEYPOINTS_MAP.put(10, 16);  // Right wrist
        COCO_BLAZEPOSE_KEYPOINTS_MAP.put(11, 23);  // Left hip
        COCO_BLAZEPOSE_KEYPOINTS_MAP.put(12, 24);  // Right hip
        COCO_BLAZEPOSE_KEYPOINTS_MAP.put(13, 25);  // Left knee
        COCO_BLAZEPOSE_KEYPOINTS_MAP.put(14, 26);  // Right knee
        COCO_BLAZEPOSE_KEYPOINTS_MAP.put(15, 27);  // Left ankle
        COCO_BLAZEPOSE_KEYPOINTS_MAP.put(16, 28);  // Right ankle
    }


    /**
     * @param contextParam
     * @param typeParam
     */
    protected BlazePose(Context contextParam, int typeParam) {
        // Comprobamos y leemos los ficheros de datos (lista de imagenes y anotaciones)
        super();

        // Comprobamos los parámetros de entrada
        if (contextParam == null) {
            throw new InvalidParameterException("[BlazePose] ERROR: Invalid CONTEXT parameter");
        }
        if (typeParam != TYPE_LITE && typeParam != TYPE_FULL && typeParam != TYPE_HEAVY) {
            throw new InvalidParameterException("[BlazePose] ERROR: Invalid TYPE parameter, must be one of LIGHTNING (1) or THUNDER (2)");
        }

//        // Comprobamos la lista de las imagenes a procesar
//        this.imageFileNameLst = readImageFileNames();
//        if (imageFileNameLst == null || imageFileNameLst.isEmpty()) {
//            throw new InvalidParameterException("[BlazePose] ERROR: Invalid IMAGE FILE NAME LIST, not found or empty");
//        }

        // Definimos los parámetros de la red
        this.context = contextParam;
        switch (typeParam) {
            case TYPE_LITE:
                // Modelo BlazePose lite
                this.fileModelName = "pose_landmark_lite.tflite";

                // Inicializamos el nombre del fichero donde almacenaremos las predicciones del modelo
                this.outputPredictionsFileName = "Blazepose_lite_predictions.json";
                // Inicializamos el nombre del fichero donde almacenaremos los datos de rendimiento del modelo
                this.outputPerformanceFileName = "Blazepose_lite_performance.txt";

                break;

            case TYPE_FULL:
                // Modelo BlazePose full
                this.fileModelName = "pose_landmark_full.tflite";

                // Inicializamos el nombre del fichero donde almacenaremos las predicciones del modelo
                this.outputPredictionsFileName = "Blazepose_full_predictions.json";
                // Inicializamos el nombre del fichero donde almacenaremos los datos de rendimiento del modelo
                this.outputPerformanceFileName = "Blazepose_full_performance.txt";

                break;

            case TYPE_HEAVY:
                // Modelo BlazePose heavy
                this.fileModelName = "pose_landmark_heavy.tflite";

                // Inicializamos el nombre del fichero donde almacenaremos las predicciones del modelo
                this.outputPredictionsFileName = "Blazepose_heavy_predictions.json";
                // Inicializamos el nombre del fichero donde almacenaremos los datos de rendimiento del modelo
                this.outputPerformanceFileName = "Blazepose_heavy_performance.txt";

                break;
        }

        // Inicializamos el procesador de las imagenes con los parametros (tamaño) del modelo especificado
        this.imageProcessor = new ImageProcessor.Builder().add(new ResizeOp(inputHeight, inputWidth, ResizeOp.ResizeMethod.BILINEAR)).build();
    }


    /**
     *
     */
    public void run() {
        try {

System.out.println(">>>>>>>> [BlazePose INIT "  + this.fileModelName + "] <<<<<<<<");

            // Creamos un TensorImage (contenedor de objeto imagen para TensorFlow) del tipo del modelo (float32)
            TensorImage inputTensorImage = new TensorImage(DataType.FLOAT32);

            // Inicializamos el modelo de la red
            MappedByteBuffer tfliteModelMappedFile = FileUtil.loadMappedFile(context, fileModelName);
            InterpreterApi interpreterApi = InterpreterApi.create(tfliteModelMappedFile, new InterpreterApi.Options());

            // Comprobamos si se ha incializado correctamente
            if (interpreterApi != null) {
                // Creamos el objeto output con el formato HashMap de cinco arrays de floats con los tamaños de salida la red:
                // (Al ser irregular lo hacemos a mano)
                //    float[1][95]
                //    float[1][1]
                //    float[1][256][256][1]
                //    float[1][64][64][39]
                //    float[1][117]
                Map<Integer, Object> outputMap = new HashMap<>();
                int[] outputShapeTemp0 = interpreterApi.getOutputTensor(0).shape();
                float[][] outputTemp0 = new float[outputShapeTemp0[0]][outputShapeTemp0[1]];
                outputMap.put(0, outputTemp0);
                int[] outputShapeTemp1 = interpreterApi.getOutputTensor(1).shape();
                float[][] outputTemp1 = new float[outputShapeTemp1[0]][outputShapeTemp1[1]];
                outputMap.put(1, outputTemp1);
                int[] outputShapeTemp2 = interpreterApi.getOutputTensor(2).shape();
                float[][][][] outputTemp2 = new float[outputShapeTemp2[0]][outputShapeTemp2[1]][outputShapeTemp2[2]][outputShapeTemp2[3]];
                outputMap.put(2, outputTemp2);
                int[] outputShapeTemp3 = interpreterApi.getOutputTensor(3).shape();
                float[][][][] outputTemp3 = new float[outputShapeTemp3[0]][outputShapeTemp3[1]][outputShapeTemp3[2]][outputShapeTemp3[3]];
                outputMap.put(3, outputTemp3);
                int[] outputShapeTemp4 = interpreterApi.getOutputTensor(4).shape();
                float[][] outputTemp4 = new float[outputShapeTemp4[0]][outputShapeTemp4[1]];
                outputMap.put(4, outputTemp4);


//                InterpreterApi.Options options = new InterpreterApi.Options();
//System.out.println("********> getNumThreads: " + options.getNumThreads());


                // Recorremos la lista de imagenes a procesar
                for (int i = 0; i < imageFileNamesList.size(); i++) {
                    // Tomamos el tiempo total de procesado de cada imagen
                    long timeTotalForImage = System.currentTimeMillis();

                    // Obtenemos un bitmap de la imagen
                    String imageFileNameTemp = imageFileNamesList.get(i);
                    Bitmap bitmap = BitmapFactory.decodeStream(getClass().getClassLoader().getResourceAsStream(imageFileNameTemp));

                    // Cargamos la imagen a procesar
                    inputTensorImage.load(bitmap);

                    // Preprocesamos la imagen para adecuarla al tamaño de entrada definido por el modelo y que hemos especificado en el procesador de imagenes
                    inputTensorImage = imageProcessor.process(inputTensorImage);
                    Object[] input = new Object[]{inputTensorImage.getBuffer()};

                    // Tomamos el tiempo para cada imagen
                    long timeForModelEstimation = System.currentTimeMillis();

                    // Ejecutamos la red en la imagen
                    interpreterApi.runForMultipleInputsOutputs(input, outputMap);

                    // Recogemos el tiempo que ha tardado cada imagen
                    timeForModelEstimation = System.currentTimeMillis() - timeForModelEstimation;
                    // Recogemos el tiempo de inferencia nativo de la ultima operación
                    long timeNativeInference = interpreterApi.getLastNativeInferenceDurationNanoseconds() / 1000000;


                    // SACAR EL TAMAÑO DE LA IMAGEN DE ENTRADA AL MODELO: SON FIJOS (no hace falta sacarlos de la entrada)                                          **** QUITAR ****
//                    int inputWidth = interpreterApi.getInputTensor(0).shape()[1];                                                                                 **** QUITAR ****
//                    int inputHeight = interpreterApi.getInputTensor(0).shape()[2];                                                                                **** QUITAR ****


                    // Calculamos los ratios de ancho y alto con respecto a la imagen original
                    float widthRatio = (float) (bitmap.getWidth()) / inputWidth;
                    float heightRatio = (float) (bitmap.getHeight()) / inputHeight;



                    // Obenemos el numero de puntos estimados de la salida de la imagen procesada
//                    int[] outputShape = interpreterApi.getOutputTensor(0).shape();
//                    int numKeyPoints = outputShape[2];

                    // Obtenemos los datos de la salida estimada
//                    float[] output = outputTensorBuffer.getFloatArray();



// **************** TRAZAS QUITAR ****************
                    int numKeyPoints = 33;
                    for (int j = 0; j < numKeyPoints; j++) {
                        float x = ((float[][]) outputMap.get(0))[0][j * 5] * widthRatio;
                        float y = ((float[][]) outputMap.get(0))[0][j * 5 + 1] * heightRatio;

//                        float score = ((float[][])outputMap.get(0))[0][j * 5 + 2];                  // ESTO ESTA MAL: no son los scores (no hacen falta)
//System.out.println("********> PUNTO " + j + ": " + x + ", " + y + ", " + score);

                        float visibility = ((float[][]) outputMap.get(0))[0][j * 5 + 3];
                        float presence = ((float[][]) outputMap.get(0))[0][j * 5 + 4];
//System.out.println("********> PUNTO " + j + ": " + x + ", " + y + ", " + sigmoid(visibility) + ", " + sigmoid(presence));
                    }
// **************** TRAZAS QUITAR ****************



                    // Sacamos los datos de la estimación de los puntos de la imagen
                    // Los puntos de este modelo tienen que ser recogidos de forma 'manual' ya que hace una prediccion
                    // de mas puntos que la familia de modelos movenet y utilizaremos solo los coincidentes
                    float[] predictionsTmp = new float[NUM_KEYPOINTS_PREDICTION * 3];
//                    float[] predictionsTmp = new float[NUM_KEYPOINTS_PREDICTION * 2];

                    float[] keypointsTmp = ((float[][]) outputMap.get(0))[0];
//
//                    predictionsTmp[0] = keypointsTmp[0 * 5] * widthRatio;                           // 0: nose x
//                    predictionsTmp[1] = keypointsTmp[0 * 5 + 1] * heightRatio;                      // 0: nose y
//                    predictionsTmp[2] = keypointsTmp[2 * 5] * widthRatio;                           // 2: left_eye x
//                    predictionsTmp[3] = keypointsTmp[2 * 5 + 1] * heightRatio;                      // 2: left_eye y
//                    predictionsTmp[4] = keypointsTmp[5 * 5] * widthRatio;                           // 5: right_eye x
//                    predictionsTmp[5] = keypointsTmp[5 * 5 + 1] * heightRatio;                      // 5: right_eye y
//                    predictionsTmp[6] = keypointsTmp[7 * 5] * widthRatio;                           // 7: left_ear x
//                    predictionsTmp[7] = keypointsTmp[7 * 5 + 1] * heightRatio;                      // 7: left_ear y
//                    predictionsTmp[8] = keypointsTmp[8 * 5] * widthRatio;                           // 8: right_ear x
//                    predictionsTmp[9] = keypointsTmp[8 * 5 + 1] * heightRatio;                      // 8: right_ear y
//                    predictionsTmp[10] = keypointsTmp[11 * 5] * widthRatio;                         // 11: left_shoulder x
//                    predictionsTmp[11] = keypointsTmp[11 * 5 + 1] * heightRatio;                    // 11: left_shoulder y
//                    predictionsTmp[12] = keypointsTmp[12 * 5] * widthRatio;                         // 12: right_shoulder x
//                    predictionsTmp[13] = keypointsTmp[12 * 5 + 1] * heightRatio;                    // 12: right_shoulder y
//                    predictionsTmp[14] = keypointsTmp[13 * 5] * widthRatio;                         // 13: left_elbow x
//                    predictionsTmp[15] = keypointsTmp[13 * 5 + 1] * heightRatio;                    // 13: left_elbow y
//                    predictionsTmp[16] = keypointsTmp[14 * 5] * widthRatio;                         // 14: right_elbow x
//                    predictionsTmp[17] = keypointsTmp[14 * 5 + 1] * heightRatio;                    // 14: right_elbow y
//                    predictionsTmp[18] = keypointsTmp[15 * 5] * widthRatio;                         // 15: left_wrist x
//                    predictionsTmp[19] = keypointsTmp[15 * 5 + 1] * heightRatio;                    // 15: left_wrist y
//                    predictionsTmp[20] = keypointsTmp[16 * 5] * widthRatio;                         // 16: right_wrist x
//                    predictionsTmp[21] = keypointsTmp[16 * 5 + 1] * heightRatio;                    // 16: right_wrist y
//                    predictionsTmp[22] = keypointsTmp[23 * 5] * widthRatio;                         // 23: left_hip x
//                    predictionsTmp[23] = keypointsTmp[23 * 5 + 1] * heightRatio;                    // 23: left_hip y
//                    predictionsTmp[24] = keypointsTmp[24 * 5] * widthRatio;                         // 24: right_hip x
//                    predictionsTmp[25] = keypointsTmp[24 * 5 + 1] * heightRatio;                    // 24: right_hip y
//                    predictionsTmp[26] = keypointsTmp[25 * 5] * widthRatio;                         // 25: left_knee x
//                    predictionsTmp[27] = keypointsTmp[25 * 5 + 1] * heightRatio;                    // 25: left_knee y
//                    predictionsTmp[28] = keypointsTmp[26 * 5] * widthRatio;                         // 26: right_knee x
//                    predictionsTmp[29] = keypointsTmp[26 * 5 + 1] * heightRatio;                    // 26: right_knee y
//                    predictionsTmp[30] = keypointsTmp[27 * 5] * widthRatio;                         // 27: left_ankle x
//                    predictionsTmp[31] = keypointsTmp[27 * 5 + 1] * heightRatio;                    // 27: left_ankle y
//                    predictionsTmp[32] = keypointsTmp[28 * 5] * widthRatio;                         // 28: right_ankle x
//                    predictionsTmp[33] = keypointsTmp[28 * 5 + 1] * heightRatio;                    // 28: right_ankle y
//
////                    // +3 para visibilidad y +4 para presencia
////                    predictionsTmp[34] = sigmoid(keypointsTmp[0 * 5 + 3]);                          // 0: nose probability
////                    predictionsTmp[35] = sigmoid(keypointsTmp[2 * 5 + 3]);                          // 1: left_eye probability
////                    predictionsTmp[36] = sigmoid(keypointsTmp[5 * 5 + 3]);                          // 2: right_eye probability
////                    predictionsTmp[37] = sigmoid(keypointsTmp[7 * 5 + 3]);                          // 3: left_ear probability
////                    predictionsTmp[38] = sigmoid(keypointsTmp[8 * 5 + 3]);                          // 4: right_ear probability
////                    predictionsTmp[39] = sigmoid(keypointsTmp[11 * 5 + 3]);                         // 5: left_shoulder probability
////                    predictionsTmp[40] = sigmoid(keypointsTmp[12 * 5 + 3]);                         // 6: right_shoulder probability
////                    predictionsTmp[41] = sigmoid(keypointsTmp[13 * 5 + 3]);                         // 7: left_elbow probability
////                    predictionsTmp[42] = sigmoid(keypointsTmp[14 * 5 + 3]);                         // 8: right_elbow probability
////                    predictionsTmp[43] = sigmoid(keypointsTmp[15 * 5 + 3]);                         // 9: left_wrist probability
////                    predictionsTmp[44] = sigmoid(keypointsTmp[16 * 5 + 3]);                         // 10: right_wrist probability
////                    predictionsTmp[45] = sigmoid(keypointsTmp[23 * 5 + 3]);                         // 11: left_hip probability
////                    predictionsTmp[46] = sigmoid(keypointsTmp[24 * 5 + 3]);                         // 12: right_hip probability
////                    predictionsTmp[47] = sigmoid(keypointsTmp[25 * 5 + 3]);                         // 13: left_knee probability
////                    predictionsTmp[48] = sigmoid(keypointsTmp[26 * 5 + 3]);                         // 14: right_knee probability
////                    predictionsTmp[49] = sigmoid(keypointsTmp[27 * 5 + 3]);                         // 15: left_ankle probability
////                    predictionsTmp[50] = sigmoid(keypointsTmp[28 * 5 + 3]);                         // 16: right_ankle probability


                    // SEGUNDO INTENTO DE PROCESADO DE KEYPOINTS Y VISIBILIDAD

//                    predictionsTmp[0] = keypointsTmp[0 * 5] * widthRatio;                           // 0: nose x
//                    predictionsTmp[1] = keypointsTmp[0 * 5 + 1] * heightRatio;                      // 0: nose y
//                    predictionsTmp[2] = convertVisibilityToCOCO(keypointsTmp[0 * 5 + 3], keypointsTmp[0 * 5 + 4]);       // CAMBIADO: antes tenia 1 FIJO (en todos)!!
//                    predictionsTmp[3] = keypointsTmp[2 * 5] * widthRatio;                           // 2: left_eye x
//                    predictionsTmp[4] = keypointsTmp[2 * 5 + 1] * heightRatio;                      // 2: left_eye y
//                    predictionsTmp[5] = convertVisibilityToCOCO(keypointsTmp[2 * 5 + 3], keypointsTmp[2 * 5 + 4]);
//                    predictionsTmp[6] = keypointsTmp[5 * 5] * widthRatio;                           // 5: right_eye x
//                    predictionsTmp[7] = keypointsTmp[5 * 5 + 1] * heightRatio;                      // 5: right_eye y
//                    predictionsTmp[8] = convertVisibilityToCOCO(keypointsTmp[5 * 5 + 3], keypointsTmp[5 * 5 + 4]);
//                    predictionsTmp[9] = keypointsTmp[7 * 5] * widthRatio;                           // 7: left_ear x
//                    predictionsTmp[10] = keypointsTmp[7 * 5 + 1] * heightRatio;                      // 7: left_ear y
//                    predictionsTmp[11] = convertVisibilityToCOCO(keypointsTmp[7 * 5 + 3], keypointsTmp[7 * 5 + 4]);
//                    predictionsTmp[12] = keypointsTmp[8 * 5] * widthRatio;                           // 8: right_ear x
//                    predictionsTmp[13] = keypointsTmp[8 * 5 + 1] * heightRatio;                      // 8: right_ear y
//                    predictionsTmp[14] = convertVisibilityToCOCO(keypointsTmp[8 * 5 + 3], keypointsTmp[8 * 5 + 4]);
//                    predictionsTmp[15] = keypointsTmp[11 * 5] * widthRatio;                         // 11: left_shoulder x
//                    predictionsTmp[16] = keypointsTmp[11 * 5 + 1] * heightRatio;                    // 11: left_shoulder y
//                    predictionsTmp[17] = convertVisibilityToCOCO(keypointsTmp[11 * 5 + 3], keypointsTmp[11 * 5 + 4]);
//                    predictionsTmp[18] = keypointsTmp[12 * 5] * widthRatio;                         // 12: right_shoulder x
//                    predictionsTmp[19] = keypointsTmp[12 * 5 + 1] * heightRatio;                    // 12: right_shoulder y
//                    predictionsTmp[20] = convertVisibilityToCOCO(keypointsTmp[12 * 5 + 3], keypointsTmp[12 * 5 + 4]);
//                    predictionsTmp[21] = keypointsTmp[13 * 5] * widthRatio;                         // 13: left_elbow x
//                    predictionsTmp[22] = keypointsTmp[13 * 5 + 1] * heightRatio;                    // 13: left_elbow y
//                    predictionsTmp[23] = convertVisibilityToCOCO(keypointsTmp[13 * 5 + 3], keypointsTmp[13 * 5 + 4]);
//                    predictionsTmp[24] = keypointsTmp[14 * 5] * widthRatio;                         // 14: right_elbow x
//                    predictionsTmp[25] = keypointsTmp[14 * 5 + 1] * heightRatio;                    // 14: right_elbow y
//                    predictionsTmp[26] = convertVisibilityToCOCO(keypointsTmp[14 * 5 + 3], keypointsTmp[14 * 5 + 4]);
//                    predictionsTmp[27] = keypointsTmp[15 * 5] * widthRatio;                         // 15: left_wrist x
//                    predictionsTmp[28] = keypointsTmp[15 * 5 + 1] * heightRatio;                    // 15: left_wrist y
//                    predictionsTmp[29] = convertVisibilityToCOCO(keypointsTmp[15 * 5 + 3], keypointsTmp[15 * 5 + 4]);
//                    predictionsTmp[30] = keypointsTmp[16 * 5] * widthRatio;                         // 16: right_wrist x
//                    predictionsTmp[31] = keypointsTmp[16 * 5 + 1] * heightRatio;                    // 16: right_wrist y
//                    predictionsTmp[32] = convertVisibilityToCOCO(keypointsTmp[16 * 5 + 3], keypointsTmp[16 * 5 + 4]);
//                    predictionsTmp[33] = keypointsTmp[23 * 5] * widthRatio;                         // 23: left_hip x
//                    predictionsTmp[34] = keypointsTmp[23 * 5 + 1] * heightRatio;                    // 23: left_hip y
//                    predictionsTmp[35] = convertVisibilityToCOCO(keypointsTmp[23 * 5 + 3], keypointsTmp[23 * 5 + 4]);
//                    predictionsTmp[36] = keypointsTmp[24 * 5] * widthRatio;                         // 24: right_hip x
//                    predictionsTmp[37] = keypointsTmp[24 * 5 + 1] * heightRatio;                    // 24: right_hip y
//                    predictionsTmp[38] = convertVisibilityToCOCO(keypointsTmp[24 * 5 + 3], keypointsTmp[24 * 5 + 4]);
//                    predictionsTmp[39] = keypointsTmp[25 * 5] * widthRatio;                         // 25: left_knee x
//                    predictionsTmp[40] = keypointsTmp[25 * 5 + 1] * heightRatio;                    // 25: left_knee y
//                    predictionsTmp[41] = convertVisibilityToCOCO(keypointsTmp[25 * 5 + 3], keypointsTmp[25 * 5 + 4]);
//                    predictionsTmp[42] = keypointsTmp[26 * 5] * widthRatio;                         // 26: right_knee x
//                    predictionsTmp[43] = keypointsTmp[26 * 5 + 1] * heightRatio;                    // 26: right_knee y
//                    predictionsTmp[44] = convertVisibilityToCOCO(keypointsTmp[26 * 5 + 3], keypointsTmp[26 * 5 + 4]);
//                    predictionsTmp[45] = keypointsTmp[27 * 5] * widthRatio;                         // 27: left_ankle x
//                    predictionsTmp[46] = keypointsTmp[27 * 5 + 1] * heightRatio;                    // 27: left_ankle y
//                    predictionsTmp[47] = convertVisibilityToCOCO(keypointsTmp[27 * 5 + 3], keypointsTmp[27 * 5 + 4]);
//                    predictionsTmp[48] = keypointsTmp[28 * 5] * widthRatio;                         // 28: right_ankle x
//                    predictionsTmp[49] = keypointsTmp[28 * 5 + 1] * heightRatio;                    // 28: right_ankle y
//                    predictionsTmp[50] = convertVisibilityToCOCO(keypointsTmp[28 * 5 + 3], keypointsTmp[28 * 5 + 4]);



//                    proccessKeypointWithConfidenceInterval(predictionsTmp, 0, keypointsTmp[0 * 5] * widthRatio, keypointsTmp[0 * 5 + 1] * heightRatio, keypointsTmp[0 * 5 + 3], keypointsTmp[0 * 5 + 4]);

                    for (int j = 0; j < 17; j++) {
                        int blazeIdx = COCO_BLAZEPOSE_KEYPOINTS_MAP.get(j);

//                        float visibility = keypointsTmp[(blazeIdx * 5) + 3];
//                        float presence = keypointsTmp[(blazeIdx * 5) + 4];
//                        int visibilityCOCO = convertVisibilityToCOCO(visibility, presence);

                        float x = keypointsTmp[(blazeIdx * 5)] * widthRatio;
                        float y = keypointsTmp[(blazeIdx * 5) + 1] * heightRatio;

//                        predictionsTmp[(j*3)] = (visibility >= 0.5f && presence >= 0.5f)? x: 0.0f;
//                        predictionsTmp[(j*3)+1] = (visibility >= 0.5f && presence >= 0.5f)? y: 0.0f;

//                        predictionsTmp[(j*3)] = visibilityCOCO == 2? x: 0.0f;
//                        predictionsTmp[(j*3)+1] = visibilityCOCO == 2? y: 0.0f;
//                        predictionsTmp[(j*3)+2] = visibilityCOCO;

//                        predictionsTmp[(j*3)] = x;
//                        predictionsTmp[(j*3)+1] = y;
//                        predictionsTmp[(j*3)+2] = 0;

                        predictionsTmp[(j*3)] = x;
                        predictionsTmp[(j*3)+1] = y;
                        predictionsTmp[(j*3)+2] = 2;



//                        cocoKeypoints.add(x);
//                        cocoKeypoints.add(y);
//                        cocoKeypoints.add((float) v);
                    }




                    // Recogemos el tiempo total de procesado dela imagen
                    timeTotalForImage = System.currentTimeMillis() - timeTotalForImage;

                    // Creamos una cadena de texto con los valores de los tiempos tomados para la imagen
                    String modelPerformanceInfo = timeTotalForImage + "," + timeForModelEstimation + "," + timeNativeInference;

                    // Almacenamos los valores de cada imagen
                    this.imagePredictionsMap.put(imageFileNameTemp, predictionsTmp);
                    this.modelPerformanceMap.put(imageFileNameTemp, modelPerformanceInfo);

//System.out.println("********> IMAGEN " + imageFileNameTemp + " [" + modelPerformanceInfo + "] ms <********");

                }

                interpreterApi.close();

                this.writeResultsToFiles();


System.out.println(">>>>>>>> [BlazePose END  "  + this.fileModelName + "] <<<<<<<<");


            } else {
                System.out.println("[BlazePose] Critical error: couldn't instantiate Tensor Flow interpreter");
                System.exit(-1);
            }
        } catch (IOException e) {
            System.out.println("[BlazePose] Error: " + e.getMessage());
        }
    }


    /**
     * Convierte keypoints BlazePose a formato COCO con filtrado por umbral
     */
//    public static List<Float> proccessKeypointWithConfidenceInterval(List<Map<String, Float>> blazeKeypoints,
//                                                                     int imageWidth,
//                                                                     int imageHeight,
//                                                                     float threshold) {
//        List<Float> cocoKeypoints = new ArrayList<>();
//
//        for (int i = 0; i < 17; i++) {
//            int blazeIdx = COCO_BLAZEPOSE_KEYPOINTS_MAP.get(i);
//            Map<String, Float> kp = blazeKeypoints.get(blazeIdx);
//
//            float x = kp.containsKey("x") ? kp.get("x") : 0.0f;
//            float y = kp.containsKey("y") ? kp.get("y") : 0.0f;
//            float visibility = kp.containsKey("visibility") ? kp.get("visibility") : 0.0f;
//            float presence = kp.containsKey("presence") ? kp.get("presence") : 0.0f;
//
//            x *= imageWidth;
//            y *= imageHeight;
//
//            int v = (visibility >= threshold && presence >= threshold) ? 2 : 0;
//
//            cocoKeypoints.add(x);
//            cocoKeypoints.add(y);
//            cocoKeypoints.add((float) v);
//        }
//
//        return cocoKeypoints;
//    }


//    private float proccessKeypointWithConfidenceInterval(float[] predictions, int index, float x, float y, float visibility, float presence){
//        int newCoordinateValue;
//        if (sigmoid(visibility) < MIN_VISIBILITY || sigmoid(presence) < MIN_VISIBILITY) {
//            // Keypoint NO etiquetado
//            newCoordinateValue = 0;
//        } else if (sigmoid(visibility) < MAX_VISIBILITY) {
//            // Keypoint etiquetado pero NO visible
//            newCoordinateValue = 1;
//        } else {
//            // Keypoint visible
//            newCoordinateValue = 2;
//        }
//
//        return newCoordinateValue;
//    }


    private int convertVisibilityToCOCO(float visibility, float presence) {
        int visibilityCOCO;
        if (sigmoid(visibility) < MIN_VISIBILITY || sigmoid(presence) < MIN_VISIBILITY) {
            // Keypoint NO etiquetado
            visibilityCOCO = 0;
        } else if (sigmoid(visibility) < MAX_VISIBILITY) {
            // Keypoint etiquetado pero NO visible
            visibilityCOCO = 1;
        } else {
            // Keypoint visible
            visibilityCOCO = 2;
        }

        return visibilityCOCO;
    }


    public float sigmoid(float value) {
        float p = (float) (1.0 / (1 + Math.exp(-value)));
        return p;
    }


}
