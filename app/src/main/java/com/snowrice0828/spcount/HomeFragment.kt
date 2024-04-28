package com.snowrice0828.spcount

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class HomeFragment : Fragment(){
    public var pYear: Int = 0
    public var pMonth: Int = 0
    public var pDay: Int = 0
    public var pName: String = ""
    private lateinit var helper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        helper = DatabaseHelper(view.context)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初期値日付をセット
        InitDate(view)

        val year = view.findViewById<View>(R.id.Year) as EditText
        year.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable) {
                var y = if(s.toString() == "") 0 else s.toString().toInt()
                pYear = y
                setCounter(view, pName, pYear)             // データを取得集計して表示
            }
        })

        val month = view.findViewById<EditText>(R.id.Month) as EditText
        val day = view.findViewById<EditText>(R.id.Day) as EditText
        month.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable) {
                var m = if(s.toString() == "") 0 else s.toString().toInt()
                if(m > 12)
                {
                    m = 12
                    month.setText(m.toString())
                    month.selectAll()
                }
                pMonth = m
                // 入力ミスで12月に強制変更されたとして、日付が未変更のまま不正になることはないので日部分は変更しない
                setCounter(view, pName, pYear)             // データを取得集計して表示
            }
        })

        day.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable) {
                var d = if(s.toString() == "") 1 else s.toString().toInt()
                var ymd = ((pYear * 10000) + (pMonth * 100) + d)
                if(checkDate(ymd) == false)
                {
                    d = 1
                    day.setText(d.toString())
                    day.selectAll()
                }
                pDay = d
            }
        })

        // リスナーを登録
        val contents = view.findViewById(R.id.Contents) as AutoCompleteTextView
        contents.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var cmp = helper.selectDataLikeContents(s.toString())  // 補完用のデータを取得
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cmp)
                val autoCompleteTextView = view.findViewById<AutoCompleteTextView>(R.id.Contents)
                if (autoCompleteTextView != null) {
                    autoCompleteTextView.setAdapter(adapter)
                    autoCompleteTextView.threshold = 1
                }
            }
            override fun afterTextChanged(s: Editable) {
            }
        })

        val name = view.findViewById(R.id.Name) as AutoCompleteTextView
        name.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                pName = s.toString()
                var cmp = helper.selectDataLikeName(pName, pYear)  // 補完用のデータを取得
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cmp)
                val autoCompleteTextView = view.findViewById<AutoCompleteTextView>(R.id.Name)
                if (autoCompleteTextView != null) {
                    autoCompleteTextView.setAdapter(adapter)
                    autoCompleteTextView.threshold = 1
                }
            }
            override fun afterTextChanged(s: Editable) {
                pName = s.toString()
                setCounter(view, pName, pYear)             // データを取得、集計して表示
            }
        })

        view.findViewById<Button>(R.id.button).setOnClickListener {
            buttonOnClick_Save(view)
        }
    }

    // デフォルト日付をセット
    fun InitDate(view:View)
    {
        val d = view.findViewById<EditText>(R.id.Day)
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        if (d != null) {
            d.setText(currentDay.toString())
        }
        pDay = currentDay

        val m = view.findViewById<EditText>(R.id.Month)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) +1 // 月は0始まりのため+1する
        m.setText(currentMonth.toString())
        pMonth = currentMonth

        val y = view.findViewById<EditText>(R.id.Year)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        y.setText(currentYear.toString())
        pYear = currentYear
    }

    fun buttonOnClick_Save(view: View){ // ①クリック時の処理を追加
        try {
            var y = view.findViewById<EditText>(R.id.Year)
            var year = y.text.toString().toInt()

            var m = view.findViewById<EditText>(R.id.Month)
            var month = m.text.toString().toInt()

            var d = view.findViewById<EditText>(R.id.Day)
            var day = d.text.toString().toInt()

            var n = view.findViewById<EditText>(R.id.Name)
            var name = n.text.toString()

            var c = view.findViewById<EditText>(R.id.Contents)
            var contents = c.text.toString()

            var r = view.findViewById<EditText>(R.id.Remarks)
            var remarks = r.text.toString()

            setCounter(view, name, year)             // データを取得、集計して表示

            AlertDialog.Builder(requireContext())
                .setTitle("確認")
                .setMessage("よろしいですか。")
                .setPositiveButton(
                    "OK"
                ) { dialog, which ->
                    // カウンタ更新処理
                    val ymd = (year.toString() + month.toString() + day.toString()).toInt()
                    var ret = submit_inc(contents, name, ymd, remarks)
                    if (ret) {
                        Toast.makeText(requireActivity(), "保存が完了しました。", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(requireActivity(), "エラーが発生しました。", Toast.LENGTH_LONG).show()
                    }
                    setCounter(view, name, year)             // データを取得、集計して表示
                }
                .setNegativeButton(
                    "キャンセル"
                ) { dialog, which ->
                    // 何もしない
                }
                .show()
        }catch (e:NumberFormatException) {
            Toast.makeText(requireActivity(), "数値を入力してください。", Toast.LENGTH_LONG).show()
        }
    }

    private fun submit_inc(contents :String, name :String, ymd: Int, remarks: String): Boolean
    {
        var ret = helper.insertData(contents, name, ymd, remarks)
        if(ret == false)
        {
            return false
        }
        return true
    }

    // 画面カウンタセット処理
    private fun setCounter(view:View, name:String, year:Int)
    {
        var rec = helper.selectData(name, year)  // 本データを取得

        Log.d("TAG", "setCounter: " + rec.name)

        var yCount = 0
        var mCount = 0

        // それぞれ画面に表示
        if(rec.id != -1) {
            var co = view.findViewById<TextView>(R.id.Contents)
            co.setText(rec.contents)
        }

        // ここでRecからデータを取得

        var yc = view.findViewById<TextView>(R.id.yCount)
        yc.setText(yCount.toString())

        var ym = view.findViewById<TextView>(R.id.mCount)
        ym.setText(mCount.toString())
    }


    /**
     * 日付の妥当性チェックを行います。
     * 指定した日付文字列（yyyy/MM/dd or yyyy-MM-dd）が
     * カレンダーに存在するかどうかを返します。
     * @param strDate チェック対象の文字列
     * @return 存在する日付の場合true
     */
    fun checkDate(yyyymmdd: Int): Boolean {
        try {
            // 整数を文字列に変換
            val dateString = yyyymmdd.toString()

            // SimpleDateFormatを使って日付形式に変換
            val dateFormat: DateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            dateFormat.isLenient = false
            dateFormat.parse(dateString)

            // 上記の変換が成功した場合は有効な日付と見なす
            return true
        } catch (e: Exception) {
            // 例外が発生した場合は無効な日付と見なす
            return false
        }
    }

    /**
     * 指定した日付文字列（yyyy/MM/dd or yyyy-MM-dd）
     * における月末日付を返します。
     *
     * @param strDate 対象の日付文字列
     * @return 月末日付
     */
    fun getLastDay(strDate: Int): Int {
        val yyyy = strDate / 10000;
        val MM = (strDate % 10000) / 100
        val dd = strDate % 100
        val cal = Calendar.getInstance()
        cal[yyyy, MM - 1] = dd
        return cal.getActualMaximum(Calendar.DATE)
    }
}