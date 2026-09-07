package com.muyue.common.core.domain.server;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OperatingSystem;
import oshi.software.os.OSFileStore;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 服务器相关信息（基于 Oshi 采集）
 *
 * @author muyue
 */
public class Server {

    private static final DecimalFormat DF = new DecimalFormat("0.00");

    private Cpu cpu = new Cpu();
    private Mem mem = new Mem();
    private Sys sys = new Sys();
    private Jvm jvm = new Jvm();
    private List<SysFile> sysFiles = new ArrayList<>();

    public void copyTo() throws Exception {
        SystemInfo si = new SystemInfo();
        setCpuInfo(si.getHardware().getProcessor());
        setMemInfo(si.getHardware().getMemory());
        setSysInfo(si.getOperatingSystem());
        setJvmInfo();
        setSysFiles(si.getOperatingSystem());
    }

    /** CPU 信息 */
    public static class Cpu {
        private int cpuNum;
        private double total; // 使用率 %
        private double sys;
        private double used;
        private double wait;
        private double free;

        public int getCpuNum() { return cpuNum; }
        public void setCpuNum(int cpuNum) { this.cpuNum = cpuNum; }
        public double getTotal() { return total; }
        public void setTotal(double total) { this.total = total; }
        public double getSys() { return sys; }
        public void setSys(double sys) { this.sys = sys; }
        public double getUsed() { return used; }
        public void setUsed(double used) { this.used = used; }
        public double getWait() { return wait; }
        public void setWait(double wait) { this.wait = wait; }
        public double getFree() { return free; }
        public void setFree(double free) { this.free = free; }
    }

    /** 内存信息 */
    public static class Mem {
        private double total; // GB
        private double used;
        private double free;
        private double usage; // 使用率 %

        public double getTotal() { return total; }
        public void setTotal(double total) { this.total = total; }
        public double getUsed() { return used; }
        public void setUsed(double used) { this.used = used; }
        public double getFree() { return free; }
        public void setFree(double free) { this.free = free; }
        public double getUsage() { return usage; }
        public void setUsage(double usage) { this.usage = usage; }
    }

    /** 服务器信息 */
    public static class Sys {
        private String computerName;
        private String computerIp;
        private String osName;
        private String osArch;
        private String userDir;

        public String getComputerName() { return computerName; }
        public void setComputerName(String computerName) { this.computerName = computerName; }
        public String getComputerIp() { return computerIp; }
        public void setComputerIp(String computerIp) { this.computerIp = computerIp; }
        public String getOsName() { return osName; }
        public void setOsName(String osName) { this.osName = osName; }
        public String getOsArch() { return osArch; }
        public void setOsArch(String osArch) { this.osArch = osArch; }
        public String getUserDir() { return userDir; }
        public void setUserDir(String userDir) { this.userDir = userDir; }
    }

    /** JVM 信息 */
    public static class Jvm {
        private String name;
        private String version;
        private double total;  // MB
        private double max;    // MB
        private double used;   // MB
        private double free;   // MB
        private double usage;  // 使用率 %
        private String startTime;
        private String runTime;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        public double getTotal() { return total; }
        public void setTotal(double total) { this.total = total; }
        public double getMax() { return max; }
        public void setMax(double max) { this.max = max; }
        public double getUsed() { return used; }
        public void setUsed(double used) { this.used = used; }
        public double getFree() { return free; }
        public void setFree(double free) { this.free = free; }
        public double getUsage() { return usage; }
        public void setUsage(double usage) { this.usage = usage; }
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public String getRunTime() { return runTime; }
        public void setRunTime(String runTime) { this.runTime = runTime; }
    }

    /** 磁盘分区 */
    public static class SysFile {
        private String dirName;
        private String sysTypeName;
        private String typeName;
        private String total;  // GB
        private String free;   // GB
        private String used;   // GB
        private double usage;  // 使用率 %

        public String getDirName() { return dirName; }
        public void setDirName(String dirName) { this.dirName = dirName; }
        public String getSysTypeName() { return sysTypeName; }
        public void setSysTypeName(String sysTypeName) { this.sysTypeName = sysTypeName; }
        public String getTypeName() { return typeName; }
        public void setTypeName(String typeName) { this.typeName = typeName; }
        public String getTotal() { return total; }
        public void setTotal(String total) { this.total = total; }
        public String getFree() { return free; }
        public void setFree(String free) { this.free = free; }
        public String getUsed() { return used; }
        public void setUsed(String used) { this.used = used; }
        public double getUsage() { return usage; }
        public void setUsage(double usage) { this.usage = usage; }
    }

    // -------------- 采集实现 --------------

