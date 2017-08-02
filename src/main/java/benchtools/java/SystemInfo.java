package benchtools.java;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import oshi.hardware.Baseboard;
import oshi.hardware.CentralProcessor;
import oshi.hardware.CentralProcessor.TickType;
import oshi.hardware.ComputerSystem;
import oshi.hardware.Display;
import oshi.hardware.Firmware;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.hardware.PowerSource;
import oshi.hardware.Sensors;
import oshi.hardware.UsbDevice;
import oshi.software.os.FileSystem;
import oshi.software.os.NetworkParams;
import oshi.software.os.OSFileStore;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import oshi.software.os.OperatingSystem.ProcessSort;
import oshi.util.FormatUtil;
import oshi.util.Util;

/**
 * Derived from oshi.SystemInfoTest example code.
 * 
 * TODO: functions that return a hashmap, 
 * rather than printing.
 *
 * @author palisades dot lakes at gmail dot com
 * @since 2017-07-12
 * @version 2017-07-29
 */
public final class SystemInfo {

  private SystemInfo () {
    throw new UnsupportedOperationException(
      "can't instantiate " + getClass()); }

  public static void printAll (final PrintWriter pw) {
    final oshi.SystemInfo si = new oshi.SystemInfo();

    final HardwareAbstractionLayer hal = si.getHardware();
    final OperatingSystem os = si.getOperatingSystem();
    pw.println(os);
    printComputerSystem(hal.getComputerSystem(),pw);
    printProcessor(hal.getProcessor(),pw);
    printMemory(hal.getMemory(),pw);
    printCpu(hal.getProcessor(),pw);
    //printProcesses(os,hal.getMemory(),pw);
    //printSensors(hal.getSensors(),pw);
    //printPowerSources(hal.getPowerSources(),pw);
    //printDisks(hal.getDiskStores(),pw);
    //printFileSystem(os.getFileSystem(),pw);
    //printNetworkInterfaces(hal.getNetworkIFs(),pw);
    //printNetworkParameters(os.getNetworkParams(),pw);
    //printDisplays(hal.getDisplays(),pw);
    //printUsbDevices(hal.getUsbDevices(true),pw);
  }

  public static final String manufacturerModel () {
    final oshi.SystemInfo si = new oshi.SystemInfo();
    final HardwareAbstractionLayer hal = si.getHardware();
    final ComputerSystem computerSystem = hal.getComputerSystem();
    return 
      computerSystem.getManufacturer() + 
      "." +
      computerSystem.getModel();
  }

  public static void printComputerSystem (final ComputerSystem computerSystem,
                                          final PrintWriter pw) {

    pw.println(
      "manufacturer: " + computerSystem.getManufacturer());
    pw.println("model: " + computerSystem.getModel());
    //pw.println("serialnumber: " + computerSystem.getSerialNumber());
    final Firmware firmware = computerSystem.getFirmware();
    pw.println("firmware:");
    pw.println("  manufacturer: " + firmware.getManufacturer());
    pw.println("  name: " + firmware.getName());
    pw.println("  description: " + firmware.getDescription());
    pw.println("  version: " + firmware.getVersion());
    pw.println(
      "  release date: " + (firmware.getReleaseDate() == null
      ? "unknown"
        : firmware.getReleaseDate() == null ? "unknown"
          : FormatUtil.formatDate(firmware.getReleaseDate())));
    final Baseboard baseboard = computerSystem.getBaseboard();
    pw.println("baseboard:");
    pw.println("  manufacturer: " + baseboard.getManufacturer());
    pw.println("  model: " + baseboard.getModel());
    pw.println("  version: " + baseboard.getVersion());
    //pw.println("  serialnumber: " + baseboard.getSerialNumber());
  }

  public static void printProcessor (final CentralProcessor processor,
                                     final PrintWriter pw) {
    pw.println(processor);
    pw.println(" " + processor.getPhysicalProcessorCount()
    + " physical CPU(s)");
    pw.println(" " + processor.getLogicalProcessorCount()
    + " logical CPU(s)");

    pw.println("Identifier: " + processor.getIdentifier());
    //pw.println("ProcessorID: " + processor.getProcessorID());
  }

