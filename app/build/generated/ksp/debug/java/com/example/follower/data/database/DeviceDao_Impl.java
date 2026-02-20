package com.example.follower.data.database;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.follower.data.model.AlertAction;
import com.example.follower.data.model.DetectedDevice;
import com.example.follower.data.model.DeviceSighting;
import com.example.follower.data.model.DeviceType;
import com.example.follower.data.model.LocationCluster;
import com.example.follower.data.model.ThreatAlert;
import com.example.follower.data.model.ThreatLevel;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class DeviceDao_Impl implements DeviceDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DetectedDevice> __insertionAdapterOfDetectedDevice;

  private final Converters __converters = new Converters();

  private final EntityInsertionAdapter<DeviceSighting> __insertionAdapterOfDeviceSighting;

  private final EntityInsertionAdapter<LocationCluster> __insertionAdapterOfLocationCluster;

  private final EntityInsertionAdapter<ThreatAlert> __insertionAdapterOfThreatAlert;

  private final EntityDeletionOrUpdateAdapter<DetectedDevice> __deletionAdapterOfDetectedDevice;

  private final EntityDeletionOrUpdateAdapter<DetectedDevice> __updateAdapterOfDetectedDevice;

  private final EntityDeletionOrUpdateAdapter<ThreatAlert> __updateAdapterOfThreatAlert;

  private final SharedSQLiteStatement __preparedStmtOfSetWhitelisted;

  private final SharedSQLiteStatement __preparedStmtOfSetFlagged;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOldDevices;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOldSightings;

  private final SharedSQLiteStatement __preparedStmtOfAcknowledgeAlert;

  private final SharedSQLiteStatement __preparedStmtOfSetAlertAction;

  public DeviceDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDetectedDevice = new EntityInsertionAdapter<DetectedDevice>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `detected_devices` (`macAddress`,`deviceType`,`deviceName`,`firstSeenTimestamp`,`lastSeenTimestamp`,`detectionCount`,`locationCount`,`threatScore`,`lastRssi`,`probedSsids`,`bluetoothClass`,`isWhitelisted`,`isFlagged`,`metadata`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DetectedDevice entity) {
        statement.bindString(1, entity.getMacAddress());
        final String _tmp = __converters.fromDeviceType(entity.getDeviceType());
        statement.bindString(2, _tmp);
        if (entity.getDeviceName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getDeviceName());
        }
        statement.bindLong(4, entity.getFirstSeenTimestamp());
        statement.bindLong(5, entity.getLastSeenTimestamp());
        statement.bindLong(6, entity.getDetectionCount());
        statement.bindLong(7, entity.getLocationCount());
        statement.bindDouble(8, entity.getThreatScore());
        statement.bindLong(9, entity.getLastRssi());
        if (entity.getProbedSsids() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getProbedSsids());
        }
        if (entity.getBluetoothClass() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getBluetoothClass());
        }
        final int _tmp_1 = entity.isWhitelisted() ? 1 : 0;
        statement.bindLong(12, _tmp_1);
        final int _tmp_2 = entity.isFlagged() ? 1 : 0;
        statement.bindLong(13, _tmp_2);
        if (entity.getMetadata() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getMetadata());
        }
      }
    };
    this.__insertionAdapterOfDeviceSighting = new EntityInsertionAdapter<DeviceSighting>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `device_sightings` (`id`,`deviceMacAddress`,`timestamp`,`latitude`,`longitude`,`locationAccuracy`,`rssi`,`deviceType`,`probedSsid`,`apSsid`,`channel`,`frequency`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DeviceSighting entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getDeviceMacAddress());
        statement.bindLong(3, entity.getTimestamp());
        statement.bindDouble(4, entity.getLatitude());
        statement.bindDouble(5, entity.getLongitude());
        statement.bindDouble(6, entity.getLocationAccuracy());
        statement.bindLong(7, entity.getRssi());
        final String _tmp = __converters.fromDeviceType(entity.getDeviceType());
        statement.bindString(8, _tmp);
        if (entity.getProbedSsid() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getProbedSsid());
        }
        if (entity.getApSsid() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getApSsid());
        }
        if (entity.getChannel() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getChannel());
        }
        if (entity.getFrequency() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getFrequency());
        }
      }
    };
    this.__insertionAdapterOfLocationCluster = new EntityInsertionAdapter<LocationCluster>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `location_clusters` (`id`,`centerLatitude`,`centerLongitude`,`radiusMeters`,`firstVisitTimestamp`,`lastVisitTimestamp`,`visitCount`,`placeName`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LocationCluster entity) {
        statement.bindLong(1, entity.getId());
        statement.bindDouble(2, entity.getCenterLatitude());
        statement.bindDouble(3, entity.getCenterLongitude());
        statement.bindDouble(4, entity.getRadiusMeters());
        statement.bindLong(5, entity.getFirstVisitTimestamp());
        statement.bindLong(6, entity.getLastVisitTimestamp());
        statement.bindLong(7, entity.getVisitCount());
        if (entity.getPlaceName() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getPlaceName());
        }
      }
    };
    this.__insertionAdapterOfThreatAlert = new EntityInsertionAdapter<ThreatAlert>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `threat_alerts` (`id`,`deviceMacAddress`,`deviceName`,`deviceType`,`timestamp`,`threatScore`,`threatLevel`,`latitude`,`longitude`,`sightingCount`,`locationCount`,`followDurationMs`,`isAcknowledged`,`userAction`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ThreatAlert entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getDeviceMacAddress());
        if (entity.getDeviceName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getDeviceName());
        }
        final String _tmp = __converters.fromDeviceType(entity.getDeviceType());
        statement.bindString(4, _tmp);
        statement.bindLong(5, entity.getTimestamp());
        statement.bindDouble(6, entity.getThreatScore());
        final String _tmp_1 = __converters.fromThreatLevel(entity.getThreatLevel());
        statement.bindString(7, _tmp_1);
        statement.bindDouble(8, entity.getLatitude());
        statement.bindDouble(9, entity.getLongitude());
        statement.bindLong(10, entity.getSightingCount());
        statement.bindLong(11, entity.getLocationCount());
        statement.bindLong(12, entity.getFollowDurationMs());
        final int _tmp_2 = entity.isAcknowledged() ? 1 : 0;
        statement.bindLong(13, _tmp_2);
        final String _tmp_3 = __converters.fromAlertAction(entity.getUserAction());
        statement.bindString(14, _tmp_3);
      }
    };
    this.__deletionAdapterOfDetectedDevice = new EntityDeletionOrUpdateAdapter<DetectedDevice>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `detected_devices` WHERE `macAddress` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DetectedDevice entity) {
        statement.bindString(1, entity.getMacAddress());
      }
    };
    this.__updateAdapterOfDetectedDevice = new EntityDeletionOrUpdateAdapter<DetectedDevice>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `detected_devices` SET `macAddress` = ?,`deviceType` = ?,`deviceName` = ?,`firstSeenTimestamp` = ?,`lastSeenTimestamp` = ?,`detectionCount` = ?,`locationCount` = ?,`threatScore` = ?,`lastRssi` = ?,`probedSsids` = ?,`bluetoothClass` = ?,`isWhitelisted` = ?,`isFlagged` = ?,`metadata` = ? WHERE `macAddress` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DetectedDevice entity) {
        statement.bindString(1, entity.getMacAddress());
        final String _tmp = __converters.fromDeviceType(entity.getDeviceType());
        statement.bindString(2, _tmp);
        if (entity.getDeviceName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getDeviceName());
        }
        statement.bindLong(4, entity.getFirstSeenTimestamp());
        statement.bindLong(5, entity.getLastSeenTimestamp());
        statement.bindLong(6, entity.getDetectionCount());
        statement.bindLong(7, entity.getLocationCount());
        statement.bindDouble(8, entity.getThreatScore());
        statement.bindLong(9, entity.getLastRssi());
        if (entity.getProbedSsids() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getProbedSsids());
        }
        if (entity.getBluetoothClass() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getBluetoothClass());
        }
        final int _tmp_1 = entity.isWhitelisted() ? 1 : 0;
        statement.bindLong(12, _tmp_1);
        final int _tmp_2 = entity.isFlagged() ? 1 : 0;
        statement.bindLong(13, _tmp_2);
        if (entity.getMetadata() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getMetadata());
        }
        statement.bindString(15, entity.getMacAddress());
      }
    };
    this.__updateAdapterOfThreatAlert = new EntityDeletionOrUpdateAdapter<ThreatAlert>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `threat_alerts` SET `id` = ?,`deviceMacAddress` = ?,`deviceName` = ?,`deviceType` = ?,`timestamp` = ?,`threatScore` = ?,`threatLevel` = ?,`latitude` = ?,`longitude` = ?,`sightingCount` = ?,`locationCount` = ?,`followDurationMs` = ?,`isAcknowledged` = ?,`userAction` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ThreatAlert entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getDeviceMacAddress());
        if (entity.getDeviceName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getDeviceName());
        }
        final String _tmp = __converters.fromDeviceType(entity.getDeviceType());
        statement.bindString(4, _tmp);
        statement.bindLong(5, entity.getTimestamp());
        statement.bindDouble(6, entity.getThreatScore());
        final String _tmp_1 = __converters.fromThreatLevel(entity.getThreatLevel());
        statement.bindString(7, _tmp_1);
        statement.bindDouble(8, entity.getLatitude());
        statement.bindDouble(9, entity.getLongitude());
        statement.bindLong(10, entity.getSightingCount());
        statement.bindLong(11, entity.getLocationCount());
        statement.bindLong(12, entity.getFollowDurationMs());
        final int _tmp_2 = entity.isAcknowledged() ? 1 : 0;
        statement.bindLong(13, _tmp_2);
        final String _tmp_3 = __converters.fromAlertAction(entity.getUserAction());
        statement.bindString(14, _tmp_3);
        statement.bindLong(15, entity.getId());
      }
    };
    this.__preparedStmtOfSetWhitelisted = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE detected_devices SET isWhitelisted = ? WHERE macAddress = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSetFlagged = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE detected_devices SET isFlagged = ? WHERE macAddress = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteOldDevices = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM detected_devices WHERE lastSeenTimestamp < ? AND isWhitelisted = 0 AND isFlagged = 0";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteOldSightings = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM device_sightings WHERE timestamp < ?";
        return _query;
      }
    };
    this.__preparedStmtOfAcknowledgeAlert = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE threat_alerts SET isAcknowledged = 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSetAlertAction = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE threat_alerts SET userAction = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertDevice(final DetectedDevice device,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDetectedDevice.insert(device);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertSighting(final DeviceSighting sighting,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfDeviceSighting.insertAndReturnId(sighting);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertCluster(final LocationCluster cluster,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfLocationCluster.insertAndReturnId(cluster);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAlert(final ThreatAlert alert, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfThreatAlert.insertAndReturnId(alert);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteDevice(final DetectedDevice device,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfDetectedDevice.handle(device);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateDevice(final DetectedDevice device,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfDetectedDevice.handle(device);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateAlert(final ThreatAlert alert, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfThreatAlert.handle(alert);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object setWhitelisted(final String macAddress, final boolean whitelisted,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetWhitelisted.acquire();
        int _argIndex = 1;
        final int _tmp = whitelisted ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindString(_argIndex, macAddress);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfSetWhitelisted.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object setFlagged(final String macAddress, final boolean flagged,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetFlagged.acquire();
        int _argIndex = 1;
        final int _tmp = flagged ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindString(_argIndex, macAddress);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfSetFlagged.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteOldDevices(final long before,
      final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOldDevices.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, before);
        try {
          __db.beginTransaction();
          try {
            final Integer _result = _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteOldDevices.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteOldSightings(final long before,
      final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOldSightings.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, before);
        try {
          __db.beginTransaction();
          try {
            final Integer _result = _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteOldSightings.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object acknowledgeAlert(final long alertId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfAcknowledgeAlert.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, alertId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfAcknowledgeAlert.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object setAlertAction(final long alertId, final AlertAction action,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetAlertAction.acquire();
        int _argIndex = 1;
        final String _tmp = __converters.fromAlertAction(action);
        _stmt.bindString(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, alertId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfSetAlertAction.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getDeviceByMac(final String macAddress,
      final Continuation<? super DetectedDevice> $completion) {
    final String _sql = "SELECT * FROM detected_devices WHERE macAddress = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, macAddress);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<DetectedDevice>() {
      @Override
      @Nullable
      public DetectedDevice call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMacAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "macAddress");
          final int _cursorIndexOfDeviceType = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceType");
          final int _cursorIndexOfDeviceName = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceName");
          final int _cursorIndexOfFirstSeenTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "firstSeenTimestamp");
          final int _cursorIndexOfLastSeenTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSeenTimestamp");
          final int _cursorIndexOfDetectionCount = CursorUtil.getColumnIndexOrThrow(_cursor, "detectionCount");
          final int _cursorIndexOfLocationCount = CursorUtil.getColumnIndexOrThrow(_cursor, "locationCount");
          final int _cursorIndexOfThreatScore = CursorUtil.getColumnIndexOrThrow(_cursor, "threatScore");
          final int _cursorIndexOfLastRssi = CursorUtil.getColumnIndexOrThrow(_cursor, "lastRssi");
          final int _cursorIndexOfProbedSsids = CursorUtil.getColumnIndexOrThrow(_cursor, "probedSsids");
          final int _cursorIndexOfBluetoothClass = CursorUtil.getColumnIndexOrThrow(_cursor, "bluetoothClass");
          final int _cursorIndexOfIsWhitelisted = CursorUtil.getColumnIndexOrThrow(_cursor, "isWhitelisted");
          final int _cursorIndexOfIsFlagged = CursorUtil.getColumnIndexOrThrow(_cursor, "isFlagged");
          final int _cursorIndexOfMetadata = CursorUtil.getColumnIndexOrThrow(_cursor, "metadata");
          final DetectedDevice _result;
          if (_cursor.moveToFirst()) {
            final String _tmpMacAddress;
            _tmpMacAddress = _cursor.getString(_cursorIndexOfMacAddress);
            final DeviceType _tmpDeviceType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfDeviceType);
            _tmpDeviceType = __converters.toDeviceType(_tmp);
            final String _tmpDeviceName;
            if (_cursor.isNull(_cursorIndexOfDeviceName)) {
              _tmpDeviceName = null;
            } else {
              _tmpDeviceName = _cursor.getString(_cursorIndexOfDeviceName);
            }
            final long _tmpFirstSeenTimestamp;
            _tmpFirstSeenTimestamp = _cursor.getLong(_cursorIndexOfFirstSeenTimestamp);
            final long _tmpLastSeenTimestamp;
            _tmpLastSeenTimestamp = _cursor.getLong(_cursorIndexOfLastSeenTimestamp);
            final int _tmpDetectionCount;
            _tmpDetectionCount = _cursor.getInt(_cursorIndexOfDetectionCount);
            final int _tmpLocationCount;
            _tmpLocationCount = _cursor.getInt(_cursorIndexOfLocationCount);
            final float _tmpThreatScore;
            _tmpThreatScore = _cursor.getFloat(_cursorIndexOfThreatScore);
            final int _tmpLastRssi;
            _tmpLastRssi = _cursor.getInt(_cursorIndexOfLastRssi);
            final String _tmpProbedSsids;
            if (_cursor.isNull(_cursorIndexOfProbedSsids)) {
              _tmpProbedSsids = null;
            } else {
              _tmpProbedSsids = _cursor.getString(_cursorIndexOfProbedSsids);
            }
            final Integer _tmpBluetoothClass;
            if (_cursor.isNull(_cursorIndexOfBluetoothClass)) {
              _tmpBluetoothClass = null;
            } else {
              _tmpBluetoothClass = _cursor.getInt(_cursorIndexOfBluetoothClass);
            }
            final boolean _tmpIsWhitelisted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsWhitelisted);
            _tmpIsWhitelisted = _tmp_1 != 0;
            final boolean _tmpIsFlagged;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsFlagged);
            _tmpIsFlagged = _tmp_2 != 0;
            final String _tmpMetadata;
            if (_cursor.isNull(_cursorIndexOfMetadata)) {
              _tmpMetadata = null;
            } else {
              _tmpMetadata = _cursor.getString(_cursorIndexOfMetadata);
            }
            _result = new DetectedDevice(_tmpMacAddress,_tmpDeviceType,_tmpDeviceName,_tmpFirstSeenTimestamp,_tmpLastSeenTimestamp,_tmpDetectionCount,_tmpLocationCount,_tmpThreatScore,_tmpLastRssi,_tmpProbedSsids,_tmpBluetoothClass,_tmpIsWhitelisted,_tmpIsFlagged,_tmpMetadata);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<DetectedDevice>> getAllDevicesFlow() {
    final String _sql = "SELECT * FROM detected_devices ORDER BY lastSeenTimestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"detected_devices"}, new Callable<List<DetectedDevice>>() {
      @Override
      @NonNull
      public List<DetectedDevice> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMacAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "macAddress");
          final int _cursorIndexOfDeviceType = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceType");
          final int _cursorIndexOfDeviceName = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceName");
          final int _cursorIndexOfFirstSeenTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "firstSeenTimestamp");
          final int _cursorIndexOfLastSeenTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSeenTimestamp");
          final int _cursorIndexOfDetectionCount = CursorUtil.getColumnIndexOrThrow(_cursor, "detectionCount");
          final int _cursorIndexOfLocationCount = CursorUtil.getColumnIndexOrThrow(_cursor, "locationCount");
          final int _cursorIndexOfThreatScore = CursorUtil.getColumnIndexOrThrow(_cursor, "threatScore");
          final int _cursorIndexOfLastRssi = CursorUtil.getColumnIndexOrThrow(_cursor, "lastRssi");
          final int _cursorIndexOfProbedSsids = CursorUtil.getColumnIndexOrThrow(_cursor, "probedSsids");
          final int _cursorIndexOfBluetoothClass = CursorUtil.getColumnIndexOrThrow(_cursor, "bluetoothClass");
          final int _cursorIndexOfIsWhitelisted = CursorUtil.getColumnIndexOrThrow(_cursor, "isWhitelisted");
          final int _cursorIndexOfIsFlagged = CursorUtil.getColumnIndexOrThrow(_cursor, "isFlagged");
          final int _cursorIndexOfMetadata = CursorUtil.getColumnIndexOrThrow(_cursor, "metadata");
          final List<DetectedDevice> _result = new ArrayList<DetectedDevice>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DetectedDevice _item;
            final String _tmpMacAddress;
            _tmpMacAddress = _cursor.getString(_cursorIndexOfMacAddress);
            final DeviceType _tmpDeviceType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfDeviceType);
            _tmpDeviceType = __converters.toDeviceType(_tmp);
            final String _tmpDeviceName;
            if (_cursor.isNull(_cursorIndexOfDeviceName)) {
              _tmpDeviceName = null;
            } else {
              _tmpDeviceName = _cursor.getString(_cursorIndexOfDeviceName);
            }
            final long _tmpFirstSeenTimestamp;
            _tmpFirstSeenTimestamp = _cursor.getLong(_cursorIndexOfFirstSeenTimestamp);
            final long _tmpLastSeenTimestamp;
            _tmpLastSeenTimestamp = _cursor.getLong(_cursorIndexOfLastSeenTimestamp);
            final int _tmpDetectionCount;
            _tmpDetectionCount = _cursor.getInt(_cursorIndexOfDetectionCount);
            final int _tmpLocationCount;
            _tmpLocationCount = _cursor.getInt(_cursorIndexOfLocationCount);
            final float _tmpThreatScore;
            _tmpThreatScore = _cursor.getFloat(_cursorIndexOfThreatScore);
            final int _tmpLastRssi;
            _tmpLastRssi = _cursor.getInt(_cursorIndexOfLastRssi);
            final String _tmpProbedSsids;
            if (_cursor.isNull(_cursorIndexOfProbedSsids)) {
              _tmpProbedSsids = null;
            } else {
              _tmpProbedSsids = _cursor.getString(_cursorIndexOfProbedSsids);
            }
            final Integer _tmpBluetoothClass;
            if (_cursor.isNull(_cursorIndexOfBluetoothClass)) {
              _tmpBluetoothClass = null;
            } else {
              _tmpBluetoothClass = _cursor.getInt(_cursorIndexOfBluetoothClass);
            }
            final boolean _tmpIsWhitelisted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsWhitelisted);
            _tmpIsWhitelisted = _tmp_1 != 0;
            final boolean _tmpIsFlagged;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsFlagged);
            _tmpIsFlagged = _tmp_2 != 0;
            final String _tmpMetadata;
            if (_cursor.isNull(_cursorIndexOfMetadata)) {
              _tmpMetadata = null;
            } else {
              _tmpMetadata = _cursor.getString(_cursorIndexOfMetadata);
            }
            _item = new DetectedDevice(_tmpMacAddress,_tmpDeviceType,_tmpDeviceName,_tmpFirstSeenTimestamp,_tmpLastSeenTimestamp,_tmpDetectionCount,_tmpLocationCount,_tmpThreatScore,_tmpLastRssi,_tmpProbedSsids,_tmpBluetoothClass,_tmpIsWhitelisted,_tmpIsFlagged,_tmpMetadata);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getRecentDevices(final int limit,
      final Continuation<? super List<DetectedDevice>> $completion) {
    final String _sql = "SELECT * FROM detected_devices ORDER BY lastSeenTimestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DetectedDevice>>() {
      @Override
      @NonNull
      public List<DetectedDevice> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMacAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "macAddress");
          final int _cursorIndexOfDeviceType = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceType");
          final int _cursorIndexOfDeviceName = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceName");
          final int _cursorIndexOfFirstSeenTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "firstSeenTimestamp");
          final int _cursorIndexOfLastSeenTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSeenTimestamp");
          final int _cursorIndexOfDetectionCount = CursorUtil.getColumnIndexOrThrow(_cursor, "detectionCount");
          final int _cursorIndexOfLocationCount = CursorUtil.getColumnIndexOrThrow(_cursor, "locationCount");
          final int _cursorIndexOfThreatScore = CursorUtil.getColumnIndexOrThrow(_cursor, "threatScore");
          final int _cursorIndexOfLastRssi = CursorUtil.getColumnIndexOrThrow(_cursor, "lastRssi");
          final int _cursorIndexOfProbedSsids = CursorUtil.getColumnIndexOrThrow(_cursor, "probedSsids");
          final int _cursorIndexOfBluetoothClass = CursorUtil.getColumnIndexOrThrow(_cursor, "bluetoothClass");
          final int _cursorIndexOfIsWhitelisted = CursorUtil.getColumnIndexOrThrow(_cursor, "isWhitelisted");
          final int _cursorIndexOfIsFlagged = CursorUtil.getColumnIndexOrThrow(_cursor, "isFlagged");
          final int _cursorIndexOfMetadata = CursorUtil.getColumnIndexOrThrow(_cursor, "metadata");
          final List<DetectedDevice> _result = new ArrayList<DetectedDevice>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DetectedDevice _item;
            final String _tmpMacAddress;
            _tmpMacAddress = _cursor.getString(_cursorIndexOfMacAddress);
            final DeviceType _tmpDeviceType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfDeviceType);
            _tmpDeviceType = __converters.toDeviceType(_tmp);
            final String _tmpDeviceName;
            if (_cursor.isNull(_cursorIndexOfDeviceName)) {
              _tmpDeviceName = null;
            } else {
              _tmpDeviceName = _cursor.getString(_cursorIndexOfDeviceName);
            }
            final long _tmpFirstSeenTimestamp;
            _tmpFirstSeenTimestamp = _cursor.getLong(_cursorIndexOfFirstSeenTimestamp);
            final long _tmpLastSeenTimestamp;
            _tmpLastSeenTimestamp = _cursor.getLong(_cursorIndexOfLastSeenTimestamp);
            final int _tmpDetectionCount;
            _tmpDetectionCount = _cursor.getInt(_cursorIndexOfDetectionCount);
            final int _tmpLocationCount;
            _tmpLocationCount = _cursor.getInt(_cursorIndexOfLocationCount);
            final float _tmpThreatScore;
            _tmpThreatScore = _cursor.getFloat(_cursorIndexOfThreatScore);
            final int _tmpLastRssi;
            _tmpLastRssi = _cursor.getInt(_cursorIndexOfLastRssi);
            final String _tmpProbedSsids;
            if (_cursor.isNull(_cursorIndexOfProbedSsids)) {
              _tmpProbedSsids = null;
            } else {
              _tmpProbedSsids = _cursor.getString(_cursorIndexOfProbedSsids);
            }
            final Integer _tmpBluetoothClass;
            if (_cursor.isNull(_cursorIndexOfBluetoothClass)) {
              _tmpBluetoothClass = null;
            } else {
              _tmpBluetoothClass = _cursor.getInt(_cursorIndexOfBluetoothClass);
            }
            final boolean _tmpIsWhitelisted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsWhitelisted);
            _tmpIsWhitelisted = _tmp_1 != 0;
            final boolean _tmpIsFlagged;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsFlagged);
            _tmpIsFlagged = _tmp_2 != 0;
            final String _tmpMetadata;
            if (_cursor.isNull(_cursorIndexOfMetadata)) {
              _tmpMetadata = null;
            } else {
              _tmpMetadata = _cursor.getString(_cursorIndexOfMetadata);
            }
            _item = new DetectedDevice(_tmpMacAddress,_tmpDeviceType,_tmpDeviceName,_tmpFirstSeenTimestamp,_tmpLastSeenTimestamp,_tmpDetectionCount,_tmpLocationCount,_tmpThreatScore,_tmpLastRssi,_tmpProbedSsids,_tmpBluetoothClass,_tmpIsWhitelisted,_tmpIsFlagged,_tmpMetadata);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<DetectedDevice>> getSuspiciousDevicesFlow(final float minScore) {
    final String _sql = "SELECT * FROM detected_devices WHERE threatScore >= ? ORDER BY threatScore DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindDouble(_argIndex, minScore);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"detected_devices"}, new Callable<List<DetectedDevice>>() {
      @Override
      @NonNull
      public List<DetectedDevice> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMacAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "macAddress");
          final int _cursorIndexOfDeviceType = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceType");
          final int _cursorIndexOfDeviceName = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceName");
          final int _cursorIndexOfFirstSeenTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "firstSeenTimestamp");
          final int _cursorIndexOfLastSeenTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSeenTimestamp");
          final int _cursorIndexOfDetectionCount = CursorUtil.getColumnIndexOrThrow(_cursor, "detectionCount");
          final int _cursorIndexOfLocationCount = CursorUtil.getColumnIndexOrThrow(_cursor, "locationCount");
          final int _cursorIndexOfThreatScore = CursorUtil.getColumnIndexOrThrow(_cursor, "threatScore");
          final int _cursorIndexOfLastRssi = CursorUtil.getColumnIndexOrThrow(_cursor, "lastRssi");
          final int _cursorIndexOfProbedSsids = CursorUtil.getColumnIndexOrThrow(_cursor, "probedSsids");
          final int _cursorIndexOfBluetoothClass = CursorUtil.getColumnIndexOrThrow(_cursor, "bluetoothClass");
          final int _cursorIndexOfIsWhitelisted = CursorUtil.getColumnIndexOrThrow(_cursor, "isWhitelisted");
          final int _cursorIndexOfIsFlagged = CursorUtil.getColumnIndexOrThrow(_cursor, "isFlagged");
          final int _cursorIndexOfMetadata = CursorUtil.getColumnIndexOrThrow(_cursor, "metadata");
          final List<DetectedDevice> _result = new ArrayList<DetectedDevice>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DetectedDevice _item;
            final String _tmpMacAddress;
            _tmpMacAddress = _cursor.getString(_cursorIndexOfMacAddress);
            final DeviceType _tmpDeviceType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfDeviceType);
            _tmpDeviceType = __converters.toDeviceType(_tmp);
            final String _tmpDeviceName;
            if (_cursor.isNull(_cursorIndexOfDeviceName)) {
              _tmpDeviceName = null;
            } else {
              _tmpDeviceName = _cursor.getString(_cursorIndexOfDeviceName);
            }
            final long _tmpFirstSeenTimestamp;
            _tmpFirstSeenTimestamp = _cursor.getLong(_cursorIndexOfFirstSeenTimestamp);
            final long _tmpLastSeenTimestamp;
            _tmpLastSeenTimestamp = _cursor.getLong(_cursorIndexOfLastSeenTimestamp);
            final int _tmpDetectionCount;
            _tmpDetectionCount = _cursor.getInt(_cursorIndexOfDetectionCount);
            final int _tmpLocationCount;
            _tmpLocationCount = _cursor.getInt(_cursorIndexOfLocationCount);
            final float _tmpThreatScore;
            _tmpThreatScore = _cursor.getFloat(_cursorIndexOfThreatScore);
            final int _tmpLastRssi;
            _tmpLastRssi = _cursor.getInt(_cursorIndexOfLastRssi);
            final String _tmpProbedSsids;
            if (_cursor.isNull(_cursorIndexOfProbedSsids)) {
              _tmpProbedSsids = null;
            } else {
              _tmpProbedSsids = _cursor.getString(_cursorIndexOfProbedSsids);
            }
            final Integer _tmpBluetoothClass;
            if (_cursor.isNull(_cursorIndexOfBluetoothClass)) {
              _tmpBluetoothClass = null;
            } else {
              _tmpBluetoothClass = _cursor.getInt(_cursorIndexOfBluetoothClass);
            }
            final boolean _tmpIsWhitelisted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsWhitelisted);
            _tmpIsWhitelisted = _tmp_1 != 0;
            final boolean _tmpIsFlagged;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsFlagged);
            _tmpIsFlagged = _tmp_2 != 0;
            final String _tmpMetadata;
            if (_cursor.isNull(_cursorIndexOfMetadata)) {
              _tmpMetadata = null;
            } else {
              _tmpMetadata = _cursor.getString(_cursorIndexOfMetadata);
            }
            _item = new DetectedDevice(_tmpMacAddress,_tmpDeviceType,_tmpDeviceName,_tmpFirstSeenTimestamp,_tmpLastSeenTimestamp,_tmpDetectionCount,_tmpLocationCount,_tmpThreatScore,_tmpLastRssi,_tmpProbedSsids,_tmpBluetoothClass,_tmpIsWhitelisted,_tmpIsFlagged,_tmpMetadata);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getThreateningDevices(final float threshold,
      final Continuation<? super List<DetectedDevice>> $completion) {
    final String _sql = "SELECT * FROM detected_devices WHERE isWhitelisted = 0 AND threatScore >= ? ORDER BY threatScore DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindDouble(_argIndex, threshold);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DetectedDevice>>() {
      @Override
      @NonNull
      public List<DetectedDevice> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMacAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "macAddress");
          final int _cursorIndexOfDeviceType = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceType");
          final int _cursorIndexOfDeviceName = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceName");
          final int _cursorIndexOfFirstSeenTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "firstSeenTimestamp");
          final int _cursorIndexOfLastSeenTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSeenTimestamp");
          final int _cursorIndexOfDetectionCount = CursorUtil.getColumnIndexOrThrow(_cursor, "detectionCount");
          final int _cursorIndexOfLocationCount = CursorUtil.getColumnIndexOrThrow(_cursor, "locationCount");
          final int _cursorIndexOfThreatScore = CursorUtil.getColumnIndexOrThrow(_cursor, "threatScore");
          final int _cursorIndexOfLastRssi = CursorUtil.getColumnIndexOrThrow(_cursor, "lastRssi");
          final int _cursorIndexOfProbedSsids = CursorUtil.getColumnIndexOrThrow(_cursor, "probedSsids");
          final int _cursorIndexOfBluetoothClass = CursorUtil.getColumnIndexOrThrow(_cursor, "bluetoothClass");
          final int _cursorIndexOfIsWhitelisted = CursorUtil.getColumnIndexOrThrow(_cursor, "isWhitelisted");
          final int _cursorIndexOfIsFlagged = CursorUtil.getColumnIndexOrThrow(_cursor, "isFlagged");
          final int _cursorIndexOfMetadata = CursorUtil.getColumnIndexOrThrow(_cursor, "metadata");
          final List<DetectedDevice> _result = new ArrayList<DetectedDevice>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DetectedDevice _item;
            final String _tmpMacAddress;
            _tmpMacAddress = _cursor.getString(_cursorIndexOfMacAddress);
            final DeviceType _tmpDeviceType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfDeviceType);
            _tmpDeviceType = __converters.toDeviceType(_tmp);
            final String _tmpDeviceName;
            if (_cursor.isNull(_cursorIndexOfDeviceName)) {
              _tmpDeviceName = null;
            } else {
              _tmpDeviceName = _cursor.getString(_cursorIndexOfDeviceName);
            }
            final long _tmpFirstSeenTimestamp;
            _tmpFirstSeenTimestamp = _cursor.getLong(_cursorIndexOfFirstSeenTimestamp);
            final long _tmpLastSeenTimestamp;
            _tmpLastSeenTimestamp = _cursor.getLong(_cursorIndexOfLastSeenTimestamp);
            final int _tmpDetectionCount;
            _tmpDetectionCount = _cursor.getInt(_cursorIndexOfDetectionCount);
            final int _tmpLocationCount;
            _tmpLocationCount = _cursor.getInt(_cursorIndexOfLocationCount);
            final float _tmpThreatScore;
            _tmpThreatScore = _cursor.getFloat(_cursorIndexOfThreatScore);
            final int _tmpLastRssi;
            _tmpLastRssi = _cursor.getInt(_cursorIndexOfLastRssi);
            final String _tmpProbedSsids;
            if (_cursor.isNull(_cursorIndexOfProbedSsids)) {
              _tmpProbedSsids = null;
            } else {
              _tmpProbedSsids = _cursor.getString(_cursorIndexOfProbedSsids);
            }
            final Integer _tmpBluetoothClass;
            if (_cursor.isNull(_cursorIndexOfBluetoothClass)) {
              _tmpBluetoothClass = null;
            } else {
              _tmpBluetoothClass = _cursor.getInt(_cursorIndexOfBluetoothClass);
            }
            final boolean _tmpIsWhitelisted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsWhitelisted);
            _tmpIsWhitelisted = _tmp_1 != 0;
            final boolean _tmpIsFlagged;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsFlagged);
            _tmpIsFlagged = _tmp_2 != 0;
            final String _tmpMetadata;
            if (_cursor.isNull(_cursorIndexOfMetadata)) {
              _tmpMetadata = null;
            } else {
              _tmpMetadata = _cursor.getString(_cursorIndexOfMetadata);
            }
            _item = new DetectedDevice(_tmpMacAddress,_tmpDeviceType,_tmpDeviceName,_tmpFirstSeenTimestamp,_tmpLastSeenTimestamp,_tmpDetectionCount,_tmpLocationCount,_tmpThreatScore,_tmpLastRssi,_tmpProbedSsids,_tmpBluetoothClass,_tmpIsWhitelisted,_tmpIsFlagged,_tmpMetadata);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getDevicesSeenSince(final long since,
      final Continuation<? super List<DetectedDevice>> $completion) {
    final String _sql = "SELECT * FROM detected_devices WHERE lastSeenTimestamp >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, since);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DetectedDevice>>() {
      @Override
      @NonNull
      public List<DetectedDevice> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMacAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "macAddress");
          final int _cursorIndexOfDeviceType = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceType");
          final int _cursorIndexOfDeviceName = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceName");
          final int _cursorIndexOfFirstSeenTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "firstSeenTimestamp");
          final int _cursorIndexOfLastSeenTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSeenTimestamp");
          final int _cursorIndexOfDetectionCount = CursorUtil.getColumnIndexOrThrow(_cursor, "detectionCount");
          final int _cursorIndexOfLocationCount = CursorUtil.getColumnIndexOrThrow(_cursor, "locationCount");
          final int _cursorIndexOfThreatScore = CursorUtil.getColumnIndexOrThrow(_cursor, "threatScore");
          final int _cursorIndexOfLastRssi = CursorUtil.getColumnIndexOrThrow(_cursor, "lastRssi");
          final int _cursorIndexOfProbedSsids = CursorUtil.getColumnIndexOrThrow(_cursor, "probedSsids");
          final int _cursorIndexOfBluetoothClass = CursorUtil.getColumnIndexOrThrow(_cursor, "bluetoothClass");
          final int _cursorIndexOfIsWhitelisted = CursorUtil.getColumnIndexOrThrow(_cursor, "isWhitelisted");
          final int _cursorIndexOfIsFlagged = CursorUtil.getColumnIndexOrThrow(_cursor, "isFlagged");
          final int _cursorIndexOfMetadata = CursorUtil.getColumnIndexOrThrow(_cursor, "metadata");
          final List<DetectedDevice> _result = new ArrayList<DetectedDevice>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DetectedDevice _item;
            final String _tmpMacAddress;
            _tmpMacAddress = _cursor.getString(_cursorIndexOfMacAddress);
            final DeviceType _tmpDeviceType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfDeviceType);
            _tmpDeviceType = __converters.toDeviceType(_tmp);
            final String _tmpDeviceName;
            if (_cursor.isNull(_cursorIndexOfDeviceName)) {
              _tmpDeviceName = null;
            } else {
              _tmpDeviceName = _cursor.getString(_cursorIndexOfDeviceName);
            }
            final long _tmpFirstSeenTimestamp;
            _tmpFirstSeenTimestamp = _cursor.getLong(_cursorIndexOfFirstSeenTimestamp);
            final long _tmpLastSeenTimestamp;
            _tmpLastSeenTimestamp = _cursor.getLong(_cursorIndexOfLastSeenTimestamp);
            final int _tmpDetectionCount;
            _tmpDetectionCount = _cursor.getInt(_cursorIndexOfDetectionCount);
            final int _tmpLocationCount;
            _tmpLocationCount = _cursor.getInt(_cursorIndexOfLocationCount);
            final float _tmpThreatScore;
            _tmpThreatScore = _cursor.getFloat(_cursorIndexOfThreatScore);
            final int _tmpLastRssi;
            _tmpLastRssi = _cursor.getInt(_cursorIndexOfLastRssi);
            final String _tmpProbedSsids;
            if (_cursor.isNull(_cursorIndexOfProbedSsids)) {
              _tmpProbedSsids = null;
            } else {
              _tmpProbedSsids = _cursor.getString(_cursorIndexOfProbedSsids);
            }
            final Integer _tmpBluetoothClass;
            if (_cursor.isNull(_cursorIndexOfBluetoothClass)) {
              _tmpBluetoothClass = null;
            } else {
              _tmpBluetoothClass = _cursor.getInt(_cursorIndexOfBluetoothClass);
            }
            final boolean _tmpIsWhitelisted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsWhitelisted);
            _tmpIsWhitelisted = _tmp_1 != 0;
            final boolean _tmpIsFlagged;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsFlagged);
            _tmpIsFlagged = _tmp_2 != 0;
            final String _tmpMetadata;
            if (_cursor.isNull(_cursorIndexOfMetadata)) {
              _tmpMetadata = null;
            } else {
              _tmpMetadata = _cursor.getString(_cursorIndexOfMetadata);
            }
            _item = new DetectedDevice(_tmpMacAddress,_tmpDeviceType,_tmpDeviceName,_tmpFirstSeenTimestamp,_tmpLastSeenTimestamp,_tmpDetectionCount,_tmpLocationCount,_tmpThreatScore,_tmpLastRssi,_tmpProbedSsids,_tmpBluetoothClass,_tmpIsWhitelisted,_tmpIsFlagged,_tmpMetadata);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<DetectedDevice>> getNearbyDevicesFlow(final long since) {
    final String _sql = "SELECT * FROM detected_devices WHERE lastSeenTimestamp >= ? ORDER BY lastSeenTimestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, since);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"detected_devices"}, new Callable<List<DetectedDevice>>() {
      @Override
      @NonNull
      public List<DetectedDevice> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMacAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "macAddress");
          final int _cursorIndexOfDeviceType = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceType");
          final int _cursorIndexOfDeviceName = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceName");
          final int _cursorIndexOfFirstSeenTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "firstSeenTimestamp");
          final int _cursorIndexOfLastSeenTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSeenTimestamp");
          final int _cursorIndexOfDetectionCount = CursorUtil.getColumnIndexOrThrow(_cursor, "detectionCount");
          final int _cursorIndexOfLocationCount = CursorUtil.getColumnIndexOrThrow(_cursor, "locationCount");
          final int _cursorIndexOfThreatScore = CursorUtil.getColumnIndexOrThrow(_cursor, "threatScore");
          final int _cursorIndexOfLastRssi = CursorUtil.getColumnIndexOrThrow(_cursor, "lastRssi");
          final int _cursorIndexOfProbedSsids = CursorUtil.getColumnIndexOrThrow(_cursor, "probedSsids");
          final int _cursorIndexOfBluetoothClass = CursorUtil.getColumnIndexOrThrow(_cursor, "bluetoothClass");
          final int _cursorIndexOfIsWhitelisted = CursorUtil.getColumnIndexOrThrow(_cursor, "isWhitelisted");
          final int _cursorIndexOfIsFlagged = CursorUtil.getColumnIndexOrThrow(_cursor, "isFlagged");
          final int _cursorIndexOfMetadata = CursorUtil.getColumnIndexOrThrow(_cursor, "metadata");
          final List<DetectedDevice> _result = new ArrayList<DetectedDevice>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DetectedDevice _item;
            final String _tmpMacAddress;
            _tmpMacAddress = _cursor.getString(_cursorIndexOfMacAddress);
            final DeviceType _tmpDeviceType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfDeviceType);
            _tmpDeviceType = __converters.toDeviceType(_tmp);
            final String _tmpDeviceName;
            if (_cursor.isNull(_cursorIndexOfDeviceName)) {
              _tmpDeviceName = null;
            } else {
              _tmpDeviceName = _cursor.getString(_cursorIndexOfDeviceName);
            }
            final long _tmpFirstSeenTimestamp;
            _tmpFirstSeenTimestamp = _cursor.getLong(_cursorIndexOfFirstSeenTimestamp);
            final long _tmpLastSeenTimestamp;
            _tmpLastSeenTimestamp = _cursor.getLong(_cursorIndexOfLastSeenTimestamp);
            final int _tmpDetectionCount;
            _tmpDetectionCount = _cursor.getInt(_cursorIndexOfDetectionCount);
            final int _tmpLocationCount;
            _tmpLocationCount = _cursor.getInt(_cursorIndexOfLocationCount);
            final float _tmpThreatScore;
            _tmpThreatScore = _cursor.getFloat(_cursorIndexOfThreatScore);
            final int _tmpLastRssi;
            _tmpLastRssi = _cursor.getInt(_cursorIndexOfLastRssi);
            final String _tmpProbedSsids;
            if (_cursor.isNull(_cursorIndexOfProbedSsids)) {
              _tmpProbedSsids = null;
            } else {
              _tmpProbedSsids = _cursor.getString(_cursorIndexOfProbedSsids);
            }
            final Integer _tmpBluetoothClass;
            if (_cursor.isNull(_cursorIndexOfBluetoothClass)) {
              _tmpBluetoothClass = null;
            } else {
              _tmpBluetoothClass = _cursor.getInt(_cursorIndexOfBluetoothClass);
            }
            final boolean _tmpIsWhitelisted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsWhitelisted);
            _tmpIsWhitelisted = _tmp_1 != 0;
            final boolean _tmpIsFlagged;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsFlagged);
            _tmpIsFlagged = _tmp_2 != 0;
            final String _tmpMetadata;
            if (_cursor.isNull(_cursorIndexOfMetadata)) {
              _tmpMetadata = null;
            } else {
              _tmpMetadata = _cursor.getString(_cursorIndexOfMetadata);
            }
            _item = new DetectedDevice(_tmpMacAddress,_tmpDeviceType,_tmpDeviceName,_tmpFirstSeenTimestamp,_tmpLastSeenTimestamp,_tmpDetectionCount,_tmpLocationCount,_tmpThreatScore,_tmpLastRssi,_tmpProbedSsids,_tmpBluetoothClass,_tmpIsWhitelisted,_tmpIsFlagged,_tmpMetadata);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<DetectedDevice>> getSuspiciousDevicesDetailedFlow(final float minScore,
      final long since) {
    final String _sql = "SELECT * FROM detected_devices WHERE threatScore >= ? AND lastSeenTimestamp >= ? ORDER BY threatScore DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindDouble(_argIndex, minScore);
    _argIndex = 2;
    _statement.bindLong(_argIndex, since);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"detected_devices"}, new Callable<List<DetectedDevice>>() {
      @Override
      @NonNull
      public List<DetectedDevice> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMacAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "macAddress");
          final int _cursorIndexOfDeviceType = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceType");
          final int _cursorIndexOfDeviceName = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceName");
          final int _cursorIndexOfFirstSeenTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "firstSeenTimestamp");
          final int _cursorIndexOfLastSeenTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSeenTimestamp");
          final int _cursorIndexOfDetectionCount = CursorUtil.getColumnIndexOrThrow(_cursor, "detectionCount");
          final int _cursorIndexOfLocationCount = CursorUtil.getColumnIndexOrThrow(_cursor, "locationCount");
          final int _cursorIndexOfThreatScore = CursorUtil.getColumnIndexOrThrow(_cursor, "threatScore");
          final int _cursorIndexOfLastRssi = CursorUtil.getColumnIndexOrThrow(_cursor, "lastRssi");
          final int _cursorIndexOfProbedSsids = CursorUtil.getColumnIndexOrThrow(_cursor, "probedSsids");
          final int _cursorIndexOfBluetoothClass = CursorUtil.getColumnIndexOrThrow(_cursor, "bluetoothClass");
          final int _cursorIndexOfIsWhitelisted = CursorUtil.getColumnIndexOrThrow(_cursor, "isWhitelisted");
          final int _cursorIndexOfIsFlagged = CursorUtil.getColumnIndexOrThrow(_cursor, "isFlagged");
          final int _cursorIndexOfMetadata = CursorUtil.getColumnIndexOrThrow(_cursor, "metadata");
          final List<DetectedDevice> _result = new ArrayList<DetectedDevice>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DetectedDevice _item;
            final String _tmpMacAddress;
            _tmpMacAddress = _cursor.getString(_cursorIndexOfMacAddress);
            final DeviceType _tmpDeviceType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfDeviceType);
            _tmpDeviceType = __converters.toDeviceType(_tmp);
            final String _tmpDeviceName;
            if (_cursor.isNull(_cursorIndexOfDeviceName)) {
              _tmpDeviceName = null;
            } else {
              _tmpDeviceName = _cursor.getString(_cursorIndexOfDeviceName);
            }
            final long _tmpFirstSeenTimestamp;
            _tmpFirstSeenTimestamp = _cursor.getLong(_cursorIndexOfFirstSeenTimestamp);
            final long _tmpLastSeenTimestamp;
            _tmpLastSeenTimestamp = _cursor.getLong(_cursorIndexOfLastSeenTimestamp);
            final int _tmpDetectionCount;
            _tmpDetectionCount = _cursor.getInt(_cursorIndexOfDetectionCount);
            final int _tmpLocationCount;
            _tmpLocationCount = _cursor.getInt(_cursorIndexOfLocationCount);
            final float _tmpThreatScore;
            _tmpThreatScore = _cursor.getFloat(_cursorIndexOfThreatScore);
            final int _tmpLastRssi;
            _tmpLastRssi = _cursor.getInt(_cursorIndexOfLastRssi);
            final String _tmpProbedSsids;
            if (_cursor.isNull(_cursorIndexOfProbedSsids)) {
              _tmpProbedSsids = null;
            } else {
              _tmpProbedSsids = _cursor.getString(_cursorIndexOfProbedSsids);
            }
            final Integer _tmpBluetoothClass;
            if (_cursor.isNull(_cursorIndexOfBluetoothClass)) {
              _tmpBluetoothClass = null;
            } else {
              _tmpBluetoothClass = _cursor.getInt(_cursorIndexOfBluetoothClass);
            }
            final boolean _tmpIsWhitelisted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsWhitelisted);
            _tmpIsWhitelisted = _tmp_1 != 0;
            final boolean _tmpIsFlagged;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsFlagged);
            _tmpIsFlagged = _tmp_2 != 0;
            final String _tmpMetadata;
            if (_cursor.isNull(_cursorIndexOfMetadata)) {
              _tmpMetadata = null;
            } else {
              _tmpMetadata = _cursor.getString(_cursorIndexOfMetadata);
            }
            _item = new DetectedDevice(_tmpMacAddress,_tmpDeviceType,_tmpDeviceName,_tmpFirstSeenTimestamp,_tmpLastSeenTimestamp,_tmpDetectionCount,_tmpLocationCount,_tmpThreatScore,_tmpLastRssi,_tmpProbedSsids,_tmpBluetoothClass,_tmpIsWhitelisted,_tmpIsFlagged,_tmpMetadata);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getSightingsForDevice(final String macAddress,
      final Continuation<? super List<DeviceSighting>> $completion) {
    final String _sql = "SELECT * FROM device_sightings WHERE deviceMacAddress = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, macAddress);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DeviceSighting>>() {
      @Override
      @NonNull
      public List<DeviceSighting> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDeviceMacAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceMacAddress");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfLocationAccuracy = CursorUtil.getColumnIndexOrThrow(_cursor, "locationAccuracy");
          final int _cursorIndexOfRssi = CursorUtil.getColumnIndexOrThrow(_cursor, "rssi");
          final int _cursorIndexOfDeviceType = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceType");
          final int _cursorIndexOfProbedSsid = CursorUtil.getColumnIndexOrThrow(_cursor, "probedSsid");
          final int _cursorIndexOfApSsid = CursorUtil.getColumnIndexOrThrow(_cursor, "apSsid");
          final int _cursorIndexOfChannel = CursorUtil.getColumnIndexOrThrow(_cursor, "channel");
          final int _cursorIndexOfFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "frequency");
          final List<DeviceSighting> _result = new ArrayList<DeviceSighting>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DeviceSighting _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpDeviceMacAddress;
            _tmpDeviceMacAddress = _cursor.getString(_cursorIndexOfDeviceMacAddress);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final float _tmpLocationAccuracy;
            _tmpLocationAccuracy = _cursor.getFloat(_cursorIndexOfLocationAccuracy);
            final int _tmpRssi;
            _tmpRssi = _cursor.getInt(_cursorIndexOfRssi);
            final DeviceType _tmpDeviceType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfDeviceType);
            _tmpDeviceType = __converters.toDeviceType(_tmp);
            final String _tmpProbedSsid;
            if (_cursor.isNull(_cursorIndexOfProbedSsid)) {
              _tmpProbedSsid = null;
            } else {
              _tmpProbedSsid = _cursor.getString(_cursorIndexOfProbedSsid);
            }
            final String _tmpApSsid;
            if (_cursor.isNull(_cursorIndexOfApSsid)) {
              _tmpApSsid = null;
            } else {
              _tmpApSsid = _cursor.getString(_cursorIndexOfApSsid);
            }
            final Integer _tmpChannel;
            if (_cursor.isNull(_cursorIndexOfChannel)) {
              _tmpChannel = null;
            } else {
              _tmpChannel = _cursor.getInt(_cursorIndexOfChannel);
            }
            final Integer _tmpFrequency;
            if (_cursor.isNull(_cursorIndexOfFrequency)) {
              _tmpFrequency = null;
            } else {
              _tmpFrequency = _cursor.getInt(_cursorIndexOfFrequency);
            }
            _item = new DeviceSighting(_tmpId,_tmpDeviceMacAddress,_tmpTimestamp,_tmpLatitude,_tmpLongitude,_tmpLocationAccuracy,_tmpRssi,_tmpDeviceType,_tmpProbedSsid,_tmpApSsid,_tmpChannel,_tmpFrequency);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getRecentSightingsForDevice(final String macAddress, final long since,
      final Continuation<? super List<DeviceSighting>> $completion) {
    final String _sql = "SELECT * FROM device_sightings WHERE deviceMacAddress = ? AND timestamp >= ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, macAddress);
    _argIndex = 2;
    _statement.bindLong(_argIndex, since);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DeviceSighting>>() {
      @Override
      @NonNull
      public List<DeviceSighting> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDeviceMacAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceMacAddress");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfLocationAccuracy = CursorUtil.getColumnIndexOrThrow(_cursor, "locationAccuracy");
          final int _cursorIndexOfRssi = CursorUtil.getColumnIndexOrThrow(_cursor, "rssi");
          final int _cursorIndexOfDeviceType = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceType");
          final int _cursorIndexOfProbedSsid = CursorUtil.getColumnIndexOrThrow(_cursor, "probedSsid");
          final int _cursorIndexOfApSsid = CursorUtil.getColumnIndexOrThrow(_cursor, "apSsid");
          final int _cursorIndexOfChannel = CursorUtil.getColumnIndexOrThrow(_cursor, "channel");
          final int _cursorIndexOfFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "frequency");
          final List<DeviceSighting> _result = new ArrayList<DeviceSighting>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DeviceSighting _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpDeviceMacAddress;
            _tmpDeviceMacAddress = _cursor.getString(_cursorIndexOfDeviceMacAddress);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final float _tmpLocationAccuracy;
            _tmpLocationAccuracy = _cursor.getFloat(_cursorIndexOfLocationAccuracy);
            final int _tmpRssi;
            _tmpRssi = _cursor.getInt(_cursorIndexOfRssi);
            final DeviceType _tmpDeviceType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfDeviceType);
            _tmpDeviceType = __converters.toDeviceType(_tmp);
            final String _tmpProbedSsid;
            if (_cursor.isNull(_cursorIndexOfProbedSsid)) {
              _tmpProbedSsid = null;
            } else {
              _tmpProbedSsid = _cursor.getString(_cursorIndexOfProbedSsid);
            }
            final String _tmpApSsid;
            if (_cursor.isNull(_cursorIndexOfApSsid)) {
              _tmpApSsid = null;
            } else {
              _tmpApSsid = _cursor.getString(_cursorIndexOfApSsid);
            }
            final Integer _tmpChannel;
            if (_cursor.isNull(_cursorIndexOfChannel)) {
              _tmpChannel = null;
            } else {
              _tmpChannel = _cursor.getInt(_cursorIndexOfChannel);
            }
            final Integer _tmpFrequency;
            if (_cursor.isNull(_cursorIndexOfFrequency)) {
              _tmpFrequency = null;
            } else {
              _tmpFrequency = _cursor.getInt(_cursorIndexOfFrequency);
            }
            _item = new DeviceSighting(_tmpId,_tmpDeviceMacAddress,_tmpTimestamp,_tmpLatitude,_tmpLongitude,_tmpLocationAccuracy,_tmpRssi,_tmpDeviceType,_tmpProbedSsid,_tmpApSsid,_tmpChannel,_tmpFrequency);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllSightingsSince(final long since,
      final Continuation<? super List<DeviceSighting>> $completion) {
    final String _sql = "SELECT * FROM device_sightings WHERE timestamp >= ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, since);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DeviceSighting>>() {
      @Override
      @NonNull
      public List<DeviceSighting> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDeviceMacAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceMacAddress");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfLocationAccuracy = CursorUtil.getColumnIndexOrThrow(_cursor, "locationAccuracy");
          final int _cursorIndexOfRssi = CursorUtil.getColumnIndexOrThrow(_cursor, "rssi");
          final int _cursorIndexOfDeviceType = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceType");
          final int _cursorIndexOfProbedSsid = CursorUtil.getColumnIndexOrThrow(_cursor, "probedSsid");
          final int _cursorIndexOfApSsid = CursorUtil.getColumnIndexOrThrow(_cursor, "apSsid");
          final int _cursorIndexOfChannel = CursorUtil.getColumnIndexOrThrow(_cursor, "channel");
          final int _cursorIndexOfFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "frequency");
          final List<DeviceSighting> _result = new ArrayList<DeviceSighting>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DeviceSighting _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpDeviceMacAddress;
            _tmpDeviceMacAddress = _cursor.getString(_cursorIndexOfDeviceMacAddress);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final float _tmpLocationAccuracy;
            _tmpLocationAccuracy = _cursor.getFloat(_cursorIndexOfLocationAccuracy);
            final int _tmpRssi;
            _tmpRssi = _cursor.getInt(_cursorIndexOfRssi);
            final DeviceType _tmpDeviceType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfDeviceType);
            _tmpDeviceType = __converters.toDeviceType(_tmp);
            final String _tmpProbedSsid;
            if (_cursor.isNull(_cursorIndexOfProbedSsid)) {
              _tmpProbedSsid = null;
            } else {
              _tmpProbedSsid = _cursor.getString(_cursorIndexOfProbedSsid);
            }
            final String _tmpApSsid;
            if (_cursor.isNull(_cursorIndexOfApSsid)) {
              _tmpApSsid = null;
            } else {
              _tmpApSsid = _cursor.getString(_cursorIndexOfApSsid);
            }
            final Integer _tmpChannel;
            if (_cursor.isNull(_cursorIndexOfChannel)) {
              _tmpChannel = null;
            } else {
              _tmpChannel = _cursor.getInt(_cursorIndexOfChannel);
            }
            final Integer _tmpFrequency;
            if (_cursor.isNull(_cursorIndexOfFrequency)) {
              _tmpFrequency = null;
            } else {
              _tmpFrequency = _cursor.getInt(_cursorIndexOfFrequency);
            }
            _item = new DeviceSighting(_tmpId,_tmpDeviceMacAddress,_tmpTimestamp,_tmpLatitude,_tmpLongitude,_tmpLocationAccuracy,_tmpRssi,_tmpDeviceType,_tmpProbedSsid,_tmpApSsid,_tmpChannel,_tmpFrequency);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getDistinctLocationCountForDevice(final String macAddress,
      final Continuation<? super Integer> $completion) {
    final String _sql = "\n"
            + "        SELECT COUNT(DISTINCT\n"
            + "            CAST((latitude * 10000) AS INTEGER) || '_' || CAST((longitude * 10000) AS INTEGER)\n"
            + "        ) FROM device_sightings\n"
            + "        WHERE deviceMacAddress = ?\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, macAddress);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllClusters(final Continuation<? super List<LocationCluster>> $completion) {
    final String _sql = "SELECT * FROM location_clusters ORDER BY lastVisitTimestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<LocationCluster>>() {
      @Override
      @NonNull
      public List<LocationCluster> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCenterLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "centerLatitude");
          final int _cursorIndexOfCenterLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "centerLongitude");
          final int _cursorIndexOfRadiusMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "radiusMeters");
          final int _cursorIndexOfFirstVisitTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "firstVisitTimestamp");
          final int _cursorIndexOfLastVisitTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastVisitTimestamp");
          final int _cursorIndexOfVisitCount = CursorUtil.getColumnIndexOrThrow(_cursor, "visitCount");
          final int _cursorIndexOfPlaceName = CursorUtil.getColumnIndexOrThrow(_cursor, "placeName");
          final List<LocationCluster> _result = new ArrayList<LocationCluster>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LocationCluster _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpCenterLatitude;
            _tmpCenterLatitude = _cursor.getDouble(_cursorIndexOfCenterLatitude);
            final double _tmpCenterLongitude;
            _tmpCenterLongitude = _cursor.getDouble(_cursorIndexOfCenterLongitude);
            final float _tmpRadiusMeters;
            _tmpRadiusMeters = _cursor.getFloat(_cursorIndexOfRadiusMeters);
            final long _tmpFirstVisitTimestamp;
            _tmpFirstVisitTimestamp = _cursor.getLong(_cursorIndexOfFirstVisitTimestamp);
            final long _tmpLastVisitTimestamp;
            _tmpLastVisitTimestamp = _cursor.getLong(_cursorIndexOfLastVisitTimestamp);
            final int _tmpVisitCount;
            _tmpVisitCount = _cursor.getInt(_cursorIndexOfVisitCount);
            final String _tmpPlaceName;
            if (_cursor.isNull(_cursorIndexOfPlaceName)) {
              _tmpPlaceName = null;
            } else {
              _tmpPlaceName = _cursor.getString(_cursorIndexOfPlaceName);
            }
            _item = new LocationCluster(_tmpId,_tmpCenterLatitude,_tmpCenterLongitude,_tmpRadiusMeters,_tmpFirstVisitTimestamp,_tmpLastVisitTimestamp,_tmpVisitCount,_tmpPlaceName);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object findNearbyCluster(final double lat, final double lon,
      final Continuation<? super LocationCluster> $completion) {
    final String _sql = "\n"
            + "        SELECT * FROM location_clusters\n"
            + "        WHERE ABS(centerLatitude - ?) < 0.001\n"
            + "        AND ABS(centerLongitude - ?) < 0.001\n"
            + "        LIMIT 1\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindDouble(_argIndex, lat);
    _argIndex = 2;
    _statement.bindDouble(_argIndex, lon);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<LocationCluster>() {
      @Override
      @Nullable
      public LocationCluster call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCenterLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "centerLatitude");
          final int _cursorIndexOfCenterLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "centerLongitude");
          final int _cursorIndexOfRadiusMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "radiusMeters");
          final int _cursorIndexOfFirstVisitTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "firstVisitTimestamp");
          final int _cursorIndexOfLastVisitTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastVisitTimestamp");
          final int _cursorIndexOfVisitCount = CursorUtil.getColumnIndexOrThrow(_cursor, "visitCount");
          final int _cursorIndexOfPlaceName = CursorUtil.getColumnIndexOrThrow(_cursor, "placeName");
          final LocationCluster _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpCenterLatitude;
            _tmpCenterLatitude = _cursor.getDouble(_cursorIndexOfCenterLatitude);
            final double _tmpCenterLongitude;
            _tmpCenterLongitude = _cursor.getDouble(_cursorIndexOfCenterLongitude);
            final float _tmpRadiusMeters;
            _tmpRadiusMeters = _cursor.getFloat(_cursorIndexOfRadiusMeters);
            final long _tmpFirstVisitTimestamp;
            _tmpFirstVisitTimestamp = _cursor.getLong(_cursorIndexOfFirstVisitTimestamp);
            final long _tmpLastVisitTimestamp;
            _tmpLastVisitTimestamp = _cursor.getLong(_cursorIndexOfLastVisitTimestamp);
            final int _tmpVisitCount;
            _tmpVisitCount = _cursor.getInt(_cursorIndexOfVisitCount);
            final String _tmpPlaceName;
            if (_cursor.isNull(_cursorIndexOfPlaceName)) {
              _tmpPlaceName = null;
            } else {
              _tmpPlaceName = _cursor.getString(_cursorIndexOfPlaceName);
            }
            _result = new LocationCluster(_tmpId,_tmpCenterLatitude,_tmpCenterLongitude,_tmpRadiusMeters,_tmpFirstVisitTimestamp,_tmpLastVisitTimestamp,_tmpVisitCount,_tmpPlaceName);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ThreatAlert>> getAllAlertsFlow() {
    final String _sql = "SELECT * FROM threat_alerts ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"threat_alerts"}, new Callable<List<ThreatAlert>>() {
      @Override
      @NonNull
      public List<ThreatAlert> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDeviceMacAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceMacAddress");
          final int _cursorIndexOfDeviceName = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceName");
          final int _cursorIndexOfDeviceType = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceType");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfThreatScore = CursorUtil.getColumnIndexOrThrow(_cursor, "threatScore");
          final int _cursorIndexOfThreatLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "threatLevel");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfSightingCount = CursorUtil.getColumnIndexOrThrow(_cursor, "sightingCount");
          final int _cursorIndexOfLocationCount = CursorUtil.getColumnIndexOrThrow(_cursor, "locationCount");
          final int _cursorIndexOfFollowDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "followDurationMs");
          final int _cursorIndexOfIsAcknowledged = CursorUtil.getColumnIndexOrThrow(_cursor, "isAcknowledged");
          final int _cursorIndexOfUserAction = CursorUtil.getColumnIndexOrThrow(_cursor, "userAction");
          final List<ThreatAlert> _result = new ArrayList<ThreatAlert>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ThreatAlert _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpDeviceMacAddress;
            _tmpDeviceMacAddress = _cursor.getString(_cursorIndexOfDeviceMacAddress);
            final String _tmpDeviceName;
            if (_cursor.isNull(_cursorIndexOfDeviceName)) {
              _tmpDeviceName = null;
            } else {
              _tmpDeviceName = _cursor.getString(_cursorIndexOfDeviceName);
            }
            final DeviceType _tmpDeviceType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfDeviceType);
            _tmpDeviceType = __converters.toDeviceType(_tmp);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final float _tmpThreatScore;
            _tmpThreatScore = _cursor.getFloat(_cursorIndexOfThreatScore);
            final ThreatLevel _tmpThreatLevel;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfThreatLevel);
            _tmpThreatLevel = __converters.toThreatLevel(_tmp_1);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final int _tmpSightingCount;
            _tmpSightingCount = _cursor.getInt(_cursorIndexOfSightingCount);
            final int _tmpLocationCount;
            _tmpLocationCount = _cursor.getInt(_cursorIndexOfLocationCount);
            final long _tmpFollowDurationMs;
            _tmpFollowDurationMs = _cursor.getLong(_cursorIndexOfFollowDurationMs);
            final boolean _tmpIsAcknowledged;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsAcknowledged);
            _tmpIsAcknowledged = _tmp_2 != 0;
            final AlertAction _tmpUserAction;
            final String _tmp_3;
            _tmp_3 = _cursor.getString(_cursorIndexOfUserAction);
            _tmpUserAction = __converters.toAlertAction(_tmp_3);
            _item = new ThreatAlert(_tmpId,_tmpDeviceMacAddress,_tmpDeviceName,_tmpDeviceType,_tmpTimestamp,_tmpThreatScore,_tmpThreatLevel,_tmpLatitude,_tmpLongitude,_tmpSightingCount,_tmpLocationCount,_tmpFollowDurationMs,_tmpIsAcknowledged,_tmpUserAction);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<ThreatAlert>> getUnacknowledgedAlertsFlow() {
    final String _sql = "SELECT * FROM threat_alerts WHERE isAcknowledged = 0 ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"threat_alerts"}, new Callable<List<ThreatAlert>>() {
      @Override
      @NonNull
      public List<ThreatAlert> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDeviceMacAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceMacAddress");
          final int _cursorIndexOfDeviceName = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceName");
          final int _cursorIndexOfDeviceType = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceType");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfThreatScore = CursorUtil.getColumnIndexOrThrow(_cursor, "threatScore");
          final int _cursorIndexOfThreatLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "threatLevel");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfSightingCount = CursorUtil.getColumnIndexOrThrow(_cursor, "sightingCount");
          final int _cursorIndexOfLocationCount = CursorUtil.getColumnIndexOrThrow(_cursor, "locationCount");
          final int _cursorIndexOfFollowDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "followDurationMs");
          final int _cursorIndexOfIsAcknowledged = CursorUtil.getColumnIndexOrThrow(_cursor, "isAcknowledged");
          final int _cursorIndexOfUserAction = CursorUtil.getColumnIndexOrThrow(_cursor, "userAction");
          final List<ThreatAlert> _result = new ArrayList<ThreatAlert>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ThreatAlert _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpDeviceMacAddress;
            _tmpDeviceMacAddress = _cursor.getString(_cursorIndexOfDeviceMacAddress);
            final String _tmpDeviceName;
            if (_cursor.isNull(_cursorIndexOfDeviceName)) {
              _tmpDeviceName = null;
            } else {
              _tmpDeviceName = _cursor.getString(_cursorIndexOfDeviceName);
            }
            final DeviceType _tmpDeviceType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfDeviceType);
            _tmpDeviceType = __converters.toDeviceType(_tmp);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final float _tmpThreatScore;
            _tmpThreatScore = _cursor.getFloat(_cursorIndexOfThreatScore);
            final ThreatLevel _tmpThreatLevel;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfThreatLevel);
            _tmpThreatLevel = __converters.toThreatLevel(_tmp_1);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final int _tmpSightingCount;
            _tmpSightingCount = _cursor.getInt(_cursorIndexOfSightingCount);
            final int _tmpLocationCount;
            _tmpLocationCount = _cursor.getInt(_cursorIndexOfLocationCount);
            final long _tmpFollowDurationMs;
            _tmpFollowDurationMs = _cursor.getLong(_cursorIndexOfFollowDurationMs);
            final boolean _tmpIsAcknowledged;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsAcknowledged);
            _tmpIsAcknowledged = _tmp_2 != 0;
            final AlertAction _tmpUserAction;
            final String _tmp_3;
            _tmp_3 = _cursor.getString(_cursorIndexOfUserAction);
            _tmpUserAction = __converters.toAlertAction(_tmp_3);
            _item = new ThreatAlert(_tmpId,_tmpDeviceMacAddress,_tmpDeviceName,_tmpDeviceType,_tmpTimestamp,_tmpThreatScore,_tmpThreatLevel,_tmpLatitude,_tmpLongitude,_tmpSightingCount,_tmpLocationCount,_tmpFollowDurationMs,_tmpIsAcknowledged,_tmpUserAction);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getLatestAlertForDevice(final String macAddress,
      final Continuation<? super ThreatAlert> $completion) {
    final String _sql = "SELECT * FROM threat_alerts WHERE deviceMacAddress = ? ORDER BY timestamp DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, macAddress);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ThreatAlert>() {
      @Override
      @Nullable
      public ThreatAlert call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDeviceMacAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceMacAddress");
          final int _cursorIndexOfDeviceName = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceName");
          final int _cursorIndexOfDeviceType = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceType");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfThreatScore = CursorUtil.getColumnIndexOrThrow(_cursor, "threatScore");
          final int _cursorIndexOfThreatLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "threatLevel");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfSightingCount = CursorUtil.getColumnIndexOrThrow(_cursor, "sightingCount");
          final int _cursorIndexOfLocationCount = CursorUtil.getColumnIndexOrThrow(_cursor, "locationCount");
          final int _cursorIndexOfFollowDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "followDurationMs");
          final int _cursorIndexOfIsAcknowledged = CursorUtil.getColumnIndexOrThrow(_cursor, "isAcknowledged");
          final int _cursorIndexOfUserAction = CursorUtil.getColumnIndexOrThrow(_cursor, "userAction");
          final ThreatAlert _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpDeviceMacAddress;
            _tmpDeviceMacAddress = _cursor.getString(_cursorIndexOfDeviceMacAddress);
            final String _tmpDeviceName;
            if (_cursor.isNull(_cursorIndexOfDeviceName)) {
              _tmpDeviceName = null;
            } else {
              _tmpDeviceName = _cursor.getString(_cursorIndexOfDeviceName);
            }
            final DeviceType _tmpDeviceType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfDeviceType);
            _tmpDeviceType = __converters.toDeviceType(_tmp);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final float _tmpThreatScore;
            _tmpThreatScore = _cursor.getFloat(_cursorIndexOfThreatScore);
            final ThreatLevel _tmpThreatLevel;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfThreatLevel);
            _tmpThreatLevel = __converters.toThreatLevel(_tmp_1);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final int _tmpSightingCount;
            _tmpSightingCount = _cursor.getInt(_cursorIndexOfSightingCount);
            final int _tmpLocationCount;
            _tmpLocationCount = _cursor.getInt(_cursorIndexOfLocationCount);
            final long _tmpFollowDurationMs;
            _tmpFollowDurationMs = _cursor.getLong(_cursorIndexOfFollowDurationMs);
            final boolean _tmpIsAcknowledged;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsAcknowledged);
            _tmpIsAcknowledged = _tmp_2 != 0;
            final AlertAction _tmpUserAction;
            final String _tmp_3;
            _tmp_3 = _cursor.getString(_cursorIndexOfUserAction);
            _tmpUserAction = __converters.toAlertAction(_tmp_3);
            _result = new ThreatAlert(_tmpId,_tmpDeviceMacAddress,_tmpDeviceName,_tmpDeviceType,_tmpTimestamp,_tmpThreatScore,_tmpThreatLevel,_tmpLatitude,_tmpLongitude,_tmpSightingCount,_tmpLocationCount,_tmpFollowDurationMs,_tmpIsAcknowledged,_tmpUserAction);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getTotalDeviceCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM detected_devices";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getSuspiciousDeviceCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM detected_devices WHERE threatScore >= 31";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getSightingCountSince(final long since,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM device_sightings WHERE timestamp >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, since);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getUnacknowledgedAlertCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM threat_alerts WHERE isAcknowledged = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
