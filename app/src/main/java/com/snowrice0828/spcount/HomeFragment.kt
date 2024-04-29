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
    private var pYear: Int = 0
    private var pMonth: Int = 0
    private var pDay: Int = 0
    private var pName: String = ""
    private var pContents: String = ""
    private lateinit var helper: DatabaseHelper
    private var ItemId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var bundle = arguments
        if(bundle != null)
        {
            ItemId = bundle.getInt("KEY_ID")
        }
        else
        {
            ItemId = -1
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        helper = DatabaseHelper(view.context)

        val buttonView = view.findViewById<Button>(R.id.button) as Button
        val itemIdView = view.findViewById<TextView>(R.id.ItemId) as TextView

        if(ItemId != -1)
        {
            itemIdView.text = ItemId.toString()
            itemIdView.visibility = View.VISIBLE    // 可視化
            buttonView.text = "更新"
        }
        else
        {
            itemIdView.text = ""
            itemIdView.visibility = View.GONE    // 不可視化
            buttonView.text = "保存"
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(ItemId != -1)
        {
            setData(view, ItemId)
        }
        else
        {
            // 初期値日付をセット
            InitDate(view)
        }

        val year = view.findViewById<View>(R.id.Year) as EditText
        year.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable) {
                var y = if(s.toString() == "") 0 else s.toString().toInt()
                pYear = y
                setCounter(view, pName, pContents, pYear, pMonth)             // データを取得集計して表示
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
                setCounter(view, pName, pContents, pYear, pMonth)             // データを取得集計して表示
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
                pContents = s.toString()
                setCounter(view, pName, pContents, pYear, pMonth)             // データを取得集計して表示
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
                setContentsName(view, pName)
                setCounter(view, pName, pContents, pYear, pMonth)             // データを取得集計して表示
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
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1 // 月は0始まりのため+1する
        m.setText(currentMonth.toString())
        pMonth = currentMonth

        val y = view.findViewById<EditText>(R.id.Year)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        y.setText(currentYear.toString())
        pYear = currentYear
    }

    fun buttonOnClick_Save(view: View){ // ①クリック時の処理を追加
        try {
            var itemId = view.findViewById<TextView>(R.id.ItemId)
            var id = itemId.text.toString().toInt()

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

            setCounter(view, name, contents, year, month)             // データを取得、集計して表示

            var msg: String = "よろしいですか？"
            if(id != -1)
            {
                msg = "ID:" + id.toString() + "を更新します。\nよろしいですか？"
            }

            AlertDialog.Builder(requireContext())
                .setTitle("確認")
                .setMessage(msg)
                .setPositiveButton(
                    "OK"
                ) { dialog, which ->
                    // カウンタ更新処理
                    val ymd = (year.toString() + month.toString().padStart(2, '0') + day.toString().padStart(2, '0').toString()).toInt()
                    var ret :Boolean = true
                    if(id != -1)
                    {
                        ret = submit_upd(id, contents, name, ymd, remarks)
                    }
                    else
                    {
                        ret = submit_inc(contents, name, ymd, remarks)
                    }
                    if (ret) {
                        Toast.makeText(requireActivity(), "保存が完了しました。", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(requireActivity(), "エラーが発生しました。", Toast.LENGTH_LONG).show()
                    }
                    setCounter(view, name, contents, year, month)             // データを取得、集計して表示
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

    // データインサート処理
    private fun submit_inc(contents :String, name :String, ymd: Int, remarks: String): Boolean
    {
        return helper.insertData(contents, name, ymd, remarks)
    }

    // データアップデート処理
    private fun submit_upd(id :Int, contents :String, name :String, ymd: Int, remarks: String): Boolean
    {
        return helper.updateData(id, ymd, contents, name, remarks)
    }

    // 画面カウンタセット処理
    private fun setCounter(view:View, name:String, contents:String, year:Int, month:Int)
    {
        var ret = helper.selectCountData(name, contents, year, month)  // 本データを取得

        var yCount = 0
        var mCount = 0

        if(ret.count() > 1) {
            yCount = ret[0]
            mCount = ret[1]
        }

        var yc = view.findViewById<TextView>(R.id.yCount)
        yc.setText(yCount.toString())

        var ym = view.findViewById<TextView>(R.id.mCount)
        ym.setText(mCount.toString())
    }

    // コンテンツ名自動セット処理
    private fun setContentsName(view:View, name:String)
    {
        var ret = helper.selectContents(name)  // 本データを取得

        var contents = view.findViewById<EditText>(R.id.Contents) as EditText

        if(ret != "")
        {
            contents.setText(ret)
        }
    }

    // 修正処理用。IDデータを画面にセット
    private fun setData(view:View, itemID:Int)
    {
        val Data = helper.selectDataId(itemID)

        if(Data.id != -1)
        {
            var itemID = view.findViewById<TextView>(R.id.ItemId)
            var year = view.findViewById<EditText>(R.id.Year) as EditText
            var month = view.findViewById<EditText>(R.id.Month) as EditText
            var day = view.findViewById<EditText>(R.id.Day) as EditText
            var name = view.findViewById<EditText>(R.id.Name)
            var contents = view.findViewById<EditText>(R.id.Contents)
            var remarks = view.findViewById<EditText>(R.id.Remarks)

            var y = Data.ymd / 10000
            var m = (Data.ymd / 100) % 100
            var d = Data.ymd % 100

            // 取得データセット
            itemID.setText(Data.id.toString())
            year.setText(y.toString())
            year.requestFocus()
            month.setText(m.toString())
            month.requestFocus()
            day.setText(d.toString())
            day.requestFocus()
            day.setSelection(day.text.length)
            name.setText(Data.name)
            remarks.setText(Data.remarks)
            contents.setText(Data.contents)

            setCounter(view, Data.name, Data.contents, y, m)
        }
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