  public static void printMemory (final GlobalMemory memory,
                                  final PrintWriter pw) {
    pw.println(
      "Memory: " + FormatUtil.formatBytes(memory.getAvailable())
      + "/" + FormatUtil.formatBytes(memory.getTotal()));
    pw.println("Swap used: "
      + FormatUtil.formatBytes(memory.getSwapUsed())
      + "/"
      + FormatUtil.formatBytes(memory.getSwapTotal()));
  }

  @SuppressWarnings("boxing")
  public static void printCpu (final CentralProcessor processor,
                               final PrintWriter pw) {
    pw.println("Uptime: " + FormatUtil
      .formatElapsedSecs(processor.getSystemUptime()));

    final long[] prevTicks = processor.getSystemCpuLoadTicks();
    pw.println("CPU, IOWait, and IRQ ticks @ 0 sec:"
      + Arrays.toString(prevTicks));
    // Wait a second...
    Util.sleep(1000);
    final long[] ticks = processor.getSystemCpuLoadTicks();
    pw.println("CPU, IOWait, and IRQ ticks @ 1 sec:"
      + Arrays.toString(ticks));
    final long user =
      ticks[TickType.USER.getIndex()]
        - prevTicks[TickType.USER.getIndex()];
    final long nice =
      ticks[TickType.NICE.getIndex()]
        - prevTicks[TickType.NICE.getIndex()];
    final long sys =
      ticks[TickType.SYSTEM.getIndex()]
        - prevTicks[TickType.SYSTEM.getIndex()];
    final long idle =
      ticks[TickType.IDLE.getIndex()]
        - prevTicks[TickType.IDLE.getIndex()];
    final long iowait =
      ticks[TickType.IOWAIT.getIndex()]
        - prevTicks[TickType.IOWAIT.getIndex()];
    final long irq =
      ticks[TickType.IRQ.getIndex()]
        - prevTicks[TickType.IRQ.getIndex()];
    final long softirq =
      ticks[TickType.SOFTIRQ.getIndex()]
        - prevTicks[TickType.SOFTIRQ.getIndex()];
    final long steal =
      ticks[TickType.STEAL.getIndex()]
        - prevTicks[TickType.STEAL.getIndex()];
    final long totalCpu =
      user + nice + sys + idle + iowait + irq + softirq + steal;

    pw.format(
      "User: %.1f%% Nice: %.1f%% System: %.1f%% Idle: %.1f%% IOwait: %.1f%% IRQ: %.1f%% SoftIRQ: %.1f%% Steal: %.1f%%%n",
      (100d * user) / totalCpu, (100d * nice) / totalCpu,
      (100d * sys) / totalCpu, (100d * idle) / totalCpu,
      (100d * iowait) / totalCpu, (100d * irq) / totalCpu,
      (100d * softirq) / totalCpu, (100d * steal) / totalCpu);
    pw.format("CPU load: %.1f%% (counting ticks)%n",
      processor.getSystemCpuLoadBetweenTicks() * 100);
    pw.format("CPU load: %.1f%% (OS MXBean)%n",
      processor.getSystemCpuLoad() * 100);
    final double[] loadAverage = processor.getSystemLoadAverage(3);
    pw.println("CPU load averages:"
      + (loadAverage[0] < 0 ? " N/A"
        : String.format(" %.2f",loadAverage[0]))
      + (loadAverage[1] < 0 ? " N/A"
        : String.format(" %.2f",loadAverage[1]))
      + (loadAverage[2] < 0 ? " N/A"
        : String.format(" %.2f",loadAverage[2])));
    // per core CPU
    final StringBuilder procCpu =
      new StringBuilder("CPU load per processor:");
    final double[] load = processor.getProcessorCpuLoadBetweenTicks();
    for (final double avg : load) {
      procCpu.append(String.format(" %.1f%%",avg * 100));
    }
    pw.println(procCpu.toString());
  }

