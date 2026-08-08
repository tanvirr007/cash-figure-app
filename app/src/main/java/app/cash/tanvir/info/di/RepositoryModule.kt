package app.cash.tanvir.info.di

import app.cash.tanvir.info.data.repository.SettingsRepositoryImpl
import app.cash.tanvir.info.data.repository.SheetRepositoryImpl
import app.cash.tanvir.info.domain.repository.SettingsRepository
import app.cash.tanvir.info.domain.repository.SheetRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSheetRepository(
        impl: SheetRepositoryImpl
    ): SheetRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository
}
