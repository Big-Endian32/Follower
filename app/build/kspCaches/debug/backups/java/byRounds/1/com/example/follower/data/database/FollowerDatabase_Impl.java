package com.example.follower.data.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class FollowerDatabase_Impl extends FollowerDatabase {
  private volatile DeviceDao _deviceDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `detected_devices` (`macAddress` TEXT NOT NULL, `deviceType` TEXT NOT NULL, `deviceName` TEXT, `firstSeenTimestamp` INTEGER NOT NULL, `lastSeenTimestamp` INTEGER NOT NULL, `detectionCount` INTEGER NOT NULL, `locationCount` INTEGER NOT NULL, `threatScore` REAL NOT NULL, `lastRssi` INTEGER NOT NULL, `probedSsids` TEXT, `bluetoothClass` INTEGER, `isWhitelisted` INTEGER NOT NULL, `isFlagged` INTEGER NOT NULL, `metadata` TEXT, PRIMARY KEY(`macAddress`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_detected_devices_macAddress` ON `detected_devices` (`macAddress`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_detected_devices_deviceType` ON `detected_devices` (`deviceType`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_detected_devices_lastSeenTimestamp` ON `detected_devices` (`lastSeenTimestamp`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `device_sightings` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `deviceMacAddress` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `locationAccuracy` REAL NOT NULL, `rssi` INTEGER NOT NULL, `deviceType` TEXT NOT NULL, `probedSsid` TEXT, `apSsid` TEXT, `channel` INTEGER, `frequency` INTEGER, FOREIGN KEY(`deviceMacAddress`) REFERENCES `detected_devices`(`macAddress`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_device_sightings_deviceMacAddress` ON `device_sightings` (`deviceMacAddress`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_device_sightings_timestamp` ON `device_sightings` (`timestamp`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_device_sightings_latitude_longitude` ON `device_sightings` (`latitude`, `longitude`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `location_clusters` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `centerLatitude` REAL NOT NULL, `centerLongitude` REAL NOT NULL, `radiusMeters` REAL NOT NULL, `firstVisitTimestamp` INTEGER NOT NULL, `lastVisitTimestamp` INTEGER NOT NULL, `visitCount` INTEGER NOT NULL, `placeName` TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `threat_alerts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `deviceMacAddress` TEXT NOT NULL, `deviceName` TEXT, `deviceType` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `threatScore` REAL NOT NULL, `threatLevel` TEXT NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `sightingCount` INTEGER NOT NULL, `locationCount` INTEGER NOT NULL, `followDurationMs` INTEGER NOT NULL, `isAcknowledged` INTEGER NOT NULL, `userAction` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '2891b29e56d1a3437a564069b622bac8')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `detected_devices`");
        db.execSQL("DROP TABLE IF EXISTS `device_sightings`");
        db.execSQL("DROP TABLE IF EXISTS `location_clusters`");
        db.execSQL("DROP TABLE IF EXISTS `threat_alerts`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsDetectedDevices = new HashMap<String, TableInfo.Column>(14);
        _columnsDetectedDevices.put("macAddress", new TableInfo.Column("macAddress", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDetectedDevices.put("deviceType", new TableInfo.Column("deviceType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDetectedDevices.put("deviceName", new TableInfo.Column("deviceName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDetectedDevices.put("firstSeenTimestamp", new TableInfo.Column("firstSeenTimestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDetectedDevices.put("lastSeenTimestamp", new TableInfo.Column("lastSeenTimestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDetectedDevices.put("detectionCount", new TableInfo.Column("detectionCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDetectedDevices.put("locationCount", new TableInfo.Column("locationCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDetectedDevices.put("threatScore", new TableInfo.Column("threatScore", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDetectedDevices.put("lastRssi", new TableInfo.Column("lastRssi", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDetectedDevices.put("probedSsids", new TableInfo.Column("probedSsids", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDetectedDevices.put("bluetoothClass", new TableInfo.Column("bluetoothClass", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDetectedDevices.put("isWhitelisted", new TableInfo.Column("isWhitelisted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDetectedDevices.put("isFlagged", new TableInfo.Column("isFlagged", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDetectedDevices.put("metadata", new TableInfo.Column("metadata", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDetectedDevices = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDetectedDevices = new HashSet<TableInfo.Index>(3);
        _indicesDetectedDevices.add(new TableInfo.Index("index_detected_devices_macAddress", false, Arrays.asList("macAddress"), Arrays.asList("ASC")));
        _indicesDetectedDevices.add(new TableInfo.Index("index_detected_devices_deviceType", false, Arrays.asList("deviceType"), Arrays.asList("ASC")));
        _indicesDetectedDevices.add(new TableInfo.Index("index_detected_devices_lastSeenTimestamp", false, Arrays.asList("lastSeenTimestamp"), Arrays.asList("ASC")));
        final TableInfo _infoDetectedDevices = new TableInfo("detected_devices", _columnsDetectedDevices, _foreignKeysDetectedDevices, _indicesDetectedDevices);
        final TableInfo _existingDetectedDevices = TableInfo.read(db, "detected_devices");
        if (!_infoDetectedDevices.equals(_existingDetectedDevices)) {
          return new RoomOpenHelper.ValidationResult(false, "detected_devices(com.example.follower.data.model.DetectedDevice).\n"
                  + " Expected:\n" + _infoDetectedDevices + "\n"
                  + " Found:\n" + _existingDetectedDevices);
        }
        final HashMap<String, TableInfo.Column> _columnsDeviceSightings = new HashMap<String, TableInfo.Column>(12);
        _columnsDeviceSightings.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeviceSightings.put("deviceMacAddress", new TableInfo.Column("deviceMacAddress", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeviceSightings.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeviceSightings.put("latitude", new TableInfo.Column("latitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeviceSightings.put("longitude", new TableInfo.Column("longitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeviceSightings.put("locationAccuracy", new TableInfo.Column("locationAccuracy", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeviceSightings.put("rssi", new TableInfo.Column("rssi", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeviceSightings.put("deviceType", new TableInfo.Column("deviceType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeviceSightings.put("probedSsid", new TableInfo.Column("probedSsid", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeviceSightings.put("apSsid", new TableInfo.Column("apSsid", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeviceSightings.put("channel", new TableInfo.Column("channel", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeviceSightings.put("frequency", new TableInfo.Column("frequency", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDeviceSightings = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysDeviceSightings.add(new TableInfo.ForeignKey("detected_devices", "CASCADE", "NO ACTION", Arrays.asList("deviceMacAddress"), Arrays.asList("macAddress")));
        final HashSet<TableInfo.Index> _indicesDeviceSightings = new HashSet<TableInfo.Index>(3);
        _indicesDeviceSightings.add(new TableInfo.Index("index_device_sightings_deviceMacAddress", false, Arrays.asList("deviceMacAddress"), Arrays.asList("ASC")));
        _indicesDeviceSightings.add(new TableInfo.Index("index_device_sightings_timestamp", false, Arrays.asList("timestamp"), Arrays.asList("ASC")));
        _indicesDeviceSightings.add(new TableInfo.Index("index_device_sightings_latitude_longitude", false, Arrays.asList("latitude", "longitude"), Arrays.asList("ASC", "ASC")));
        final TableInfo _infoDeviceSightings = new TableInfo("device_sightings", _columnsDeviceSightings, _foreignKeysDeviceSightings, _indicesDeviceSightings);
        final TableInfo _existingDeviceSightings = TableInfo.read(db, "device_sightings");
        if (!_infoDeviceSightings.equals(_existingDeviceSightings)) {
          return new RoomOpenHelper.ValidationResult(false, "device_sightings(com.example.follower.data.model.DeviceSighting).\n"
                  + " Expected:\n" + _infoDeviceSightings + "\n"
                  + " Found:\n" + _existingDeviceSightings);
        }
        final HashMap<String, TableInfo.Column> _columnsLocationClusters = new HashMap<String, TableInfo.Column>(8);
        _columnsLocationClusters.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLocationClusters.put("centerLatitude", new TableInfo.Column("centerLatitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLocationClusters.put("centerLongitude", new TableInfo.Column("centerLongitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLocationClusters.put("radiusMeters", new TableInfo.Column("radiusMeters", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLocationClusters.put("firstVisitTimestamp", new TableInfo.Column("firstVisitTimestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLocationClusters.put("lastVisitTimestamp", new TableInfo.Column("lastVisitTimestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLocationClusters.put("visitCount", new TableInfo.Column("visitCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLocationClusters.put("placeName", new TableInfo.Column("placeName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLocationClusters = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesLocationClusters = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoLocationClusters = new TableInfo("location_clusters", _columnsLocationClusters, _foreignKeysLocationClusters, _indicesLocationClusters);
        final TableInfo _existingLocationClusters = TableInfo.read(db, "location_clusters");
        if (!_infoLocationClusters.equals(_existingLocationClusters)) {
          return new RoomOpenHelper.ValidationResult(false, "location_clusters(com.example.follower.data.model.LocationCluster).\n"
                  + " Expected:\n" + _infoLocationClusters + "\n"
                  + " Found:\n" + _existingLocationClusters);
        }
        final HashMap<String, TableInfo.Column> _columnsThreatAlerts = new HashMap<String, TableInfo.Column>(14);
        _columnsThreatAlerts.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsThreatAlerts.put("deviceMacAddress", new TableInfo.Column("deviceMacAddress", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsThreatAlerts.put("deviceName", new TableInfo.Column("deviceName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsThreatAlerts.put("deviceType", new TableInfo.Column("deviceType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsThreatAlerts.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsThreatAlerts.put("threatScore", new TableInfo.Column("threatScore", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsThreatAlerts.put("threatLevel", new TableInfo.Column("threatLevel", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsThreatAlerts.put("latitude", new TableInfo.Column("latitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsThreatAlerts.put("longitude", new TableInfo.Column("longitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsThreatAlerts.put("sightingCount", new TableInfo.Column("sightingCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsThreatAlerts.put("locationCount", new TableInfo.Column("locationCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsThreatAlerts.put("followDurationMs", new TableInfo.Column("followDurationMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsThreatAlerts.put("isAcknowledged", new TableInfo.Column("isAcknowledged", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsThreatAlerts.put("userAction", new TableInfo.Column("userAction", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysThreatAlerts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesThreatAlerts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoThreatAlerts = new TableInfo("threat_alerts", _columnsThreatAlerts, _foreignKeysThreatAlerts, _indicesThreatAlerts);
        final TableInfo _existingThreatAlerts = TableInfo.read(db, "threat_alerts");
        if (!_infoThreatAlerts.equals(_existingThreatAlerts)) {
          return new RoomOpenHelper.ValidationResult(false, "threat_alerts(com.example.follower.data.model.ThreatAlert).\n"
                  + " Expected:\n" + _infoThreatAlerts + "\n"
                  + " Found:\n" + _existingThreatAlerts);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "2891b29e56d1a3437a564069b622bac8", "30ffc04a9f4ab82952b38cce3bee3a9e");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "detected_devices","device_sightings","location_clusters","threat_alerts");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `detected_devices`");
      _db.execSQL("DELETE FROM `device_sightings`");
      _db.execSQL("DELETE FROM `location_clusters`");
      _db.execSQL("DELETE FROM `threat_alerts`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(DeviceDao.class, DeviceDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public DeviceDao deviceDao() {
    if (_deviceDao != null) {
      return _deviceDao;
    } else {
      synchronized(this) {
        if(_deviceDao == null) {
          _deviceDao = new DeviceDao_Impl(this);
        }
        return _deviceDao;
      }
    }
  }
}