  @SuppressWarnings("boxing")
  public static void printProcesses (final OperatingSystem os,
                                     final GlobalMemory memory,
                                     final PrintWriter pw) {
    pw.println("Processes: " + os.getProcessCount()
    + ", Threads: " + os.getThreadCount());
    // Sort by highest CPU
    final List<OSProcess> procs =
      Arrays.asList(os.getProcesses(5,ProcessSort.CPU));

    pw.println("   PID  %CPU %MEM       VSZ       RSS Name");
    for (int i = 0; (i < procs.size()) && (i < 5); i++) {
      final OSProcess p = procs.get(i);
      pw.format(" %5d %5.1f %4.1f %9s %9s %s%n",
        p.getProcessID(),
        (100d * (p.getKernelTime() + p.getUserTime()))
        / p.getUpTime(),
        (100d * p.getResidentSetSize()) / memory.getTotal(),
        FormatUtil.formatBytes(p.getVirtualSize()),
        FormatUtil.formatBytes(p.getResidentSetSize()),
        p.getName());
    }
  }

  @SuppressWarnings("boxing")
  public static void printSensors (final Sensors sensors,
                                   final PrintWriter pw) {
    pw.println("Sensors:");
    pw.format(" CPU Temperature: %.1f°C%n",
      sensors.getCpuTemperature());
    pw.println(
      " Fan Speeds: " + Arrays.toString(sensors.getFanSpeeds()));
    pw.format(" CPU Voltage: %.1fV%n",
      sensors.getCpuVoltage());
  }

  @SuppressWarnings("boxing")
  public static void printPowerSources (final PowerSource[] powerSources,
                                        final PrintWriter pw) {
    final StringBuilder sb = new StringBuilder("Power: ");
    if (powerSources.length == 0) {
      sb.append("Unknown");
    }
    else {
      final double timeRemaining = powerSources[0].getTimeRemaining();
      if (timeRemaining < -1d) {
        sb.append("Charging");
      }
      else if (timeRemaining < 0d) {
        sb.append("Calculating time remaining");
      }
      else {
        sb.append(String.format("%d:%02d remaining",
          (int) (timeRemaining / 3600),
          (int) (timeRemaining / 60) % 60));
      }
    }
    for (final PowerSource pSource : powerSources) {
      sb.append(String.format("%n %s @ %.1f%%",pSource.getName(),
        pSource.getRemainingCapacity() * 100d));
    }
    pw.println(sb.toString());
  }

  @SuppressWarnings("boxing")
  public static void printDisks (final HWDiskStore[] diskStores,
                                 final PrintWriter pw) {
    pw.println("Disks:");
    for (final HWDiskStore disk : diskStores) {
      final boolean readwrite =
        (disk.getReads() > 0) || (disk.getWrites() > 0);
      pw.format(
        " %s: (model: %s - S/N: %s) size: %s, reads: %s (%s), writes: %s (%s), xfer: %s ms%n",
        disk.getName(),disk.getModel(),disk.getSerial(),
        disk.getSize() > 0
        ? FormatUtil.formatBytesDecimal(disk.getSize()) : "?",
          readwrite ? disk.getReads() : "?",
            readwrite ? FormatUtil.formatBytes(disk.getReadBytes())
              : "?",
              readwrite ? disk.getWrites() : "?",readwrite
                ? FormatUtil.formatBytes(disk.getWriteBytes()) : "?",
                  readwrite ? disk.getTransferTime() : "?");
      final HWPartition[] partitions = disk.getPartitions();
      if (partitions == null) {
        // TODO Remove when all OS's implemented
        continue;
      }
      for (final HWPartition part : partitions) {
        pw.format(
          " |-- %s: %s (%s) Maj:Min=%d:%d, size: %s%s%n",
          part.getIdentification(),part.getName(),part.getType(),
          part.getMajor(),part.getMinor(),
          FormatUtil.formatBytesDecimal(part.getSize()),
          part.getMountPoint().isEmpty() ? ""
            : " @ " + part.getMountPoint());
      }
    }
  }

