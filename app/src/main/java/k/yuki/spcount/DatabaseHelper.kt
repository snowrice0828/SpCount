package k.yuki.spcount

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper internal constructor(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        // テーブル作成
        // SQLiteファイルがなければSQLiteファイルが作成される
        db.execSQL(
            SQL_CREATE_ENTRIES
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // アップデートの判別
        db.execSQL(
            SQL_DELETE_ENTRIES
        )
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    public fun resetDb(db: SQLiteDatabase)
    {
        db.execSQL(
            SQL_DELETE_ENTRIES
        )
        onCreate(db)
    }

    // データベース作成
    companion object {
        // データーベースのバージョン
        private const val DATABASE_VERSION = 3

        // データーベース名
        private const val DATABASE_NAME = "SpCountDB.db"
        public const val TABLE_NAME = "SpCount"
        private const val SQL_CREATE_ENTRIES = "CREATE TABLE " + TABLE_NAME + " (" +
                " _id INTEGER PRIMARY KEY, " +
                "ymd INTEGER, " +
                "name TEXT, " +
                "contents TEXT, " +
                "remarks TEXT " +
                " )"

        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME
    }

    // 部分一致人物名取得
    internal fun selectDataLikeName(name: String, year: Int): Array<String> {
        val db = this.readableDatabase
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

    // 部分一致コンテンツ名取得
    internal fun selectDataLikeContents(contents: String): Array<String> {
        val db = this.readableDatabase
        var setList = mutableListOf<String>()
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

    internal fun selectData(name: String, year: Int): MainActivity.Record {
        val db = this.readableDatabase
        val retRecord = MainActivity.Record()
        val sql = " select * " +
                " from SpCount " +
                " where name = '" + name.replace("'","''") + "'" +      // SQLインジェクションされそう。とりあえずこれで。どうせ俺しか使わんし
                " and (ymd / 10000) = " + year.toString()
                " order by _id "
        try {
            val cursor = db.rawQuery(sql, null)
            cursor.use {
                if (cursor.count > 0) {
                    cursor.moveToFirst()
                    // 最初の行のみ取得
                    retRecord.id = cursor.getInt(0)
                    retRecord.ymd = cursor.getInt(1)
                    retRecord.name = cursor.getString(2)
                    retRecord.contents = cursor.getString(3)
                    retRecord.remarks = cursor.getString(4)
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
                    " order by _id, ymd DESC " +
                    " LIMIT 1"
            try {
                val cursor = db.rawQuery(sql, null)
                cursor.use {
                    if (cursor.count > 0) {
                        cursor.moveToFirst()
                        // 最初の行のみ取得
                        retRecord.id = cursor.getInt(0)
                        retRecord.ymd = cursor.getInt(1)
                        retRecord.name = cursor.getString(2)
                        retRecord.contents = cursor.getString(3)
                        retRecord.remarks = cursor.getString(4)
                    }
                }

                cursor.close()
            } catch(exception: Exception) {
                Log.e("selectData", exception.toString())
            }
        }

        return retRecord
    }

    // カレンダー表示用 日付単位でデータ取得
    internal fun selectData(ymd: Int): Array<MainActivity.Record> {
        var setList = mutableListOf<MainActivity.Record>()
        val db = this.readableDatabase
        val retRecord = MainActivity.Record()
        val sql = " select * " +
                " from SpCount " +
                " where ymd = " + ymd.toString()
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
                    val retRecord = MainActivity.Record(
                        id = cursor.getInt(0),
                        ymd = cursor.getInt(1),
                        name = cursor.getString(2),
                        contents = cursor.getString(3),
                        remarks = cursor.getString(4)
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

    // 全データ取得
    internal fun selectDataAll(): Array<MainActivity.Record> {

        val db = this.readableDatabase
        var setList = mutableListOf<MainActivity.Record>()
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
                    val retRecord = MainActivity.Record(
                        id = cursor.getInt(0),
                        ymd = cursor.getInt(1),
                        name = cursor.getString(2),
                        contents = cursor.getString(3),
                        remarks = cursor.getString(4)
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

    // 集計後データ取得
    internal fun selectAggData(pYear: Int): Array<MainActivity.AggRecord>
    {
        val db = this.readableDatabase
        var setList = mutableListOf<MainActivity.AggRecord>()
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
                    val retRecord = MainActivity.AggRecord(
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

    // データ追加
    internal fun insertData(contents: String, name: String, ymd: Int, remarks: String): Boolean{
        try {
            val database = this.writableDatabase
            val values = ContentValues()
            values.put("ymd", ymd)
            values.put("name", name)
            values.put("contents", contents)
            values.put("remarks", remarks)

            database.insert("SpCount", null, values)
        }catch(exception: Exception) {
            Log.e("insertData", exception.toString())
            return false
        }

        return true
    }

    /// データ追加
    internal fun insertData(rec: MainActivity.Record): Boolean{
        return insertData(rec.contents, rec.name, rec.ymd, rec.remarks)
    }

    // データ更新
    internal fun updateData(whereId: Int, ymd: Int, contents: String, name: String, remarks: String ): Boolean{

        try {
            val database = this.writableDatabase
            val values = ContentValues()

            values.put("name", name)
            values.put("contents", contents)
            values.put("ymd", ymd)
            values.put("remarks", remarks)

            val whereClauses = "_id = ?"
            val whereArgs = arrayOf(whereId.toString())
            database.update("SpCount", values, whereClauses, whereArgs)
        }catch(exception: Exception) {
            Log.e("updateData", exception.toString())
            return false
        }

        return true
    }

    // データ更新
    internal fun updateData(rec: MainActivity.Record): Boolean{
        return updateData(rec.id, rec.ymd, rec.contents, rec.name, rec.remarks)
    }

}