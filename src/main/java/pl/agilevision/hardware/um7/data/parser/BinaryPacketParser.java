package pl.agilevision.hardware.um7.data.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;
import pl.agilevision.hardware.um7.UM7Attributes;
import pl.agilevision.hardware.um7.UM7Constants;
import pl.agilevision.hardware.um7.callback.DataCallback;
import pl.agilevision.hardware.um7.data.UM7Packet;
import pl.agilevision.hardware.um7.data.attributes.ConfigurableRateAttribute;
import pl.agilevision.hardware.um7.data.nmea.*;
import pl.agilevision.hardware.um7.impl.DefaultUM7Client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by volodymyr on 02.12.16.
 */
public class BinaryPacketParser extends PacketParser {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultUM7Client.class);
  private static final byte[] PACKET_PREFIX = "$PCHR".getBytes(StandardCharsets.US_ASCII);
  private static final int PACKET_HEADER_LENGTH = PACKET_PREFIX.length + 2;

  private static final int CHECKSUM_BLOCK_SIZE = 4;
  private static final int RADIX_HEX = 16;

  private static double DEGREES_DIVIDER = 91.02222; // divider for degrees
  private static double RATE_DIVIDER = 16.0;     // divider for rate
  private static double QUAT_DIVIDER = 29789.09091; //divider for Quat

  private static BinaryPacketParser single = null;

  public BinaryPacketParser() {

  }

  public static BinaryPacketParser getParser() {
    if (single == null) {
      single = new BinaryPacketParser();
    }
    return single;
  }

  @Override
  public boolean canParse(byte[] data, Integer... startAddress) {
    return true;
  }

  @Override
  public UM7Packet parse(byte[] data, Map<ConfigurableRateAttribute, DataCallback> callbacks, Integer... startAddress) {
    if (startAddress.length != 1) {
      return null;
    }
    if (data == null) {

      UM7Packet u = new UM7Packet();
      return u;
    }
    int startAddr = startAddress[0];
    UM7Packet u = new UM7Packet();
    final DataInputStream is = new DataInputStream(new ByteArrayInputStream(data));
    int size = data.length / 4;
    try {

      if (startAddr == UM7Constants.Registers.DREG_HEALTH) {
        // (0x55,  85) NmeaHealth register
        u.getAttributes().put(UM7Attributes.Health.Value, is.readInt());
        this.callBack(callbacks, UM7Attributes.Health, u);

    /* **************************
       Processed sensor Section
     **************************** */
      } else if (startAddr == UM7Constants.Registers.DREG_GYRO_PROC_X && size == 12) {
        //All processed data comprises registers 97 through 108 (a total of 12 registers). If
        //ALL_PROC_RATE is non-zero, the processed data will be transmitted as a single packet of batch
        //length 12, starting at address 97.
        // (0x61,  97) Processed Data: gyro (deg/s) xyzt, accel (m/s²) xyzt, mag xyzt

        u.getAttributes().put(UM7Attributes.Gyro.Processed.X, is.readFloat()); //f
        u.getAttributes().put(UM7Attributes.Gyro.Processed.Y, is.readFloat());
        u.getAttributes().put(UM7Attributes.Gyro.Processed.Z, is.readFloat());
        u.getAttributes().put(UM7Attributes.Gyro.Processed.Time, is.readFloat());

        u.getAttributes().put(UM7Attributes.Accelerator.Processed.X, is.readFloat());
        u.getAttributes().put(UM7Attributes.Accelerator.Processed.Y, is.readFloat());
        u.getAttributes().put(UM7Attributes.Accelerator.Processed.Z, is.readFloat());
        u.getAttributes().put(UM7Attributes.Accelerator.Processed.Time, is.readFloat());

        u.getAttributes().put(UM7Attributes.Magnetometer.Processed.X, is.readFloat());
        u.getAttributes().put(UM7Attributes.Magnetometer.Processed.Y, is.readFloat());
        u.getAttributes().put(UM7Attributes.Magnetometer.Processed.Z, is.readFloat());
        u.getAttributes().put(UM7Attributes.Magnetometer.Processed.Time, is.readFloat());

        // create separate packets for callbacks
        UM7Packet u1 = new UM7Packet();
        u1.getAttributes().put(UM7Attributes.Gyro.Processed.X, u.getAttributes().get(UM7Attributes.Gyro.Processed.X)); //f
        u1.getAttributes().put(UM7Attributes.Gyro.Processed.Y, u.getAttributes().get(UM7Attributes.Gyro.Processed.Y));
        u1.getAttributes().put(UM7Attributes.Gyro.Processed.Z, u.getAttributes().get(UM7Attributes.Gyro.Processed.Z));
        u1.getAttributes().put(UM7Attributes.Gyro.Processed.Time, u.getAttributes().get(UM7Attributes.Gyro.Processed.Time));
        this.callBack(callbacks, UM7Attributes.Gyro.Processed, u1);

        UM7Packet u2 = new UM7Packet();
        u2.getAttributes().put(UM7Attributes.Accelerator.Processed.X, u.getAttributes().get(UM7Attributes.Accelerator.Processed.X));
        u2.getAttributes().put(UM7Attributes.Accelerator.Processed.Y, u.getAttributes().get(UM7Attributes.Accelerator.Processed.Y));
        u2.getAttributes().put(UM7Attributes.Accelerator.Processed.Z, u.getAttributes().get(UM7Attributes.Accelerator.Processed.Z));
        u2.getAttributes().put(UM7Attributes.Accelerator.Processed.Time, u.getAttributes().get(UM7Attributes.Accelerator.Processed.Time));
        this.callBack(callbacks, UM7Attributes.Accelerator.Processed, u2);

        UM7Packet u3 = new UM7Packet();
        u3.getAttributes().put(UM7Attributes.Magnetometer.Processed.X, u.getAttributes().get(UM7Attributes.Magnetometer.Processed.X));
        u3.getAttributes().put(UM7Attributes.Magnetometer.Processed.Y, u.getAttributes().get(UM7Attributes.Magnetometer.Processed.Y));
        u3.getAttributes().put(UM7Attributes.Magnetometer.Processed.Z, u.getAttributes().get(UM7Attributes.Magnetometer.Processed.Z));
        u3.getAttributes().put(UM7Attributes.Magnetometer.Processed.Time, u.getAttributes().get(UM7Attributes.Magnetometer.Processed.Time));
        this.callBack(callbacks, UM7Attributes.Magnetometer.Processed, u3);

      } else if (startAddr == UM7Constants.Registers.DREG_GYRO_PROC_X) {

        u.getAttributes().put(UM7Attributes.Gyro.Processed.X, is.readFloat()); //f
        u.getAttributes().put(UM7Attributes.Gyro.Processed.Y, is.readFloat());
        u.getAttributes().put(UM7Attributes.Gyro.Processed.Z, is.readFloat());
        u.getAttributes().put(UM7Attributes.Gyro.Processed.Time, is.readFloat());
        this.callBack(callbacks, UM7Attributes.Gyro.Processed, u);

      } else if (startAddr == UM7Constants.Registers.DREG_ACCEL_PROC_X) {

        u.getAttributes().put(UM7Attributes.Accelerator.Processed.X, is.readFloat());
        u.getAttributes().put(UM7Attributes.Accelerator.Processed.Y, is.readFloat());
        u.getAttributes().put(UM7Attributes.Accelerator.Processed.Z, is.readFloat());
        u.getAttributes().put(UM7Attributes.Accelerator.Processed.Time, is.readFloat());
        this.callBack(callbacks, UM7Attributes.Accelerator.Processed, u);

      } else if (startAddr == UM7Constants.Registers.DREG_MAG_PROC_X) {
        u.getAttributes().put(UM7Attributes.Magnetometer.Processed.X, is.readFloat());
        u.getAttributes().put(UM7Attributes.Magnetometer.Processed.Y, is.readFloat());
        u.getAttributes().put(UM7Attributes.Magnetometer.Processed.Z, is.readFloat());
        u.getAttributes().put(UM7Attributes.Magnetometer.Processed.Time, is.readFloat());
        this.callBack(callbacks, UM7Attributes.Magnetometer.Processed, u);

    /* **************************
       Raw sensor/temperature Section
     **************************** */
      } else if (startAddr == UM7Constants.Registers.DREG_GYRO_RAW_XY && size == 11) {
        // if All Row data rate is not null  all raw data and temperature data is sent in one batch packet of length
        // 11, with start address 86.
        // (0x56,  86) Raw Rate Gyro Data: gyro xyz#t, accel xyz#t, mag xyz#t, temp ct

        u.getAttributes().put(UM7Attributes.Gyro.Raw.X, is.readShort() / DEGREES_DIVIDER); //h
        u.getAttributes().put(UM7Attributes.Gyro.Raw.Y, is.readShort() / DEGREES_DIVIDER);
        u.getAttributes().put(UM7Attributes.Gyro.Raw.Z, is.readShort() / DEGREES_DIVIDER);
        is.skipBytes(2); //2x
        u.getAttributes().put(UM7Attributes.Gyro.Raw.Time, is.readFloat()); //f

        u.getAttributes().put(UM7Attributes.Accelerator.Raw.X, is.readShort() / DEGREES_DIVIDER); //h
        u.getAttributes().put(UM7Attributes.Accelerator.Raw.Y, is.readShort() / DEGREES_DIVIDER);
        u.getAttributes().put(UM7Attributes.Accelerator.Raw.Z, is.readShort() / DEGREES_DIVIDER);
        is.skipBytes(2); //2x
        u.getAttributes().put(UM7Attributes.Accelerator.Raw.Time, is.readFloat()); //f

        u.getAttributes().put(UM7Attributes.Magnetometer.Raw.X, is.readShort()); //h
        u.getAttributes().put(UM7Attributes.Magnetometer.Raw.Y, is.readShort());
        u.getAttributes().put(UM7Attributes.Magnetometer.Raw.Z, is.readShort());
        is.skipBytes(2); //2x
        u.getAttributes().put(UM7Attributes.Magnetometer.Raw.Time, is.readFloat());

        u.getAttributes().put(UM7Attributes.Temperature.Value, is.readFloat()); //f
        u.getAttributes().put(UM7Attributes.Temperature.Time, is.readFloat()); //f

        // create separate packets for callbacks
        UM7Packet u1 = new UM7Packet();
        u1.getAttributes().put(UM7Attributes.Gyro.Raw.X, u.getAttributes().get(UM7Attributes.Gyro.Raw.X)); //h
        u1.getAttributes().put(UM7Attributes.Gyro.Raw.Y, u.getAttributes().get(UM7Attributes.Gyro.Raw.Y));
        u1.getAttributes().put(UM7Attributes.Gyro.Raw.Z, u.getAttributes().get(UM7Attributes.Gyro.Raw.Z));
        is.skipBytes(2); //2x
        u1.getAttributes().put(UM7Attributes.Gyro.Raw.Time, u.getAttributes().get(UM7Attributes.Gyro.Raw.Time)); //f
        this.callBack(callbacks, UM7Attributes.Gyro.Raw, u1);

        UM7Packet u2 = new UM7Packet();
        u2.getAttributes().put(UM7Attributes.Accelerator.Raw.X, u.getAttributes().get(UM7Attributes.Accelerator.Raw.X)); //h
        u2.getAttributes().put(UM7Attributes.Accelerator.Raw.Y, u.getAttributes().get(UM7Attributes.Accelerator.Raw.Y));
        u2.getAttributes().put(UM7Attributes.Accelerator.Raw.Z, u.getAttributes().get(UM7Attributes.Accelerator.Raw.Z));
        is.skipBytes(2); //2x
        u2.getAttributes().put(UM7Attributes.Accelerator.Raw.Time, u.getAttributes().get(UM7Attributes.Accelerator.Raw.Time)); //f
        this.callBack(callbacks, UM7Attributes.Accelerator.Raw, u2);

        UM7Packet u3 = new UM7Packet();
        u3.getAttributes().put(UM7Attributes.Magnetometer.Raw.X, u.getAttributes().get(UM7Attributes.Magnetometer.Raw.X)); //h
        u3.getAttributes().put(UM7Attributes.Magnetometer.Raw.Y, u.getAttributes().get(UM7Attributes.Magnetometer.Raw.Y));
        u3.getAttributes().put(UM7Attributes.Magnetometer.Raw.Z, u.getAttributes().get(UM7Attributes.Magnetometer.Raw.Z));
        is.skipBytes(2); //2x
        u3.getAttributes().put(UM7Attributes.Magnetometer.Raw.Time, u.getAttributes().get(UM7Attributes.Magnetometer.Raw.Time));
        this.callBack(callbacks, UM7Attributes.Magnetometer.Raw, u3);

        UM7Packet u4 = new UM7Packet();
        u4.getAttributes().put(UM7Attributes.Temperature.Value, u.getAttributes().get(UM7Attributes.Temperature.Value)); //f
        u4.getAttributes().put(UM7Attributes.Temperature.Time, u.getAttributes().get(UM7Attributes.Temperature.Time)); //f
        this.callBack(callbacks, UM7Attributes.Temperature, u4);
      } else if (startAddr == UM7Constants.Registers.DREG_GYRO_RAW_XY) {
        // 3x
        u.getAttributes().put(UM7Attributes.Gyro.Raw.X, is.readShort() / DEGREES_DIVIDER); //h
        u.getAttributes().put(UM7Attributes.Gyro.Raw.Y, is.readShort() / DEGREES_DIVIDER);
        u.getAttributes().put(UM7Attributes.Gyro.Raw.Z, is.readShort() / DEGREES_DIVIDER);
        is.skipBytes(2); //2x
        u.getAttributes().put(UM7Attributes.Gyro.Raw.Time, is.readFloat()); //f
        this.callBack(callbacks, UM7Attributes.Gyro.Raw, u);

      } else if (startAddr == UM7Constants.Registers.DREG_ACCEL_RAW_XY) {
        // 3x
        u.getAttributes().put(UM7Attributes.Accelerator.Raw.X, is.readShort() / DEGREES_DIVIDER); //h
        u.getAttributes().put(UM7Attributes.Accelerator.Raw.Y, is.readShort() / DEGREES_DIVIDER);
        u.getAttributes().put(UM7Attributes.Accelerator.Raw.Z, is.readShort() / DEGREES_DIVIDER);
        is.skipBytes(2); //2x
        u.getAttributes().put(UM7Attributes.Accelerator.Raw.Time, is.readFloat()); //f
        this.callBack(callbacks, UM7Attributes.Accelerator.Raw, u);
      } else if (startAddr == UM7Constants.Registers.DREG_MAG_RAW_XY) {
        // 3x
        u.getAttributes().put(UM7Attributes.Magnetometer.Raw.X, is.readShort()); //h
        u.getAttributes().put(UM7Attributes.Magnetometer.Raw.Y, is.readShort());
        u.getAttributes().put(UM7Attributes.Magnetometer.Raw.Z, is.readShort());
        is.skipBytes(2); //2x
        u.getAttributes().put(UM7Attributes.Magnetometer.Raw.Time, is.readFloat());
        this.callBack(callbacks, UM7Attributes.Magnetometer.Raw, u);
      } else if (startAddr == UM7Constants.Registers.DREG_TEMPERATURE) {
        // 3x 2bytes
        u.getAttributes().put(UM7Attributes.Temperature.Value, is.readFloat()); //f
        u.getAttributes().put(UM7Attributes.Temperature.Time, is.readFloat()); //f
        this.callBack(callbacks, UM7Attributes.Temperature, u);
    /* *********
       Quat
     ******** */
      } else if (startAddr == UM7Constants.Registers.DREG_QUAT_AB) {
        // 3x Quaternion data
        u.getAttributes().put(UM7Attributes.Quat.A, is.readShort() / QUAT_DIVIDER); //h
        u.getAttributes().put(UM7Attributes.Quat.B, is.readShort() / QUAT_DIVIDER); //h
        u.getAttributes().put(UM7Attributes.Quat.C, is.readShort() / QUAT_DIVIDER); //h
        u.getAttributes().put(UM7Attributes.Quat.D, is.readShort() / QUAT_DIVIDER); //h
        u.getAttributes().put(UM7Attributes.Quat.Time, is.readFloat()); //f
        this.callBack(callbacks, UM7Attributes.Quat, u);
    /* ************
       POSE - Euler/position packet
     ************** */
      } else if (startAddr == UM7Constants.Registers.DREG_EULER_PHI_THETA && size == 9) {
        // Pose data (Euler Angles and position) is stored in registers 112 to 120. If the pose rate is
        //greater than 0, then pose data will be transmitted in a batch packet with length 9 and start
        // address 112.
        // (0x70, 112) Processed Euler Data:
        u.getAttributes().put(UM7Attributes.Euler.Roll, is.readShort() / DEGREES_DIVIDER); //h
        u.getAttributes().put(UM7Attributes.Euler.Pitch, is.readShort() / DEGREES_DIVIDER); //h
        u.getAttributes().put(UM7Attributes.Euler.Yaw, is.readShort() / DEGREES_DIVIDER); //h
        is.skipBytes(2); //2x
        u.getAttributes().put(UM7Attributes.Euler.RollRate, is.readShort() / RATE_DIVIDER); //h
        u.getAttributes().put(UM7Attributes.Euler.PitchRate, is.readShort() / RATE_DIVIDER); //h
        u.getAttributes().put(UM7Attributes.Euler.YawRate, is.readShort() / RATE_DIVIDER); //h
        is.skipBytes(2); //2x
        u.getAttributes().put(UM7Attributes.Euler.Time, is.readFloat()); //f

        u.getAttributes().put(UM7Attributes.Position.North, is.readFloat());
        u.getAttributes().put(UM7Attributes.Position.East, is.readFloat());
        u.getAttributes().put(UM7Attributes.Position.Up, is.readFloat());
        u.getAttributes().put(UM7Attributes.Position.Time, is.readFloat());

        // create separate packets for callbacks
        UM7Packet u1 = new UM7Packet();
        u1.getAttributes().put(UM7Attributes.Euler.Roll, u.getAttributes().get(UM7Attributes.Euler.Roll)); //h
        u1.getAttributes().put(UM7Attributes.Euler.Pitch, u.getAttributes().get(UM7Attributes.Euler.Pitch)); //h
        u1.getAttributes().put(UM7Attributes.Euler.Yaw, u.getAttributes().get(UM7Attributes.Euler.Yaw)); //h
        is.skipBytes(2); //2x
        u1.getAttributes().put(UM7Attributes.Euler.RollRate, u.getAttributes().get(UM7Attributes.Euler.RollRate)); //h
        u1.getAttributes().put(UM7Attributes.Euler.PitchRate, u.getAttributes().get(UM7Attributes.Euler.RollRate)); //h
        u1.getAttributes().put(UM7Attributes.Euler.YawRate, u.getAttributes().get(UM7Attributes.Euler.YawRate)); //h
        is.skipBytes(2); //2x
        u1.getAttributes().put(UM7Attributes.Euler.Time, u.getAttributes().get(UM7Attributes.Euler.Time)); //f
        this.callBack(callbacks, UM7Attributes.Euler, u1);

        UM7Packet u2 = new UM7Packet();
        u2.getAttributes().put(UM7Attributes.Position.North, u.getAttributes().get(UM7Attributes.Position.North));
        u2.getAttributes().put(UM7Attributes.Position.East, u.getAttributes().get(UM7Attributes.Position.East));
        u2.getAttributes().put(UM7Attributes.Position.Up, u.getAttributes().get(UM7Attributes.Position.Up));
        u2.getAttributes().put(UM7Attributes.Position.Time, u.getAttributes().get(UM7Attributes.Position.Time));
        this.callBack(callbacks, UM7Attributes.Position, u2);

      } else if (startAddr == UM7Constants.Registers.DREG_EULER_PHI_THETA) {
        // 5x Euler Angle data
        // (0x70, 112) Processed Euler Data:
        u.getAttributes().put(UM7Attributes.Euler.Roll, is.readShort() / DEGREES_DIVIDER); //h
        u.getAttributes().put(UM7Attributes.Euler.Pitch, is.readShort() / DEGREES_DIVIDER); //h
        u.getAttributes().put(UM7Attributes.Euler.Yaw, is.readShort() / DEGREES_DIVIDER); //h
        is.skipBytes(2); //2x
        u.getAttributes().put(UM7Attributes.Euler.RollRate, is.readShort() / RATE_DIVIDER); //h
        u.getAttributes().put(UM7Attributes.Euler.PitchRate, is.readShort() / RATE_DIVIDER); //h
        u.getAttributes().put(UM7Attributes.Euler.YawRate, is.readShort() / RATE_DIVIDER); //h
        is.skipBytes(2); //2x
        u.getAttributes().put(UM7Attributes.Euler.Time, is.readFloat()); //f
        this.callBack(callbacks, UM7Attributes.Euler, u);

      } else if (startAddr == UM7Constants.Registers.DREG_POSITION_NORTH) {
        // 4x Position
        u.getAttributes().put(UM7Attributes.Position.North, is.readFloat());
        u.getAttributes().put(UM7Attributes.Position.East, is.readFloat());
        u.getAttributes().put(UM7Attributes.Position.Up, is.readFloat());
        u.getAttributes().put(UM7Attributes.Position.Time, is.readFloat());
        this.callBack(callbacks, UM7Attributes.Position, u);
    /* ***************
       Velocity
    *************** */

      } else if (startAddr == UM7Constants.Registers.DREG_VELOCITY_NORTH) {
        // 4x Velocity
        u.getAttributes().put(UM7Attributes.Velocity.North, is.readFloat());
        u.getAttributes().put(UM7Attributes.Velocity.East, is.readFloat());
        u.getAttributes().put(UM7Attributes.Velocity.Up, is.readFloat());
        u.getAttributes().put(UM7Attributes.Velocity.Time, is.readFloat());
        this.callBack(callbacks, UM7Attributes.Velocity, u);
      } else if (startAddr == UM7Constants.Registers.DREG_GYRO_BIAS_X) {
        //(0x89, 137) gyro bias xyz
        // values=struct.unpack('!fff', data)
        u.getAttributes().put(UM7Attributes.GyroBias.X, is.readFloat());
        u.getAttributes().put(UM7Attributes.GyroBias.Y, is.readFloat());
        u.getAttributes().put(UM7Attributes.GyroBias.Z, is.readFloat());
        this.callBack(callbacks, UM7Attributes.GyroBias, u);
      } else if (startAddr == UM7Constants.Registers.DREG_GPS_LATITUDE) {
        //6x
        u.getAttributes().put(UM7Attributes.Gps.Latitude, is.readFloat());
        u.getAttributes().put(UM7Attributes.Gps.Longitude, is.readFloat());
        u.getAttributes().put(UM7Attributes.Gps.Altitude, is.readFloat());

        u.getAttributes().put(UM7Attributes.Gps.Course, is.readFloat());
        u.getAttributes().put(UM7Attributes.Gps.Speed, is.readFloat());
        u.getAttributes().put(UM7Attributes.Gps.Time, is.readFloat());
        this.callBack(callbacks, UM7Attributes.Gps, u);
      } else if (startAddr == UM7Constants.Registers.DREG_GPS_SAT_1_2) {
        //6x
        u.getAttributes().put(UM7Attributes.GpsSateliteDetails.Sat1Id, is.readByte());
        u.getAttributes().put(UM7Attributes.GpsSateliteDetails.Sat1Snr, is.readByte());

        u.getAttributes().put(UM7Attributes.GpsSateliteDetails.Sat2Id, is.readByte());
        u.getAttributes().put(UM7Attributes.GpsSateliteDetails.Sat2Snr, is.readByte());

        u.getAttributes().put(UM7Attributes.GpsSateliteDetails.Sat3Id, is.readByte());
        u.getAttributes().put(UM7Attributes.GpsSateliteDetails.Sat3Snr, is.readByte());

        u.getAttributes().put(UM7Attributes.GpsSateliteDetails.Sat4Id, is.readByte());
        u.getAttributes().put(UM7Attributes.GpsSateliteDetails.Sat4Snr, is.readByte());

        u.getAttributes().put(UM7Attributes.GpsSateliteDetails.Sat5Id, is.readByte());
        u.getAttributes().put(UM7Attributes.GpsSateliteDetails.Sat5Snr, is.readByte());

        u.getAttributes().put(UM7Attributes.GpsSateliteDetails.Sat6Id, is.readByte());
        u.getAttributes().put(UM7Attributes.GpsSateliteDetails.Sat6Snr, is.readByte());

        u.getAttributes().put(UM7Attributes.GpsSateliteDetails.Sat7Id, is.readByte());
        u.getAttributes().put(UM7Attributes.GpsSateliteDetails.Sat7Snr, is.readByte());

        u.getAttributes().put(UM7Attributes.GpsSateliteDetails.Sat8Id, is.readByte());
        u.getAttributes().put(UM7Attributes.GpsSateliteDetails.Sat8Snr, is.readByte());

        u.getAttributes().put(UM7Attributes.GpsSateliteDetails.Sat9Id, is.readByte());
        u.getAttributes().put(UM7Attributes.GpsSateliteDetails.Sat9Snr, is.readByte());

        u.getAttributes().put(UM7Attributes.GpsSateliteDetails.Sat10Id, is.readByte());
        u.getAttributes().put(UM7Attributes.GpsSateliteDetails.Sat10Snr, is.readByte());

        u.getAttributes().put(UM7Attributes.GpsSateliteDetails.Sat11Id, is.readByte());
        u.getAttributes().put(UM7Attributes.GpsSateliteDetails.Sat11Snr, is.readByte());

        u.getAttributes().put(UM7Attributes.GpsSateliteDetails.Sat12Id, is.readByte());
        u.getAttributes().put(UM7Attributes.GpsSateliteDetails.Sat12Snr, is.readByte());
        this.callBack(callbacks, UM7Attributes.GpsSateliteDetails, u);
// todo CREG_GYRO_TRIM_* is not data register, should we parse it here?
//    } else if (startAddress == UM7Constants.Registers.CREG_GYRO_TRIM_X) {
//      // (0x0C,  12)
//      // values=struct.unpack('!fff', data)
//      output.put(UM7Attributes.GyroTrim.X, is.readFloat());
//      output.put(UM7Attributes.GyroBias.Y, is.readFloat());
//      output.put(UM7Attributes.GyroBias.Z, is.readFloat());


      } else {
        LOG.warn(String.format("Unknown batch packet start=0x%4x len=%4d}"), startAddress, data.length);
        return null;
      }
    } catch (IOException e) {
      LOG.warn(String.format("Unknown batch packet start=0x%4x len=%4d}"), startAddress, data.length);
      e.printStackTrace();
    }
    return u;
  }
}
