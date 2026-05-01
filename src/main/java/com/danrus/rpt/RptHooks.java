package com.danrus.rpt;

import com.danrus.rpf.Rpf;
import com.danrus.rpf.api.event.AbstractStagedEvent;
import com.danrus.rpf.api.event.RpfEvent;
import com.danrus.rpf.api.event.type.*;
import com.danrus.rpt.core.item.RptField;
import com.danrus.rpt.duck.*;
import com.danrus.rpt.impl.select.RptSelectItemModelProperty;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class RptHooks {
    private static final Logger log = LoggerFactory.getLogger(RptHooks.class);

    // -----------------------HOOKS----------------------------

    @RptHook
    private static void hookUpdateModel(UpdateModelEvent event) {
        Rpt.prepareModelParams(event.getModel(), event.getStack());
    }

    @RptHook
    private static void hookSelectModelPropertyGetWhenDoDelegate(SelectModelPropertyGetWhenDoDelegateEvent event) {
        SelectItemModelProperty property = event.getProperty();
        if (property instanceof RptSelectItemModelProperty rptProperty) {
            RptField field = c(RptFieldHolder.class, event.getStack())
                    .rpt$getParams()
                    .orElse(
                            c(RptSelectItemModel.class, event.getModel()).rpt$getField()
                    );
            event.setGetter(() -> rptProperty.get(
                    event.getStack(), event.getContext().level(), event.getOwner()
                    //? if >=1.21.10
                    //.asLivingEntity()
                    , event.getContext().seed(), event.getContext().displayContext(), field
            ));
        }
    }

    @RptHook
    private static void hookModelDiscovery(ModelDiscoveryEvent event) {
        if (event.getStage() == AbstractStagedEvent.Stage.PRE) {
            if (Rpt.rpt$repairFuture == null) {
                log.error("Templates were not prepared in time for value discovery. it shouldn't happen!");
                return;
            }
            Rpt.rpt$repairFuture.join();
            Rpt.rpt$repairFuture = null;
            Rpt.getTemplatesManager().forEachUnbakedTemplate(event.getModelDiscovery()::addRoot);
        }
    }

    @RptHook
    private static void hookPreBake(PreBakeEvent event) {
        c(RptBakingContext.class, event.getBakingContext())
                .rpt$addFields(
                        c(RptClientItem.class, event.getClientItem())
                                .rpt$getField().orElse(RptField.EMPTY)
                );
    }

    @RptHook
    private static void hookPostBake(PostBakeEvent event) {
        RptSignedItemModel signed = c(RptSignedItemModel.class, event.getResult());
        RptClientItem clientItem = c(RptClientItem.class, event.getClientItem());
        clientItem.rpt$getField().ifPresent(params -> {
            signed.rpt$setField(params);
        });
    }

    // ----------------------END--------------------------------

    public static void register() {
//        Rpf.getEventBus().register(UpdateModelEvent.class, RptHooks::hookUpdateModel);
//        Rpf.getEventBus().register(SelectModelPropertyGetWhenDoDelegateEvent.class, RptHooks::hookSelectModelPropertyGetWhenDoDelegate);
//        Rpf.getEventBus().register(ModelDiscoveryEvent.class, RptHooks::hookModelDiscovery);
//        Rpf.getEventBus().register(PreBakeEvent.class, RptHooks::hookPreBake);
//        Rpf.getEventBus().register(PostBakeEvent.class, RptHooks::hookPostBake);
        for (Method method : RptHooks.class.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(RptHook.class)) continue;

            if (method.getParameterCount() != 1) {
                throw new IllegalStateException(
                        "@RptHook method must have exactly one parameter: " + method
                );
            }

            if (!Modifier.isStatic(method.getModifiers())) {
                throw new IllegalStateException(
                        "@RptHook method must be static: " + method
                );
            }

            Class<?> parameterType = method.getParameterTypes()[0];

            if (!RpfEvent.class.isAssignableFrom(parameterType)) {
                throw new IllegalStateException(
                        "@RptHook parameter must extend RpfEvent: " + method
                );
            }

            @SuppressWarnings("unchecked")
            Class<? extends RpfEvent> eventType =
                    (Class<? extends RpfEvent>) parameterType;

            method.setAccessible(true);

            Rpf.getEventBus().register(eventType, event -> {
                try {
                    method.invoke(null, event);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Failed to call hook: " + method, e);
                }
            });
        }
    }

    private static <T> T c(Class<T> clazz, Object o) {
        return clazz.cast(o);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    private @interface RptHook {
    }
}
