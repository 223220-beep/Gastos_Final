package com.gastosapp;

import dagger.hilt.InstallIn;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.components.SingletonComponent;
import dagger.hilt.internal.GeneratedEntryPoint;

@OriginatingElement(
    topLevelClass = GastosApplication.class
)
@GeneratedEntryPoint
@InstallIn(SingletonComponent.class)
public interface GastosApplication_GeneratedInjector {
  void injectGastosApplication(GastosApplication gastosApplication);
}
