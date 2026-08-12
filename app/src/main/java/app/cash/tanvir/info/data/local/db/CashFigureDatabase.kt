package app.cash.tanvir.info.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.tanvir.info.data.local.db.dao.DraftDao
import app.cash.tanvir.info.data.local.db.dao.SheetDao
import app.cash.tanvir.info.data.local.db.entity.DraftEntity
import app.cash.tanvir.info.data.local.db.entity.SheetEntity

/**
 * Main Room Database definition for Cash Figure app.
 */
@Database(
    entities = [SheetEntity::class, DraftEntity::class],
    version = 3,
    exportSchema = false
)
abstract class CashFigureDatabase : RoomDatabase() {
    abstract fun sheetDao(): SheetDao
    abstract fun draftDao(): DraftDao

    companion object {
        const val DATABASE_NAME = "cash_figure.db"

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `drafts` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`grandTotal` INTEGER NOT NULL, " +
                        "`totalPieces` INTEGER NOT NULL, " +
                        "`activeDenominations` INTEGER NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "`updatedAt` INTEGER NOT NULL, " +
                        "`quantitiesJson` TEXT NOT NULL)"
                )
            }
        }
    }
}
