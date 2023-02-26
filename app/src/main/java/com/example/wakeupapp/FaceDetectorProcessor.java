package com.example.wakeupapp;

import android.content.Context;
import android.graphics.PointF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Face Detector Demo. */
public class FaceDetectorProcessor extends VisionProcessorBase<List<Face>> {

  private static final String TAG = "FaceDetectorProcessor";

  private final FaceDetector detector;

  private static SoundPool soundPool; //경고음
  private static int sound1; //경고음 전역변수 선언
  private static int sound2; //경고음 전역변수 선언
  private static int sound3; //경고음 전역변수 선언

  boolean[] closed = new boolean[30];
  boolean eyes_sleeping = false;
  static boolean yawn_sleeping = false;
  static boolean eye_sound = false;
  static boolean yawn_sound = false;

  static int eyes_counter = 0;
  static int yawn_counter = 0;
  int rppg_counter = 0;

  static double s_d3 = 0;


  Date d1 = new Date(); //eyes
  static Date y1 = new Date(); //yawn
  long ti = 0;

  int counter = 0;

  public FaceDetectorProcessor(Context context) {
    super(context);
    FaceDetectorOptions faceDetectorOptions = PreferenceUtils.getFaceDetectorOptions(context);
    Log.v(MANUAL_TESTING_LOG, "Face detector options: " + faceDetectorOptions);
    detector = FaceDetection.getClient(faceDetectorOptions);

    soundPool = new SoundPool(6, AudioManager.STREAM_MUSIC, 0);  //SoundPool생성
    sound1 = soundPool.load(context, R.raw.eyes_sound, 1); //사운드 로드
    sound2 = soundPool.load(context, R.raw.yawn_sound, 1); //사운드 로드
    sound3 = soundPool.load(context, R.raw.siren, 1); //사운드 로드
  }

  @Override
  public void stop() {
    super.stop();
    detector.close();
  }

  @Override
  protected Task<List<Face>> detectInImage(InputImage image) {
    return detector.process(image);
  }

