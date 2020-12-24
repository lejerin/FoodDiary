package lej.happy.fooddiary.Helper;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import lej.happy.fooddiary.R;


public class DatePickerDialog  extends Dialog {

    private Context context;
    private Dialog dialog;

    private Button beforeClickBtn;
    private int monthData = 0;

    private CustomDialogListener customDialogListener;

    //인터페이스 설정
    public interface CustomDialogListener{
        void onPositiveClicked(Boolean isOk, int year, int month);
    }

    //호출할 리스너 초기화
    public void setDialogListener(CustomDialogListener customDialogListener){
        this.customDialogListener = customDialogListener;
    }


    public DatePickerDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public void showDialog(int year, int month) {
        dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.GRAY));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.item_date_picker_dialog);
     //   dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        ImageButton prev_year_btn = dialog.findViewById(R.id.prev_year_btn);
        ImageButton next_year_btn = dialog.findViewById(R.id.next_year_btn);
        final TextView year_text  = dialog.findViewById(R.id.picker_year_text);

        year_text.setText("" + year);

        prev_year_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                year_text.setText("" + ( Integer.parseInt(year_text.getText().toString()) - 1));
            }
        });

        next_year_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                year_text.setText("" + (Integer.parseInt(year_text.getText().toString()) + 1));
            }
        });

        Button button = dialog.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedBtn(v);
                monthData = 1;
            }
        });

        Button button2 = dialog.findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedBtn(v);
                monthData = 2;
            }
        });

        Button button3 = dialog.findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedBtn(v);
                monthData = 3;
            }
        });


        Button button4 = dialog.findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedBtn(v);
                monthData = 4;
            }
        });


        Button button5 = dialog.findViewById(R.id.button5);
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedBtn(v);
                monthData = 5;
            }
        });


        Button button6 = dialog.findViewById(R.id.button6);
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedBtn(v);
                monthData = 6;
            }
        });


        Button button7 = dialog.findViewById(R.id.button7);
        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedBtn(v);
                monthData = 7;
            }
        });


        Button button8 = dialog.findViewById(R.id.button8);
        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedBtn(v);
                monthData = 8;
            }
        });


        Button button9 = dialog.findViewById(R.id.button9);
        button9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedBtn(v);
                monthData = 9;
            }
        });


        Button button10 = dialog.findViewById(R.id.button10);
        button10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedBtn(v);
                monthData = 10;
            }
        });


        Button button11 = dialog.findViewById(R.id.button11);
        button11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedBtn(v);
                monthData = 11;
            }
        });


        Button button12 = dialog.findViewById(R.id.button12);
        button12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedBtn(v);
                monthData = 12;
            }
        });


        Button mDialogOk = dialog.findViewById(R.id.picker_save_btn);
        mDialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //   Toast.makeText(getContext(),"Okay" ,Toast.LENGTH_SHORT).show();
                if(monthData == 0){
                    Toast.makeText(getContext(),"검색할 달을 선택해주세요." ,Toast.LENGTH_SHORT).show();
                }else{
                    int year = Integer.parseInt(year_text.getText().toString());
                    customDialogListener.onPositiveClicked(true,year,monthData);
                    dialog.cancel();
                }

            }
        });

        Button mDialogCancle = dialog.findViewById(R.id.picker_cancle_btn);
        mDialogCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        Button mAllBtn = dialog.findViewById(R.id.picker_all_btn);
        mAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                int year = Integer.parseInt(new SimpleDateFormat("yyyy", Locale.KOREA).format(new Date()));
                int month = Integer.parseInt(new SimpleDateFormat("M", Locale.KOREA).format(new Date()));
                customDialogListener.onPositiveClicked(false,year,month);
                dialog.cancel();
            }
        });

        dialog.show();
    }

    private void selectedBtn(View v){
        if(beforeClickBtn != null) beforeClickBtn.setSelected(false);
        v.setSelected(true);
        beforeClickBtn = (Button) v;
    }

    public void cancelDialog(){
        if(dialog.isShowing()){
            dialog.cancel();
        }


    }
}
