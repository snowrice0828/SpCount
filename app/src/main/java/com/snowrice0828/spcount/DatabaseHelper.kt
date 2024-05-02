package com.snowrice0828.spcount

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

    // コンテンツ名取得
    internal fun selectContents(name: String): String {
        val db = this.readableDatabase
        var retString: String = ""
        val sql = " select DISTINCT contents, count(*) " +
                " from SpCount " +
                " where name = '" + name.replace("'", "''") + "'" +     // SQLインジェクションされそう。とりあえずこれで。どうせ俺しか使わんし
                " group by contents "
        try {
            val cursor = db.rawQuery(sql, null)
            cursor.use {
                if (cursor.count > 0) {
                    cursor.moveToFirst()
                    // 最初の行のみ取得
                    retString = cursor.getString(0)
                }
            }

            cursor.close()
        } catch (exception: Exception) {
            Log.e("selectContentsLike", exception.toString())
        }

        return retString
    }

    internal fun selectCountData(name: String, contents: String, year: Int, month:Int): IntArray {
        val db = this.readableDatabase
        var retList: IntArray = intArrayOf(0,0)
        var sql = " select count(*)" +
                " from SpCount " +
                " WHERE ymd / 10000 = " + year.toString() +
                "   AND name = '" + name.replace("'", "''") + "'" +
                "   AND contents = '" + contents.replace("'", "''") + "'" +
                " group by name, contents"
        try {
            val cursor = db.rawQuery(sql, null)
            cursor.use {
                if (cursor.count > 0) {
                    cursor.moveToFirst()
                    // 最初の行のみ取得
                    retList[0] = cursor.getInt(0)
                }
            }
            cursor.close()
        } catch(exception: Exception) {
            Log.e("selectCountData1", exception.toString())
        }

        sql = " select count(*)" +
                " from SpCount " +
                " WHERE ymd / 100 = " + (year * 100 + month).toString() +
                "   AND name = '" + name.replace("'", "''") + "'" +
                "   AND contents = '" + contents.replace("'", "''") + "'" +
                " group by name, contents"
        try {
            val cursor = db.rawQuery(sql, null)
            cursor.use {
                if (cursor.count > 0) {
                    cursor.moveToFirst()
                    // 最初の行のみ取得
                    retList[1] = cursor.getInt(0)
                }
            }
            cursor.close()
        } catch(exception: Exception) {
            Log.e("selectCountData2", exception.toString())
        }

        return retList
    }


    // 年間カウント取得
    internal fun selectCountData(year: Int): Int {
        val db = this.readableDatabase
        var ret: Int = 0
        var sql = " select count(*)" +
                " from SpCount " +
                " WHERE ymd / 10000 = " + year.toString()
        try {
            val cursor = db.rawQuery(sql, null)
            cursor.use {
                if (cursor.count > 0) {
                    cursor.moveToFirst()
                    // 最初の行のみ取得
                    ret = cursor.getInt(0)
                }
            }
            cursor.close()
        } catch(exception: Exception) {
            Log.e("selectCountDataInt", exception.toString())
        }
        db.close()

        return ret
    }


    // カレンダー表示用 日付単位でデータ取得
    internal fun selectDataYmd(ymd: Int): Array<MainActivity.Record>
    {
        var setList = mutableListOf<MainActivity.Record>()
        val db = this.readableDatabase
        val retRecord = MainActivity.Record()
        val sql = " select * " +
                " from SpCount " +
                " where ymd = " + ymd.toString() +
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
            Log.e("selectDataYmd", exception.toString())
        }
        return setList.toTypedArray()
    }

    // カレンダー表示用 日付単位でデータ取得
    internal fun selectDataId(Id: Int): MainActivity.Record {
        var retRecord = MainActivity.Record()
        val db = this.readableDatabase
        val sql = " select _id " +
                "         ,ymd" +
                "         ,name" +
                "         ,contents" +
                "         ,remarks" +
                " from SpCount " +
                " where _id = " + Id.toString() +
                " order by _id "
        try {
            val cursor = db.rawQuery(sql, null)
            cursor.use {
                // ループによるデータ取得
                // Cursor.moveToNext(): 次の行に移動
                // -> 次の行が存在する場合はtrue, 存在しない場合はfalseを返す
                // <- 最初はテーブルの0行目に位置しているため、
                //    while構文を用いて最初に1行目に移る処理を行う
                if (cursor.count > 0) {
                    cursor.moveToFirst()
                    // 最初の行のみ取得
                    retRecord = MainActivity.Record(
                        id = cursor.getInt(0),
                        ymd = cursor.getInt(1),
                        name = cursor.getString(2),
                        contents = cursor.getString(3),
                        remarks = cursor.getString(4)
                    )
                }
            }
            cursor.close()
        } catch(exception: Exception) {
            Log.e("selectDataId", exception.toString())
        }
        return retRecord
    }

    // 全データ取得
    internal fun selectDataAll(): Array<MainActivity.Record>
    {
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
        val sql = " select (ymd / 10000)  AS year" +
                " ,name, contents " +
                "         ,sum(case when (ymd / 100) % 100 = 1" +
                "                  then 1" +
                "                  else 0" +
                "              end) as jan" +
                "         ,sum(case when (ymd / 100) % 100 = 2" +
                "                  then 1" +
                "                  else 0" +
                "              end) as feb" +
                "         ,sum(case when (ymd / 100) % 100 = 3" +
                "                  then 1" +
                "                  else 0" +
                "              end) as mar" +
                "         ,sum(case when (ymd / 100) % 100 = 4" +
                "                  then 1" +
                "                  else 0" +
                "              end) as apr" +
                "         ,sum(case when (ymd / 100) % 100 = 5" +
                "                  then 1" +
                "                  else 0" +
                "              end) as may" +
                "         ,sum(case when (ymd / 100) % 100 = 6" +
                "                  then 1" +
                "                  else 0" +
                "              end) as jun" +
                "         ,sum(case when (ymd / 100) % 100 = 7" +
                "                  then 1" +
                "                  else 0" +
                "              end) as jul" +
                "         ,sum(case when (ymd / 100) % 100 = 8" +
                "                  then 1" +
                "                  else 0" +
                "              end) as aug" +
                "         ,sum(case when (ymd / 100) % 100 = 9" +
                "                  then 1" +
                "                  else 0" +
                "              end) as sep" +
                "         ,sum(case when (ymd / 100) % 100 = 10" +
                "                  then 1" +
                "                  else 0" +
                "              end) as oct" +
                "         ,sum(case when (ymd / 100) % 100 = 11" +
                "                  then 1" +
                "                  else 0" +
                "              end) as nov" +
                "         ,sum(case when (ymd / 100) % 100 = 12" +
                "                  then 1" +
                "                  else 0" +
                "              end) as dec" +
                    " from SpCount " +
                    " WHERE ymd / 10000 = " + pYear.toString() +
                    " group by year, name, contents"
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
                        year = cursor.getInt(0),
                        name = cursor.getString(1),
                        contents = cursor.getString(2),
                        jan = cursor.getInt(3),
                        feb = cursor.getInt(4),
                        mar = cursor.getInt(5),
                        apr = cursor.getInt(6),
                        may = cursor.getInt(7),
                        jun = cursor.getInt(8),
                        jul = cursor.getInt(9),
                        aug = cursor.getInt(10),
                        sep = cursor.getInt(11),
                        oct = cursor.getInt(12),
                        nov = cursor.getInt(13),
                        dec = cursor.getInt(14),
                    )
                    setList.add(retRecord)
                }
            }
            cursor.close()
        } catch(exception: Exception) {
            Log.e("selectAggData", exception.toString())
        }
        return setList.toTypedArray()
    }

    // データ追加
    internal fun insertData(contents: String, name: String, ymd: Int, remarks: String): Boolean
    {
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
    internal fun insertData(rec: MainActivity.Record): Boolean
    {
        return insertData(rec.contents, rec.name, rec.ymd, rec.remarks)
    }

    // データ更新
    internal fun updateData(whereId: Int, ymd: Int, contents: String, name: String, remarks: String ): Boolean
    {
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

    // データ削除
    internal fun deleteData(whereId: Int): Boolean
    {
        try {
            val database = this.writableDatabase
            val whereClauses = "_id = ?"
            val whereArgs = arrayOf(whereId.toString())
            database.delete("SpCount", whereClauses, whereArgs)
            database.close()
        }catch(exception: Exception) {
            Log.e("updateData", exception.toString())
            return false
        }

        return true
    }


}