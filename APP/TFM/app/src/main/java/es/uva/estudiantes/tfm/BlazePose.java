package es.uva.estudiantes.tfm;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.core.content.ContextCompat;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.InterpreterApi;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.CastOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;


/**
 * Clase de estimación de posturas de la familia de modelos BlazePose
 */
public class BlazePose extends TensorFlowLiteModel {

    /**
     * Constante para el tipo de modelo Lite
     */
    public final static int TYPE_LITE = 1;
    /**
     * Constante para el tipo de modelo Full
     */
    public final static int TYPE_FULL = 2;
    /**
     * Constante para el tipo de modelo Heavy
     */
    public final static int TYPE_HEAVY = 3;

    private DataType modelDataType;

    private int inputWidth = 256;
    private int inputHeight = 256;

    private static final Map<Integer, Integer> COCO_BLAZEPOSE_KEYPOINTS_MAP;

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
     * Constructor de la clase
     *
     * @param mainActivityParam Actividad principal de la APP
     * @param typeParam         Tipo de modelo
     */
    protected BlazePose(MainActivity mainActivityParam, int typeParam) {
        // Comprobamos y leemos los ficheros de datos (lista de imágenes y anotaciones)
        super();

        // Comprobamos los parámetros de entrada
        if (mainActivityParam == null) {
            throw new InvalidParameterException("[BLAZEPOSE] ERROR: Invalid CONTEXT parameter");
        }
        if (typeParam != TYPE_LITE && typeParam != TYPE_FULL && typeParam != TYPE_HEAVY) {
            throw new InvalidParameterException("[BLAZEPOSE] ERROR: Invalid TYPE parameter, must be one of LITE (1), FULL (2) or HEAVY (3)");
        }

        // Definimos los parámetros de la red
        this.mainActivity = mainActivityParam;

        switch (typeParam) {
            case TYPE_LITE:
                // Nombre visual para el modelo
                this.modelName = "BlazePose lite";

                // Modelo BlazePose lite
                this.fileModelName = "pose_landmark_lite.tflite";

                // Inicializamos el nombre del fichero donde almacenaremos las predicciones del modelo
                this.outputPredictionsFileName = "Blazepose_lite_predictions.json";
                // Inicializamos el nombre del fichero donde almacenaremos los datos de rendimiento del modelo
                this.outputPerformanceFileName = "Blazepose_lite_performance.txt";

                // Componente de la interfaz para mostrar el proceso del test
                this.component = mainActivity.findViewById(R.id.textViewBlazeposeL);
                break;

            case TYPE_FULL:
                // Nombre visual para el modelo
                this.modelName = "BlazePose full";

                // Modelo BlazePose full
                this.fileModelName = "pose_landmark_full.tflite";

                // Inicializamos el nombre del fichero donde almacenaremos las predicciones del modelo
                this.outputPredictionsFileName = "Blazepose_full_predictions.json";
                // Inicializamos el nombre del fichero donde almacenaremos los datos de rendimiento del modelo
                this.outputPerformanceFileName = "Blazepose_full_performance.txt";

                // Componente de la interfaz para mostrar el proceso del test
                this.component = mainActivity.findViewById(R.id.textViewBlazeposeF);
                break;

            case TYPE_HEAVY:
                // Nombre visual para el modelo
                this.modelName = "BlazePose heavy";

                // Modelo BlazePose heavy
                this.fileModelName = "pose_landmark_heavy.tflite";

                // Inicializamos el nombre del fichero donde almacenaremos las predicciones del modelo
                this.outputPredictionsFileName = "Blazepose_heavy_predictions.json";
                // Inicializamos el nombre del fichero donde almacenaremos los datos de rendimiento del modelo
                this.outputPerformanceFileName = "Blazepose_heavy_performance.txt";

                // Componente de la interfaz para mostrar el proceso del test
                this.component = mainActivity.findViewById(R.id.textViewBlazeposeH);
                break;
        }

        // Inicializamos el valor del tipo de datos de entrada de la red
        modelDataType = DataType.FLOAT32;

        // Inicializamos el procesador de las imágenes con los parametros (tamaño) del modelo especificado
        ImageProcessor.Builder imageProcessorBuilder = new ImageProcessor.Builder()
                .add(new ResizeOp(inputWidth, inputHeight, ResizeOp.ResizeMethod.BILINEAR))
                .add(new NormalizeOp(0f, 255f)) // Normaliza a [0,1] si es necesario
                .add(new CastOp(modelDataType));
        imageProcessor = imageProcessorBuilder.build();
    }


