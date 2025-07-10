package es.uva.estudiantes.tfm;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.core.content.ContextCompat;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.InterpreterApi;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.security.InvalidParameterException;


/**
 *
 */
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
     * @param mainActivityParam
     * @param typeParam
     */
    protected Movenet(MainActivity mainActivityParam, int typeParam, int dataTypeParam) {
        // Comprobamos y leemos los ficheros de datos (lista de imagenes y anotaciones)
        super();

        // Comprobamos los parámetros de entrada
        if (mainActivityParam == null) {
            throw new InvalidParameterException("[Movenet] ERROR: Invalid CONTEXT parameter");
        }
        if (typeParam != TYPE_LIGHTNING && typeParam != TYPE_THUNDER) {
            throw new InvalidParameterException("[Movenet] ERROR: Invalid TYPE parameter, must be one of LIGHTNING (1) or THUNDER (2)");
        }
        if (dataTypeParam != DATA_TYPE_UINT8 && dataTypeParam != DATA_TYPE_FLOAT16 && dataTypeParam != DATA_TYPE_FLOAT32) {
            throw new InvalidParameterException("[Movenet] ERROR: Invalid TYPE parameter, must be one of UINT8 (1), FLOAT16 (2) or FLOAT32 (3)");
        }

        // Definimos los parámetros de la red
        this.mainActivity = mainActivityParam;

        switch (typeParam) {
            case TYPE_LIGHTNING:
                // Inicializamos los valores de alto y ancho para el modelo especificado
                inputWidth = 192;
                inputHeight = 192;

                switch (dataTypeParam) {
                    case DATA_TYPE_UINT8:
                        // Nombre visual para el modelo
                        this.modelName = "Movenet lightning 8";

                        // Modelo Movenet lightning int8
                        this.fileModelName = "lite-model_movenet_singlepose_lightning_tflite_int8_4.tflite";

                        // Inicializamos el nombre del fichero donde almacenaremos las predicciones del modelo
                        this.outputPredictionsFileName = "Movenet_lightning_predictions_8.json";
                        // Inicializamos el nombre del fichero donde almacenaremos los datos de rendimiento del modelo
                        this.outputPerformanceFileName = "Movenet_lightning_performance_8.txt";

                        // Inicializamos el valor del tipo de datos de entrada de la red
                        this.modelDataType = DataType.UINT8;

                        // Componente de la interfaz para mostrar el proceso del test
                        this.component = mainActivity.findViewById(R.id.textViewMovenetL8);
                        break;

                    case DATA_TYPE_FLOAT16:
                        // Nombre visual para el modelo
                        this.modelName = "Movenet lightning 16";

                        // Modelo Movenet lightning float 16
                        this.fileModelName = "lite-model_movenet_singlepose_lightning_tflite_float16_4.tflite";

                        // Inicializamos el nombre del fichero donde almacenaremos las predicciones del modelo
                        this.outputPredictionsFileName = "Movenet_lightning_predictions_16.json";
                        // Inicializamos el nombre del fichero donde almacenaremos los datos de rendimiento del modelo
                        this.outputPerformanceFileName = "Movenet_lightning_performance_16.txt";

                        // Inicializamos el valor del tipo de datos de entrada de la red
                        this.modelDataType = DataType.UINT8;

                        // Componente de la interfaz para mostrar el proceso del test
                        this.component = mainActivity.findViewById(R.id.textViewMovenetL16);
                        break;

                    case DATA_TYPE_FLOAT32:
                        // Nombre visual para el modelo
                        this.modelName = "Movenet lightning 32";

                        // Modelo Movenet lightning float32
                        this.fileModelName = "lite-model_movenet_singlepose_lightning_3.tflite";

                        // Inicializamos el nombre del fichero donde almacenaremos las predicciones del modelo
                        this.outputPredictionsFileName = "Movenet_lightning_predictions_32.json";
                        // Inicializamos el nombre del fichero donde almacenaremos los datos de rendimiento del modelo
                        this.outputPerformanceFileName = "Movenet_lightning_performance_32.txt";

                        // Inicializamos el valor del tipo de datos de entrada de la red
                        this.modelDataType = DataType.FLOAT32;

                        // Componente de la interfaz para mostrar el proceso del test
                        this.component = mainActivity.findViewById(R.id.textViewMovenetL32);
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
                        // Nombre visual para el modelo
                        this.modelName = "Movenet thunder 8";

                        // Modelo Movenet thunder int8
                        this.fileModelName = "lite-model_movenet_singlepose_thunder_tflite_int8_4.tflite";

                        // Inicializamos el nombre del fichero donde almacenaremos las predicciones del modelo
                        this.outputPredictionsFileName = "Movenet_thunder_predictions_8.json";
                        // Inicializamos el nombre del fichero donde almacenaremos los datos de rendimiento del modelo
                        this.outputPerformanceFileName = "Movenet_thunder_performance_8.txt";

                        // Inicializamos el valor del tipo de datos de entrada de la red
                        this.modelDataType = DataType.UINT8;

                        // Componente de la interfaz para mostrar el proceso del test
                        this.component = mainActivity.findViewById(R.id.textViewMovenetT8);
                        break;

                    case DATA_TYPE_FLOAT16:
                        // Nombre visual para el modelo
                        this.modelName = "Movenet thunder 16";

                        // Modelo Movenet thunder float16
                        this.fileModelName = "lite-model_movenet_singlepose_thunder_tflite_float16_4.tflite";

                        // Inicializamos el nombre del fichero donde almacenaremos las predicciones del modelo
                        this.outputPredictionsFileName = "Movenet_thunder_predictions_16.json";
                        // Inicializamos el nombre del fichero donde almacenaremos los datos de rendimiento del modelo
                        this.outputPerformanceFileName = "Movenet_thunder_performance_16.txt";

                        // Inicializamos el valor del tipo de datos de entrada de la red
                        this.modelDataType = DataType.UINT8;

                        // Componente de la interfaz para mostrar el proceso del test
                        this.component = mainActivity.findViewById(R.id.textViewMovenetT16);
                        break;

                    case DATA_TYPE_FLOAT32:
                        // Nombre visual para el modelo
                        this.modelName = "Movenet thunder 32";

                        // Modelo Movenet thunder float32
                        this.fileModelName = "lite-model_movenet_singlepose_thunder_3.tflite";

                        // Inicializamos el nombre del fichero donde almacenaremos las predicciones del modelo
                        this.outputPredictionsFileName = "Movenet_thunder_predictions_32.json";
                        // Inicializamos el nombre del fichero donde almacenaremos los datos de rendimiento del modelo
                        this.outputPerformanceFileName = "Movenet_thunder_performance_32.txt";

                        // Inicializamos el valor del tipo de datos de entrada de la red
                        this.modelDataType = DataType.FLOAT32;

                        // Componente de la interfaz para mostrar el proceso del test
                        this.component = mainActivity.findViewById(R.id.textViewMovenetT32);
                        break;
                }

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

System.out.println("----------------------------------------------------------------------------------------------------");
System.out.println("[MOVENET.run] STARTED MODEL: " + this.modelName);

            // Ejecutamos en el thread de la interfaz la actualización del componente visual del modelo: AMARILLO (el modelo está ejecutando el test)
            mainActivity.runOnUiThread(() -> {
                component.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.uva_yellow));
            });

            // Inicializamos el modelo de la red
            MappedByteBuffer tfliteModelMappedFile = FileUtil.loadMappedFile(mainActivity, fileModelName);
            InterpreterApi interpreterApi = InterpreterApi.create(tfliteModelMappedFile, new InterpreterApi.Options());