  @SuppressWarnings("boxing")
  public static void printFileSystem (final FileSystem fileSystem,
                                      final PrintWriter pw) {
    pw.println("File System:");

    pw.format(" File Descriptors: %d/%d%n",
      fileSystem.getOpenFileDescriptors(),
      fileSystem.getMaxFileDescriptors());

    final OSFileStore[] fsArray = fileSystem.getFileStores();
    for (final OSFileStore fs : fsArray) {
      final long usable = fs.getUsableSpace();
      final long total = fs.getTotalSpace();
      pw.format(" %s (%s) [%s] %s of %s free (%.1f%%) is %s "
        + ((fs.getLogicalVolume() != null)
          && (fs.getLogicalVolume().length() > 0) ? "[%s]"
            : "%s")
        + " and is mounted at %s%n",
        fs.getName(),
        fs.getDescription().isEmpty() ? "file system"
          : fs.getDescription(),
          fs.getType(),FormatUtil.formatBytes(usable),
          FormatUtil.formatBytes(fs.getTotalSpace()),
          (100d * usable) / total,fs.getVolume(),
          fs.getLogicalVolume(),fs.getMount());
    }
  }

  @SuppressWarnings("boxing")
  public static void printNetworkInterfaces (final NetworkIF[] networkIFs,
                                             final PrintWriter pw) {
    pw.println("Network interfaces:");
    for (final NetworkIF net : networkIFs) {
      pw.format(" Name: %s (%s)%n",net.getName(),
        net.getDisplayName());
      pw.format("   MAC Address: %s %n",net.getMacaddr());
      pw.format("   MTU: %s, Speed: %s %n",net.getMTU(),
        FormatUtil.formatValue(net.getSpeed(),"bps"));
      pw.format("   IPv4: %s %n",
        Arrays.toString(net.getIPv4addr()));
      pw.format("   IPv6: %s %n",
        Arrays.toString(net.getIPv6addr()));
      final boolean hasData =
        (net.getBytesRecv() > 0) || (net.getBytesSent() > 0)
        || (net.getPacketsRecv() > 0)
        || (net.getPacketsSent() > 0);
      pw.format(
        "   Traffic: received %s/%s%s; transmitted %s/%s%s %n",
        hasData ? net.getPacketsRecv() + " packets" : "?",
          hasData ? FormatUtil.formatBytes(net.getBytesRecv())
            : "?",
            hasData ? " (" + net.getInErrors() + " err)" : "",
              hasData ? net.getPacketsSent() + " packets" : "?",
                hasData ? FormatUtil.formatBytes(net.getBytesSent())
                  : "?",
                  hasData ? " (" + net.getOutErrors() + " err)" : "");
    }
  }

  public static void printNetworkParameters (final NetworkParams networkParams,
                                             final PrintWriter pw) {
    pw.println("Network parameters:");
    pw.format(" Host name: %s%n",
      networkParams.getHostName());
    pw.format(" Domain name: %s%n",
      networkParams.getDomainName());
    pw.format(" DNS servers: %s%n",
      Arrays.toString(networkParams.getDnsServers()));
    pw.format(" IPv4 Gateway: %s%n",
      networkParams.getIpv4DefaultGateway());
    pw.format(" IPv6 Gateway: %s%n",
      networkParams.getIpv6DefaultGateway());
  }

  public static void printDisplays (final Display[] displays,
                                    final PrintWriter pw) {
    pw.println("Displays:");
    int i = 0;
    for (final Display display : displays) {
      pw.println(" Display " + i + ":");
      pw.println(display.toString());
      i++;
    }
  }

  public static void printUsbDevices (final UsbDevice[] usbDevices,
                                      final PrintWriter pw) {
    pw.println("USB Devices:");
    for (final UsbDevice usbDevice : usbDevices) {
      pw.println(usbDevice.toString());
    }
  }
}
