package com.xarch.example.monitor.entity;

/** Server monitor information — runtime CPU / mem / JVM / system metrics. */
public class Server {
    private Cpu cpu = new Cpu();
    private Mem mem = new Mem();
    private Jvm jvm = new Jvm();
    private Sys sys = new Sys();

    public static class Cpu {
        private String name;
        private int cores;
        private double used;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getCores() { return cores; }
        public void setCores(int cores) { this.cores = cores; }
        public double getUsed() { return used; }
        public void setUsed(double used) { this.used = used; }
    }

    public static class Mem {
        private long total;
        private long used;
        private long free;

        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        public long getUsed() { return used; }
        public void setUsed(long used) { this.used = used; }
        public long getFree() { return free; }
        public void setFree(long free) { this.free = free; }
    }

    public static class Jvm {
        private long totalMem;
        private long usedMem;
        private long freeMem;
        private double usage;

        public long getTotalMem() { return totalMem; }
        public void setTotalMem(long totalMem) { this.totalMem = totalMem; }
        public long getUsedMem() { return usedMem; }
        public void setUsedMem(long usedMem) { this.usedMem = usedMem; }
        public long getFreeMem() { return freeMem; }
        public void setFreeMem(long freeMem) { this.freeMem = freeMem; }
        public double getUsage() { return usage; }
        public void setUsage(double usage) { this.usage = usage; }
    }

    public static class Sys {
        private String computerName;
        private String computerIp;
        private String osName;
        private double osArch;

        public String getComputerName() { return computerName; }
        public void setComputerName(String computerName) { this.computerName = computerName; }
        public String getComputerIp() { return computerIp; }
        public void setComputerIp(String computerIp) { this.computerIp = computerIp; }
        public String getOsName() { return osName; }
        public void setOsName(String osName) { this.osName = osName; }
        public double getOsArch() { return osArch; }
        public void setOsArch(double osArch) { this.osArch = osArch; }
    }

    public Cpu getCpu() { return cpu; }
    public void setCpu(Cpu cpu) { this.cpu = cpu; }
    public Mem getMem() { return mem; }
    public void setMem(Mem mem) { this.mem = mem; }
    public Jvm getJvm() { return jvm; }
    public void setJvm(Jvm jvm) { this.jvm = jvm; }
    public Sys getSys() { return sys; }
    public void setSys(Sys sys) { this.sys = sys; }

    public void copyTo() {
        this.cpu.setName("CPU");
        this.cpu.setCores(Runtime.getRuntime().availableProcessors());
        this.cpu.setUsed(0);

        this.mem.setTotal(Runtime.getRuntime().maxMemory());
        this.mem.setUsed(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        this.mem.setFree(Runtime.getRuntime().freeMemory());

        this.jvm.setTotalMem(Runtime.getRuntime().maxMemory());
        this.jvm.setUsedMem(Runtime.getRuntime().totalMemory());
        this.jvm.setFreeMem(Runtime.getRuntime().freeMemory());
        this.jvm.setUsage((double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / Runtime.getRuntime().maxMemory() * 100);

        try {
            this.sys.setComputerName(java.net.InetAddress.getLocalHost().getHostName());
            this.sys.setComputerIp(java.net.InetAddress.getLocalHost().getHostAddress());
            this.sys.setOsName(System.getProperty("os.name"));
            this.sys.setOsArch(64);
        } catch (Exception ignored) {
            // best effort
        }
    }
}