//            File tfliteModelMappedFile = new File(fileModelName);
//            InterpreterApi interpreterApi = new Interpreter(tfliteModelMappedFile, new Interpreter.Options());

            // Creamos un TensorImage (contenedor de objeto imagen para TensorFlow) del tipo del modelo de la red (uint8, float16 o float32)
            TensorImage inputTensorImage = new TensorImage(modelDataType);

            // Creamos un TensorBuffer (buffer contenedor de datos para la salida) con el tamaño del modelo (1, 1, 17, 3) y el tipo del modelo (float32)
            TensorBuffer outputTensorBuffer = TensorBuffer.createFixedSize(new int[]{1, 1, 17, 3}, DataType.FLOAT32);

            // Comprobamos si se ha incializado correctamente
            if (interpreterApi != null) {
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

                    // Tomamos el tiempo para cada imagen
                    long timeForModelEstimation = System.currentTimeMillis();

                    // Ejecutamos la red en la imagen
                    interpreterApi.run(inputTensorImage.getBuffer(), outputTensorBuffer.getBuffer());

                    // Recogemos el tiempo que ha tardado cada imagen
                    timeForModelEstimation = System.currentTimeMillis() - timeForModelEstimation;
                    // Recogemos el tiempo de inferencia nativo de la ultima operación
                    long timeNativeInference = interpreterApi.getLastNativeInferenceDurationNanoseconds() / 1000000;

                    // Recuperamos el tamaño de la imagen real para la normalización de las coordenadas de los keypoints
                    int origWidth = bitmap.getWidth();
                    int origHeight = bitmap.getHeight();

                    // Obtenemos los datos de la salida estimada
                    float[] outputArray = outputTensorBuffer.getFloatArray();

                    // Almacenamos los datos de la estimación de los keypoints de la imagen
                    float[] predictionsTmp = new float[NUM_KEYPOINTS_PREDICTION * 3];

                    // Recorremos los keypoints de los datos de salida y los almacenamos normalizados
                    for (int j = 0; j < NUM_KEYPOINTS_PREDICTION; j++) {
                        int indexTemp = j * 3;

                        float x = outputArray[indexTemp + 1];
                        float y = outputArray[indexTemp];
                        float score = outputArray[indexTemp + 2];

                        predictionsTmp[indexTemp] = x * origWidth;
                        predictionsTmp[indexTemp + 1] = y * origHeight;
                        predictionsTmp[indexTemp + 2] = 2;
                    }

                    // Recogemos el tiempo total de procesado de la imagen
                    timeTotalForImage = System.currentTimeMillis() - timeTotalForImage;

                    // Creamos una cadena de texto con los valores de los tiempos tomados para la imagen
                    String modelPerformanceInfo = timeTotalForImage + "," + timeForModelEstimation + "," + timeNativeInference;

                    // Almacenamos los valores de cada imagen
                    this.imagePredictionsMap.put(imageFileNameTemp, predictionsTmp);
                    this.modelPerformanceMap.put(imageFileNameTemp, modelPerformanceInfo);
                }

                // Cerramos la red
                interpreterApi.close();

                // Escribimos a disco los resultados del modelo
                this.writeResultsToFiles();

                // Ejecutamos en el thread de la interfaz la actualización del componente visual del modelo: VERDE (el modelo ha terminado el test)
                mainActivity.runOnUiThread(() -> {
                    component.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.uva_green));
                });

System.out.println("[MOVENET.run] FINISHED MODEL: " + this.modelName);
System.out.println("----------------------------------------------------------------------------------------------------");

            } else {
                System.out.println("[MOVENET.run] CRITICAL ERROR: couldn't instantiate Tensor Flow interpreter");
                System.exit(-1);
            }

        } catch (IOException e) {
            // Ejecutamos en el thread de la interfaz la actualización del componente visual del modelo: ROJO (el modelo ha dado algún error)
            mainActivity.runOnUiThread(() -> {
                int color = ContextCompat.getColor(this.mainActivity, R.color.uva_red);
                component.setBackgroundColor(color);
            });

            System.out.println("[MOVENET.run] ERROR: " + e.getMessage());
        }
    }
}
