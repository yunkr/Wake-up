package com.example.wakeupapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
//import com.google.mlkit.vision.demo.CameraSource;
//import com.google.mlkit.vision.demo.CameraSourcePreview;
//import com.google.mlkit.vision.demo.GraphicOverlay;
//import com.google.mlkit.vision.demo.R;
//import com.google.mlkit.vision.demo.java.barcodescanner.BarcodeScannerProcessor;
//import com.google.mlkit.vision.demo.java.facedetector.FaceDetectorProcessor;
//import com.google.mlkit.vision.demo.java.labeldetector.LabelDetectorProcessor;
//import com.google.mlkit.vision.demo.java.objectdetector.ObjectDetectorProcessor;
//import com.google.mlkit.vision.demo.java.posedetector.PoseDetectorProcessor;
//import com.google.mlkit.vision.demo.java.segmenter.SegmenterProcessor;
//import com.google.mlkit.vision.demo.java.textdetector.TextRecognitionProcessor;
//import com.google.mlkit.vision.demo.preference.PreferenceUtils;
//import com.google.mlkit.vision.demo.preference.SettingsActivity;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import static android.content.Context.MODE_PRIVATE;

public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_BACK; // Camera.CameraInfo.CAMERA_FACING_FRONT

    private static final String OBJECT_DETECTION = "Object Detection";
    private static final String OBJECT_DETECTION_CUSTOM = "Custom Object Detection";
    private static final String CUSTOM_AUTOML_OBJECT_DETECTION =
            "Custom AutoML Object Detection (Flower)";
    private static final String FACE_DETECTION = "Face Detection";
    private static final String BARCODE_SCANNING = "Barcode Scanning";
    private static final String IMAGE_LABELING = "Image Labeling";
    private static final String IMAGE_LABELING_CUSTOM = "Custom Image Labeling (Birds)";
    private static final String CUSTOM_AUTOML_LABELING = "Custom AutoML Image Labeling (Flower)";
    private static final String POSE_DETECTION = "Pose Detection";
    private static final String SELFIE_SEGMENTATION = "Selfie Segmentation";
    private static final String TEXT_RECOGNITION_LATIN = "Text Recognition Latin";
    private static final String TEXT_RECOGNITION_CHINESE = "Text Recognition Chinese (Beta)";
    private static final String TEXT_RECOGNITION_DEVANAGARI = "Text Recognition Devanagari (Beta)";
    private static final String TEXT_RECOGNITION_JAPANESE = "Text Recognition Japanese (Beta)";
    private static final String TEXT_RECOGNITION_KOREAN = "Text Recognition Korean (Beta)";

    private static final String TAG = "LivePreviewActivity";

    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private String selectedModel = OBJECT_DETECTION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        //setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_vision_live_preview);

        preview = findViewById(R.id.preview_view);
        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }
        graphicOverlay = findViewById(R.id.graphic_overlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }

        Spinner spinner = findViewById(R.id.spinner);
        spinner.setVisibility(View.INVISIBLE);
        List<String> options = new ArrayList<>();
        options.add(FACE_DETECTION);

        options.add(OBJECT_DETECTION);
        options.add(OBJECT_DETECTION_CUSTOM);
        options.add(CUSTOM_AUTOML_OBJECT_DETECTION);
        options.add(FACE_DETECTION);
        options.add(BARCODE_SCANNING);
        options.add(IMAGE_LABELING);
        options.add(IMAGE_LABELING_CUSTOM);
        options.add(CUSTOM_AUTOML_LABELING);
        //options.add(POSE_DETECTION);
        options.add(SELFIE_SEGMENTATION);
        options.add(TEXT_RECOGNITION_LATIN);
        options.add(TEXT_RECOGNITION_CHINESE);
        options.add(TEXT_RECOGNITION_DEVANAGARI);
        options.add(TEXT_RECOGNITION_JAPANESE);
        options.add(TEXT_RECOGNITION_KOREAN);

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_style, options);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(this);

        ToggleButton facingSwitch = findViewById(R.id.facing_switch);
        //facingSwitch.setVisibility(View.INVISIBLE);
        facingSwitch.setOnCheckedChangeListener(this);

        Button btnStatistics = (Button) findViewById(R.id.btnStatistics);

        btnStatistics.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), StatisticsActivity.class);
                startActivity(intent);
            }
        });

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            int writeExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (cameraPermission == PackageManager.PERMISSION_GRANTED
                    && writeExternalStoragePermission == PackageManager.PERMISSION_GRANTED) {
                //createCameraSource(selectedModel);

                // cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT); // 이상홍 추가

                preview.stop();
                createCameraSource(FACE_DETECTION);
                startCameraSource();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    //Snackbar.make(mLayout, "이 앱을 실행하려면 카메라와 외부 저장소 접근 권한이 필요합니다.",
                    //        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                    //    @Override
                    //    public void onClick(View view) {
                    //        ActivityCompat.requestPermissions( MainActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                    //    }
                    //}).show();
                } else {
                    // 2. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                    // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                    ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                }
            }
        } /*else {
            final Snackbar snackbar = Snackbar.make(mLayout, "디바이스가 카메라를 지원하지 않습니다.", Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("확인", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                }
            });
            snackbar.show();
        }*/

        //createCameraSource(selectedModel);

        //preview.stop();
        //createCameraSource(FACE_DETECTION);
        //startCameraSource();

        facingSwitch.performClick();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grandResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            boolean check_result = true;
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }
            if (check_result) {
                preview.stop();
                createCameraSource(FACE_DETECTION);
                startCameraSource();
            }
           /* else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }else {
                    Snackbar.make(mLayout, "설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }
            }*/
        }
    }

    @Override
    public synchronized void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)

        //parent.getSelectedView().getId()
        //this.iStage = pos;
        //selectedModel = parent.getItemAtPosition(pos).toString();
        //Log.d(TAG, "Selected model: " + selectedModel);
        //preview.stop();
        //createCameraSource(selectedModel);
        //startCameraSource();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing.
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "Set facing");

        if (cameraSource != null) {
            if (isChecked) {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
            } else {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
            }
        }
        preview.stop();
        createCameraSource(FACE_DETECTION);
        startCameraSource();
    }

    private void createCameraSource(String model) {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }

        try {
            switch (model) {
                /*
                case OBJECT_DETECTION:
                    Log.i(TAG, "Using Object Detector Processor");
                    ObjectDetectorOptions objectDetectorOptions =
                            PreferenceUtils.getObjectDetectorOptionsForLivePreview(this);
                    cameraSource.setMachineLearningFrameProcessor(
                            new ObjectDetectorProcessor(this, objectDetectorOptions));
                    break;
                case OBJECT_DETECTION_CUSTOM:
                    Log.i(TAG, "Using Custom Object Detector Processor");
                    LocalModel localModel =
                            new LocalModel.Builder()
                                    .setAssetFilePath("custom_models/object_labeler.tflite")
                                    .build();
                    CustomObjectDetectorOptions customObjectDetectorOptions =
                            PreferenceUtils.getCustomObjectDetectorOptionsForLivePreview(this, localModel);
                    cameraSource.setMachineLearningFrameProcessor(
                            new ObjectDetectorProcessor(this, customObjectDetectorOptions));
                    break;
                case CUSTOM_AUTOML_OBJECT_DETECTION:
                    Log.i(TAG, "Using Custom AutoML Object Detector Processor");
                    LocalModel customAutoMLODTLocalModel =
                            new LocalModel.Builder().setAssetManifestFilePath("automl/manifest.json").build();
                    CustomObjectDetectorOptions customAutoMLODTOptions =
                            PreferenceUtils.getCustomObjectDetectorOptionsForLivePreview(
                                    this, customAutoMLODTLocalModel);
                    cameraSource.setMachineLearningFrameProcessor(
                            new ObjectDetectorProcessor(this, customAutoMLODTOptions));
                    break;
                case TEXT_RECOGNITION_LATIN:
                    Log.i(TAG, "Using on-device Text recognition Processor for Latin.");
                    cameraSource.setMachineLearningFrameProcessor(
                            new TextRecognitionProcessor(this, new TextRecognizerOptions.Builder().build()));
                    break;
                case TEXT_RECOGNITION_CHINESE:
                    Log.i(TAG, "Using on-device Text recognition Processor for Latin and Chinese.");
                    cameraSource.setMachineLearningFrameProcessor(
                            new TextRecognitionProcessor(
                                    this, new ChineseTextRecognizerOptions.Builder().build()));
                    break;
                case TEXT_RECOGNITION_DEVANAGARI:
                    Log.i(TAG, "Using on-device Text recognition Processor for Latin and Devanagari.");
                    cameraSource.setMachineLearningFrameProcessor(
                            new TextRecognitionProcessor(
                                    this, new DevanagariTextRecognizerOptions.Builder().build()));
                    break;
                case TEXT_RECOGNITION_JAPANESE:
                    Log.i(TAG, "Using on-device Text recognition Processor for Latin and Japanese.");
                    cameraSource.setMachineLearningFrameProcessor(
                            new TextRecognitionProcessor(
                                    this, new JapaneseTextRecognizerOptions.Builder().build()));
                    break;
                case TEXT_RECOGNITION_KOREAN:
                    Log.i(TAG, "Using on-device Text recognition Processor for Latin and Korean.");
                    cameraSource.setMachineLearningFrameProcessor(
                            new TextRecognitionProcessor(
                                    this, new KoreanTextRecognizerOptions.Builder().build()));
                    break;*/
                case FACE_DETECTION:
                    Log.i(TAG, "Using Face Detector Processor");
                    cameraSource.setMachineLearningFrameProcessor(new FaceDetectorProcessor(this));
                    break;
                /*case BARCODE_SCANNING:
                    Log.i(TAG, "Using Barcode Detector Processor");
                    cameraSource.setMachineLearningFrameProcessor(new BarcodeScannerProcessor(this));
                    break;
                case IMAGE_LABELING:
                    Log.i(TAG, "Using Image Label Detector Processor");
                    cameraSource.setMachineLearningFrameProcessor(
                            new LabelDetectorProcessor(this, ImageLabelerOptions.DEFAULT_OPTIONS));
                    break;
                case IMAGE_LABELING_CUSTOM:
                    Log.i(TAG, "Using Custom Image Label Detector Processor");
                    LocalModel localClassifier =
                            new LocalModel.Builder()
                                    .setAssetFilePath("custom_models/bird_classifier.tflite")
                                    .build();
                    CustomImageLabelerOptions customImageLabelerOptions =
                            new CustomImageLabelerOptions.Builder(localClassifier).build();
                    cameraSource.setMachineLearningFrameProcessor(
                            new LabelDetectorProcessor(this, customImageLabelerOptions));
                    break;
                case CUSTOM_AUTOML_LABELING:
                    Log.i(TAG, "Using Custom AutoML Image Label Detector Processor");
                    LocalModel customAutoMLLabelLocalModel =
                            new LocalModel.Builder().setAssetManifestFilePath("automl/manifest.json").build();
                    CustomImageLabelerOptions customAutoMLLabelOptions =
                            new CustomImageLabelerOptions.Builder(customAutoMLLabelLocalModel)
                                    .setConfidenceThreshold(0)
                                    .build();
                    cameraSource.setMachineLearningFrameProcessor(
                            new LabelDetectorProcessor(this, customAutoMLLabelOptions));
                    break;
                case POSE_DETECTION:
                    PoseDetectorOptionsBase poseDetectorOptions =
                            PreferenceUtils.getPoseDetectorOptionsForLivePreview(this);
                    Log.i(TAG, "Using Pose Detector with options " + poseDetectorOptions);
                    boolean shouldShowInFrameLikelihood =
                            PreferenceUtils.shouldShowPoseDetectionInFrameLikelihoodLivePreview(this);
                    boolean visualizeZ = PreferenceUtils.shouldPoseDetectionVisualizeZ(this);
                    boolean rescaleZ = PreferenceUtils.shouldPoseDetectionRescaleZForVisualization(this);
                    boolean runClassification = PreferenceUtils.shouldPoseDetectionRunClassification(this);

                    /*VideoView mVideoView = (VideoView) findViewById(R.id.videoView);
                    Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sample);
                    mVideoView.setVideoURI(uri);

                    mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            // TODO Auto-generated method stub
                            mp.start();
                            mp.setLooping(true);
                        }
                    });*/

                    /*
                    MediaController mediaController = new MediaController(this);
                    mediaController.setAnchorView(mVideoView);
                    mediaController.setPadding(0, 0, 0, 80); //상위 레이어의 바닥에서 얼마 만큼? 패딩을 줌
                    mVideoView.setMediaController(mediaController);

                    //mVideoView.setVisibility(VideoView.INVISIBLE);
                    mVideoView.start();*/

                    /*mVideoView.setVisibility(View.INVISIBLE);

                    cameraSource.setMachineLearningFrameProcessor(
                            new PoseDetectorProcessor(
                                    this,
                                    burstImage,
                                    walkImage,
                                    button4skip,
                                    iStage,
                                    poseDetectorOptions,
                                    shouldShowInFrameLikelihood,
                                    visualizeZ,
                                    rescaleZ,
                                    runClassification,
                                    /* isStreamMode = */ //true));
                    //break;
                //case SELFIE_SEGMENTATION:
                //    cameraSource.setMachineLearningFrameProcessor(new SegmenterProcessor(this));
                //    break;
                default:
                    Log.e(TAG, "Unknown model: " + model);
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "Can not create image processor: " + model, e);
            Toast.makeText(
                            getApplicationContext(),
                            "Can not create image processor: " + e.getMessage(),
                            Toast.LENGTH_LONG)
                    .show();
        }
    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        createCameraSource(FACE_DETECTION);
        startCameraSource();
    }

    /** Stops the camera. */
    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }
}