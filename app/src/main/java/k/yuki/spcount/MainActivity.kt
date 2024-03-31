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

    public data class Record(
        var id: Int = -1,
        var ymd: Int = -1,
        var name: String = "",
        var contents: String = "",
        var remarks: String = ""
    )

    public data class AggRecord(
        var year: Int = -1,
        var name: String = "",
        var contents: String = "",
        var jan: Int = 0,
        var feb: Int = 0,
        var mar: Int = 0,
        var apr: Int = 0,
        var may: Int = 0,
        var jun: Int = 0,
        var jul: Int = 0,
        var aug: Int = 0,
        var sep: Int = 0,
        var oct: Int = 0,
        var nov: Int = 0,
        var dec: Int = 0
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
        bottomNavigationView.itemIconSize = 60
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
//			ファイルボタンが押された時
            R.id.action_file -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container,FileFragment())
                    .setReorderingAllowed(true)
                    .commit()
            }
        }

        return true
    }

    companion object{
        const val TAG : String = "MainActivity"
    }


}