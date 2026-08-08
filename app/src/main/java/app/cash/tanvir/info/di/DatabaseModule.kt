package app.cash.tanvir.info.di

import android.content.Context
import androidx.room.Room
import app.cash.tanvir.info.data.local.db.CashFigureDatabase
import app.cash.tanvir.info.data.local.db.dao.SheetDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CashFigureDatabase {
        return Room.databaseBuilder(
            context,
            CashFigureDatabase::class.java,
            CashFigureDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideSheetDao(database: CashFigureDatabase): SheetDao {
        return database.sheetDao()
    }
}
