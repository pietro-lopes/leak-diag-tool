package github.uncandango.leakdiagtool.tracker;

import com.sun.management.HotSpotDiagnosticMXBean;
import github.uncandango.leakdiagtool.LeakDiagTool;

import java.lang.management.ManagementFactory;

public enum MxBean {
    INSTANCE;

    private HotSpotDiagnosticMXBean mxBean;
    private Boolean explicitGcOption = null;

    public HotSpotDiagnosticMXBean get(){
        if (mxBean == null) {
            try {
                var server = ManagementFactory.getPlatformMBeanServer();
                mxBean = ManagementFactory.newPlatformMXBeanProxy(
                        server, "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
            } catch (Exception e){
                LeakDiagTool.LOGGER.error("Error while instancing MXBean: {}", e.getMessage());
            }
        }
        return mxBean;
    }

    public boolean isExplicitGcDisabled(){
        if (explicitGcOption == null) {
            explicitGcOption = Boolean.parseBoolean(this.get().getVMOption("DisableExplicitGC").getValue());
        }
        return explicitGcOption;
    }
}
