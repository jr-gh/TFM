package es.uva.estudiantes.tfm;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.core.content.ContextCompat;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.InterpreterApi;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.security.InvalidParameterException;


/**
 * Clase de estimación de posturas de la familia de modelos Yolo8-pose
 */
public class Yolo extends TensorFlowLiteModel {

    /**
     * Constante para el tipo de modelo nano
     */
    public final static int TYPE_NANO = 1;
    /**
     * Constante para el tipo de modelo small
     */
    public final static int TYPE_SMALL = 2;
    /**
     * Constante para el tipo de modelo medium
     */
    public final static int TYPE_MEDIUM = 3;

    private DataType modelDataType;

    private int inputWidth = 320;
    private int inputHeight = 320;


    /**
     * Constructor de la clase
     *
     * @param mainActivityParam Actividad principal de la APP
     * @param typeParam         Tipo de modelo
     */
    protected Yolo(MainActivity mainActivityParam, int typeParam) {
        // Comprobamos y leemos los ficheros de datos (lista de imágenes y anotaciones)
        super();

        // Comprobamos los parámetros de entrada
        if (mainActivityParam == null) {
            throw new InvalidParameterException("[YOLO8] ERROR: Invalid CONTEXT parameter");
        }
        if (typeParam != TYPE_NANO && typeParam != TYPE_SMALL && typeParam != TYPE_MEDIUM) {
            throw new InvalidParameterException("[YOLO8] ERROR: Invalid TYPE parameter, must be one of NANO (1), SMALL (2) or MEDIUM (3)");
        }

        // Definimos los parámetros de la red
        this.mainActivity = mainActivityParam;

        switch (typeParam) {
            case TYPE_NANO:
                // Nombre visual para el modelo
                this.modelName = "Yolo8-pose nano";

                // Modelo Yolo nano
                this.fileModelName = "yolov8n-pose_float16.tflite";

                // Inicializamos el nombre del fichero donde almacenaremos las predicciones del modelo
                this.outputPredictionsFileName = "Yolo8_nano_predictions.json";
                // Inicializamos el nombre del fichero donde almacenaremos los datos de rendimiento del modelo
                this.outputPerformanceFileName = "Yolo8_nano_performance.txt";

                // Componente de la interfaz para mostrar el proceso del test
                this.component = mainActivity.findViewById(R.id.textViewYolo8n);
                break;

            case TYPE_SMALL:
                // Nombre visual para el modelo
                this.modelName = "Yolo8-pose small";

                // Modelo Yolo small
                this.fileModelName = "yolov8s-pose_float16.tflite";

                // Inicializamos el nombre del fichero donde almacenaremos las predicciones del modelo
                this.outputPredictionsFileName = "Yolo8_small_predictions.json";
                // Inicializamos el nombre del fichero donde almacenaremos los datos de rendimiento del modelo
                this.outputPerformanceFileName = "Yolo8_small_performance.txt";

                // Componente de la interfaz para mostrar el proceso del test
                this.component = mainActivity.findViewById(R.id.textViewYolo8s);
                break;

            case TYPE_MEDIUM:
                // Nombre visual para el modelo
                this.modelName = "Yolo8-pose medium";

                // Modelo Yolo medium
                this.fileModelName = "yolov8m-pose_float16.tflite";

                // Inicializamos el nombre del fichero donde almacenaremos las predicciones del modelo
                this.outputPredictionsFileName = "Yolo8_medium_predictions.json";
                // Inicializamos el nombre del fichero donde almacenaremos los datos de rendimiento del modelo
                this.outputPerformanceFileName = "Yolo8_medium_performance.txt";

                // Componente de la interfaz para mostrar el proceso del test
                this.component = mainActivity.findViewById(R.id.textViewYolo8m);
                break;
        }

        // Inicializamos el valor del tipo de datos de entrada de la red
        modelDataType = DataType.FLOAT32;

        // Inicializamos el procesador de las imágenes con los parametros (tamaño) del modelo especificado
        this.imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(inputHeight, inputWidth, ResizeOp.ResizeMethod.BILINEAR))
                .add(new NormalizeOp(0f, 255f)) // Normaliza a [0,1] si es necesario
                .build();
    }


    /**
     * Método para correr la red sobre las imágenes
     */
    public void run() {
        try {

            System.out.println("----------------------------------------------------------------------------------------------------");
            System.out.println("[YOLO8.run] STARTED MODEL: " + this.modelName);

            // Ejecutamos en el thread de la interfaz la actualización del componente visual del modelo: AMARILLO (el modelo está ejecutando el test)
            mainActivity.runOnUiThread(() -> {
                component.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.uva_yellow));
            });

            // Inicializamos el modelo de la red
            MappedByteBuffer tfliteModelMappedFile = FileUtil.loadMappedFile(mainActivity, fileModelName);
            InterpreterApi interpreterApi = InterpreterApi.create(tfliteModelMappedFile, new InterpreterApi.Options());

            // Creamos un TensorImage (contenedor de objeto imagen para TensorFlow) del tipo del modelo de la red (uint8, float16 o float32)
            TensorImage inputTensorImage = new TensorImage(modelDataType);

            // Creamos un TensorBuffer (buffer contenedor de datos para la salida) con el tamaño del modelo (1, 1, 57) y el tipo del modelo (float32)
            TensorBuffer outputTensorBuffer = TensorBuffer.createFixedSize(new int[]{1, 1, 57}, modelDataType);

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
                        int indexTemp = (j * 3) + 6;

                        float x = outputArray[indexTemp];
                        float y = outputArray[indexTemp + 1];
                        float score = outputArray[indexTemp + 2];

                        predictionsTmp[(j * 3)] = x * origWidth;
                        predictionsTmp[(j * 3) + 1] = y * origHeight;
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

                System.out.println("[YOLO8.run] FINISHED MODEL: " + this.modelName);
                System.out.println("----------------------------------------------------------------------------------------------------");

            } else {
                System.out.println("[YOLO8.run] CRITICAL ERROR: couldn't instantiate Tensor Flow interpreter");
                System.exit(-1);
            }

        } catch (IOException e) {
            // Ejecutamos en el thread de la interfaz la actualización del componente visual del modelo: ROJO (el modelo ha dado algún error)
            mainActivity.runOnUiThread(() -> {
                int color = ContextCompat.getColor(this.mainActivity, R.color.uva_red);
                component.setBackgroundColor(color);
            });

            System.out.println("[YOLO8.run] ERROR: " + e.getMessage());
        }
    }
}
