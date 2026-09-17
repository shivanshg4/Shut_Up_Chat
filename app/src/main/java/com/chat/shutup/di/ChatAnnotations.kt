package com.chat.shutup.di

import javax.inject.Qualifier

object ChatAnnotations {

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class debug

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class preProd

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class release

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class ServiceScopeInjection

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class ActivityScopeInjection

}