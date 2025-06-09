package es.uva.estudiantes.tfm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Debug;
import android.os.Trace;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.InterpreterApi;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.MappedByteBuffer;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Movenet extends TensorFlowLiteModel {

    public final static int TYPE_LIGHTNING = 1;
    public final static int TYPE_THUNDER = 2;

    public final static int DATA_TYPE_UINT8 = 1;
    public final static int DATA_TYPE_FLOAT16 = 2;
    public final static int DATA_TYPE_FLOAT32 = 3;

    private DataType modelDataType;

    private int inputWidth;
    private int inputHeight;


    /**
     * @param contextParam
     * @param typeParam
     */
    protected Movenet(Context contextParam, int typeParam, int dataTypeParam) {
        // Comprobamos y leemos los ficheros de datos (lista de imagenes y anotaciones)
        super();

        // Comprobamos los parámetros de entrada
        if (contextParam == null) {
            throw new InvalidParameterException("[Movenet] ERROR: Invalid CONTEXT parameter");
        }
        if (typeParam != TYPE_LIGHTNING && typeParam != TYPE_THUNDER) {
            throw new InvalidParameterException("[Movenet] ERROR: Invalid TYPE parameter, must be one of LIGHTNING (1) or THUNDER (2)");
        }
        if (dataTypeParam != DATA_TYPE_UINT8 && dataTypeParam != DATA_TYPE_FLOAT16 && dataTypeParam != DATA_TYPE_FLOAT32) {
            throw new InvalidParameterException("[Movenet] ERROR: Invalid TYPE parameter, must be one of UINT8 (1), FLOAT16 (2) or FLOAT32 (3)");
        }

        // Definimos los parámetros de la red
        this.context = contextParam;
        switch (typeParam) {
            case TYPE_LIGHTNING:
                // Inicializamos los valores de alto y ancho para el modelo especificado
                inputWidth = 192;
                inputHeight = 192;

                switch (dataTypeParam) {
                    case DATA_TYPE_UINT8:
                        // Modelo Movenet lightning
                        this.fileModelName = "lite-model_movenet_singlepose_lightning_tflite_int8_4.tflite";

                        // Inicializamos el nombre del fichero donde almacenaremos las predicciones del modelo
                        this.outputPredictionsFileName = "Movenet_lightning_predictions_8.json";
                        // Inicializamos el nombre del fichero donde almacenaremos los datos de rendimiento del modelo
                        this.outputPerformanceFileName = "Movenet_lightning_performance_8.txt";

                        // Inicializamos el valor del tipo de datos de entrada de la red
                        modelDataType = DataType.UINT8;
                        break;

                    case DATA_TYPE_FLOAT16:
                        // Modelo Movenet lightning
                        this.fileModelName = "lite-model_movenet_singlepose_lightning_tflite_float16_4.tflite";

                        // Inicializamos el nombre del fichero donde almacenaremos las predicciones del modelo
                        this.outputPredictionsFileName = "Movenet_lightning_predictions_16.json";
                        // Inicializamos el nombre del fichero donde almacenaremos los datos de rendimiento del modelo
                        this.outputPerformanceFileName = "Movenet_lightning_performance_16.txt";

                        // Inicializamos el valor del tipo de datos de entrada de la red
                        modelDataType = DataType.UINT8;
                        break;

                    case DATA_TYPE_FLOAT32:
                        // Modelo Movenet lightning
                        this.fileModelName = "lite-model_movenet_singlepose_lightning_3.tflite";

                        // Inicializamos el nombre del fichero donde almacenaremos las predicciones del modelo
                        this.outputPredictionsFileName = "Movenet_lightning_predictions_32.json";
                        // Inicializamos el nombre del fichero donde almacenaremos los datos de rendimiento del modelo
                        this.outputPerformanceFileName = "Movenet_lightning_performance_32.txt";

                        // Inicializamos el valor del tipo de datos de entrada de la red
                        modelDataType = DataType.FLOAT32;
                        break;
                }

                break;

            case TYPE_THUNDER:
                // Inicializamos los valores de alto y ancho para el modelo especificado
                inputWidth = 256;
                inputHeight = 256;

                // Inicializamos el valor del tipo de datos de entrada de la red
                switch (dataTypeParam) {
                    case DATA_TYPE_UINT8:
                        // Modelo Movenet thunder
                        this.fileModelName = "lite-model_movenet_singlepose_thunder_tflite_int8_4.tflite";

                        // Inicializamos el nombre del fichero donde almacenaremos las predicciones del modelo
                        this.outputPredictionsFileName = "Movenet_thunder_predictions_8.json";
                        // Inicializamos el nombre del fichero donde almacenaremos los datos de rendimiento del modelo
                        this.outputPerformanceFileName = "Movenet_thunder_performance_8.txt";

                        // Inicializamos el valor del tipo de datos de entrada de la red
                        modelDataType = DataType.UINT8;
                        break;

                    case DATA_TYPE_FLOAT16:
                        // Modelo Movenet thunder
                        this.fileModelName = "lite-model_movenet_singlepose_thunder_tflite_float16_4.tflite";

                        // Inicializamos el nombre del fichero donde almacenaremos las predicciones del modelo
                        this.outputPredictionsFileName = "Movenet_thunder_predictions_16.json";
                        // Inicializamos el nombre del fichero donde almacenaremos los datos de rendimiento del modelo
                        this.outputPerformanceFileName = "Movenet_thunder_performance_16.txt";

                        // Inicializamos el valor del tipo de datos de entrada de la red
                        modelDataType = DataType.UINT8;
                        break;

                    case DATA_TYPE_FLOAT32:
                        // Modelo Movenet thunder
                        this.fileModelName = "lite-model_movenet_singlepose_thunder_3.tflite";

                        // Inicializamos el nombre del fichero donde almacenaremos las predicciones del modelo
                        this.outputPredictionsFileName = "Movenet_thunder_predictions_32.json";
                        // Inicializamos el nombre del fichero donde almacenaremos los datos de rendimiento del modelo
                        this.outputPerformanceFileName = "Movenet_thunder_performance_32.txt";

                        // Inicializamos el valor del tipo de datos de entrada de la red
                        modelDataType = DataType.FLOAT32;
                        break;
                }

                break;
        }


//Debug.startMethodTracing("sample");
//Trace.beginSection("ImageProcessorCreation");
System.out.println(">>>>>>>> [Movenet] <<<<<<<<");
System.out.println(">>>>>>>> [Movenet] debug.tflite.trace: " + System.getProperties().getProperty("debug.tflite.trace"));
System.getProperties().setProperty("debug.tflite.trace", "1");
System.out.println(">>>>>>>> [Movenet] debug.tflite.trace: " + System.getProperties().getProperty("debug.tflite.trace"));
System.out.println(">>>>>>>> [Movenet] <<<<<<<<");


        // Inicializamos el procesador de las imagenes con los parametros (tamaño) del modelo especificado
        this.imageProcessor = new ImageProcessor.Builder().add(new ResizeOp(inputHeight, inputWidth, ResizeOp.ResizeMethod.BILINEAR)).build();


//Trace.endSection();


    }


    /**
     *
     */
    public void run() {
        try {

System.out.println(">>>>>>>> [Movenet INIT "  + this.fileModelName + "] <<<<<<<<");


            // Creamos un TensorImage (contenedor de objeto imagen para TensorFlow) del tipo del modelo (uint8)
            TensorImage inputTensorImage = new TensorImage(modelDataType);

            // Creamos un TensorBuffer (buffer contenedor de datos para la salida) con el tamaño del modelo (1, 1, 17, 3) y el tipo del modelo (float32)
            TensorBuffer outputTensorBuffer = TensorBuffer.createFixedSize(new int[]{1, 1, 17, 3}, DataType.FLOAT32);

            // Inicializamos el modelo de la red
            MappedByteBuffer tfliteModelMappedFile = FileUtil.loadMappedFile(context, fileModelName);
            InterpreterApi interpreterApi = InterpreterApi.create(tfliteModelMappedFile, new InterpreterApi.Options());

            // Comprobamos si se ha incializado correctamente
            if (interpreterApi != null) {
                // Recorremos la lista de imagenes a procesar
                for (int i = 0; i < imageFileNamesList.size(); i++) {


//Debug.startMethodTracing("sample" + imageFileNamesList.get(i));
//Trace.beginSection("ImageProcess: " + imageFileNamesList.get(i));

                    // Tomamos el tiempo total de procesado de cada imagen
                    long timeTotalForImage = System.currentTimeMillis();

                    // Obtenemos un bitmap de la imagen
                    String imageFileNameTemp = imageFileNamesList.get(i);
                    Bitmap bitmap = BitmapFactory.decodeStream(getClass().getClassLoader().getResourceAsStream(imageFileNameTemp));

                    // Cargamos la imagen a procesar
                    inputTensorImage.load(bitmap);

                    // Preprocesamos la imagen para adecuarla al tamaño de entrada definido por el modelo y que hemos especificado en el procesador de imagenes
                    inputTensorImage = imageProcessor.process(inputTensorImage);

                    // Tomamos el tiempo para cada imagen
                    long timeForModelEstimation = System.currentTimeMillis();

                    // Ejecutamos la red en la imagen
                    interpreterApi.run(inputTensorImage.getBuffer(), outputTensorBuffer.getBuffer());

                    // Recogemos el tiempo que ha tardado cada imagen
                    timeForModelEstimation = System.currentTimeMillis() - timeForModelEstimation;
                    // Recogemos el tiempo de inferencia nativo de la ultima operación
                    long timeNativeInference = interpreterApi.getLastNativeInferenceDurationNanoseconds() / 1000000;


//Debug.stopMethodTracing();


                    // SACAR EL TAMAÑO DE LA IMAGEN DE ENTRADA AL MODELO: SON FIJOS (no hace falta sacarlos de la entrada)                                          **** QUITAR ****
//                    int inputWidth = interpreterApi.getInputTensor(0).shape()[1];                                                                                 **** QUITAR ****
//                    int inputHeight = interpreterApi.getInputTensor(0).shape()[2];                                                                                **** QUITAR ****


                    // Calculamos los ratios de ancho y alto con respecto a la imagen original
//                    float widthRatio = (float) (bitmap.getWidth()) / inputWidth;
//                    float heightRatio = (float) (bitmap.getHeight()) / inputHeight;


                    // Obenemos el numero de puntos estimados de la salida de la imagen procesada
                    int[] outputShape = interpreterApi.getOutputTensor(0).shape();
                    int numKeyPoints = outputShape[2];

                    // Obtenemos los datos de la salida estimada
                    float[] output = outputTensorBuffer.getFloatArray();

                    // Sacamos los datos de la estimación de los puntos de la imagen
                    float[] predictionsTmp = new float[NUM_KEYPOINTS_PREDICTION * 3];
//                    float[] predictionsTmp = new float[NUM_KEYPOINTS_PREDICTION * 2];

                    for (int j = 0; j < numKeyPoints; j++) {
////                        float x2 = output[j * 3 + 1] * inputWidth * widthRatio;
////                        float y2 = output[j * 3 + 0] * inputHeight * heightRatio;
////
////                        float score = output[j * 3 + 2];                                            // LOS SCORES NO HACEN FALTA
////System.out.println("********> PUNTO2  " + j + ": " + x2 + ", " + y2 + ", " + score);
//
//
//System.out.println("########> INDICE:" + (j * 3 + 1));
//System.out.println("########> VALOR :" + output[j * 3 + 1]);
//System.out.println("########> bitmap.getWidth():" + bitmap.getWidth());

                        int indexTemp = j * 3;
                        float x = output[indexTemp + 1] * (float) (bitmap.getWidth());
                        float y = output[indexTemp] * (float) (bitmap.getHeight());

                        float score = output[indexTemp + 2];


//System.out.println("########> x:" + x);
//System.out.println("########><########");
//
////System.out.println("********> PUNTO " + j + ": " + x + ", " + y);


//                        if(score > 0.1) {
                            predictionsTmp[indexTemp] = x;
                            predictionsTmp[indexTemp + 1] = y;
                            predictionsTmp[indexTemp + 2] = 2;                                              // Metemos un 2 en el tercer valor
//                        }
//                        else{
//                            predictionsTmp[indexTemp] = 0;
//                            predictionsTmp[indexTemp + 1] = 0;
//                            predictionsTmp[indexTemp + 2] = 0;                                              // Metemos un 0 en el tercer valor
//                        }
                    }

//                    // Scores
//                    int startIndexTemp = NUM_KEYPOINTS_PREDICTION * 2;
//                    for (int j = 0; j < numKeyPoints; j++) {
//                        float score = output[j * 3 + 2];
//                        predictionsTmp[startIndexTemp + j] = score;
//                    }


                    // Recogemos el tiempo total de procesado dela imagen
                    timeTotalForImage = System.currentTimeMillis() - timeTotalForImage;

                    // Creamos una cadena de texto con los valores de los tiempos tomados para la imagen
                    String modelPerformanceInfo = timeTotalForImage + "," + timeForModelEstimation + "," + timeNativeInference;

                    // Almacenamos los valores de cada imagen
                    this.imagePredictionsMap.put(imageFileNameTemp, predictionsTmp);
                    this.modelPerformanceMap.put(imageFileNameTemp, modelPerformanceInfo);

//System.out.println("********> IMAGEN " + imageFileNameTemp + " [" + modelPerformanceInfo + "] ms <********");

//Trace.endSection();


                }

                interpreterApi.close();

                this.writeResultsToFiles();


System.out.println(">>>>>>>> [Movenet END  "  + this.fileModelName + "] <<<<<<<<");


            } else {
                System.out.println("[Movenet] Critical error: couldn't instantiate Tensor Flow interpreter");
                System.exit(-1);
            }
        } catch (IOException e) {
            System.out.println("[Movenet] Error: " + e.getMessage());
        }

//Debug.stopMethodTracing();

    }


}
