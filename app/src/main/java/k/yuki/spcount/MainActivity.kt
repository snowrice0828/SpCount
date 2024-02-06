package k.yuki.spcount

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.size
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() , BottomNavigationView.OnNavigationItemSelectedListener{
    private lateinit var helper: DatabaseHelper

    private val REQUEST_OPEN_FILE = 1001
    private val REQUEST_CREATE_FILE = 1002
    public data class Record(
        var id: Int = -1,
        var ymd: Int = -1,
        var name: String = "",
        var contents: String = "",
        var remarks: String = ""
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setContentView(R.layout.activity_main)
        helper = DatabaseHelper(applicationContext)


        initializeResource()
    }

    fun initializeResource(){
//		BottomNavigationViewを設定
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        bottomNavigationView.setOnNavigationItemSelectedListener(this)
        bottomNavigationView.itemIconSize = 70
        bottomNavigationView.scaleX = 1.2f
        bottomNavigationView.scaleY = 1.2f

//		初期Fragmentを設定
        supportFragmentManager.beginTransaction()
            .replace(R.id.container,HomeFragment())
            .setReorderingAllowed(true)
            .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.d(TAG,"Selected item: " + item)

        when(item.itemId){
//			ホームボタンが押された時
            R.id.action_home -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container,HomeFragment())
                    .setReorderingAllowed(true)
                    .commit()
            }
//			カレンダーボタンが押された時
            R.id.action_calender -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container,CalendarFragment())
                    .setReorderingAllowed(true)
                    .commit()
            }
        }

        return true
    }

    companion object{
        const val TAG : String = "MainActivity"
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
            ymd = csvRow[1].toInt(),
            name = csvRow[2],
            contents = csvRow[3],
            remarks = csvRow[4]
        )

        var ret = helper.selectData(rec.name, (rec.ymd / 10000))
        if(ret.id == -1)
        {
            var ret = helper.insertData(rec)
            if(ret == false)
            {
                return false
            }
        }
        else
        {
            var ret = helper.updateData(rec)
            if(ret == false)
            {
                return false
            }
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //外部ストレージ　ルートフォルダパス取得
        var externalFilesDirs = MainActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.path.split("/")
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

        when (item?.itemId) {
            R.id.action_import -> startFileBrowser(HomeFragment.Mode.IMPORT)
            R.id.action_export -> startFileBrowser(HomeFragment.Mode.EXPORT)

            else -> {}
        }
        return true
    }

    @Suppress("DEPRECATION")
    private fun startFileBrowser(mode: HomeFragment.Mode) {
        var intent: Intent? = null
        try {
            when (mode) {
                HomeFragment.Mode.IMPORT -> {
                    intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.type = "*/*" //TEXT file only
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false) // 1件しか選択できないようにする
                    startActivityForResult(
                        Intent.createChooser(intent, "Open a file"),
                        REQUEST_OPEN_FILE
                    )
                }

                HomeFragment.Mode.EXPORT -> {
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
        var dbHelper = DatabaseHelper(this)
        var database = dbHelper.writableDatabase
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
        var getList = helper.selectDataAll()
        var retString :String = "id,year,name,content,jan,feb,mar,apr,may,jun,jul,aug,sep,oct,nov,dec" + "\r\n"
        for(i in 0 until getList.count())
        {
            val rec = getList[i]
            retString += rec.id.toString() + ","
            retString += rec.ymd.toString() + ","
            retString += rec.name + ","
            retString += rec.contents + ","
            retString += rec.remarks
            retString += "\r\n"
        }

        return retString
    }
}