package com.example.gsyvideoplayer.cast;

import android.content.Context;

import org.jupnp.binding.LocalServiceBindingException;
import org.jupnp.binding.annotations.AnnotationLocalServiceBinder;
import org.jupnp.model.DefaultServiceManager;
import org.jupnp.model.ValidationException;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.DeviceIdentity;
import org.jupnp.model.meta.LocalDevice;
import org.jupnp.model.meta.LocalService;
import org.jupnp.model.meta.ManufacturerDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.types.UDADeviceType;
import org.jupnp.model.types.UDN;
import org.jupnp.support.connectionmanager.ConnectionManagerService;

import java.util.UUID;

/**
 * 组装一台 UPnP {@code MediaRenderer:1} LocalDevice：
 * <ul>
 *   <li>AVTransport → {@link LoopbackAvTransportService}（父类注解，扫描父类拿 metadata）</li>
 *   <li>RenderingControl → {@link LoopbackRenderingControlService}（同上）</li>
 *   <li>ConnectionManager → jUPnP 自带 {@link ConnectionManagerService}</li>
 * </ul>
 *
 * <p>{@link AnnotationLocalServiceBinder#read(Class)} 会按类层次扫描 @UpnpAction / @UpnpStateVariable
 * 注解，因此我们的子类只重写方法体、复用父类注解就够。
 *
 * <p>{@link DefaultServiceManager} 负责把 LocalService 和"服务实现实例"绑定；这里注入我们的
 * 实例，避免 jUPnP 用反射 no-arg 构造出错（我们的 AVTransport 需要 Context）。
 */
final class LoopbackDeviceFactory {

    private LoopbackDeviceFactory() {}

    static LocalDevice create(Context appContext, String friendlyName,
                              String manufacturer, String modelName, String modelDescription)
            throws ValidationException, LocalServiceBindingException {

        String stableSeed = "gsy-loopback-" + appContext.getPackageName();
        UDN udn = new UDN(UUID.nameUUIDFromBytes(stableSeed.getBytes()));

        UDADeviceType type = new UDADeviceType("MediaRenderer", 1);

        DeviceDetails details = new DeviceDetails(
                friendlyName,
                new ManufacturerDetails(manufacturer),
                new ModelDetails(modelName, modelDescription, "1")
        );

        LocalService<LoopbackAvTransportService> avService = bindAvTransport(appContext);
        LocalService<LoopbackRenderingControlService> renderingService = bindRenderingControl();
        LocalService<ConnectionManagerService> connectionService = bindConnectionManager();

        LocalService<?>[] services = new LocalService[]{
                avService, renderingService, connectionService
        };

        return new LocalDevice(
                new DeviceIdentity(udn),
                type,
                details,
                services
        );
    }

    @SuppressWarnings("unchecked")
    private static LocalService<LoopbackAvTransportService> bindAvTransport(final Context appContext)
            throws LocalServiceBindingException {
        LocalService<LoopbackAvTransportService> service =
                new AnnotationLocalServiceBinder().read(LoopbackAvTransportService.class);
        service.setManager(new DefaultServiceManager<LoopbackAvTransportService>(service, LoopbackAvTransportService.class) {
            @Override
            protected LoopbackAvTransportService createServiceInstance() {
                return new LoopbackAvTransportService(appContext);
            }
        });
        return service;
    }

    @SuppressWarnings("unchecked")
    private static LocalService<LoopbackRenderingControlService> bindRenderingControl()
            throws LocalServiceBindingException {
        LocalService<LoopbackRenderingControlService> service =
                new AnnotationLocalServiceBinder().read(LoopbackRenderingControlService.class);
        service.setManager(new DefaultServiceManager<>(service, LoopbackRenderingControlService.class));
        return service;
    }

    @SuppressWarnings("unchecked")
    private static LocalService<ConnectionManagerService> bindConnectionManager()
            throws LocalServiceBindingException {
        LocalService<ConnectionManagerService> service =
                new AnnotationLocalServiceBinder().read(ConnectionManagerService.class);
        service.setManager(new DefaultServiceManager<>(service, ConnectionManagerService.class));
        return service;
    }
}
