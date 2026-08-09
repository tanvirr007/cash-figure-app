package app.cash.tanvir.info.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import app.cash.tanvir.info.data.local.db.dao.SheetDao
import app.cash.tanvir.info.data.local.db.entity.SheetEntity

/**
 * Main Room Database definition for Cash Figure app.
 */
@Database(
    entities = [SheetEntity::class],
    version = 2,
    exportSchema = false
)
abstract class CashFigureDatabase : RoomDatabase() {
    abstract fun sheetDao(): SheetDao

    companion object {
        const val DATABASE_NAME = "cash_figure.db"
    }
}
