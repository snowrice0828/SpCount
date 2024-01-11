package k.yuki.spcount

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

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

    companion object {
        // データーベースのバージョン
        private const val DATABASE_VERSION = 2

        // データーベース名
        private const val DATABASE_NAME = "SpCountDB.db"
        public const val TABLE_NAME = "SpCount"
        private const val SQL_CREATE_ENTRIES = "CREATE TABLE " + TABLE_NAME + " (" +
                " _id INTEGER PRIMARY KEY, " +
                "year INTEGER, " +
                "name TEXT, " +
                "contents TEXT, " +
                "jan INTEGER, " +
                "feb INTEGER, " +
                "mar INTEGER, " +
                "apr INTEGER, " +
                "may INTEGER, " +
                "jun INTEGER, " +
                "jul INTEGER, " +
                "aug INTEGER, " +
                "sep INTEGER, " +
                "oct INTEGER, " +
                "nov INTEGER, " +
                "dec INTEGER  " +
                " )"

        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME
    }
}