    /**
     * Método para correr la red sobre las imágenes
     */
    public void run() {
        try {

            System.out.println("----------------------------------------------------------------------------------------------------");
            System.out.println("[BLAZEPOSE.run] STARTED MODEL: " + this.modelName);

            // Ejecutamos en el thread de la interfaz la actualización del componente visual del modelo: AMARILLO (el modelo está ejecutando el test)
            mainActivity.runOnUiThread(() -> {
                component.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.uva_yellow));
            });

            // Inicializamos el modelo de la red
            MappedByteBuffer tfliteModelMappedFile = FileUtil.loadMappedFile(mainActivity, fileModelName);
            InterpreterApi interpreterApi = InterpreterApi.create(tfliteModelMappedFile, new InterpreterApi.Options());

            // Creamos un TensorImage (contenedor de objeto imagen para TensorFlow) del tipo del modelo de la red (uint8, float16 o float32)
            TensorImage inputTensorImage = new TensorImage(modelDataType);

            // Creamos un TensorBuffer (buffer contenedor de datos para la salida) con el tamaño del modelo (1, 195) y el tipo del modelo (float32)
            TensorBuffer outputTensorBuffer = TensorBuffer.createFixedSize(new int[]{1, 195}, modelDataType);

            // Comprobamos si se ha incializado correctamente
            if (interpreterApi != null) {
                // Recorremos la lista de imágenes a procesar
                for (int i = 0; i < imageFileNamesList.size(); i++) {
                    // Tomamos el tiempo total de procesado de cada imagen
                    long timeTotalForImage = System.currentTimeMillis();

                    // Obtenemos un bitmap de la imagen
                    String imageFileNameTemp = imageFileNamesList.get(i);
                    Bitmap bitmap = BitmapFactory.decodeStream(getClass().getClassLoader().getResourceAsStream(imageFileNameTemp));

                    // Cargamos la imagen a procesar
                    inputTensorImage.load(bitmap);

                    // Preprocesamos la imagen para adecuarla al tamaño de entrada definido por el modelo y que hemos especificado en el procesador de imágenes
                    TensorImage processedimage = imageProcessor.process(inputTensorImage);

                    // Tomamos el tiempo para cada imagen
                    long timeForModelEstimation = System.currentTimeMillis();

                    // Ejecutamos la red en la imagen
                    interpreterApi.run(processedimage.getBuffer(), outputTensorBuffer.getBuffer());

                    // Recogemos el tiempo que ha tardado cada imagen
                    timeForModelEstimation = System.currentTimeMillis() - timeForModelEstimation;
                    // Recogemos el tiempo de inferencia nativo de la ultima operación
                    long timeNativeInference = interpreterApi.getLastNativeInferenceDurationNanoseconds() / 1000000;

                    // Calculamos los ratios de ancho y alto con respecto a la imagen original para la normalización de las coordenadas de los keypoints
                    float widthRatio = (float) (bitmap.getWidth()) / inputWidth;
                    float heightRatio = (float) (bitmap.getHeight()) / inputHeight;

                    // Obtenemos los datos de la salida estimada
                    float[] landmarksArray = outputTensorBuffer.getFloatArray();

                    // Almacenamos los datos de la estimación de los keypoints de la imagen
                    float[] predictionsTmp = new float[NUM_KEYPOINTS_PREDICTION * 3];

                    // Recorremos los keypoints de los datos de salida y los almacenamos normalizados
                    for (int j = 0; j < NUM_KEYPOINTS_PREDICTION; j++) {
                        int indexTemp = COCO_BLAZEPOSE_KEYPOINTS_MAP.get(j) * 5;

                        float x = landmarksArray[indexTemp];
                        float y = landmarksArray[indexTemp + 1];
                        float visibility = landmarksArray[indexTemp + 3];

                        predictionsTmp[(j * 3)] = x * widthRatio;
                        predictionsTmp[(j * 3) + 1] = y * heightRatio;
                        predictionsTmp[(j * 3) + 2] = 2;
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

                System.out.println("[BLAZEPOSE.run] FINISHED MODEL: " + this.modelName);
                System.out.println("----------------------------------------------------------------------------------------------------");

            } else {
                System.out.println("[BLAZEPOSE.run] CRITICAL ERROR: couldn't instantiate Tensor Flow interpreter");
                System.exit(-1);
            }

        } catch (IOException e) {
            // Ejecutamos en el thread de la interfaz la actualización del componente visual del modelo: ROJO (el modelo ha dado algún error)
            mainActivity.runOnUiThread(() -> {
                int color = ContextCompat.getColor(this.mainActivity, R.color.uva_red);
                component.setBackgroundColor(color);
            });

            System.out.println("[BLAZEPOSE.run] ERROR: " + e.getMessage());
        }
    }
}
