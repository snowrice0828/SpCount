package k.yuki.spcount

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.Calendar
import kotlin.math.log


class MainActivity : AppCompatActivity() {
    private lateinit var helper: DatabaseHelper
    private var pYear: Int = 0
    private var pMonth: Int = 0
    private var pName: String = ""
    internal enum class Mode {
        IMPORT, EXPORT
    }
    private val REQUEST_OPEN_FILE = 1001
    private val REQUEST_CREATE_FILE = 1002
    var editText: EditText? = null

    data class Record(
        var id: Int,
        var contents: String,
        var name: String,
        var year: Int,
        var jan: Int,
        var feb: Int,
        var mar: Int,
        var apr: Int,
        var may: Int,
        var jun: Int,
        var jul: Int,
        var aug: Int,
        var sep: Int,
        var oct: Int,
        var nov: Int,
        var dec: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // リスナーを登録
        val contents = findViewById(R.id.Contents) as AutoCompleteTextView
        contents.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var cmp = selectContentsLike(s.toString())  // 補完用のデータを取得
                val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, cmp)
                val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.Contents)
                autoCompleteTextView.setAdapter(adapter)
                autoCompleteTextView.threshold = 1
            }
            override fun afterTextChanged(s: Editable) {
            }
        })

        val name = findViewById(R.id.Name) as AutoCompleteTextView
        name.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                pName = s.toString()
                var cmp = selectDataLike(pName, pYear)  // 補完用のデータを取得
                val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, cmp)
                val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.Name)
                autoCompleteTextView.setAdapter(adapter)
                autoCompleteTextView.threshold = 1
            }
            override fun afterTextChanged(s: Editable) {
                pName = s.toString()
                var rec = selectData(pName, pYear)  // 本データを取得
                setCounter(rec, pMonth)             // データを集計して表示
            }
        })

        val month = findViewById<EditText>(R.id.Month) as EditText
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
                var rec = selectData(pName, pYear)  // データを取得
                setCounter(rec, pMonth)             // データを集計して表示
            }
        })

        val year = findViewById<View>(R.id.Year) as EditText
        year.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable) {
                var y = if(s.toString() == "") 0 else s.toString().toInt()
                pYear = y
                var rec = selectData(pName, pYear)  // データを取得
                setCounter(rec, pMonth)             // データを集計して表示
            }
        })

        // 初期値日付をセット
        InitDate()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //外部ストレージ　ルートフォルダパス取得
        var externalFilesDirs = this@MainActivity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.path.split("/")
        var externalFilesDirsPath = ""
        var i = 0
        externalFilesDirs.forEach { externalFilesDirsPeart ->
            if(i > 3){
                return@forEach
            }
            if(externalFilesDirsPeart != ""){
                externalFilesDirsPath = externalFilesDirsPath + "/" + externalFilesDirsPeart
            }
            i++
        }

        when (item?.itemId) {
            R.id.action_import -> startFileBrowser(Mode.IMPORT)
            R.id.action_export -> startFileBrowser(Mode.EXPORT)

            else -> {}
        }
        return true
    }

    // デフォルト日付をセット
    fun InitDate()
    {
        val m = findViewById<EditText>(R.id.Month)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) +1 // 月は0始まりのため+1する
        m.setText(currentMonth.toString())

        val y = findViewById<EditText>(R.id.Year)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        y.setText(currentYear.toString())
    }

    fun buttonOnClick_CountUp(view: View){ // ①クリック時の処理を追加

        try {
            var y = findViewById<EditText>(R.id.Year)
            var year = y.text.toString().toInt()

            var m = findViewById<EditText>(R.id.Month)
            var month = m.text.toString().toInt()

            var n = findViewById<EditText>(R.id.Name)
            var name = n.text.toString()

            var c = findViewById<EditText>(R.id.Contents)
            var contents = c.text.toString()

            var rec = selectData(name, year)  // データを取得
            setCounter(rec, month)             // データを集計して表示

            AlertDialog.Builder(this@MainActivity)
                .setTitle("確認")
                .setMessage("カウントを1増やします。よろしいですか。")
                .setPositiveButton(
                    "OK"
                ) { dialog, which ->
                    // カウンタ更新処理
                    var ret = submit_inc(contents, name, year, month)
                    if (ret) {
                        Toast.makeText(this, "加算が完了しました。", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "エラーが発生しました。", Toast.LENGTH_LONG).show()
                    }
                    var rec = selectData(name, year)  // データを取得
                    setCounter(rec, month)             // データを集計して表示
                }
                .setNegativeButton(
                    "キャンセル"
                ) { dialog, which ->
                    // 何もしない
                }
                .show()
        }catch (e:NumberFormatException) {
            Toast.makeText(this, "数値を入力してください。", Toast.LENGTH_LONG).show()
        }
    }

    fun buttonOnClick_Update(view: View){ // ①クリック時の処理を追加
        try {
            var y = findViewById<EditText>(R.id.Year)
            var year = y.text.toString().toInt()

            var m = findViewById<EditText>(R.id.Month)
            var month = m.text.toString().toInt()

            var n = findViewById<EditText>(R.id.Name)
            var name = n.text.toString()

            var c = findViewById<EditText>(R.id.Contents)
            var contents = c.text.toString()

            var uc = findViewById<EditText>(R.id.UpdateCount)
            var updcount = uc.text.toString().toInt()

            var rec = selectData(name, year)  // データを取得
            setCounter(rec, month)             // データを集計して表示

            AlertDialog.Builder(this@MainActivity)
                .setTitle("確認")
                .setMessage("カウントを" + updcount.toString() + "に変更します。よろしいですか。")
                .setPositiveButton(
                    "OK"
                ) { dialog, which ->
                    // カウンタ更新処理
                    var ret = submit_upd(contents, name, year, month, updcount)
                    if (ret) {
                        Toast.makeText(this, "更新が完了しました。", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "エラーが発生しました。", Toast.LENGTH_LONG).show()
                    }
                    var rec = selectData(name, year)  // データを取得
                    setCounter(rec, month)             // データを集計して表示
                }
                .setNegativeButton(
                    "キャンセル"
                ) { dialog, which ->
                    // 何もしない
                }
                .show()
        }catch (e:NumberFormatException) {
            Toast.makeText(this, "数値を入力してください。", Toast.LENGTH_LONG).show()
        }
    }

    private fun submit_inc(contents :String, name :String, year: Int, month:Int): Boolean
    {
        var rec = selectData(name, year)
        if(rec.id == -1 || rec.year != year)        // データそのものが取得できなかった場合、取得できた年が違う場合、Insert
        {
            var ret = insertData(contents, name, year, month, 1)
            if(ret == false)
            {
                return false
            }
        }
        else
        {
            var count = 0
            when (month) {
                1 -> count = rec.jan + 1
                2 -> count = rec.feb + 1
                3 -> count = rec.mar + 1
                4 -> count = rec.apr + 1
                5 -> count = rec.may + 1
                6 -> count = rec.jun + 1
                7 -> count = rec.jul + 1
                8 -> count = rec.aug + 1
                9 -> count = rec.sep + 1
                10 -> count = rec.oct + 1
                11 -> count = rec.nov + 1
                12 -> count = rec.dec + 1
            }

            var ret = updateData(rec.id.toString(), year, month, count)
            if(ret == false)
            {
                return false
            }
        }

        return true
    }

    private fun submit_upd(contents :String, name :String, year: Int, month:Int, count:Int): Boolean
    {
        var rec = selectData(name, year)
        if(rec.id == -1 || rec.year != year)        // データそのものが取得できなかった場合、取得できた年が違う場合、Insert
        {
            var ret = insertData(contents, name, year, month, count)
            if(ret == false)
            {
                return false
            }
        }
        else
        {
            var ret = updateData(rec.id.toString(), year, month, count)
            if(ret == false)
            {
                return false
            }
        }

        return true
    }

    private fun csvInsert(csvRow: List<String>): Boolean
    {
        if(csvRow.count() > 16)
        {
            Toast.makeText(this, "カラム数が足りません。", Toast.LENGTH_LONG).show()
            return false
        }
        val rec = Record(
            id = -1,
            year = csvRow[1].toInt(),
            name = csvRow[2],
            contents = csvRow[3],
            jan = if(csvRow[4].isNullOrEmpty()) 0 else csvRow[4].toInt() ,
            feb = if(csvRow[5].isNullOrEmpty()) 0 else csvRow[5].toInt() ,
            mar = if(csvRow[6].isNullOrEmpty()) 0 else csvRow[6].toInt() ,
            apr = if(csvRow[7].isNullOrEmpty()) 0 else csvRow[7].toInt() ,
            may = if(csvRow[8].isNullOrEmpty()) 0 else csvRow[8].toInt() ,
            jun = if(csvRow[9].isNullOrEmpty()) 0 else csvRow[9].toInt() ,
            jul = if(csvRow[10].isNullOrEmpty()) 0 else csvRow[10].toInt(),
            aug = if(csvRow[11].isNullOrEmpty()) 0 else csvRow[11].toInt(),
            sep = if(csvRow[12].isNullOrEmpty()) 0 else csvRow[12].toInt(),
            oct = if(csvRow[13].isNullOrEmpty()) 0 else csvRow[13].toInt(),
            nov = if(csvRow[14].isNullOrEmpty()) 0 else csvRow[14].toInt(),
            dec = if(csvRow[15].isNullOrEmpty()) 0 else csvRow[15].toInt(),
        )

        var ret = selectData(rec.name, rec.year)
        if(ret.id == -1)
        {
            var ret = insertData(rec)
            if(ret == false)
            {
                return false
            }
        }
        else
        {
            var ret = updateData(rec)
            if(ret == false)
            {
                return false
            }
        }

        return true
    }

    private fun setCounter(record:Record, month:Int)
    {
        var yCount = 0
        var mCount = 0

        if(pYear == record.year) // 年が一致しない場合、今年データがないため、コンテンツ名称のみをセット
        {
            // 引数の月度のカウントを格納
            when (month) {
                1 -> mCount = record.jan
                2 -> mCount = record.feb
                3 -> mCount = record.mar
                4 -> mCount = record.apr
                5 -> mCount = record.may
                6 -> mCount = record.jun
                7 -> mCount = record.jul
                8 -> mCount = record.aug
                9 -> mCount = record.sep
                10 -> mCount = record.oct
                11 -> mCount = record.nov
                12 -> mCount = record.dec
            }

            // データのカウントを合計
            yCount = record.jan + record.feb + record.mar + record.apr +
                    record.may + record.jun + record.jul + record.aug +
                    record.sep + record.oct + record.nov + record.dec
        }

        // それぞれ画面に表示
        if(record.id != -1) {
            var co = findViewById<TextView>(R.id.Contents)
            co.setText(record.contents)
        }

        var yc = findViewById<TextView>(R.id.yCount)
        yc.setText(yCount.toString())

        var ym = findViewById<TextView>(R.id.mCount)
        ym.setText(mCount.toString())
    }

    private fun selectDataLike(name: String, year: Int): Array<String> {
        helper = DatabaseHelper(applicationContext)
        val db = helper.readableDatabase
        var setList = mutableListOf<String>()
        val sql = " select distinct name " +
                " from SpCount " +
                " where name LIKE '%" + name.replace("'", "''") + "%'" +      // SQLインジェクションされそう。とりあえずこれで。どうせ俺しか使わんし
                " order by _id "
        try {
            val cursor = db.rawQuery(sql, null)
            cursor.use {
                // ループによるデータ取得
                // Cursor.moveToNext(): 次の行に移動
                // -> 次の行が存在する場合はtrue, 存在しない場合はfalseを返す
                // <- 最初はテーブルの0行目に位置しているため、
                //    while構文を用いて最初に1行目に移る処理を行う
                while (cursor.moveToNext()) {
                    val idxDescription = cursor.getColumnIndex("name")
                    setList.add(cursor.getString(idxDescription))
                }
            }

            cursor.close()
        } catch (exception: Exception) {
            Log.e("selectDataLike", exception.toString())
        }

        return setList.toTypedArray()
    }

    private fun selectData(name: String, year: Int): Record{
        helper = DatabaseHelper(applicationContext)
        val db = helper.readableDatabase
        val retRecord = Record(
            id = -1,
            contents = "",
            name = "",
            year = -1,
            jan = 0,
            feb = 0,
            mar = 0,
            apr = 0,
            may = 0,
            jun = 0,
            jul = 0,
            aug = 0,
            sep = 0,
            oct = 0,
            nov = 0,
            dec = 0
        )
        val sql = " select * " +
                  " from SpCount " +
                  " where name = '" + name.replace("'","''") + "'" +      // SQLインジェクションされそう。とりあえずこれで。どうせ俺しか使わんし
                  " and year = " + year.toString()
                  " order by _id "
        try {
            val cursor = db.rawQuery(sql, null)
            cursor.use {
                if (cursor.count > 0) {
                    cursor.moveToFirst()
                    // 最初の行のみ取得
                    retRecord.id = cursor.getInt(0)
                    retRecord.year = cursor.getInt(1)
                    retRecord.name = cursor.getString(2)
                    retRecord.contents = cursor.getString(3)
                    retRecord.jan = cursor.getInt(4)
                    retRecord.feb = cursor.getInt(5)
                    retRecord.mar = cursor.getInt(6)
                    retRecord.apr = cursor.getInt(7)
                    retRecord.may = cursor.getInt(8)
                    retRecord.jun = cursor.getInt(9)
                    retRecord.jul = cursor.getInt(10)
                    retRecord.aug = cursor.getInt(11)
                    retRecord.sep = cursor.getInt(12)
                    retRecord.oct = cursor.getInt(13)
                    retRecord.nov = cursor.getInt(14)
                    retRecord.dec = cursor.getInt(15)
                }
            }

            cursor.close()
        } catch(exception: Exception) {
            Log.e("selectData", exception.toString())
        }

        if(retRecord.id == -1)      // 取得できなかった場合、名称と年違いのデータを取得するために再Select
        {
            val sql = " select * " +
                    " from SpCount " +
                    " where name = '" + name.replace("'","''") + "'" +      // SQLインジェクションされそう。とりあえずこれで。どうせ俺しか使わんし
                    " order by _id, year DESC "
            try {
                val cursor = db.rawQuery(sql, null)
                cursor.use {
                    if (cursor.count > 0) {
                        cursor.moveToFirst()
                        // 最初の行のみ取得
                        retRecord.id = cursor.getInt(0)
                        retRecord.year = cursor.getInt(1)
                        retRecord.name = cursor.getString(2)
                        retRecord.contents = cursor.getString(3)
                        retRecord.jan = cursor.getInt(4)
                        retRecord.feb = cursor.getInt(5)
                        retRecord.mar = cursor.getInt(6)
                        retRecord.apr = cursor.getInt(7)
                        retRecord.may = cursor.getInt(8)
                        retRecord.jun = cursor.getInt(9)
                        retRecord.jul = cursor.getInt(10)
                        retRecord.aug = cursor.getInt(11)
                        retRecord.sep = cursor.getInt(12)
                        retRecord.oct = cursor.getInt(13)
                        retRecord.nov = cursor.getInt(14)
                        retRecord.dec = cursor.getInt(15)
                    }
                }

                cursor.close()
            } catch(exception: Exception) {
                Log.e("selectData", exception.toString())
            }
        }

        return retRecord
    }

    private fun selectContentsLike(contents: String): Array<String> {
        helper = DatabaseHelper(applicationContext)
        val db = helper.readableDatabase
        var setList = mutableListOf<String>()
        val retRecord = Record(
            id = -1,
            contents = "",
            name = "",
            year = -1,
            jan = 0,
            feb = 0,
            mar = 0,
            apr = 0,
            may = 0,
            jun = 0,
            jul = 0,
            aug = 0,
            sep = 0,
            oct = 0,
            nov = 0,
            dec = 0
        )
        val sql = " select DISTINCT contents " +
                " from SpCount " +
                " where contents LIKE '%" + contents.replace("'", "''") + "%'" +     // SQLインジェクションされそう。とりあえずこれで。どうせ俺しか使わんし
                " order by _id "
        try {
            val cursor = db.rawQuery(sql, null)
            cursor.use {
                // ループによるデータ取得
                // Cursor.moveToNext(): 次の行に移動
                // -> 次の行が存在する場合はtrue, 存在しない場合はfalseを返す
                // <- 最初はテーブルの0行目に位置しているため、
                //    while構文を用いて最初に1行目に移る処理を行う
                while (cursor.moveToNext()) {
                    val idxDescription = cursor.getColumnIndex("contents")
                    setList.add(cursor.getString(idxDescription))
                }
            }

            cursor.close()
        } catch (exception: Exception) {
            Log.e("selectContentsLike", exception.toString())
        }

        return setList.toTypedArray()
    }

    private fun selectDataAll(): Array<Record> {
        helper = DatabaseHelper(applicationContext)
        val db = helper.readableDatabase
        var setList = mutableListOf<Record>()
        val sql = " select * " +
                " from SpCount " +
                " order by _id "
        try {
            val cursor = db.rawQuery(sql, null)
            cursor.use {
                // ループによるデータ取得
                // Cursor.moveToNext(): 次の行に移動
                // -> 次の行が存在する場合はtrue, 存在しない場合はfalseを返す
                // <- 最初はテーブルの0行目に位置しているため、
                //    while構文を用いて最初に1行目に移る処理を行う
                while (cursor.moveToNext()) {
                        // 最初の行のみ取得
                    val retRecord = Record(
                        id = cursor.getInt(0),
                        year = cursor.getInt(1),
                        name = cursor.getString(2),
                        contents = cursor.getString(3),
                        jan = cursor.getInt(4),
                        feb = cursor.getInt(5),
                        mar = cursor.getInt(6),
                        apr = cursor.getInt(7),
                        may = cursor.getInt(8),
                        jun = cursor.getInt(9),
                        jul = cursor.getInt(10),
                        aug = cursor.getInt(11),
                        sep = cursor.getInt(12),
                        oct = cursor.getInt(13),
                        nov = cursor.getInt(14),
                        dec = cursor.getInt(15)
                    )
                    setList.add(retRecord)
                }
            }
            cursor.close()
        } catch(exception: Exception) {
            Log.e("selectDataAll", exception.toString())
        }
        return setList.toTypedArray()
    }

    private fun insertData(contents: String, name: String, year: Int, month: Int, count: Int): Boolean{
        try {
            val dbHelper = DatabaseHelper(applicationContext)
            val database = dbHelper.writableDatabase
            val values = ContentValues()
            values.put("name", name)
            values.put("contents", contents)
            values.put("year", year)
            values.put("jan", 0)
            values.put("feb", 0)
            values.put("mar", 0)
            values.put("apr", 0)
            values.put("may", 0)
            values.put("jun", 0)
            values.put("jul", 0)
            values.put("aug", 0)
            values.put("sep", 0)
            values.put("oct", 0)
            values.put("nov", 0)
            values.put("dec", 0)

            var key = getMonthName(month)

            if (values.containsKey(key)){
                values.remove(key)
                values.put(key, count)
            }

            database.insert("SpCount", null, values)
        }catch(exception: Exception) {
            Log.e("insertData", exception.toString())
            return false
        }

        return true
    }

    private fun insertData(rec:Record): Boolean{
        try {
            val dbHelper = DatabaseHelper(applicationContext);
            val database = dbHelper.writableDatabase
            val values = ContentValues()
            values.put("name", rec.name)
            values.put("contents", rec.contents)
            values.put("year", rec.year)
            values.put("jan", rec.jan)
            values.put("feb", rec.feb)
            values.put("mar", rec.mar)
            values.put("apr", rec.apr)
            values.put("may", rec.may)
            values.put("jun", rec.jun)
            values.put("jul", rec.jul)
            values.put("aug", rec.aug)
            values.put("sep", rec.sep)
            values.put("oct", rec.oct)
            values.put("nov", rec.nov)
            values.put("dec", rec.dec)
            database.insert("SpCount", null, values)
        }catch(exception: Exception) {
            Log.e("insertData", exception.toString())
            return false
        }

        return true
    }

    private fun updateData(whereId: String, year: Int, month: Int, newCount: Int): Boolean{
        try {
            val dbHelper = DatabaseHelper(applicationContext);
            val database = dbHelper.writableDatabase

            val values = ContentValues()
            var key = getMonthName(month)
            values.put(key, newCount)

            val whereClauses = "_id = ? and year = ?"
            val whereArgs = arrayOf(whereId, year.toString())
            database.update("SpCount", values, whereClauses, whereArgs)
        }catch(exception: Exception) {
            Log.e("updateData", exception.toString())
            return false
        }

        return true
    }

    private fun updateData(rec:Record): Boolean{
        try {
            val dbHelper = DatabaseHelper(applicationContext);
            val database = dbHelper.writableDatabase
            val values = ContentValues()

            values.put("name", rec.name)
            values.put("contents", rec.contents)
            values.put("year", rec.year)
            values.put("jan", rec.jan)
            values.put("feb", rec.feb)
            values.put("mar", rec.mar)
            values.put("apr", rec.apr)
            values.put("may", rec.may)
            values.put("jun", rec.jun)
            values.put("jul", rec.jul)
            values.put("aug", rec.aug)
            values.put("sep", rec.sep)
            values.put("oct", rec.oct)
            values.put("nov", rec.nov)
            values.put("dec", rec.dec)

            val whereClauses = "_id = ? and year = ?"
            val whereArgs = arrayOf(rec.id.toString(), rec.year.toString())
            database.update("SpCount", values, whereClauses, whereArgs)
        }catch(exception: Exception) {
            Log.e("updateData", exception.toString())
            return false
        }

        return true
    }

    private fun getMonthName(month: Int) :String {
        var key = ""
        when (month) {
            1 ->  key = "jan"
            2 ->  key = "feb"
            3 ->  key = "mar"
            4 ->  key = "apr"
            5 ->  key = "may"
            6 ->  key = "jun"
            7 ->  key = "jul"
            8 ->  key = "aug"
            9 ->  key = "sep"
            10 ->  key = "oct"
            11 ->  key = "nov"
            12 ->  key = "dec"
        }

        return key
    }

    @Suppress("DEPRECATION")
    private fun startFileBrowser(mode: Mode) {
        var intent: Intent? = null
        try {
            when (mode) {
                Mode.IMPORT -> {
                    intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.type = "*/*" //TEXT file only
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false) // 1件しか選択できないようにする
                    startActivityForResult(
                        Intent.createChooser(intent, "Open a file"),
                        REQUEST_OPEN_FILE
                    )
                }

                Mode.EXPORT -> {
                    intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                    intent.type = "*/*" //TEXT file only
                    intent.putExtra(Intent.EXTRA_TITLE, "SpCount.csv")
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    startActivityForResult(
                        Intent.createChooser(intent, "Create a file"),
                        REQUEST_CREATE_FILE
                    )
                }

                else -> {}
            }
        } catch (ex: ActivityNotFoundException) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Browser/Manager", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // File load
        if (requestCode == REQUEST_OPEN_FILE) {
            if (resultCode == RESULT_OK && data != null) {
                val uri = data.data
                // ファイルパスがCSV形式の拡張子で終わるか確認
                if (uri != null) {
                    loadStrFromUri(uri)
                    var rec = selectData(pName, pYear)  // 本データを取得
                    setCounter(rec, pMonth)             // データを集計して表示
                }
            }

        } else if (requestCode == REQUEST_CREATE_FILE) {
            if (resultCode == RESULT_OK && data != null) {
                val uri = data.data
                if (uri != null) {
                    saveStrToUri(uri, exportString());
                }
            }
        } else {
        }
    }

    private fun loadStrFromUri(uri: Uri){
        var str = ""
        val csvData: MutableList<List<String>> = mutableListOf()

        // DBをリセット
        val dbHelper = DatabaseHelper(applicationContext)
        val database = dbHelper.writableDatabase
        helper.resetDb(database)
        try {
            if (uri.scheme == "content") {
                val iStream = contentResolver.openInputStream(uri)
                if (iStream != null) {
                    val reader = BufferedReader(InputStreamReader(iStream, "Shift-JIS"))
                    var line: String?
                    // 初行を読み飛ばす
                    reader.readLine()
                    reader.use { br ->
                        var line: String?
                        while (br.readLine().also { line = it } != null) {
                            val csvRow = line!!.split(",") // CSVのデリミタに応じて変更
                            val r = csvInsert(csvRow)
                            if(r == false)
                            {
                                reader.close()
                                return
                            }
                        }
                    }
                    reader.close()
                    Toast.makeText(this, "ファイルのインポートが完了しました。", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Unknown scheme", Toast.LENGTH_LONG).show()
            }
        } catch (e: java.lang.Exception) {
            Toast.makeText(this, "ファイルのインポートに失敗しました。:$e", Toast.LENGTH_LONG).show()
            Log.e("a", e.toString())
        }
        return
    }

    private fun saveStrToUri(uri: Uri, text: String) {
        try {
            if (uri.scheme == "content") {
                // ContentResolverを使用してOutputStreamを取得
                val oStream: OutputStream? = contentResolver.openOutputStream(uri)

                // OutputStreamが取得できた場合はデータを書き込む
                if (oStream != null) {
                    oStream.write(text.toByteArray(Charset.forName("Shift-JIS")))
                    oStream.close()
                } else {
                    Toast.makeText(this, "OutputStreamが取得できません", Toast.LENGTH_LONG).show()
                    return
                }

                Toast.makeText(this, "ファイルのエクスポートが完了しました。", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Unknown scheme", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Cannot write the file: $e", Toast.LENGTH_LONG).show()
        }
    }

    fun exportString(): String {
        var getList = selectDataAll()
        var retString :String = "id,year,name,content,jan,feb,mar,apr,may,jun,jul,aug,sep,oct,nov,dec" + "\r\n"
        for(i in 0 until getList.count())
        {
            val rec = getList[i]
            retString += rec.id.toString() + ","
            retString += rec.year.toString() + ","
            retString += rec.name + ","
            retString += rec.contents + ","
            retString += rec.jan.toString() + ","
            retString += rec.feb.toString() + ","
            retString += rec.mar.toString() + ","
            retString += rec.apr.toString() + ","
            retString += rec.may.toString() + ","
            retString += rec.jun.toString() + ","
            retString += rec.jul.toString() + ","
            retString += rec.aug.toString() + ","
            retString += rec.sep.toString() + ","
            retString += rec.oct.toString() + ","
            retString += rec.nov.toString() + ","
            retString += rec.dec.toString()
            retString += "\r\n"
        }

        return retString
    }
}
