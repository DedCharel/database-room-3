package ua.cn.stu.room.model.room

import android.database.sqlite.SQLiteDatabase
import androidx.core.content.contentValuesOf
import androidx.room.RenameColumn
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase
import ua.cn.stu.room.Repositories

@RenameColumn(tableName = "accounts", fromColumnName = "password", toColumnName = "hash")
class AutoMigrationSpec1To2: AutoMigrationSpec{
    val securityUtils = Repositories.securityUtils

    override fun onPostMigrate(db: SupportSQLiteDatabase) {
        super.onPostMigrate(db)
        db.query("SELECT * FROM accounts").use { cursor ->
            val passwordIndex = cursor.getColumnIndex("hash")
            val idIndex = cursor.getColumnIndex("id")
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val passwordChars = cursor.getString(passwordIndex).toCharArray()
                val salt = securityUtils.generateSalt()
                val hashBytes = securityUtils.passwordToHash(passwordChars, salt)
                db.update(
                    "accounts",
                    SQLiteDatabase.CONFLICT_NONE,
                    contentValuesOf(
                        "hash" to securityUtils.bytesToString(hashBytes),
                        "salt" to securityUtils.bytesToString(salt)
                    ),
                    "id = ?",
                    arrayOf(id.toString())
                )
            }
        }
    }
}

// todo #16: Create a migration object derived from Migration class. Specify 'from' (2) and 'to' (3)
//           versions you want to migrate. Override 'migrate' method and use 'ALTER TABLE' SQL-query
//           in order to add a new 'phone' column to the 'accounts' table.

// todo #18: Before running the project check the database of the installed app; then run the project,
//           check and compare the databases again.

// todo #19: Do not forget to update pre-packaged database in the assets. It's schema should be the same
//           as the latest schema of your current database (version 3, with 'hash' and 'salt' columns
//           instead of 'password' column and with 'phone' column).