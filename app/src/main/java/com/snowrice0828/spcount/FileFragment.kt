package com.snowrice0828.spcount

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.Calendar
import android.os.Bundle
import android.os.Environment
import android.content.Intent
import android.content.ActivityNotFoundException
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FileFragment : Fragment(){
    private val REQUEST_OPEN_FILE = 1001
    private val REQUEST_CREATE_FILE = 1002
    private val REQUEST_CREATE_FILE2 = 1003
    private lateinit var helper: DatabaseHelper
    public var pYear: Int = 0

    public enum class Mode {
        IMPORT, EXPORT, AGGREGATE
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_file, container, false)
        helper = DatabaseHelper(view.context)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                setCounter(view, y)
            }
        })

        view.findViewById<Button>(R.id.aggbutton).setOnClickListener {
            fileControl(FileFragment.Mode.AGGREGATE)
        }

        view.findViewById<Button>(R.id.exportbutton).setOnClickListener {
            fileControl(FileFragment.Mode.EXPORT)
        }

        view.findViewById<Button>(R.id.importbutton).setOnClickListener {
            fileControl(FileFragment.Mode.IMPORT)
        }
    }

    fun InitDate(view:View)
    {
        val y = view.findViewById<EditText>(R.id.Year)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        y.setText(currentYear.toString())
        pYear = currentYear

        setCounter(view, currentYear)
    }

    private fun csvInsert(csvRow: List<String>): Boolean
    {
        if(csvRow.count() > 16)
        {
            Toast.makeText(requireActivity(), "カラム数が足りません。", Toast.LENGTH_LONG).show()
            return false
        }
        val rec = MainActivity.Record(
            id = -1,
            ymd = csvRow[1].toInt(),
            name = csvRow[2],
            contents = csvRow[3],
            remarks = csvRow[4]
        )

        var ret = helper.insertData(rec)
        if(ret == false)
        {
            return false
        }

        return true
    }

    fun fileControl(mode: Mode): Boolean {
        //外部ストレージ　ルートフォルダパス取得
        var externalFilesDirs = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.path.split("/")
        var externalFilesDirsPath = ""
        var i = 0
        externalFilesDirs.forEach { externalFilesDirsPart ->
            if(i > 3){
                return@forEach
            }
            if(externalFilesDirsPart != ""){
                externalFilesDirsPath = externalFilesDirsPath + "/" + externalFilesDirsPart
            }
            i++
        }

        startFileBrowser(mode)

        return true
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
                    intent.putExtra(Intent.EXTRA_TITLE, "BuckUp.csv")
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    startActivityForResult(
                        Intent.createChooser(intent, "Create a file"),
                        REQUEST_CREATE_FILE
                    )
                }

                Mode.AGGREGATE -> {
                    intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                    intent.type = "*/*" //TEXT file only
                    intent.putExtra(Intent.EXTRA_TITLE, "AggregateData.csv")
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    startActivityForResult(
                        Intent.createChooser(intent, "Create a file"),
                        REQUEST_CREATE_FILE2
                    )
                }

                else -> {}
            }
        } catch (ex: ActivityNotFoundException) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(requireContext(), "Please install a File Browser/Manager", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // File load
        if (requestCode == REQUEST_OPEN_FILE) {
            if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                val uri = data.data
                // ファイルパスがCSV形式の拡張子で終わるか確認
                if (uri != null) {
                    loadStrFromUri(uri)
                }
            }
        } else if (requestCode == REQUEST_CREATE_FILE) {
            if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                val uri = data.data
                if (uri != null) {
                    saveStrToUri(uri, exportStringBkup());
                }
            }
        } else if (requestCode == REQUEST_CREATE_FILE2){
            if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                val uri = data.data
                if (uri != null) {
                    saveStrToUri(uri, exportStringAgg());
                }
            }
        }
    }

    private fun loadStrFromUri(uri: Uri){
        var str = ""
        val csvData: MutableList<List<String>> = mutableListOf()

        // DBをリセット
        var dbHelper = DatabaseHelper(requireContext())
        var database = dbHelper.writableDatabase
        helper.resetDb(database)
        try {
            if (uri.scheme == "content") {
                val iStream = requireContext().contentResolver.openInputStream(uri)
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
                    Toast.makeText(requireContext(), "ファイルのインポートが完了しました。", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Unknown scheme", Toast.LENGTH_LONG).show()
            }
        } catch (e: java.lang.Exception) {
            Toast.makeText(requireContext(), "ファイルのインポートに失敗しました。:$e", Toast.LENGTH_LONG).show()
            Log.e("a", e.toString())
        }
        return
    }

    private fun saveStrToUri(uri: Uri, text: String) {
        try {
            if (uri.scheme == "content") {
                // ContentResolverを使用してOutputStreamを取得
                val oStream: OutputStream? = requireContext().contentResolver.openOutputStream(uri)

                // OutputStreamが取得できた場合はデータを書き込む
                if (oStream != null) {
                    oStream.write(text.toByteArray(Charset.forName("Shift-JIS")))
                    oStream.close()
                } else {
                    Toast.makeText(requireContext(), "OutputStreamが取得できません", Toast.LENGTH_LONG).show()
                    return
                }

                Toast.makeText(requireContext(), "ファイルのエクスポートが完了しました。", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Unknown scheme", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Cannot write the file: $e", Toast.LENGTH_LONG).show()
        }
    }

    fun exportStringBkup(): String {
        var getList = helper.selectDataAll()
        var retString :String = "id,ymd,name,contents,remarks" + "\r\n"
        for(i in 0 until getList.count())
        {
            val rec = getList[i]
            retString += rec.id.toString() + ","
            retString += rec.ymd.toString() + ","
            retString += rec.name.replace(",","") + ","
            retString += rec.contents.replace(",","") + ","
            retString += rec.remarks.replace("\n", " ").replace(",","")
            retString += "\r\n"
        }

        return retString
    }

    fun exportStringAgg(): String {
        var getList = helper.selectAggData(pYear)
        var retString :String = "year,name,contents,jan,feb,mar,apr,may,jun,jul,aug,sep,oct,nov,dec" + "\r\n"
        for(i in 0 until getList.count())
        {
            val rec = getList[i]
            retString += rec.year.toString() + ","
            retString += rec.name.replace(",","") + ","
            retString += rec.contents.replace(",","") + ","
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

    fun setCounter(view: View, year: Int)
    {
        var ret = helper.selectCountData(year)  // 本データを取得

        var yc = view.findViewById<TextView>(R.id.yaerCount)
        yc.setText(ret.toString())
    }
}