  @Override
  protected void onSuccess(@NonNull List<Face> faces, @NonNull GraphicOverlay graphicOverlay) {
    for (Face face : faces) {
      graphicOverlay.add(new FaceGraphic(graphicOverlay, face));
      logExtrasForTesting(face);

      //eyes
      if ((face.getLeftEyeOpenProbability() != null) && (face.getRightEyeOpenProbability() != null)) {

        float l = face.getLeftEyeOpenProbability();
        float r = face.getRightEyeOpenProbability();


        if ((l >= 0.3) && (r >= 0.3)) { //eyes open
          Log.v(
                  MANUAL_TESTING_LOG,
                  "close: " + face.getLeftEyeOpenProbability());
          //3초 카운트...

          d1 = new Date();

          if (eyes_sleeping == true) { //눈 카운트
            eyes_counter = eyes_counter + 1;
            Log.v(
                    MANUAL_TESTING_LOG, // 눈 감은 횟수
                    "eyes_counter: "
                            + eyes_counter);
            eyes_sleeping = false;


          }
          eye_sound = true;


        } else if ((l < 0.3) && (r < 0.3) && s_d3 < 0.2) { //eyes close
          Date d2 = new Date();
          long diff = d2.getTime() - d1.getTime();
          long sec = diff / 1000;
          Log.v(
                  MANUAL_TESTING_LOG,
                  "눈: " + sec);


          if (sec >= 2) {
            Log.d("tag", "SLEEPING!!!");
            if (eye_sound == true) {
              soundPool.play(sound1, 1, 1, 0, 0, 1); //사운드 재생
              eye_sound = false;

            }

            eyes_sleeping = true;

          }
          if (sec >= 7) {     // eyes_sound = 4초, 눈 감은 뒤 소리는 2초 뒤
            Log.d("tag", "SLEEPING!!!");

            soundPool.play(sound3, 1, 1, 0, 0, 1); //사운드 재생

            // eyes_sleeping = true;
          }
        }

      }
    }
  }
  private static void logExtrasForTesting (Face face){
    if (face != null) {
      Log.v(MANUAL_TESTING_LOG, "face bounding box: " + face.getBoundingBox().flattenToString());
      Log.v(MANUAL_TESTING_LOG, "face Euler Angle X: " + face.getHeadEulerAngleX());
      Log.v(MANUAL_TESTING_LOG, "face Euler Angle Y: " + face.getHeadEulerAngleY());
      Log.v(MANUAL_TESTING_LOG, "face Euler Angle Z: " + face.getHeadEulerAngleZ());

      // All landmarks
      int[] landMarkTypes =
              new int[]{
                      FaceLandmark.MOUTH_BOTTOM,
                      FaceLandmark.MOUTH_RIGHT,
                      FaceLandmark.MOUTH_LEFT,
                      FaceLandmark.RIGHT_EYE,
                      FaceLandmark.LEFT_EYE,
                      FaceLandmark.RIGHT_EAR,
                      FaceLandmark.LEFT_EAR,
                      FaceLandmark.RIGHT_CHEEK,
                      FaceLandmark.LEFT_CHEEK,
                      FaceLandmark.NOSE_BASE
              };
      String[] landMarkTypesStrings =
              new String[]{
                      "MOUTH_BOTTOM",
                      "MOUTH_RIGHT",
                      "MOUTH_LEFT",
                      "RIGHT_EYE",
                      "LEFT_EYE",
                      "RIGHT_EAR",
                      "LEFT_EAR",
                      "RIGHT_CHEEK",
                      "LEFT_CHEEK",
                      "NOSE_BASE"
              };
      for (int i = 0; i < landMarkTypes.length; i++) {
        FaceLandmark landmark = face.getLandmark(landMarkTypes[i]);
        if (landmark == null) {
//          Log.v(
//              MANUAL_TESTING_LOG,
//              "No landmark of type: " + landMarkTypesStrings[i] + " has been detected");
        } else {
          PointF landmarkPosition = landmark.getPosition();
          String landmarkPositionStr =
                  String.format(Locale.US, "x: %f , y: %f", landmarkPosition.x, landmarkPosition.y);


          //PointF moutht = face.getContour(9).getPoints().get(9);
          //FaceContour moutht1 = face.getAllContours().get(10);
          //PointF moutht12 = face.getContour(9).getPoints().get(0);

          //List<PointF> moutht11 = face.getContour(1).getPoints(); //type1 , 36, 얼굴 외곽선 윤곽인듯?
          //List<PointF> moutht12 = face.getContour(2).getPoints(); //type2 , 5, 왼쪽눈썹위쪽


          //https://developers.google.com/ml-kit/vision/face-detection 얼굴 윤곽선 참고. facecontourtype에 관한 자료


          //List<PointF> mouth_u_b = face.getContour(9).getPoints(); //type9 , 9, UPPER_LIT_BOTTOM 윗입술 윤곽선
          //List<PointF> mouth_l_t = face.getContour(10).getPoints(); //type10 , 9, LOWER_LIT_TOP 아랫입술 윤곽선


          FaceContour mouth_u_bt = face.getContour(9);
          FaceContour mouth_u_lt = face.getContour(10);
          if (mouth_u_bt != null && mouth_u_lt != null) {
            List<PointF> mouth_u_b = mouth_u_bt.getPoints();
            List<PointF> mouth_l_t = mouth_u_lt.getPoints();


            PointF mouth_right = Objects.requireNonNull(face.getLandmark(landMarkTypes[1])).getPosition(); //오른쪽 입가
            PointF mouth_left = Objects.requireNonNull(face.getLandmark(landMarkTypes[2])).getPosition(); //왼쪽 입가

            //PointF mouthb = face.getLandmark(landMarkTypes[0]).getPosition(); //아랫입술
            //PointF noseb = face.getLandmark(landMarkTypes[9]).getPosition();  //코 중앙


            PointF mouth_upperlip = mouth_u_b.get(4); //UPPER_LIP_BOTTOM의 중앙 좌표값 (윗입술)
            PointF mouth_lowerlip = mouth_l_t.get(4); //LOWER_LIP_TOP의 중앙 좌표값 (아랫입술)


            double d1; //윗입술, 아랫입술 거리 값
            double xd1, yd1;
            yd1 = Math.pow((mouth_upperlip.y - mouth_lowerlip.y), 2.0);
            xd1 = Math.pow((mouth_upperlip.x - mouth_lowerlip.x), 2.0);
            d1 = Math.sqrt(yd1 + xd1);


            double d2; //왼쪽, 오른쪽 입가 거리 값
            double xd2, yd2;
            yd2 = Math.pow((mouth_left.y - mouth_right.y), 2.0);
            xd2 = Math.pow((mouth_left.x - mouth_right.x), 2.0);
            d2 = Math.sqrt(yd2 + xd2);

            double d3; // 평소 1.0 , 하품 시 1.2 이상
            d3 = d1 / d2;
            s_d3 = d3;

/*          Log.v(
                  MANUAL_TESTING_LOG, // 아랫입술 ,코 사이 (세로)
                  "mouthd1: "
                          + d1);*/
/*          Log.v(
                  MANUAL_TESTING_LOG, //왼쪽입가, 오른쪽입가 사이 (가로)
                  "mouthd2: "
                          + d2);*/
            Log.v(
                    MANUAL_TESTING_LOG, //평소 1.0, 하품 시 1.2 이상
                    "mouthd3: "
                            + d3);
/*          Log.v(
              MANUAL_TESTING_LOG,
              "Position for face landmark: "
                  + landMarkTypesStrings[i]
                  + " is :"
                  + landmarkPositionStr);*/
            if (d3 < 0.3) {
              y1 = new Date();

              if (yawn_sleeping == true) { //하품 카운트
                yawn_counter = yawn_counter + 1;
                Log.v(
                        MANUAL_TESTING_LOG, // 하품한 횟수
                        "yawn_counter: "
                                + yawn_counter);
                yawn_sleeping = false;
              }
              yawn_sound = true;
            } else if (d3 >= 0.3) {
              Date y2 = new Date();
              long diff = y2.getTime() - y1.getTime();
              long sec = diff / 1000;
              Log.v(
                      MANUAL_TESTING_LOG,
                      "하품시간: " + sec);

              if (sec >= 2) {
                Log.d("하품시간:", "yawn!!!");
                if (yawn_sound == true) {
                  soundPool.play(sound2, 1, 1, 0, 0, 1); //사운드 재생
                  yawn_sound = false;
                }
                yawn_sleeping = true;


              }

            }
          }
        }
//      Log.v(
//          MANUAL_TESTING_LOG,
//          "face left eye open probability: " + face.getLeftEyeOpenProbability());
//      Log.v(
//          MANUAL_TESTING_LOG,
//          "face right eye open probability: " + face.getRightEyeOpenProbability());
//      Log.v(MANUAL_TESTING_LOG, "face smiling probability: " + face.getSmilingProbability());
//      Log.v(MANUAL_TESTING_LOG, "face tracking id: " + face.getTrackingId());

      }
    }
  }


  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.e(TAG, "Face detection failed " + e);
  }
}