    private void setCpuInfo(CentralProcessor processor) throws InterruptedException {
        long[] prev = processor.getSystemCpuLoadTicks();
        Thread.sleep(500);
        long[] ticks = processor.getSystemCpuLoadTicks();
        long user = Math.max(0, ticks[0] - prev[0]);
        long nice = Math.max(0, ticks[1] - prev[1]);
        long sys = Math.max(0, ticks[2] - prev[2]);
        long idle = Math.max(0, ticks[3] - prev[3]);
        long wait = Math.max(0, ticks[4] - prev[4]);
        long irq = Math.max(0, ticks[5] - prev[5]);
        long soft = Math.max(0, ticks[6] - prev[6]);
        long steal = Math.max(0, ticks[7] - prev[7]);
        long total = user + nice + sys + idle + wait + irq + soft + steal;
        cpu.setCpuNum(processor.getLogicalProcessorCount());
        if (total > 0) {
            cpu.setUsed(Double.parseDouble(DF.format(user * 100.0 / total)));
            cpu.setSys(Double.parseDouble(DF.format(sys * 100.0 / total)));
            cpu.setWait(Double.parseDouble(DF.format(wait * 100.0 / total)));
            cpu.setFree(Double.parseDouble(DF.format(idle * 100.0 / total)));
            cpu.setTotal(Double.parseDouble(DF.format((user + sys + wait + irq + soft + steal) * 100.0 / total)));
        }
    }

    private void setMemInfo(GlobalMemory memory) {
        long totalBytes = memory.getTotal();
        long available = memory.getAvailable();
        long usedBytes = totalBytes - available;
        mem.setTotal(bytesToGb(totalBytes));
        mem.setUsed(bytesToGb(usedBytes));
        mem.setFree(bytesToGb(available));
        mem.setUsage(Double.parseDouble(DF.format(usedBytes * 100.0 / totalBytes)));
    }

    private void setSysInfo(OperatingSystem os) throws UnknownHostException {
        sys.setComputerName(InetAddress.getLocalHost().getHostName());
        sys.setComputerIp(InetAddress.getLocalHost().getHostAddress());
        sys.setOsName(os.toString());
        sys.setOsArch(System.getProperty("os.arch"));
        sys.setUserDir(System.getProperty("user.dir"));
    }

    private void setJvmInfo() {
        Runtime r = Runtime.getRuntime();
        long totalBytes = r.totalMemory();
        long maxBytes = r.maxMemory();
        long freeBytes = r.freeMemory();
        long usedBytes = totalBytes - freeBytes;
        jvm.setName(ManagementFactory.getRuntimeMXBean().getVmName());
        jvm.setVersion(System.getProperty("java.version"));
        jvm.setTotal(bytesToMb(totalBytes));
        jvm.setMax(bytesToMb(maxBytes));
        jvm.setFree(bytesToMb(freeBytes));
        jvm.setUsed(bytesToMb(usedBytes));
        jvm.setUsage(Double.parseDouble(DF.format(usedBytes * 100.0 / totalBytes)));
        long startTime = ManagementFactory.getRuntimeMXBean().getStartTime();
        jvm.setStartTime(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startTime));
        jvm.setRunTime(msToTime(System.currentTimeMillis() - startTime));
    }

    private void setSysFiles(OperatingSystem os) {
        for (OSFileStore fs : os.getFileSystem().getFileStores()) {
            long totalBytes = fs.getTotalSpace();
            long usableBytes = fs.getUsableSpace();
            long freeBytes = usableBytes;
            long usedBytes = totalBytes - usableBytes;
            SysFile sf = new SysFile();
            sf.setDirName(fs.getMount());
            sf.setSysTypeName(fs.getType());
            sf.setTypeName(fs.getName());
            sf.setTotal(DF.format(bytesToGb(totalBytes)));
            sf.setFree(DF.format(bytesToGb(freeBytes)));
            sf.setUsed(DF.format(bytesToGb(usedBytes)));
            sf.setUsage(totalBytes == 0 ? 0 : Double.parseDouble(DF.format(usedBytes * 100.0 / totalBytes)));
            sysFiles.add(sf);
        }
    }

    private static double bytesToGb(long bytes) {
        return Double.parseDouble(DF.format(bytes / 1024.0 / 1024.0 / 1024.0));
    }

    private static double bytesToMb(long bytes) {
        return Double.parseDouble(DF.format(bytes / 1024.0 / 1024.0));
    }

    private static String msToTime(long ms) {
        long ss = ms / 1000;
        long dd = ss / 86400;
        long hh = (ss % 86400) / 3600;
        long mm = (ss % 3600) / 60;
        long s = ss % 60;
        return dd + "天 " + hh + "小时 " + mm + "分钟 " + s + "秒";
    }

    public Cpu getCpu() { return cpu; }
    public Mem getMem() { return mem; }
    public Sys getSys() { return sys; }
    public Jvm getJvm() { return jvm; }
    public List<SysFile> getSysFiles() { return sysFiles; }

    public void setCpu(Cpu cpu) { this.cpu = cpu; }
    public void setMem(Mem mem) { this.mem = mem; }
    public void setSys(Sys sys) { this.sys = sys; }
    public void setJvm(Jvm jvm) { this.jvm = jvm; }
    public void setSysFiles(List<SysFile> sysFiles) { this.sysFiles = sysFiles; }
}
