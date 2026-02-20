package com.example.follower.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.follower.data.model.*

@Database(
    entities = [
        DetectedDevice::class,
        DeviceSighting::class,
        LocationCluster::class,
        ThreatAlert::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class FollowerDatabase : RoomDatabase() {

    abstract fun deviceDao(): DeviceDao

    companion object {
        @Volatile
        private var INSTANCE: FollowerDatabase? = null

        fun getDatabase(context: Context): FollowerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FollowerDatabase::class.java,
                    "follower_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @androidx.room.TypeConverter
    fun fromDeviceType(value: DeviceType): String = value.name

    @androidx.room.TypeConverter
    fun toDeviceType(value: String): DeviceType = DeviceType.valueOf(value)

    @androidx.room.TypeConverter
    fun fromThreatLevel(value: ThreatLevel): String = value.name

    @androidx.room.TypeConverter
    fun toThreatLevel(value: String): ThreatLevel = ThreatLevel.valueOf(value)

    @androidx.room.TypeConverter
    fun fromAlertAction(value: AlertAction): String = value.name

    @androidx.room.TypeConverter
    fun toAlertAction(value: String): AlertAction = AlertAction.valueOf(value)
}
