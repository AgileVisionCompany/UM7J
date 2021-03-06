package pl.agilevision.hardware.um7.data.attributes;

/**
 * Rate configuration
 * @author Ivan Borschov (iborschov@agilevision.pl)
 * @author Volodymyr Rudyi (volodymyr@agilevision.pl)
 */
public class AcceleratorProcessed extends ConfigurableRateAttribute {
  public static String X = "accel_proc_x";
  public static String Y = "accel_proc_y";
  public static String Z = "accel_proc_z";
  public static String Time = "accel_proc_time";

  public AcceleratorProcessed(int registerAddress, String name, int bitOffset) {
    super(registerAddress, name, bitOffset);
  }
}
