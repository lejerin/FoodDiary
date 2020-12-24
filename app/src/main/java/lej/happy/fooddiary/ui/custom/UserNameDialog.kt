package lej.happy.fooddiary.ui.custom

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.Button
import android.widget.EditText
import lej.happy.fooddiary.R

class UserNameDialog(context : Context) {


    private val dlg = Dialog(context)   //부모 액티비티의 context 가 들어감
    private lateinit var btnOK : Button
    private lateinit var btnCancel : Button


    private var userNameDialogListener: UserNameDialogListener? = null

    //인터페이스 설정
    interface UserNameDialogListener {
        fun onPositiveClicked(str: String)
    }

    //호출할 리스너 초기화
    fun setDialogListener(customDialogListener: UserNameDialogListener?) {
        this.userNameDialogListener = customDialogListener
    }

    fun start(content : String) {
        dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        dlg.setContentView(R.layout.user_name_dialog)     //다이얼로그에 사용할 xml 파일을 불러옴
   //     dlg.setCancelable(false)    //다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지 않도록 함

        var userName = dlg.findViewById(R.id.dialog_user_name) as EditText
        userName.setText(content)

        btnOK = dlg.findViewById(R.id.user_name_ok_btn)
        btnOK.setOnClickListener {
            userNameDialogListener!!.onPositiveClicked(userName.text.toString())
            dlg.dismiss()
        }

        btnCancel = dlg.findViewById(R.id.user_name_cancel_btn)
        btnCancel.setOnClickListener {
            dlg.dismiss()
        }

        dlg.show()
    }

}