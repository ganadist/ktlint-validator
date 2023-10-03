package com.example.kotlin.ast

import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.config.CompilerConfiguration

// https://github.com/pinterest/ktlint/blob/master/ktlint-rule-engine/src/main/kotlin/com/pinterest/ktlint/rule/engine/internal/KotlinPsiFileFactory.kt
internal class KotlinPsiFileFactoryProvider {
    private lateinit var psiFileFactory: PsiFileFactory

    @Synchronized
    fun getKotlinPsiFileFactory(): PsiFileFactory =
        if (::psiFileFactory.isInitialized) {
            psiFileFactory
        } else {
            initPsiFileFactory().also { psiFileFactory = it }
        }
}

internal fun initPsiFileFactory(): PsiFileFactory {
    val compilerConfiguration = CompilerConfiguration()

    val disposable = Disposer.newDisposable()
    try {
        val project = KotlinCoreEnvironment
            .createForProduction(
                disposable,
                compilerConfiguration,
                EnvironmentConfigFiles.JVM_CONFIG_FILES,
            ).project as MockProject
        return PsiFileFactory.getInstance(project)

    } finally {
        disposable.dispose()
    }
}