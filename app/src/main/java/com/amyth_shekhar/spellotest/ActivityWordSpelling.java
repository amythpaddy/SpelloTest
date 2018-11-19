package com.amyth_shekhar.spellotest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class ActivityWordSpelling extends CameraRecogActivity {

    private final int REQ_CODE = 007;

    private LinearLayout hintHolder;
    LinearLayout.LayoutParams params;
    int presentWordPos;
    ImageView speakBtn;
    Button checkSpellingBtn,reportOkBtn,reportRetryBtn;
    ImageView wordImage,reportImage;
    RelativeLayout wordReportHolder;
    LinearLayout wordReport;
    EditText testIn;
    private TextView reportMsg;
    private String presentWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_spelling);

        hintHolder = findViewById(R.id.word_hint_holder);
        checkSpellingBtn = findViewById(R.id.check_spelling_btn);
        wordImage = findViewById(R.id.word_spelling_image);
        reportOkBtn = findViewById(R.id.report_ok_btn);
        reportRetryBtn= findViewById(R.id.report_retry_btn);
        reportImage = findViewById(R.id.report_message);
        reportMsg = findViewById(R.id.recognition_status);
        wordReportHolder = findViewById(R.id.word_recognition_report_holder);
        testIn = findViewById(R.id.dummyin);
        wordReport = findViewById(R.id.word_recognition_report);
        checkSpellingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecogniseSentenceSmall(presentWord);
//                showWordRecogReport(testIn.getText().toString() , presentWord);
            }
        });
        initializeNextWord();
        populateHintHolder();


        startActivityForResult(new Intent(this,LetusBeginActivity.class),REQ_CODE);

        reportOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordReport.removeAllViews();
                getNextWord();
                wordReportHolder.setVisibility(View.INVISIBLE);

            }
        });
        reportRetryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordReport.removeAllViews();
                wordReportHolder.setVisibility(View.INVISIBLE);
            }
        });

        speakBtn = findViewById(R.id.speak_btn);
        speakBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
//                    SpellEnglishHelper.playSound(ActivityWordSpelling.this,wordDb.getSound());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void initializeNextWord() {
        try {
//            presentWord = wordSpellingDb.getWordsList().get(presentWordPos);
//            wordDb = realm.where(SpellEnglishWordDatabase.class).equalTo("Spelling", presentWord.getSpelling()).findFirst();
//            wordImage.setImageDrawable(SpellEnglishHelper.getImageResource(this, wordDb.getImage()));
            presentWord = SpelloData.getSpellings();
            Toast.makeText(this, presentWord, Toast.LENGTH_SHORT).show();
            wordImage.setImageResource(SpelloData.getImage());
        }catch (Exception e){
            Log.e("Error",e.getMessage());}

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != ALIGNMENT_SUCCESS)
            finish();
        else{
            initialise();
        }
    }

    @Override
    public void ReadyToStartCapturing() {
        super.ReadyToStartCapturing();
        checkSpellingBtn.setEnabled(true);
        
    }

    @Override
    public void BeginCapturing() {
        super.BeginCapturing();
        checkSpellingBtn.setEnabled(false);
    }

    @Override
    public void RecognitionDone(String spelling) {
        super.RecognitionDone(spelling);
//        showWordRecogReport(spelling , presentWord.getSpelling());
    }

    private void getNextWord() {
SpelloData.updateCounter();
        initializeNextWord();
        populateHintHolder();

    }

    private void populateHintHolder() {
        hintHolder.removeAllViews();
        for (int i = 0; i < presentWord.length(); i++) {
            View tiles = LayoutInflater.from(this).inflate(R.layout.card_character_tile, hintHolder, false);
            if (i == 0) {
                TextView textViewSpello = tiles.findViewById(R.id.character_tile);
                textViewSpello.setText(String.valueOf(presentWord.charAt(0)));
            }
            switch (presentWord.length()) {
                case 6:
                    params = (LinearLayout.LayoutParams) tiles.getLayoutParams();
                    params.width = params.width - params.width/6;
                    tiles.setLayoutParams(params);
                    break;
                case 7:
                    params = (LinearLayout.LayoutParams) tiles.getLayoutParams();
                    params.width = params.width - params.width/7;
                    tiles.setLayoutParams(params);
                    break;
                case 8:
                    params = (LinearLayout.LayoutParams) tiles.getLayoutParams();
                    params.width = params.width - params.width/8;
                    tiles.setLayoutParams(params);
                    break;
                case 9:
                     params = (LinearLayout.LayoutParams) tiles.getLayoutParams();
                    params.width = params.width - params.width/9;
                    tiles.setLayoutParams(params);
                    break;
                case 10:
                    /*tiles.setScaleX(0.7f);
                    tiles.setScaleY(0.7f);*/
                     params = (LinearLayout.LayoutParams) tiles.getLayoutParams();
                    params.width = params.width - params.width/10;
                    tiles.setLayoutParams(params);

                    break;
                default:
                     params = (LinearLayout.LayoutParams) tiles.getLayoutParams();
                    params.weight = 0;
                    tiles.setLayoutParams(params);

                    tiles.setScaleY(1f);
                    tiles.setScaleX(1f);

            }
            hintHolder.addView(tiles, i);
        }
    }
//

    public void showInformation(View view){
//        SpellEnglishHelper.showInformation(this,"Encourage your child to identify the name of the object in picture. Tap on the sound icon to hear the name of the object in the picture. Ask your child to repeat the name. Help your child select tiles that spell the name of the object and place it in Spell English. Tap on Check to check the spelling. If the spelling is correct, \"Well Done\" message will appear. If the spelling is wrong, the wrong letter will be marked in red. Replace the wrong letter or place correct spelling tiles to spell the object name correctly.");
    }

    public void showWordRecogReport(String wordInSpello, String originalWord) {
        checkSpellingBtn.setEnabled(true);
        wordReportHolder.setVisibility(View.VISIBLE);
        wordReportHolder.setBackground(BlurHelper.builder(this).blur((View) wordReportHolder.getParent(), wordReportHolder, 15));
        for (int i = 0; i < (wordInSpello.length() > originalWord.length() ? wordInSpello.length() : originalWord.length()); i++) {
            try {
                if (String.valueOf(wordInSpello.charAt(i)).equals(String.valueOf(originalWord.charAt(i)))) {
                    View view = LayoutInflater.from(this).inflate(R.layout.card_right_character_tile, wordReport, false);
                    ((TextView) view.findViewById(R.id.character_tile)).setText(String.valueOf(wordInSpello.charAt(i)));
                    wordReport.addView(view);
                } else {
                    View view = LayoutInflater.from(this).inflate(R.layout.card_wrong_character_tile, wordReport, false);
                    ((TextView) view.findViewById(R.id.character_tile)).setText(String.valueOf(wordInSpello.charAt(i)));
                    wordReport.addView(view);
                }
            } catch (Exception e) {
                View view = LayoutInflater.from(this).inflate(R.layout.card_wrong_character_tile, wordReport, false);
                try {
                    ((TextView) view.findViewById(R.id.character_tile)).setText(String.valueOf(wordInSpello.charAt(i)));

                } catch (Exception e1) {
                }
                wordReport.addView(view);
            }
        }
        if (wordInSpello.equals(originalWord)) {
//            getNextWord();
            reportOkBtn.setVisibility(View.VISIBLE);
            reportMsg.setText("CORRECT");

        } else {
            reportOkBtn.setVisibility(View.INVISIBLE);
            reportMsg.setText("INCORRECT");
            try {

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
